package eu.danieldk.treetbl.learn.conditions;

import eu.danieldk.treetbl.dtree.Node;

public class HasAttributeCondition implements RuleCondition {
	public static class Generator implements RuleConditionGenerator {
		private final String d_attr;
		
		public Generator(String attr) {
			d_attr = attr;
		}
		
		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject)
				return true;
			
			if (otherObject == null)
				return false;
			
			if (getClass() != otherObject.getClass())
				return false;
			
			Generator other = (Generator) otherObject;
			
			return d_attr.equals(other.d_attr);
		}
		
		@Override
		public int hashCode() {
			return d_attr.hashCode();
		}

		public RuleCondition generateCondition(Node node) {
			if (!node.containsAttribute(d_attr))
				return null;
			
			String value = node.getAttrValue(d_attr);
			
			if (value == null)
				return null;
			
			return new HasAttributeCondition(d_attr, value);
		}		
	}

	private final String d_attr;
	private final String d_value;
	
	public HasAttributeCondition(String attr, String value) {
		d_attr = attr;
		d_value = value;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		HasAttributeCondition other = (HasAttributeCondition) otherObject;
		
		return d_attr.equals(other.d_attr) &&
			d_value.equals(other.d_value);
	}
	
	@Override
	public int hashCode() {
		return (d_attr + d_value).hashCode();		
	}
	
	public String toString() {
		return "[HasAttributeCondition \"" + d_attr + "\" = \"" +
			d_value + "\"]";
	}

	public boolean appliesAt(Node node) {
		if (!node.containsAttribute(d_attr))
			return false;
		
		String value = node.getAttrValue(d_attr);
		
		if (value == null)
			return false;
		
		return node.getAttrValue(d_attr).equals(d_value);
	}
}
