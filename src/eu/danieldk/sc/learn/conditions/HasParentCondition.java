package eu.danieldk.sc.learn.conditions;

import eu.danieldk.sc.dtree.Node;

public class HasParentCondition implements RuleCondition {
	public static class Generator implements RuleConditionGenerator {
		private final String d_attr;
		private final int d_levelsUp;
		
		public Generator(String attr, int levelsUp) {
			d_attr = attr;
			d_levelsUp = levelsUp;
		}
		
		public Generator(String attr) {
			this(attr, 1);
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
			
			return d_attr.equals(other.d_attr) &&
				d_levelsUp == other.d_levelsUp;
		}
		
		@Override
		public int hashCode() {
			return d_attr.hashCode();
		}

		public RuleCondition generateCondition(Node node) {
			Node parent = node;
			for (int i = 0; i < d_levelsUp; ++i) {
				parent = node.getParent();
				if (parent == null)
					return null;
			}

			if (!parent.containsAttribute(d_attr))
				return null;
			
			String value = parent.getAttrValue(d_attr);
			
			if (value == null)
				return null;

			return new HasParentCondition(d_attr, value, d_levelsUp);
		}
		
	}
	
	private final String d_attr;
	private final String d_value;
	private final int d_levelsUp;
	
	public HasParentCondition(String attr, String value, int levelsUp) {
		d_attr = attr;
		d_value = value;
		d_levelsUp = levelsUp;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		HasParentCondition other = (HasParentCondition) otherObject;
		
		return d_attr.equals(other.d_attr) &&
			d_value.equals(other.d_value) &&
			d_levelsUp == other.d_levelsUp;
	}
	
	@Override
	public int hashCode() {
		return (d_attr + d_value).hashCode();		
	}
	
	public boolean appliesAt(Node node) {
		Node parent = node;
		for (int i = 0; i < d_levelsUp; ++i) {
			parent = parent.getParent();
			if (parent == null)
				return false;
		}

		if (!parent.containsAttribute(d_attr))
			return false;
		
		String value = parent.getAttrValue(d_attr);
		
		if (value == null)
			return false;
		
		return parent.getAttrValue(d_attr).equals(d_value);
	}
	
	public String toString() {
		return "[HasParentCondition " + d_levelsUp + " " + "\"" +
			d_attr + "\" = \"" + d_value + "\"]";
	}
}
