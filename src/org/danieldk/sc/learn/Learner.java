package org.danieldk.sc.learn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.danieldk.sc.dtree.DependencyTree;
import org.danieldk.sc.dtree.Node;
import org.danieldk.sc.dtree.util.DependencyTreeNodeIterator;
import org.danieldk.sc.learn.conditions.HasAttributeCondition;
import org.danieldk.sc.learn.conditions.HasParentCondition;
import org.danieldk.sc.learn.conditions.RuleCondition;

public class Learner {
	private final Set<RuleGenerator> d_ruleGens;
	private final ScoreFun d_scoreFun;
	private final int d_stopThreshold;

	private String printSentence(String[] seq) {
		StringBuffer sb = new StringBuffer();
		for (String word: seq) {
			sb.append(word);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public Learner(Set<RuleGenerator> ruleGens, ScoreFun scoreFun,
			int stopThreshold) {
		d_ruleGens = new HashSet<RuleGenerator>(ruleGens);
		d_scoreFun = scoreFun;
		d_stopThreshold = stopThreshold;
	}
	
	private void applyRule(Vector<DependencyTree> dummyCorpus, Rule rule,
			Set<Integer> corpusIndexes) {

		for (int corpusIndex: corpusIndexes)
			rule.apply(dummyCorpus.get(corpusIndex).getRootNode());
	}
	
	private Set<Rule> gatherTreeRules(DependencyTree goldTree,
			DependencyTree dummyTree) {
		Set<Rule> rules= new HashSet<Rule>();
		
		DependencyTreeNodeIterator iter =
			new DependencyTreeNodeIterator(dummyTree.getRootNode());
		while (iter.hasNext()) {
			Node node = iter.next();
			for (RuleGenerator ruleGen: d_ruleGens) {
				Rule rule = ruleGen.generateRule(node);
				if (rule != null)
					rules.add(rule);
			}
		}
		
		return rules;
	}
	
	public List<Rule> learn(Vector<DependencyTree> goldCorpus,
			Vector<DependencyTree> dummyCorpus) {

		List<Rule> results = new Vector<Rule>();

		Map<Rule, Map<Integer, Integer>> rules = new HashMap<Rule, Map<Integer, Integer>>();

		Set<Integer> changedTreeIndexes = new HashSet<Integer>();
		for (int i = 0; i < goldCorpus.size(); ++i)
			changedTreeIndexes.add(i);		
		
		while (true) {
			// Gather potential rules from changed sentences.
			for (int corpusIndex: changedTreeIndexes) {
				Set<Rule> treeRules = gatherTreeRules(goldCorpus.get(corpusIndex),
						dummyCorpus.get(corpusIndex));
				for (Rule rule: treeRules) {
					if (!rules.containsKey(rule))
						rules.put(rule, new HashMap<Integer, Integer>());
					rules.get(rule).put(corpusIndex, 0);
				}
			}
			
			// Rescore affected rules.
			scoreRules(goldCorpus, dummyCorpus, rules, changedTreeIndexes);

			int bestScore = Integer.MIN_VALUE;
			Rule bestRule = null;
			for (Entry<Rule, Map<Integer, Integer>> ruleEntry: rules.entrySet()) {
				Rule rule = ruleEntry.getKey();
				int ruleScore = 0;
				
				for (Entry<Integer, Integer> corpusIndexEntry: ruleEntry.getValue().entrySet())
					ruleScore += corpusIndexEntry.getValue();
				
				if (ruleScore > bestScore) {
					bestScore = ruleScore;
					bestRule = rule;
				}
			}
			
			if (bestScore < d_stopThreshold)
				break;

			System.out.println(bestRule + ": " + bestScore);
			
			results.add(bestRule);
			
			applyRule(dummyCorpus, bestRule, rules.get(bestRule).keySet());
			
			changedTreeIndexes = new HashSet<Integer>(rules.get(bestRule).keySet());
			
			// Purge
			for (Entry<Rule, Map<Integer, Integer>> entry: rules.entrySet()) {
				Map<Integer, Integer> ruleScores = entry.getValue();
				
				for (int corpusIndex: changedTreeIndexes)
					ruleScores.remove(corpusIndex);
			}
			
			changedTreeIndexes = purgeSufficientCompressions(goldCorpus, dummyCorpus, changedTreeIndexes);
			
			rules.remove(bestRule);
		}
		
		System.out.println("Stopped cycling!");
		System.out.println("Last changed indexes size: " + changedTreeIndexes);

		for (int i = 0; i < goldCorpus.size(); ++i) {
			System.out.println("R: " + printSentence(goldCorpus.get(i).getSentence()));
			System.out.println("C: " + printSentence(dummyCorpus.get(i).getSentence()));
		}
		return results;
	}
	
	private Set<Integer> purgeSufficientCompressions(Vector<DependencyTree> goldCorpus,
			Vector<DependencyTree> dummyCorpus, Set<Integer> changedTreeIndexes) {
		Set<Integer> relevantIndexes = new HashSet<Integer>();
		
		// Remove samples that have been trimmed to their reference
		// length.
		for (int corpusIndex: changedTreeIndexes) {
			if (goldCorpus.get(corpusIndex).getSentence().length <
				dummyCorpus.get(corpusIndex).getSentence().length)
				relevantIndexes.add(corpusIndex);
			else {
				System.out.println("R: " + printSentence(goldCorpus.get(corpusIndex).getSentence()));
				System.out.println("C: " + printSentence(dummyCorpus.get(corpusIndex).getSentence()));				
			}
		}
		
		return relevantIndexes;		
	}
	
	private void scoreRules(Vector<DependencyTree> goldCorpus,
			Vector<DependencyTree> dummyCorpus, Map<Rule, Map<Integer, Integer>> rules,
			Set<Integer> changedTreeIndexes) {
		// Calculate original compression scores.
		List<Double> origScores = new Vector<Double>();
		for (int i = 0; i < goldCorpus.size(); ++i) {
			DependencyTree goldTree = goldCorpus.get(i);
			DependencyTree dummyTree = dummyCorpus.get(i);
			origScores.add(d_scoreFun.call(goldTree, dummyTree));
		}
		
		for (Entry<Rule, Map<Integer, Integer>> entry: rules.entrySet()) {
			Rule rule = entry.getKey();

			for (Entry<Integer, Integer> corpusIndexEntry: entry.getValue().entrySet()) {
				// Skip trees that haven't changed during the previous cycle.
				int corpusIndex = corpusIndexEntry.getKey();
				if (!changedTreeIndexes.contains(corpusIndex))
					continue;
				
				DependencyTree goldTree = goldCorpus.get(corpusIndex);
				DependencyTree dummyTree = dummyCorpus.get(corpusIndex);

				// Apply the rule on a copy of the tree.
				DependencyTree modDummyTree = new DependencyTree(dummyTree);
				rule.apply(modDummyTree.getRootNode());

				// Calculate the score of the modified compression.
				double modScore = d_scoreFun.call(goldTree, modDummyTree);

				// Score improvements/regressions.
				if (modScore > origScores.get(corpusIndex))
					rules.get(rule).put(corpusIndex, rule.goodPoints());
				else if (modScore < origScores.get(corpusIndex))
					rules.get(rule).put(corpusIndex, rule.badPoints());
			}
		}
	}
}
