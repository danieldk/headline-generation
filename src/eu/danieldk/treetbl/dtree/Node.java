package eu.danieldk.treetbl.dtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 35050854750163646L;
	private Node d_parent;
	private final int d_id;
	private int d_begin;
	private int d_end;
	private final List<Node> d_children;
	private final Map<String, String> d_attributes;
	
	public Node(Node parent, int id, int begin, int end,
			Map<String, String> attributes) {
		d_parent = parent;
		d_id = id;
		d_begin = begin;
		d_end = end;
		d_children = new ArrayList<Node>();
		d_attributes = new HashMap<String, String>(attributes);
	}
	
	/**
	 * This copy constructor creates a new node from another node, but
	 * child nodes are recursively recreated.
	 * @param other
	 */
	public Node(Node other) {
		d_parent = other.d_parent;
		d_id = other.d_id;
		d_begin = other.d_begin;
		d_end = other.d_end;
		d_children = new ArrayList<Node>(other.d_children);
		d_attributes = new HashMap<String, String>(other.d_attributes);
		
		for (int i = 0; i < d_children.size(); ++i) {
			d_children.set(i, new Node(d_children.get(i)));
			d_children.get(i).setParent(this);
		}
	}
	
	/**
	 * Add a child node to this node.
	 * @param child
	 */
	public void addChild(Node child) {
		d_children.add(child);
	}

	/**
	 * Checks whether the node has the attribute <i>attr</i>.
	 * @param attr
	 * @return
	 */
	public boolean containsAttribute(String attr) {
		return d_attributes.containsKey(attr);
	}
	
	/**
	 * Returns the value of an attribute.
	 * @param attr
	 * @return
	 */
	public String getAttrValue(String attr) {
		return d_attributes.get(attr);
	}

	/**
	 * Return the start index of the set of tokens represented by this node.
	 * @return
	 */
	public int getBegin() {
		return d_begin;
	}
	
	/**
	 * Return the children of this node. If the node is a leaf, an
	 * empty list will be returned.
	 * @return
	 */
	public List<Node> getChildren() {
		//return new ArrayList<Node>(d_children);
		return d_children;
	}

	/**
	 * Return the start index of the set of tokens represented by this node.
	 * @return
	 */
	public int getEnd() {
		return d_end;
	}
	
	public int getId() {
		return d_id;
	}

	/**
	 * Return the parent of this node.
	 * @return
	 */
	public Node getParent() {
		return d_parent;
	}
	
	public void removeChild(Node node) {
		d_children.remove(node);
	}
	
	public void setBegin(int begin) {
		d_begin = begin;
	}
	
	public void setEnd(int end) {
		d_end = end;
	}
	
	/**
	 * Change the parent node to <i>parentNode</i>.
	 * @param parentNode
	 */
	public void setParent(Node parentNode) {
		d_parent = parentNode;
	}
}
