package eu.danieldk.sc.learn.conditions;

import java.io.Serializable;

import eu.danieldk.sc.dtree.Node;

public interface RuleCondition extends Serializable {
	public boolean appliesAt(Node node);
}
