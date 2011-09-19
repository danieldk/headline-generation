package eu.danieldk.treetbl.cli;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtractSentences {
	static class Headline {
		String id;
		String headline;
		String firstPara;
	}

	/**
	 * @param args
	 */
	
	private static List<Headline> extractText(Document doc)
			throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		
		List<Headline> results = new Vector<Headline>();
		XPathExpression expr = xpath.compile("//artikel");
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		for (int i = 0; i < nodes.getLength(); ++i) {
			Headline headline = new Headline();
			NamedNodeMap attrs = nodes.item(i).getAttributes();
			Node idNode = attrs.getNamedItem("id");
			if (idNode == null)
				continue;
			headline.id = idNode.getTextContent();
			
			XPathExpression titleExpr = xpath.compile("body/ti/p[1]/text()");
			Object headlineStr = titleExpr.evaluate(nodes.item(i), XPathConstants.STRING);
			if (headlineStr == null)
				continue;
			headline.headline = (String) headlineStr;

			XPathExpression bodyTextExpr = xpath.compile("body/le/p[1]/text()");
			Object sentenceStr = bodyTextExpr.evaluate(nodes.item(i), XPathConstants.STRING);
			if (sentenceStr == null)
				continue;
			headline.firstPara = (String) sentenceStr;
			results.add(headline);
		}
		
		return results;
	}
	
	private static String extractFirstSentence(String text)
	{
		// Try to find the beginning of the first real sentence. Most articles
		// have a heading with a place and date.
		Pattern firstSentencePattern = Pattern.compile("[A-Z\"][A-Z\".]*[a-z]");
		Matcher firstSentenceMatcher = firstSentencePattern.matcher(text);
		if (!firstSentenceMatcher.find())
			return null;
		text = text.substring(firstSentenceMatcher.start());

		// Find the end of the first sentence.
		Pattern nextSentencePattern = Pattern.compile("[.!?]\\s+[A-Z\"]");
		Matcher nextSentenceMatcher = nextSentencePattern.matcher(text);
		// If there doesn't seem to be a match (possibly because there is no
		// succeeding sentence), return what we have.
		if (!nextSentenceMatcher.find())
			return text;
		return text.substring(0, nextSentenceMatcher.start() + 1);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Syntax: ExtractSentences corpus");
			System.exit(1);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(args[0]);
		
		List<Headline> texts = extractText(doc);
		for (Headline headline: texts) {
			String sentence = extractFirstSentence(headline.firstPara);
			if (sentence != null) {
				System.out.print(headline.id + "h|");
				System.out.println(headline.headline);
				System.out.print(headline.id + "s|");
				System.out.println(sentence);
			}
		}
	}
}
