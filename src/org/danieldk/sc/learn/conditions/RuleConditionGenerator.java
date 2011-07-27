package org.danieldk.sc.learn.conditions;


import org.danieldk.sc.dtree.Node;

/**
 * Classes that implement this interface can generate rule conditions.
 */
public interface RuleConditionGenerator {
	/**
	 * Generate relevant conditions for <i>node</i>.
	 * @param node
	 * @return
	 */
	public RuleCondition generateCondition(Node node);
}
