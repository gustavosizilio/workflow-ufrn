package org.domain.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLManager {
	protected String extraxtName(String nodeName) {
		if(nodeName.contains(":")){
			return nodeName.split(":")[1].toLowerCase(); 
		}else{
			return nodeName.toLowerCase(); 
		}
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
