package eu.danieldk.treetbl.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.danieldk.treetbl.dtree.DataSet;
import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.eval.Rouge;
import eu.danieldk.treetbl.learn.DeleteRule;
import eu.danieldk.treetbl.learn.Learner;
import eu.danieldk.treetbl.learn.MakeRootRule;
import eu.danieldk.treetbl.learn.ReplaceParentRule;
import eu.danieldk.treetbl.learn.Rule;
import eu.danieldk.treetbl.learn.RuleGenerator;
import eu.danieldk.treetbl.learn.RuleScope;
import eu.danieldk.treetbl.learn.ScoreFun;
import eu.danieldk.treetbl.learn.SwapSiblingRule;
import eu.danieldk.treetbl.learn.conditions.HasAttributeCondition;
import eu.danieldk.treetbl.learn.conditions.HasParentCondition;
import eu.danieldk.treetbl.learn.conditions.HasSiblingCondition;
import eu.danieldk.treetbl.learn.conditions.RuleConditionGenerator;

public class Learn {
	static class HeadlineFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith("h.xml");
		}	
	}
	
	static class SentenceFileFilter implements FilenameFilter {
		public SentenceFileFilter(String basename) {
			d_basename = basename + "a";
		}
		
		public boolean accept(File dir, String name) {
			if (name.endsWith(".xml") && name.startsWith(d_basename))
				return true;
			else
				return false;
		}
		
		String d_basename;
	}
	
	private static String[] toLower(String[] seq) {
		String[] lower = new String[seq.length];
		
		for (int i = 0; i < seq.length; ++i)
			lower[i] = seq[i].toLowerCase();
		
		return lower;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Learn directory rules_output");
			System.exit(0);
		}

		File data = new File(args[0]);
		if (!data.isFile() || !data.canRead()) {
			System.out.println(args[0] + " cannot be opened or read!");
			System.exit(1);
		}

		System.out.print("Reading training data... ");
		
		DataSet dataset = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
			dataset = (DataSet) in.readObject();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Done!");
		
		Set<RuleGenerator> ruleGens = new HashSet<RuleGenerator>();

		/*
		 * Hedge trimmer like rules
		 */
		
		// Make a certain node the root of the tree.
		Set<RuleConditionGenerator> nodeRelCatCondGen =
			new HashSet<RuleConditionGenerator>();
		nodeRelCatCondGen.add(new HasAttributeCondition.Generator("rel"));
		nodeRelCatCondGen.add(new HasAttributeCondition.Generator("cat"));
		ruleGens.add(new MakeRootRule.Generator(nodeRelCatCondGen, RuleScope.LEFTMOST_NODE));
		
		// Removal of low-content words, by word and part-of-speech tag.
		Set<RuleConditionGenerator> wordPosCondGen =
			new HashSet<RuleConditionGenerator>();
		wordPosCondGen.add(new HasAttributeCondition.Generator("pos"));
		wordPosCondGen.add(new HasAttributeCondition.Generator("word"));
		RuleGenerator wordPosRuleGen = new DeleteRule.Generator(wordPosCondGen,
				RuleScope.ANY_NODE);
		ruleGens.add(wordPosRuleGen);

		// Removal of low-content words, by part-of-speech tag and relation.
		Set<RuleConditionGenerator> wordPosRelCondGen =
			new HashSet<RuleConditionGenerator>();
		wordPosRelCondGen.add(new HasAttributeCondition.Generator("pos"));
		wordPosRelCondGen.add(new HasAttributeCondition.Generator("rel"));
		RuleGenerator wordPosRelRuleGen = new DeleteRule.Generator(wordPosRelCondGen,
				RuleScope.ANY_NODE);
		ruleGens.add(wordPosRelRuleGen);
		
		// Removal of constituents with some child that indicates low-content,
		// e.g. time expressions.
		Set<RuleConditionGenerator> parentRelCatCondGen = new HashSet<RuleConditionGenerator>();
		parentRelCatCondGen.add(new HasParentCondition.Generator("rel"));
		parentRelCatCondGen.add(new HasParentCondition.Generator("cat"));
		Set<RuleConditionGenerator> add = new HashSet<RuleConditionGenerator>(wordPosCondGen);
		add.addAll(parentRelCatCondGen);
		ruleGens.add(new DeleteRule.Generator(add, RuleScope.ANY_NODE, 1, -6, 1));
		
		Set<RuleConditionGenerator> grandParentRelCatCondGen =
			new HashSet<RuleConditionGenerator>();
		grandParentRelCatCondGen.add(new HasParentCondition.Generator("rel", 2));
		grandParentRelCatCondGen.add(new HasParentCondition.Generator("cat", 2));
		add = new HashSet<RuleConditionGenerator>(wordPosCondGen);
		add.addAll(parentRelCatCondGen);
		add.addAll(grandParentRelCatCondGen);
		ruleGens.add(new DeleteRule.Generator(add, RuleScope.ANY_NODE, 1, -6, 2));
		
		// XP over XP-like rule
		add = new HashSet<RuleConditionGenerator>(parentRelCatCondGen);
		add.addAll(nodeRelCatCondGen);
		//ruleGens.add(new ReplaceParentRule.Generator(add, RuleScope.ANY_NODE));
		ruleGens.add(new ReplaceParentRule.Generator(add, RuleScope.ANY_NODE));

		// Parent S, next sibling: NP
		Set<RuleConditionGenerator> nextSiblingRelCondGen =
			new HashSet<RuleConditionGenerator>();
		nextSiblingRelCondGen.add(new HasSiblingCondition.Generator("rel", 1));
		add = new HashSet<RuleConditionGenerator>(nextSiblingRelCondGen);
		add.addAll(parentRelCatCondGen);
		add.addAll(nodeRelCatCondGen);
		ruleGens.add(new DeleteRule.Generator(add,
				RuleScope.ANY_NODE));
		
		// Delete rightmost nodes with a certain rel/cat (comparable to rightmost PP
		// and SBAR removal in the hedge trimmer).
		ruleGens.add(new DeleteRule.Generator(nodeRelCatCondGen, RuleScope.RIGHTMOST_NODE));
		
		/*
		 * End of hedge trimmer like rules.
		 */

		/* 
		 * Begin of extra rules
		 */

		add = new HashSet<RuleConditionGenerator>(nodeRelCatCondGen);
		add.addAll(parentRelCatCondGen);
		ruleGens.add(new DeleteRule.Generator(add, RuleScope.ANY_NODE));
		
		Set<RuleConditionGenerator> grandParentRelCondGen =
			new HashSet<RuleConditionGenerator>();
		grandParentRelCondGen.add(new HasAttributeCondition.Generator("rel"));
		grandParentRelCondGen.add(new HasParentCondition.Generator("rel"));
		grandParentRelCondGen.add(new HasParentCondition.Generator("rel", 2));
		RuleGenerator grandParentRelRuleGen =
			new DeleteRule.Generator(grandParentRelCondGen,
				RuleScope.ANY_NODE);
		ruleGens.add(grandParentRelRuleGen);
		
		add = new HashSet<RuleConditionGenerator>(nextSiblingRelCondGen);
		add.add(new HasSiblingCondition.Generator("cat", 1));
		add.add(new HasAttributeCondition.Generator("rel"));
		add.add(new HasAttributeCondition.Generator("cat"));
		RuleGenerator nextSiblingRelRuleGen = new DeleteRule.Generator(add,
				RuleScope.ANY_NODE);
		ruleGens.add(nextSiblingRelRuleGen);

		Set<RuleConditionGenerator> prevSiblingRelCondGen =
			new HashSet<RuleConditionGenerator>();
		prevSiblingRelCondGen.add(new HasAttributeCondition.Generator("rel"));
		prevSiblingRelCondGen.add(new HasAttributeCondition.Generator("cat"));
		prevSiblingRelCondGen.add(new HasSiblingCondition.Generator("rel", -1));
		prevSiblingRelCondGen.add(new HasSiblingCondition.Generator("cat", -1));
		RuleGenerator prevSiblingRelRuleGen = new DeleteRule.Generator(prevSiblingRelCondGen,
				RuleScope.ANY_NODE);
		ruleGens.add(prevSiblingRelRuleGen);
		
		add = new HashSet<RuleConditionGenerator>(nextSiblingRelCondGen);
		add.add(new HasAttributeCondition.Generator("rel"));
		add.add(new HasSiblingCondition.Generator("cat", 1));
		add.add(new HasAttributeCondition.Generator("cat"));
		ruleGens.add(new SwapSiblingRule.Generator(add,
				RuleScope.ANY_NODE));

		Learner learner = new Learner(ruleGens, new ScoreFun() {
			public Double call(DependencyTree arg1, DependencyTree arg2) {
				String[] arg1Seq = toLower(arg1.getRoots());
				String[] arg2Seq = toLower(arg2.getRoots());
				return Rouge.rougeSU(arg1Seq, arg2Seq, Integer.MAX_VALUE,
						1.0);
			}
			
		}, 1);

		List<Rule> rules = learner.learn(dataset.goldCorpus(), dataset.dummyCorpus());
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(args[1]));
			out.writeObject(rules);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
