package eu.danieldk.treetbl.learn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.danieldk.treetbl.dtree.Node;
import eu.danieldk.treetbl.learn.conditions.RuleCondition;
import eu.danieldk.treetbl.learn.conditions.RuleConditionGenerator;

public class MakeRootRule extends Rule {
	public static class Generator implements RuleGenerator {
		private final Set<RuleConditionGenerator> d_condGenerators;
		private final RuleScope d_ruleScope;
		private final int d_goodPoints;
		private final int d_badPoints;
		
		public Generator(Set<RuleConditionGenerator> condGenerators,
				RuleScope ruleScope, int goodPoints, int badPoints) {
			d_condGenerators =
				new HashSet<RuleConditionGenerator>(condGenerators);
			d_ruleScope = ruleScope;
			d_goodPoints = goodPoints;
			d_badPoints = badPoints;
		}
		
		public Generator(Set<RuleConditionGenerator> condGenerators,
				RuleScope ruleScope) {
			this(condGenerators, ruleScope, 1, -6);
		}
		
		public Rule generateRule(Node node) {
			Set<RuleCondition> conditions = new HashSet<RuleCondition>();
			
			for (RuleConditionGenerator gen: d_condGenerators) {
				RuleCondition ruleCondition = gen.generateCondition(node);
				if (ruleCondition == null)
					return null;
				
				// A make root cannot be applied if it does not have a parent.
				if (node.getParent() == null)
					return null;
				
				// The node is already top-level.
				if (node.getParent().getParent() == null)
					return null;
				
				conditions.add(ruleCondition);
			}
			
			return new MakeRootRule(conditions, d_ruleScope, d_goodPoints, d_badPoints);
		}
	}

	public MakeRootRule(Set<RuleCondition> ruleConditions,
			RuleScope ruleScope, int goodPoints, int badPoints) {
		super(ruleConditions, ruleScope, goodPoints, badPoints);
	}
	
	@Override
	protected boolean appliesAt(Node node) {
		// A make root cannot be applied if it does not have a parent.
		if (node.getParent() == null)
			return false;
		
		// The node is already top-level.
		if (node.getParent().getParent() == null)
			return false;

		// Check all conditions.
		for (RuleCondition condition: d_ruleConditions)
			if (!condition.appliesAt(node))
				return false;
		
		return true;
	}
		
	public void applyAt(Node node) {
		if (!appliesAt(node))
			return;

		Node root = node;
		
		while (root.getParent() != null)
			root = root.getParent();

		node.setParent(root);
		List<Node> rootChildren =  root.getChildren();
		rootChildren.clear();
		rootChildren.add(node);
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		MakeRootRule other = (MakeRootRule) otherObject;
		return d_ruleConditions.equals(other.d_ruleConditions) &&
			d_ruleScope.equals(other.d_ruleScope);
	}
	
	@Override
	public int hashCode() {
		return d_ruleConditions.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("MakeRootRule ");
		sb.append(d_ruleScope.toString());
		sb.append(" ");
		
		for (RuleCondition ruleCondition: d_ruleConditions) {
			sb.append(ruleCondition.toString());
			sb.append(" ");
		}
		
		return sb.toString();
	}
}
