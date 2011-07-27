package org.danieldk.sc.dtree.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.danieldk.sc.dtree.Node;

/**
 * This class implements a depth-first right-to-left node iterator.
 */
public class RightDepthNodeIterator implements Iterator<Node> {
	Node d_root = null;
	List<Node> d_nodeStack = null;
	Set<Node> d_expanded = null;

	/**
	 * Construct the iterator, which will iterate over the tree of which
	 * <i>root</i> is the root.
	 * @param root The root of the tree to iterate over.
	 */
	public RightDepthNodeIterator(Node root) {
		d_root = root;
		d_nodeStack = new ArrayList<Node>();
		d_nodeStack.add(root);
		d_expanded = new HashSet<Node>();
	}
	
	public boolean hasNext() {
		return !d_nodeStack.isEmpty();
	}

	public Node next() {
		// Expand from the most rightmost node.
		while (true) {
			Node expandNode = d_nodeStack.get(d_nodeStack.size() - 1);
			
			// If we encounter a node that was previously expanded,
			// all children have been visited already, so we'll want to
			// return this node.
			if (d_expanded.contains(expandNode))
				break;

			// Leaf node.
			if (expandNode.getChildren().size() == 0)
				break;

			// Expand the current node.
			for (Node child: expandNode.getChildren())
				d_nodeStack.add(child);
			d_expanded.add(expandNode);
			
		}
		
		return d_nodeStack.remove(d_nodeStack.size() - 1);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
