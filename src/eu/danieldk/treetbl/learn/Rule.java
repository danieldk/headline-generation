package eu.danieldk.treetbl.learn;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.danieldk.treetbl.dtree.Node;
import eu.danieldk.treetbl.dtree.util.DependencyTreeNodeIterator;
import eu.danieldk.treetbl.dtree.util.LeftDepthNodeIterator;
import eu.danieldk.treetbl.dtree.util.RightDepthNodeIterator;
import eu.danieldk.treetbl.learn.conditions.RuleCondition;

public abstract class Rule implements Serializable {
	protected final Set<RuleCondition> d_ruleConditions;
	protected final RuleScope d_ruleScope;
	private final int d_goodPoints;
	private final int d_badPoints;
	
	public Rule(Set<RuleCondition> ruleConditions,
			RuleScope ruleScope, int goodPoints, int badPoints) {
		d_ruleConditions = new HashSet<RuleCondition>(ruleConditions);
		d_ruleScope = ruleScope;
		d_goodPoints = goodPoints;
		d_badPoints = badPoints;
	}
	
	public boolean applies(Node node) {
		Iterator<Node> iter = new DependencyTreeNodeIterator(node);
		
		while (iter.hasNext())
			if (appliesAt(iter.next()))
				return true;
		
		return false;
	}
	
	public void apply(Node node) {
		Iterator<Node> iter = null;
		
		if (d_ruleScope == RuleScope.LEFTMOST_NODE)
			iter = new LeftDepthNodeIterator(node);
		else if (d_ruleScope == RuleScope.RIGHTMOST_NODE)
			iter = new RightDepthNodeIterator(node);
		else
			iter = new DependencyTreeNodeIterator(node);
		
		while (iter.hasNext()) {
			Node curNode = iter.next();
			if (appliesAt(curNode)) {
				applyAt(curNode);
				if (d_ruleScope != RuleScope.ANY_NODE)
					return;
			}
		}
				
	}
	
	abstract protected boolean appliesAt(Node node);
	abstract protected void applyAt(Node node);

	public int badPoints() {
		return d_badPoints;
	}
	
	public int goodPoints() {
		return d_goodPoints;
	}
}
