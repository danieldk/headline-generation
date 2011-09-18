package eu.danieldk.sc.dtree.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.danieldk.sc.dtree.Node;

public class DependencyTreeNodeIterator implements Iterator<Node> {
	Node d_root = null;
	List<Node> d_nodeStack = null;

	public DependencyTreeNodeIterator(Node root) {
		d_root = root;
		d_nodeStack = new ArrayList<Node>();
		d_nodeStack.add(root);
	}
	
	public boolean hasNext() {
		purgeDeadNodes();
		return !d_nodeStack.isEmpty();
	}

	public Node next() {
		Node n = d_nodeStack.remove(0);
		
		for (Node child: n.getChildren())
			d_nodeStack.add(child);
		
		return n;
	}
	
	private void purgeDeadNodes() {
		for (int i = d_nodeStack.size() - 1; i >=0; --i) {
			Node node = d_nodeStack.get(i);

			if (node.getParent() == null)
				continue;
			
			Node curNode = node;
			while (true) {
				if (curNode.getParent() == null)
					break;
				
				if (!curNode.getParent().getChildren().contains(curNode)) {
					d_nodeStack.remove(node);
					break;
				}
				
				curNode = curNode.getParent();
			}
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
