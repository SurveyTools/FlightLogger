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

import com.vulcan.flightlogger.geo.data.Route;
import com.vulcan.flightlogger.geo.data.Transect;

import android.location.Location;


public class GPSUtils {
	
	/**
	 * Parses GPX routes for use in navigation. Expected format of the form:
	 * <rte><name>Session 1</name>
	 *   <rtept lat="-3.4985590" lon="38.9554692"><ele>-32768.000</ele><name>T01_S</name><sym>Waypoint</sym></rtept>
	 *   <rtept lat="-3.0642325" lon="39.2115345"><ele>-32768.000</ele><name>T01_N</name><sym>Waypoint</sym></rtept>
	 *   <rtept lat="-3.0546140" lon="39.1860712"><ele>-32768.000</ele><name>T02_N</name><sym>Waypoint</sym></rtept>
	 *   <rtept lat="-3.5290935" lon="38.9157593"><ele>-32768.000</ele><name>T02_S</name><sym>Waypoint</sym></rtept>
	 *   <rtept lat="-3.5115202" lon="38.8969005"><ele>-32768.000</ele><name>T03_S</name><sym>Waypoint</sym></rtept>
	 *   <rtept lat="-3.0044045" lon="39.1936147"><ele>-32768.000</ele><name>T03_N</name><sym>Waypoint</sym></rtept>
	 * </rte>
	 * @author jayl
	 *
	 */
	public static List<Route> parseRoute(File gpxFile) {
		List<Route>routeMap = new ArrayList<Route>();

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			FileInputStream fileInputStream = new FileInputStream(gpxFile);
			Document document = documentBuilder.parse(fileInputStream);
			Element elementRoot = document.getDocumentElement();
			
			NodeList nodelist_routes = elementRoot.getElementsByTagName("rte");
			
			for (int i = 0; i < nodelist_routes.getLength(); i++) 
			{
				Node routeNode = nodelist_routes.item(i);
				Element routeEl = (Element) routeNode;
				Route r = new Route();
				r.mName = routeEl.getElementsByTagName("name").item(0).getTextContent();
				// see if there are waypoints marked by the element 'rtept'
				NodeList nodelist_rtkpt = elementRoot.getElementsByTagName("rtept");
	
				for (int j = 0; j < nodelist_rtkpt.getLength(); j++) {
			
					Node node = nodelist_rtkpt.item(j);
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
					String wayptName = e.getElementsByTagName("name").item(0).getTextContent();
					Location loc = new Location(wayptName);
					loc.setLatitude(newLatitude_double);
					loc.setLongitude(newLongitude_double);
					
					r.addWayPoint(loc);
				}
				routeMap.add(r);
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

		return routeMap;
	}
	
	public static List<Transect> parseTransects(Route route) {
		List<Transect> transects = new ArrayList<Transect>();
		int transectIndex = 1;
		List<Location> wp = route.mWayPoints;
		// Naively assume that transects are ordered pairs of waypoints for now
		if (wp.size() > 0 && wp.size() % 2 == 0)
		{
			for (int i=0; i<wp.size(); i+=2)
			{
				Transect tp = new Transect();
				tp.mName = "transect " + transectIndex;
				tp.mStartWaypt = wp.get(i);
				tp.mEndWaypt = wp.get(i+1);
				
				transects.add(tp);
			}
		}
		return transects;
	}
	
	


}
