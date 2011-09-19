package eu.danieldk.treetbl.learn.conditions;

import java.io.Serializable;

import eu.danieldk.treetbl.dtree.Node;

public interface RuleCondition extends Serializable {
	public boolean appliesAt(Node node);
}
