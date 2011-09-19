package eu.danieldk.sc.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import eu.danieldk.sc.dtree.DependencyTree;
import eu.danieldk.sc.dtree.Node;
import eu.danieldk.sc.dtree.io.AlpinoDSReader;
import eu.danieldk.sc.dtree.io.DependencyTreeReader;

public class FindPairs {
	static class SentenceFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith("s.xml");
		}	
	}
	
	private static Set<String> wordRoots(Document doc) throws XPathException {
		Set<String> roots = new HashSet<String>();
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		XPathExpression expr = xpath.compile("//@root");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		for (int i = 0; i < nodes.getLength(); ++i)
			roots.add(nodes.item(i).getTextContent());
		
		return roots;
	}
	
	private static boolean isOneNodeTree(DependencyTree tree) {
		// Get the children of the root.
		List<Node> children = tree.getRootNode().getChildren();
		if (children.size() == 0)
			return true;
		
		// If the root node has only one child, check whether this child
		// has no children.
		if (children.size() == 1 && children.get(0).getChildren().size() == 0)
			return true;
		
		return false;		
	}
	
	private static boolean isMWU(DependencyTree tree) {
		// Get the children of the root.
		List<Node> children = tree.getRootNode().getChildren();
		if (children.size() == 0)
			return true;
		
		// If the root node has only one child, check whether this child
		// is a MWU.
		if (children.size() == 1 && children.get(0).getAttrValue("cat").equals("mwu"))
			return true;
		
		return false;		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("FindPairs directory");
			System.exit(0);
		}

		File dir = new File(args[0]);
		if (!dir.isDirectory()) {
			System.out.println(args[0] + " is not a directory!");
			System.exit(1);
		}
		
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			builder = factory.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			System.out.println("Could not create an XML parser!");
			e.printStackTrace();
			System.exit(0);
		}
		
		DependencyTreeReader dependencyTreeReader = new AlpinoDSReader();

		String sentenceFilenames[] = dir.list(new SentenceFileFilter());
		for (String sentenceFilename: sentenceFilenames) {
			sentenceFilename = args[0] + "/" + sentenceFilename;
			String headlineFilename = sentenceFilename.replaceAll("s\\.xml$",
					"h.xml");
			
			Set<String> headlineRoots = null;
			Set<String> sentenceRoots = null;

			try {
				Document headlineDoc = builder.parse(headlineFilename);
				headlineRoots = wordRoots(headlineDoc);
				Document sentenceDoc = builder.parse(sentenceFilename);
				sentenceRoots = wordRoots(sentenceDoc);
				
				if (sentenceRoots.containsAll(headlineRoots)) {
					DependencyTree depTree = dependencyTreeReader.readTree(new FileInputStream(headlineFilename));
					//if (depTree.getSentence().length < 3)
					//	continue;
					if (isOneNodeTree(depTree))// || isMWU(depTree))
						continue;

					System.out.println(sentenceFilename);
					System.out.println(headlineFilename);
/*					String[] sentence = depTree.getSentence();
					for (String word: sentence)
						System.out.print(word + " ");
					System.out.println();*/
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
		}
	}
}
