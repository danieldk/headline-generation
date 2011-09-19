package eu.danieldk.treetbl.learn;

public enum RuleScope {	
	ANY_NODE("ANY_NODE"), LEFTMOST_NODE("LEFTMOST_NODE"), 
		RIGHTMOST_NODE("RIGHTMOST_NODE");

	private final String d_abbreviation;

	private RuleScope(String abbreviation) {
		d_abbreviation = abbreviation;
	}
	
	@Override
	public String toString() {
		return d_abbreviation;
	}
}
