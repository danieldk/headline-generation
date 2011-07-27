package org.danieldk.sc.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.danieldk.sc.cli.Learn.SentenceFileFilter;
import org.danieldk.sc.dtree.DependencyTree;
import org.danieldk.sc.dtree.io.AlpinoDSReader;
import org.danieldk.sc.learn.Rule;
import org.danieldk.sc.trim.Trimmer;

public class TrimRandom {
	static final int NSENTENCES = 50;
	
	private static List<DependencyTree> extractIndices(List<DependencyTree> trees,
			TreeSet<Integer> indices)
	{
		List<DependencyTree> extractedTrees = new Vector<DependencyTree>();
		for (int index: indices)
			extractedTrees.add(trees.get(index));
		
		return extractedTrees;
	}
	
	private static String detokenizeSentence(String[] seq) {
		StringBuffer sb = new StringBuffer();
		for (String word: seq) {
			sb.append(word);
			sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Learn rules-ht rules-all pair_dir");
			System.exit(0);
		}
		
		// Read ht rules
		List<Rule> htRules = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
			htRules = (List<Rule>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Read all rules
		List<Rule> allRules = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[1]));
			allRules = (List<Rule>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Read training data.
		File dir = new File(args[2]);
		if (!dir.isDirectory()) {
			System.out.println(args[2] + " is not a directory!");
			System.exit(1);
		}
		
		System.out.print("Reading evaluation data... ");
		
//		List<DependencyTree> goldCorpus = new Vector<DependencyTree>();
		List<DependencyTree> dummyCorpus = new Vector<DependencyTree>();
		
		String sentenceFilenames[] = dir.list(new SentenceFileFilter());
		for (String sentenceFilename: sentenceFilenames) {
			sentenceFilename = args[2] + "/" + sentenceFilename;
			String headlineFilename = sentenceFilename.replaceAll("s\\.xml$",
					"h.xml");
			
//			DependencyTree goldTree = null;
			DependencyTree dummyTree = null;

			try {
				dummyTree = new AlpinoDSReader().readTree(
						new FileInputStream(sentenceFilename));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}

//			goldCorpus.add(goldTree);
			dummyCorpus.add(dummyTree);
		}
		
		TreeSet<Integer> randomIndices = new TreeSet<Integer>();
		while (randomIndices.size() != NSENTENCES) {
			int idx = (int) (Math.random() * (dummyCorpus.size() - 1));
			randomIndices.add(idx);
		}
		
//		goldCorpus = extractIndices(goldCorpus, randomIndices);
		dummyCorpus = extractIndices(dummyCorpus, randomIndices);
		
		Trimmer htTrimmer = new Trimmer(htRules);
		List<DependencyTree> htTrimmedCorpus = htTrimmer.trim(dummyCorpus, 8);

		Trimmer allTrimmer = new Trimmer(allRules);
		List<DependencyTree> allTrimmedCorpus = allTrimmer.trim(dummyCorpus, 8);

		double htComprSum = 0.0;
		double allComprSum = 0.0;
		
		for (int i = 0; i < dummyCorpus.size(); ++i) {
			System.out.println("S:" + detokenizeSentence(dummyCorpus.get(i).getSentence()));
			
			String htSentence = detokenizeSentence(htTrimmedCorpus.get(i).getSentence());
			String allSentence = detokenizeSentence(allTrimmedCorpus.get(i).getSentence());
			
			htComprSum += (double) htTrimmedCorpus.get(i).getSentence().length /
				dummyCorpus.get(i).getSentence().length;
			allComprSum += (double) allTrimmedCorpus.get(i).getSentence().length /
			dummyCorpus.get(i).getSentence().length;
			
			System.out.println("H:" + detokenizeSentence(htTrimmedCorpus.get(i).getSentence()));
			System.out.println("A:" + detokenizeSentence(allTrimmedCorpus.get(i).getSentence()));
			System.out.println("---");
		}
		
		System.out.println("H compression:" + htComprSum / NSENTENCES);
		System.out.println("A compression:" + htComprSum / NSENTENCES);	
	}
}
