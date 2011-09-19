package eu.danieldk.treetbl.learn.conditions;

import java.util.List;

import eu.danieldk.treetbl.dtree.Node;

public class HasSiblingCondition implements RuleCondition {
	public static class Generator implements RuleConditionGenerator {
		private final String d_attr;
		private final int d_siblingPos;
		
		public Generator(String attr, int siblingPos) {
			d_attr = attr;
			d_siblingPos = siblingPos;
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
				d_siblingPos == other.d_siblingPos;
		}
		
		@Override
		public int hashCode() {
			return d_attr.hashCode() + d_siblingPos;
		}

		public RuleCondition generateCondition(Node node) {
			Node parent = node.getParent();
			
			if (parent == null)
				return null;
			
			List<Node> children = parent.getChildren();
			int index = Integer.MAX_VALUE;
			for (int i = 0; i < children.size(); ++i)
				if (children.get(i) == node)
					index = i;
			
			int siblingIndex = index + d_siblingPos;
			if (siblingIndex >= children.size() || siblingIndex < 0)
				return null;
			
			String value = children.get(siblingIndex).getAttrValue(d_attr);
				
			if (value == null)
				return null;

			return new HasSiblingCondition(d_attr, value, siblingIndex);
		}		
	}

	private final String d_attr;
	private final String d_value;
	private final int d_siblingPos;
	
	public HasSiblingCondition(String attr, String value, int siblingPos) {
		d_attr = attr;
		d_value = value;
		d_siblingPos = siblingPos;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		HasSiblingCondition other = (HasSiblingCondition) otherObject;
		
		return d_attr.equals(other.d_attr) &&
			d_value.equals(other.d_value) &&
			d_siblingPos == other.d_siblingPos;
	}
	
	@Override
	public int hashCode() {
		return (d_attr + d_value).hashCode() + d_siblingPos;		
	}
	
	public String toString() {
		return "[HasSiblingCondition " + d_siblingPos + " \"" +
		d_attr + "\" = \"" + d_value + "\"]";
	}

	public boolean appliesAt(Node node) {
		Node parent = node.getParent();
		if (parent == null)
			return false;
		
		List<Node> children = parent.getChildren();
		int index = Integer.MAX_VALUE;
		for (int i = 0; i < children.size(); ++i)
			if (children.get(i) == node)
				index = i;
		
		int siblingIndex = index + d_siblingPos;
		if (siblingIndex >= children.size() || siblingIndex < 0)
			return false;
		
		String value = children.get(siblingIndex).getAttrValue(d_attr);
			
		if (value == null)
			return false;
		
		return d_value.equals(value);
	}
}
