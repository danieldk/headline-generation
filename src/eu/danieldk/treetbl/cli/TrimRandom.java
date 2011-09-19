package eu.danieldk.treetbl.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import eu.danieldk.treetbl.dtree.DataSet;
import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.learn.Rule;
import eu.danieldk.treetbl.trim.Trimmer;

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
		
		
		File data = new File(args[2]);
		if (!data.isFile() || !data.canRead()) {
			System.out.println(args[2] + " cannot be opened or read!");
			System.exit(1);
		}

		System.out.print("Reading evaluation data... ");
		
		DataSet dataset = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
			dataset = (DataSet) in.readObject();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Done!");

		List<DependencyTree> dummyCorpus = dataset.dummyCorpus();
		
		TreeSet<Integer> randomIndices = new TreeSet<Integer>();
		while (randomIndices.size() != NSENTENCES) {
			int idx = (int) (Math.random() * (dummyCorpus.size() - 1));
			randomIndices.add(idx);
		}
		
		dummyCorpus = extractIndices(dummyCorpus, randomIndices);
		
		Trimmer htTrimmer = new Trimmer(htRules);
		List<DependencyTree> htTrimmedCorpus = htTrimmer.trim(dummyCorpus, 8);

		Trimmer allTrimmer = new Trimmer(allRules);
		List<DependencyTree> allTrimmedCorpus = allTrimmer.trim(dummyCorpus, 8);

		double htComprSum = 0.0;
		
		for (int i = 0; i < dummyCorpus.size(); ++i) {
			System.out.println("S:" + detokenizeSentence(dummyCorpus.get(i).getSentence()));
						
			htComprSum += (double) htTrimmedCorpus.get(i).getSentence().length /
				dummyCorpus.get(i).getSentence().length;
			
			System.out.println("H:" + detokenizeSentence(htTrimmedCorpus.get(i).getSentence()));
			System.out.println("A:" + detokenizeSentence(allTrimmedCorpus.get(i).getSentence()));
			System.out.println("---");
		}
		
		System.out.println("H compression:" + htComprSum / NSENTENCES);
		System.out.println("A compression:" + htComprSum / NSENTENCES);	
	}
}
