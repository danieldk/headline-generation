package eu.danieldk.treetbl.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectOutputStream;
import java.util.Vector;

import eu.danieldk.treetbl.dtree.DataSet;
import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.dtree.io.AlpinoDSReader;

public class SerializeTrees {
	static class HeadlineFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith("h.xml");
		}	
	}
	
	static class SentenceFileFilter implements FilenameFilter {
		public SentenceFileFilter(String basename) {
			d_basename = basename + "a";
		}
		
		public boolean accept(File dir, String name) {
			if (name.endsWith(".xml") && name.startsWith(d_basename))
				return true;
			else
				return false;
		}
		
		String d_basename;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Learn directory rules_output");
			System.exit(0);
		}

		File dir = new File(args[0]);
		if (!dir.isDirectory()) {
			System.out.println(args[0] + " is not a directory!");
			System.exit(1);
		}

		System.out.print("Reading training data... ");
		
		Vector<DependencyTree> goldCorpus = new Vector<DependencyTree>();
		Vector<DependencyTree> dummyCorpus = new Vector<DependencyTree>();
		
		String headlineFilenames[] = dir.list(new HeadlineFileFilter());
		for (String headlineFilename: headlineFilenames) {
			String basename = headlineFilename.replaceAll("h\\.xml$", "");
			
			// Not deterministic, there can be more than one sentence per headline.
			String[] sentenceFilenames = dir.list(new SentenceFileFilter(basename));

			headlineFilename = args[0] + "/" + headlineFilename;

			for (String sentenceFilename: sentenceFilenames) {
				sentenceFilename = args[0] + "/" + sentenceFilename;				
				
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
				
				/*if (goldTree.getSentence().length < 3 ||
						dummyTree.getSentence().length < 3)
					continue;*/
				
				
				if (goldTree.getSentence().length == 0 ||
						dummyTree.getSentence().length == 0)
					continue;
				
				goldCorpus.add(goldTree);
				dummyCorpus.add(dummyTree);
			}
		}
		
		
		DataSet dataSet = new DataSet(goldCorpus, dummyCorpus);
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(args[1]));
			out.writeObject(dataSet);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
