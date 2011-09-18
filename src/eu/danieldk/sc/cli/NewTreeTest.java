package eu.danieldk.sc.cli;

import java.io.FileInputStream;

import eu.danieldk.sc.dtree.DependencyTree;
import eu.danieldk.sc.dtree.io.AlpinoDSReader;


public class NewTreeTest {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Incorrect syntax!");
			System.exit(1);
		}
		
		DependencyTree tree = null;
		try {
			tree = new AlpinoDSReader().readTree(new FileInputStream(args[0]));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		for (String word: tree.getSentence())
			System.out.println(word);
	}
}
