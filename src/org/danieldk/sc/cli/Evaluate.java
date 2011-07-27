package org.danieldk.sc.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.danieldk.sc.cli.Learn.SentenceFileFilter;
import org.danieldk.sc.dtree.DependencyTree;
import org.danieldk.sc.dtree.io.AlpinoDSReader;
import org.danieldk.sc.eval.NgramEval;
import org.danieldk.sc.eval.Rouge;
import org.danieldk.sc.learn.Rule;
import org.danieldk.sc.learn.ScoreFun;
import org.danieldk.sc.trim.Trimmer;

public class Evaluate {

	private static double average(Collection<Double> data) {
		double sum = 0.0;

		for (double elem: data)
			sum += elem;
		
		return sum / data.size();
	}
	
	private static double stddev(Collection<Double> data) {
		double avg = average(data);
		
		double errorSquareSum = 0.0;
		for (double elem: data) {
			double error = elem - avg;
			errorSquareSum += error * error;
		}
		
		return Math.sqrt(errorSquareSum / (data.size() - 1));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Learn rules pair_dir");
			System.exit(0);
		}
		
		// Read rules
		List<Rule> rules = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
			rules = (List<Rule>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Read training data.
		File dir = new File(args[1]);
		if (!dir.isDirectory()) {
			System.out.println(args[1] + " is not a directory!");
			System.exit(1);
		}
		
		System.out.print("Reading evaluation data... ");
		
		Vector<DependencyTree> goldCorpus = new Vector<DependencyTree>();
		Vector<DependencyTree> dummyCorpus = new Vector<DependencyTree>();
		
		String sentenceFilenames[] = dir.list(new SentenceFileFilter());
		for (String sentenceFilename: sentenceFilenames) {
			sentenceFilename = args[1] + "/" + sentenceFilename;
			String headlineFilename = sentenceFilename.replaceAll("s\\.xml$",
					"h.xml");
			
			DependencyTree goldTree = null;
			DependencyTree dummyTree = null;

			try {
				goldTree = new AlpinoDSReader().readTree(
						new FileInputStream(headlineFilename));
				dummyTree = new AlpinoDSReader().readTree(
						new FileInputStream(sentenceFilename));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}

			// XXX
			if (goldTree.getSentence().length < 1 ||
					dummyTree.getSentence().length < 1)
				continue;

			goldCorpus.add(goldTree);
			dummyCorpus.add(dummyTree);
		}
		
		System.out.println("Done!");
		
		System.out.print("Trimming... ");
		Trimmer trimmer = new Trimmer(rules);
		List<DependencyTree> trimmedCorpus =
			trimmer.trim(goldCorpus, dummyCorpus);
		System.out.println("Done!");
		
		ScoreFun scoreFun = new ScoreFun() {
			public Double call(DependencyTree arg1, DependencyTree arg2) {
				String[] arg1Words = arg1.getSentence();
				String[] arg2Words = arg2.getSentence();
				return Rouge.rougeL(arg1Words, arg2Words, 1.0);
			}
		};
		
		ScoreFun unigramPrecScore = new ScoreFun() {
			public Double call(DependencyTree arg1, DependencyTree arg2) {
				String[] arg1Words = arg1.getRoots();
				String[] arg2Words = arg2.getRoots();
				return NgramEval.nGramPrecision(arg1Words, arg2Words, 1);
			}
		};
		
		double dummySum = 0.0;
		double trimmedSum = 0.0;
		double dummyNGramPrecSum = 0.0;
		double trimmedNGramPrecSum = 0.0;

		List<Double> dummyScores = new Vector<Double>();
		List<Double> trimmedScores = new Vector<Double>();
		List<Double> dummyUnigramPrecs = new Vector<Double>();
		List<Double> trimmedUnigramPrecs = new Vector<Double>();
		
		for (int i = 0; i < goldCorpus.size(); ++i) {
			double dummyScore = scoreFun.call(goldCorpus.get(i), dummyCorpus.get(i));
			double trimmedScore = scoreFun.call(goldCorpus.get(i), trimmedCorpus.get(i));
			double dummyNGramPrec = unigramPrecScore.call(goldCorpus.get(i), dummyCorpus.get(i));
			double trimmedNGramPrec = unigramPrecScore.call(goldCorpus.get(i), trimmedCorpus.get(i));

			dummyScores.add(dummyScore);
			trimmedScores.add(trimmedScore);
			dummyUnigramPrecs.add(dummyNGramPrec);
			trimmedUnigramPrecs.add(trimmedNGramPrec);
			
			System.out.print(i + ": ");
			System.out.println(dummyScore + " " + trimmedScore + " " + dummyNGramPrec + " " + trimmedNGramPrec);
		}
		
		System.out.println("Total (ROUGE-L): " + average(dummyScores) + " (" +
				stddev(dummyScores) + ") " + average(trimmedScores) + " (" +
				stddev(trimmedScores) + ")");
		System.out.println("Unigram Precision: " + average(dummyUnigramPrecs) + " (" +
				stddev(dummyUnigramPrecs)  + ") " + average(trimmedUnigramPrecs) +
				" (" + stddev(trimmedUnigramPrecs) + ")");
		

	}
}
