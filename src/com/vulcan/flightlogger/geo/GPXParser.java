package com.vulcan.flightlogger.geo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.location.Location;

public class GPXParser {
	
	public static List<Location> parseRoutePoints(File gpxFile) {
		List<Location> list = new ArrayList<Location>();

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			FileInputStream fileInputStream = new FileInputStream(gpxFile);
			Document document = documentBuilder.parse(fileInputStream);
			Element elementRoot = document.getDocumentElement();

			// first, see if there are routes marked by the element 'waypt'
			NodeList nodelist_rtkpt = elementRoot.getElementsByTagName("wpt");

			for (int i = 0; i < nodelist_rtkpt.getLength(); i++) {

				Node node = nodelist_rtkpt.item(i);
				NamedNodeMap attributes = node.getAttributes();

				String newLatitude = attributes.getNamedItem("lat")
						.getTextContent();
				Double newLatitude_double = Double.parseDouble(newLatitude);

				String newLongitude = attributes.getNamedItem("lon")
						.getTextContent();
				Double newLongitude_double = Double.parseDouble(newLongitude);

				// for now, use the name field of the location to store
				// the waypt ordinal. TODO: Need to really go through the 
				// child nodes and find 'name'
				Element e = (Element)node;
				String newLocationName = e.getElementsByTagName("name").item(0).getTextContent();
				Location newLocation = new Location(newLocationName);
				newLocation.setLatitude(newLatitude_double);
				newLocation.setLongitude(newLongitude_double);

				list.add(newLocation);
			}
			fileInputStream.close();	

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}
	


}
