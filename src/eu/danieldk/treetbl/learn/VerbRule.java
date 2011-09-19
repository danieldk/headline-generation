package eu.danieldk.treetbl.learn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.danieldk.treetbl.dtree.Node;
import eu.danieldk.treetbl.learn.conditions.RuleCondition;
import eu.danieldk.treetbl.learn.conditions.RuleConditionGenerator;

public class VerbRule extends Rule {
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
			this(condGenerators, ruleScope, 10, -20);
		}
		
		public Rule generateRule(Node node) {
			Set<RuleCondition> conditions = new HashSet<RuleCondition>();
			
			for (RuleConditionGenerator gen: d_condGenerators) {
				RuleCondition ruleCondition = gen.generateCondition(node);
				if (ruleCondition == null)
					return null;
				conditions.add(ruleCondition);
			}
			
			return new VerbRule(conditions, d_ruleScope, d_goodPoints, d_badPoints);
		}
	}
	
	public VerbRule(Set<RuleCondition> ruleConditions, RuleScope ruleScope, int goodPoints,
			int badPoints) {
		super(ruleConditions, ruleScope, goodPoints, badPoints);
	}

	@Override
	protected boolean appliesAt(Node node) {
		Node parent = node.getParent();
		if (parent == null)
			return false;
		
		Node grandParent = parent.getParent();
		if (grandParent == null)
			return false;
		
		String pos = node.getAttrValue("pos");
		if (pos == null || !pos.equals("verb"))
			return false;
		
		// Check all conditions.
		for (RuleCondition condition: d_ruleConditions)
			if (!condition.appliesAt(node))
				return false;
		
		// Does the grandparent have a child with hd/verb?
		Node hdVerb = getHeadVerb(grandParent);
		if (hdVerb == null)
			return false;
		
		String root = hdVerb.getAttrValue("root");
		if (!root.equals("heb"))
			return false;
		
		return true;
	}

	@Override
	protected void applyAt(Node node) {
		if (!appliesAt(node))
			return;
		
		Node parent = node.getParent();
		Node grandParent = parent.getParent();
		Node hdVerb = getHeadVerb(grandParent);
		
		node.setBegin(hdVerb.getBegin());
		node.setEnd(hdVerb.getEnd());
		
		List<Node> gpChildren = grandParent.getChildren();
		int hdVerbIdx = gpChildren.indexOf(hdVerb);
		gpChildren.set(hdVerbIdx, node);
		node.setParent(grandParent);
		
		parent.getChildren().remove(node);
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		VerbRule other = (VerbRule) otherObject;
		
		return d_ruleConditions.equals(other.d_ruleConditions);
	}
	
	@Override
	public int hashCode() {
		return d_ruleConditions.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("VerbRule ");
		sb.append(d_ruleScope.toString());
		sb.append(" ");
		
		for (RuleCondition ruleCondition: d_ruleConditions) {
			sb.append(ruleCondition.toString());
			sb.append(" ");
		}
		
		return sb.toString();
	}

	private Node getHeadVerb(Node grandParent) {
		for (Node child: grandParent.getChildren()) {
			String pos = child.getAttrValue("pos");
			if (pos == null || !pos.equals("verb"))
				continue;
			
			String rel = child.getAttrValue("rel");
			if (rel == null || !rel.equals("hd"))
				continue;
			
			return child;
		}
		
		return null;
	}
}
