package eu.danieldk.treetbl.learn.conditions;


import eu.danieldk.treetbl.dtree.Node;

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
