package eu.danieldk.treetbl.cli.dsfix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.danieldk.treetbl.dtree.DataSet;
import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.dtree.Node;
import eu.danieldk.treetbl.dtree.util.DependencyTreeNodeIterator;
import eu.danieldk.treetbl.learn.RuleGenerator;

public class Learn {
	private static void trimTree(DependencyTree tree, Set<String> keepRoots) {
		Iterator<Node> iter = new DependencyTreeNodeIterator(tree.getRootNode());
		
		while (iter.hasNext()) {
			Node node = iter.next();
			
			if (node.containsAttribute("root")) {
				if (!keepRoots.contains(node.getAttrValue("root"))) {
					node.getParent().removeChild(node);
					return;
				}
			}

		}		
	}
	
	private static void trimTrees(DataSet ds) {
		for (int i = 0; i < ds.dummyCorpus().size(); ++i) {
			DependencyTree candidate = ds.dummyCorpus().elementAt(i);
			Set<String> candRoots = new HashSet<String>(candidate.getRoots());
			
			Set<String> goldRoots = new HashSet<String>(ds.goldCorpus().elementAt(i).getRoots());
			
			Set<String> intersect = new HashSet<String>(candRoots);
			intersect.retainAll(goldRoots);
			
			trimTree(candidate, intersect);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Learn directory rules_output");
			System.exit(0);
		}

		File data = new File(args[0]);
		if (!data.isFile() || !data.canRead()) {
			System.out.println(args[0] + " cannot be opened or read!");
			System.exit(1);
		}

		System.out.print("Reading training data... ");
		
		DataSet dataset = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
			dataset = (DataSet) in.readObject();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Done!");
		
		System.out.print("Trimming candidates... ");
		trimTrees(dataset);
		System.out.println("Done!");		
		
		Set<RuleGenerator> ruleGens = new HashSet<RuleGenerator>();
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
