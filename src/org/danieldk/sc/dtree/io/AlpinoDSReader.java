package org.danieldk.sc.dtree.io;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.danieldk.sc.dtree.DependencyTree;
import org.danieldk.sc.dtree.Node;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class AlpinoDSReader implements DependencyTreeReader {
	public static interface Tokens {
		// Elements
		public static final String NODE = "node";

		// Attributes
		public static final String ID = "id";
		public static final String BEGIN = "begin";
		public static final String END = "end";
		public static final String CAT = "cat";
		public static final String REL = "rel";
		public static final String POS = "pos";
		public static final String ROOT = "root";
		public static final String WORD = "word";
	}

	public class AlpinoDSHandler extends DefaultHandler implements Tokens {
		private DependencyTree d_tree;
		private org.danieldk.sc.dtree.Node d_currentNode;
		
			public DependencyTree getTree() {
			return d_tree;
		}

		@Override
		public void startDocument() {
		}
		
		@Override
		public void endDocument() {
			d_tree = new DependencyTree(d_currentNode);
		}
		
		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes attrs) {
			Node n;
			if (qName.equals(NODE)) {
				if (attrs.getIndex(WORD) != -1)
					n = createWordNode(attrs);
				else
					n = createStructureNode(attrs);
				
				if (d_currentNode != null)
					d_currentNode.addChild(n);
				d_currentNode = n;
			}
		}
		
		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) {
			if (qName.equals(NODE) && d_currentNode.getParent() != null)
				d_currentNode = d_currentNode.getParent();
		}

		protected Node createStructureNode(Attributes attrs) {
			int id = Integer.parseInt(attrs.getValue(ID));
			int begin = Integer.parseInt(attrs.getValue(BEGIN));
			int end = Integer.parseInt(attrs.getValue(END));
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put(CAT, attrs.getValue(CAT));
			attributes.put(REL, attrs.getValue(REL));

			return new Node(d_currentNode, id, begin, end, attributes);
		}
		
		protected Node createWordNode(Attributes attrs) {
			int id = Integer.parseInt(attrs.getValue(ID));
			int begin = Integer.parseInt(attrs.getValue(BEGIN));
			int end = Integer.parseInt(attrs.getValue(END));
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put(REL, attrs.getValue(REL));
			attributes.put(POS, attrs.getValue(POS));
			attributes.put(ROOT, attrs.getValue(ROOT));
			attributes.put(WORD, attrs.getValue(WORD));


			return new Node(d_currentNode, id, begin, end, attributes);
		}
	}
	
	public DependencyTree readTree(InputStream is) throws Exception {
		AlpinoDSHandler handler = new AlpinoDSHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		javax.xml.parsers.SAXParser parser = factory.newSAXParser();
		parser.parse(is, handler);
		return handler.getTree();
	}
}
