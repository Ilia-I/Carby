import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Dictonary class conforming to the UK gov standard
 * for Nutrition table contents
 * @author Martin Peev
 *
 */
public final class Dictionary {
	Map<String,String> dict;
	
	public Dictionary() {
		dict = new HashMap<String,String>();
		readXML();
	}

	/**
	 * read in XML file
	 */
	private void readXML() {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File("Dictionary"+File.separator+"dict.xml"));

			// normalize text representation
			doc.getDocumentElement().normalize();
//			System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());

			NodeList listOfRows = doc.getElementsByTagName("row");
			int totalRows = listOfRows.getLength();
			System.out.println("Total no of entries : " + totalRows);

			for (int s = 0; s < listOfRows.getLength(); s++) {

				Node firstNode = listOfRows.item(s);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element firstElement = (Element) firstNode;

					NodeList col1List = firstElement.getElementsByTagName("col1");
					Element firstColElement = (Element) col1List.item(0);

					NodeList textCol1List = firstColElement.getChildNodes();
//					System.out.println("col1:" + ((Node) textCol1List.item(0)).getNodeValue().trim());

					NodeList col2List = firstElement.getElementsByTagName("col2");
					Element lastNameElement = (Element) col2List.item(0);

					NodeList textCol2List = lastNameElement.getChildNodes();
//					System.out.println("col2:" + ((Node) textCol2List.item(0)).getNodeValue().trim());
					
					dict.put(((Node) textCol1List.item(0)).getNodeValue().trim(), ((Node) textCol2List.item(0)).getNodeValue().trim());

				}
			}

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}	
	
	/**
	 * check if col2 corresponds to expected col2 contents of col1 row
	 * 
	 * @param col1 column 1 content
	 * @param col2 column 2 content
	 * @return check if confirms to regex expr
	 */
	public boolean expectedCol2(String col1, String col2) {
		if(dict.get(col1)==null) return false;
		
		Pattern p = Pattern.compile(dict.get(col1));		
		return Pattern.matches(dict.get(col1), col2);
	}
}
