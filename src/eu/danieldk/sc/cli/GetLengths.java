package eu.danieldk.sc.cli;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Vector;

import eu.danieldk.sc.cli.Learn.SentenceFileFilter;
import eu.danieldk.sc.dtree.DependencyTree;
import eu.danieldk.sc.dtree.io.AlpinoDSReader;

public class GetLengths {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Learn pair_dir");
			System.exit(0);
		}

		// Read training data.
		File dir = new File(args[0]);
		if (!dir.isDirectory()) {
			System.out.println(args[0] + " is not a directory!");
			System.exit(1);
		}

		String sentenceFilenames[] = dir.list(new SentenceFileFilter());
		for (String sentenceFilename: sentenceFilenames) {
			sentenceFilename = args[0] + "/" + sentenceFilename;
			String headlineFilename = sentenceFilename.replaceAll("s\\.xml$",
				"h.xml");

			DependencyTree tree = null;

			try {
				tree = new AlpinoDSReader().readTree(
						new FileInputStream(headlineFilename));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			System.out.println(tree.getSentence().length);
		}
	}
}
