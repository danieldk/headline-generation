package eu.danieldk.treetbl.dtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.danieldk.treetbl.dtree.util.DependencyTreeNodeIterator;

public class DependencyTree implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5384009195437094638L;
	private Node d_rootNode;
	
	public DependencyTree(Node rootNode) {
		d_rootNode = rootNode;
	}
	
	public DependencyTree(DependencyTree other) {
		d_rootNode = new Node(other.getRootNode());
	}
	
	public Node getRootNode() {
		return d_rootNode;
	}
	
	public String[] getSentence() {
		DependencyTreeNodeIterator iter = new DependencyTreeNodeIterator(getRootNode());
		List<Node> words = new ArrayList<Node>();
		while(iter.hasNext()) {
			Node node = iter.next();
			if (node.containsAttribute("word"))
				words.add(node);
		}
		
		Collections.sort(words, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				if (o1.getBegin() > o2.getBegin())
					return 1;
				else
					return -1;
			}
		});
		
		String results[] = new String[words.size()];
		for (int i = 0; i < words.size(); ++i)
			results[i] = words.get(i).getAttrValue("word");
		
		return results;
	}
	
	public List<String> getRoots() {
		DependencyTreeNodeIterator iter = new DependencyTreeNodeIterator(getRootNode());
		List<Node> words = new ArrayList<Node>();
		while(iter.hasNext()) {
			Node node = iter.next();
			if (node.containsAttribute("root"))
				words.add(node);
		}
		
		Collections.sort(words, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				if (o1.getBegin() > o2.getBegin())
					return 1;
				else
					return -1;
			}
		});
		
		List<String> results = new ArrayList<String>();
		for (Node node: words)
			results.add(node.getAttrValue("root"));
		
		return results;
	}
}
