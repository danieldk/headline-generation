package eu.danieldk.treetbl.learn;

import java.util.HashSet;
import java.util.Set;

import eu.danieldk.treetbl.dtree.Node;
import eu.danieldk.treetbl.learn.conditions.RuleCondition;
import eu.danieldk.treetbl.learn.conditions.RuleConditionGenerator;

public class DeleteRule extends Rule {
	public static class Generator implements RuleGenerator {
		private final Set<RuleConditionGenerator> d_condGenerators;
		private final RuleScope d_ruleScope;
		private final int d_goodPoints;
		private final int d_badPoints;
		private final int d_levelsUp;
		
		public Generator(Set<RuleConditionGenerator> condGenerators,
				RuleScope ruleScope, int goodPoints, int badPoints,
				int levelsUp) {
			assert(levelsUp >= 0);
			
			d_condGenerators =
				new HashSet<RuleConditionGenerator>(condGenerators);
			d_ruleScope = ruleScope;
			d_goodPoints = goodPoints;
			d_badPoints = badPoints;
			d_levelsUp = levelsUp;
		}
		
		public Generator(Set<RuleConditionGenerator> condGenerators,
				RuleScope ruleScope) {
			this(condGenerators, ruleScope, 1, -6, 0);
		}
		
		public Rule generateRule(Node node) {
			Set<RuleCondition> conditions = new HashSet<RuleCondition>();
			
			for (RuleConditionGenerator gen: d_condGenerators) {
				RuleCondition ruleCondition = gen.generateCondition(node);
				if (ruleCondition == null)
					return null;
				conditions.add(ruleCondition);
			}
			
			return new DeleteRule(conditions, d_ruleScope, d_goodPoints, d_badPoints,
					d_levelsUp);
		}
	}
	
	private final int d_levelsUp;

	public DeleteRule(Set<RuleCondition> ruleConditions,
			RuleScope ruleScope, int goodPoints, int badPoints, int levelsUp) {
		super(ruleConditions, ruleScope, goodPoints, badPoints);
		
		assert(levelsUp >= 0);
		d_levelsUp = levelsUp;
	}
	
	@Override
	protected boolean appliesAt(Node node) {
		// Can we go up the number of specified levels up?
		Node delNode = node;
		for (int i = 0; i < d_levelsUp; ++i) {
			delNode = delNode.getParent();
			if (delNode == null)
				return false;
		}

		// A deletion rule cannot be applied if it does not have a parent.
		if (delNode.getParent() == null)
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

		Node delNode = node;
		for (int i = 0; i < d_levelsUp; ++i)
			delNode = delNode.getParent();

		delNode.getParent().removeChild(delNode);
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		DeleteRule other = (DeleteRule) otherObject;
		return d_ruleConditions.equals(other.d_ruleConditions) &&
			d_ruleScope.equals(other.d_ruleScope) &&
			d_levelsUp == other.d_levelsUp;
	}
	
	@Override
	public int hashCode() {
		return d_ruleConditions.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("DeleteRule ");
		sb.append(d_ruleScope.toString());
		sb.append(" ");
		sb.append(d_levelsUp);
		sb.append(" ");
		
		for (RuleCondition ruleCondition: d_ruleConditions) {
			sb.append(ruleCondition.toString());
			sb.append(" ");
		}
		
		return sb.toString();
	}
}
