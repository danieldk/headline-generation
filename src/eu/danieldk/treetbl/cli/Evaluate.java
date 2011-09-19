package eu.danieldk.treetbl.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import eu.danieldk.treetbl.cli.Learn.HeadlineFileFilter;
import eu.danieldk.treetbl.cli.Learn.SentenceFileFilter;
import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.dtree.io.AlpinoDSReader;
import eu.danieldk.treetbl.eval.NgramEval;
import eu.danieldk.treetbl.eval.Rouge;
import eu.danieldk.treetbl.learn.Rule;
import eu.danieldk.treetbl.learn.ScoreFun;
import eu.danieldk.treetbl.trim.Trimmer;

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
		
		String headlineFilenames[] = dir.list(new HeadlineFileFilter());
		for (String headlineFilename: headlineFilenames) {
			String basename = headlineFilename.replaceAll("h\\.xml$", "");

			// Not deterministic, there can be more than one sentence per headline.
			String[] sentenceFilenames = dir.list(new SentenceFileFilter(basename));

			headlineFilename = args[1] + "/" + headlineFilename;

			for (String sentenceFilename: sentenceFilenames) {
				sentenceFilename = args[1] + "/" + sentenceFilename;				

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
