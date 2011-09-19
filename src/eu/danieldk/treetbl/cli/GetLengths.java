package eu.danieldk.treetbl.cli;

import java.io.File;
import java.io.FileInputStream;

import eu.danieldk.treetbl.cli.Learn.HeadlineFileFilter;
import eu.danieldk.treetbl.cli.Learn.SentenceFileFilter;
import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.dtree.io.AlpinoDSReader;

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
		
		String headlineFilenames[] = dir.list(new HeadlineFileFilter());
		for (String headlineFilename: headlineFilenames) {
			String basename = headlineFilename.replaceAll("h\\.xml$", "");

			// Not deterministic, there can be more than one sentence per headline.
			String[] sentenceFilenames = dir.list(new SentenceFileFilter(basename));

			headlineFilename = args[0] + "/" + headlineFilename;

			for (String sentenceFilename: sentenceFilenames) {
				sentenceFilename = args[0] + "/" + sentenceFilename;				

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
}
