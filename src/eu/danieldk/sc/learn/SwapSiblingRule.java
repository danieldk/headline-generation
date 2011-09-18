package eu.danieldk.sc.learn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.danieldk.sc.dtree.Node;
import eu.danieldk.sc.dtree.util.DependencyTreeNodeIterator;
import eu.danieldk.sc.learn.conditions.RuleCondition;
import eu.danieldk.sc.learn.conditions.RuleConditionGenerator;

public class SwapSiblingRule extends Rule {
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
			this(condGenerators, ruleScope, 1, -3);
		}
		
		public Rule generateRule(Node node) {
			Set<RuleCondition> conditions = new HashSet<RuleCondition>();
			
			for (RuleConditionGenerator gen: d_condGenerators) {
				RuleCondition ruleCondition = gen.generateCondition(node);
				if (ruleCondition == null)
					return null;
				conditions.add(ruleCondition);
			}
			
			return new SwapSiblingRule(conditions, d_ruleScope, d_goodPoints, d_badPoints);
		}
	}

	public SwapSiblingRule(Set<RuleCondition> ruleConditions,
			RuleScope ruleScope, int goodPoints, int badPoints) {
		super(ruleConditions, ruleScope, goodPoints, badPoints);
	}
	
	@Override
	protected boolean appliesAt(Node node) {		
		// No parent, no sibling.
		Node parent = node.getParent();
		if (parent == null)
			return false;
		
		// If there is no sibling on the right, this rule can not
		// apply here.
		List<Node> children = parent.getChildren();
		if (children.indexOf(node) == children.size() - 1)
			return false;
		
		// Check all conditions.
		for (RuleCondition condition: d_ruleConditions)
			if (!condition.appliesAt(node))
				return false;
		
		return true;
	}
	
	@Override
	public void applyAt(Node node) {
		if (!appliesAt(node))
			return;
		
		List<Node> siblings = node.getParent().getChildren();
		int nodeIndex = siblings.indexOf(node);
		Node sibling = siblings.get(nodeIndex + 1);

		int leftPlus = sibling.getEnd() - sibling.getBegin();
		int rightMin = node.getEnd() - node.getBegin();

		// Renumber the left node.
		DependencyTreeNodeIterator iter = new DependencyTreeNodeIterator(node);
		while (iter.hasNext()) {
			Node n = iter.next();
			n.setBegin(n.getBegin() + leftPlus);
			n.setEnd(n.getEnd() + leftPlus);
		}
		
		// Renumber the right node.
		iter = new DependencyTreeNodeIterator(sibling);
		while (iter.hasNext()) {
			Node n = iter.next();
			n.setBegin(n.getBegin() - rightMin);
			n.setEnd(n.getEnd() - rightMin);
		}

		
		/*
		int tmpBegin = node.getBegin();
		int tmpEnd = node.getEnd();
		node.setBegin(sibling.getBegin());
		node.setEnd(sibling.getEnd());
		sibling.setBegin(tmpBegin);
		sibling.setEnd(tmpEnd);
		*/
		
		siblings.set(nodeIndex, sibling);
		siblings.set(nodeIndex + 1, node);
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		SwapSiblingRule other = (SwapSiblingRule) otherObject;
		
		return d_ruleConditions.equals(other.d_ruleConditions);
	}
	
	@Override
	public int hashCode() {
		return d_ruleConditions.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SwapSiblingRule ");
		sb.append(d_ruleScope.toString());
		sb.append(" ");
		
		for (RuleCondition ruleCondition: d_ruleConditions) {
			sb.append(ruleCondition.toString());
			sb.append(" ");
		}
		
		return sb.toString();
	}
}
