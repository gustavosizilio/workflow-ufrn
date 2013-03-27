package org.domain.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLManager {
	protected String file;
	
	protected String extraxtName(String nodeName) {
		if(nodeName.contains(":")){
			return nodeName.split(":")[1].toLowerCase(); 
		}else{
			return nodeName.toLowerCase(); 
		}
	}
	
	public Document getDOM() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(this.file);
	}
	
	protected String getAttribute(Node item, String attribute) {
		return (item.getAttributes().getNamedItem(attribute) != null ? item.getAttributes().getNamedItem(attribute).getNodeValue() : null);
	}
	
	protected String getTagName(Node item) {
		return extraxtName(item.getNodeName());
	}
	
	protected List<Node> getElements(NodeList nl) {
		List<Node> newNl = new ArrayList<Node>();
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				if(nl.item(i).getNodeType() == 1){ //Somente do tipo ELEMENT
					newNl.add(nl.item(i));
				}
			}
		}
		return newNl;
	}

	
}
