package eu.danieldk.sc.learn;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.danieldk.sc.dtree.Node;
import eu.danieldk.sc.learn.conditions.RuleCondition;
import eu.danieldk.sc.learn.conditions.RuleConditionGenerator;

public class DeleteSiblingsRule extends Rule {
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
				conditions.add(ruleCondition);
			}
			
			return new DeleteSiblingsRule(conditions, d_ruleScope, d_goodPoints, d_badPoints);
		}
	}

	public DeleteSiblingsRule(Set<RuleCondition> ruleConditions,
			RuleScope ruleScope, int goodPoints, int badPoints) {
		super(ruleConditions, ruleScope, goodPoints, badPoints);
	}
	
	@Override
	protected boolean appliesAt(Node node) {
		// A deletion rule cannot be applied if it does not have a parent.
		if (node.getParent() == null)
			return false;
		
		// This rule is not interesting if there are no siblings.
		if (node.getParent().getChildren().size() == 1)
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

		// Only keep this node.
		Set<Node> keep = new HashSet<Node>();
		keep.add(node);
		node.getParent().getChildren().retainAll(keep);
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		DeleteSiblingsRule other = (DeleteSiblingsRule) otherObject;
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
		sb.append("DeleteSiblingsRule ");
		sb.append(d_ruleScope.toString());
		sb.append(" ");
		
		for (RuleCondition ruleCondition: d_ruleConditions) {
			sb.append(ruleCondition.toString());
			sb.append(" ");
		}
		
		return sb.toString();
	}
}
