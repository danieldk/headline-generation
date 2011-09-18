package eu.danieldk.sc.trim;

import java.util.List;
import java.util.Vector;

import eu.danieldk.sc.dtree.DependencyTree;
import eu.danieldk.sc.learn.Rule;

public class Trimmer {
	private final List<Rule> d_rules;
	
	public Trimmer(List<Rule> rules) {
		d_rules = rules;
	}
	
	public List<DependencyTree> trim(List<DependencyTree> goldCorpus,
			List<DependencyTree> dummyCorpus) {
		// Make a copy of the dummy corpus that we'll trim.
		List<DependencyTree> trimmedCorpus =
			new Vector<DependencyTree>(dummyCorpus);
		for (int i = 0; i < trimmedCorpus.size(); ++i)
			trimmedCorpus.set(i, new DependencyTree(trimmedCorpus.get(i)));
		
		// Apply rules.
		for (Rule rule: d_rules)
			applyRule(goldCorpus, trimmedCorpus, rule);
		
		return trimmedCorpus;
	}
	
	public List<DependencyTree> trim(List<DependencyTree> dummyCorpus,
			int maxLength) {
		// Make a copy of the dummy corpus that we'll trim.
		List<DependencyTree> trimmedCorpus =
			new Vector<DependencyTree>(dummyCorpus);
		for (int i = 0; i < trimmedCorpus.size(); ++i)
			trimmedCorpus.set(i, new DependencyTree(trimmedCorpus.get(i)));
		
		// Apply rules.
		for (Rule rule: d_rules)
			applyRule(trimmedCorpus, rule, maxLength);
		
		return trimmedCorpus;
	}

	private void applyRule(List<DependencyTree> goldCorpus,
			List<DependencyTree> dummyCorpus, Rule rule) {
		for (int i = 0; i < goldCorpus.size(); ++i)
			if (dummyCorpus.get(i).getSentence().length >
					goldCorpus.get(i).getSentence().length)
				rule.apply(dummyCorpus.get(i).getRootNode());
	}
	
	private void applyRule(List<DependencyTree> dummyCorpus, Rule rule, int maxLength) {
		for (int i = 0; i < dummyCorpus.size(); ++i)
			if (dummyCorpus.get(i).getSentence().length > maxLength)
				rule.apply(dummyCorpus.get(i).getRootNode());
	}

}
