package org.danieldk.sc.learn.conditions;

import java.io.Serializable;

import org.danieldk.sc.dtree.Node;

public interface RuleCondition extends Serializable {
	public boolean appliesAt(Node node);
}
