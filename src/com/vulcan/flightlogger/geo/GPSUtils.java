package com.vulcan.flightlogger.geo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

	public final static float METERS_PER_FOOT = (float) 3.28084;
	
	public static final double EARTH_RADIUS_METERS = 6371008.7714; // mean avg for WGS84 projection 

	public enum TransectParsingMethod { 
		ADJACENT_PAIRS, 
		ANGLES_OVER_20_NO_DUPS, 
		ANGLES_OVER_30_NO_DUPS
	}
	
	/**
	 * Parses GPX routes for use in navigation. Expected format of the form:
	 * <rte><name>Session 1</name> <rtept lat="-3.4985590"
	 * lon="38.9554692"><ele>
	 * -32768.000</ele><name>T01_S</name><sym>Waypoint</sym></rtept> <rtept
	 * lat="-3.0642325"
	 * lon="39.2115345"><ele>-32768.000</ele><name>T01_N</name><
	 * sym>Waypoint</sym></rtept> <rtept lat="-3.0546140"
	 * lon="39.1860712"><ele>-
	 * 32768.000</ele><name>T02_N</name><sym>Waypoint</sym></rtept> <rtept
	 * lat="-3.5290935"
	 * lon="38.9157593"><ele>-32768.000</ele><name>T02_S</name><
	 * sym>Waypoint</sym></rtept> <rtept lat="-3.5115202"
	 * lon="38.8969005"><ele>-
	 * 32768.000</ele><name>T03_S</name><sym>Waypoint</sym></rtept> <rtept
	 * lat="-3.0044045"
	 * lon="39.1936147"><ele>-32768.000</ele><name>T03_N</name><
	 * sym>Waypoint</sym></rtept> </rte>
	 * 
	 * @author jayl
	 * 
	 */
	public static List<Route> parseRoute(File gpxFile) {
		List<Route> routeMap = new ArrayList<Route>();

		if (gpxFile == null)
			return routeMap;
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			FileInputStream fileInputStream = new FileInputStream(gpxFile);
			Document document = documentBuilder.parse(fileInputStream);
			Element elementRoot = document.getDocumentElement();

			NodeList nodelist_routes = elementRoot.getElementsByTagName("rte");

			for (int i = 0; i < nodelist_routes.getLength(); i++) {
				Node routeNode = nodelist_routes.item(i);
				Element routeEl = (Element) routeNode;
				Route r = new Route();
				r.gpxFile = gpxFile.getName();
				r.mName = routeEl.getElementsByTagName("name").item(0)
						.getTextContent();
				// see if there are waypoints marked by the element 'rtept'
				NodeList nodelist_rtkpt = elementRoot
						.getElementsByTagName("rtept");

				for (int j = 0; j < nodelist_rtkpt.getLength(); j++) {

					Node node = nodelist_rtkpt.item(j);
					NamedNodeMap attributes = node.getAttributes();

					String newLatitude = attributes.getNamedItem("lat")
							.getTextContent();
					Double newLatitude_double = Double.parseDouble(newLatitude);

					String newLongitude = attributes.getNamedItem("lon")
							.getTextContent();
					Double newLongitude_double = Double
							.parseDouble(newLongitude);

					// for now, use the name field of the location to store
					// the waypt ordinal. TODO: Need to really go through the
					// child nodes and find 'name'
					Element e = (Element) node;
					String wayptName = e.getElementsByTagName("name").item(0)
							.getTextContent();
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

	public static List<Transect> parseTransectsUsingPairs(Route route) {
		List<Transect> transects = new ArrayList<Transect>();
		
		if (route == null)
			return transects;
		
		int transectIndex = 1;
		List<Location> wp = route.mWayPoints;
		// Naively assume that transects are ordered pairs of waypoints for now
		if (wp.size() > 0 && wp.size() % 2 == 0) {
			for (int i = 0; i < wp.size(); i += 2) {
				transects.add(new Transect(wp.get(i), wp.get(i+1), route.gpxFile, route.mName, transectIndex++));
			}
		}
		return transects;
	}

	public static List<Transect> parseTransectsUsingAngles(Route route, double maxAngle, boolean allowDuplicates) {
		List<Transect> transects = new ArrayList<Transect>();
		
		if ((route == null) || (route.mWayPoints == null) || (route.mWayPoints.size() <= 1))
			return transects;
		
		int transectIndex = 1;
		List<Location> wp = route.mWayPoints;

		int n = wp.size();
		Location start = null;
		Location end = null;
		float lastAngle = 0;
		
		for (int i=0; i < n; i++) {
			
			if (start == null) {
				// start a brand new transect
				start = wp.get(i);
			} else if (end == null) {
				// finish making the brand new transect (easy index checking)
				end = wp.get(i);
				lastAngle = start.bearingTo(end);
			} else {
				// get the current loc
				Location cur = wp.get(i);
				float curAngle = end.bearingTo(cur);
				float deviationAngle = Math.abs(curAngle - lastAngle);
				boolean isDuplicate = (curAngle < .01) && (end.distanceTo(cur) < .01); 

				if ((deviationAngle > maxAngle) || (isDuplicate && !allowDuplicates)) {
					// make a new transect
					transects.add(new Transect(start, end, route.gpxFile, route.mName, transectIndex++));

					// start a new transect at the current point
					start = cur;
					end = null;
				} else {
					// current point is the new end of the transect
					end = cur;
					// adjust the angle accordingly
					lastAngle = curAngle;
				}
			}
		}
		
		// open transect or 2-point route
		if ((start != null) && (end != null)) {
			transects.add(new Transect(start, end, route.gpxFile, route.mName, transectIndex++));
			end = null;
		} else if (end != null) {
			// TODO - start with no end... waypoint mismatch
		}
		
		return transects;
	}

	public static List<Transect> parseTransectsWithMethod(Route route, TransectParsingMethod parsingMethod) {
		switch (parsingMethod) {
		
		case ADJACENT_PAIRS:
			return parseTransectsUsingPairs(route);

		case ANGLES_OVER_20_NO_DUPS:
			return parseTransectsUsingAngles(route, 20, false);
			
		case ANGLES_OVER_30_NO_DUPS:
			return parseTransectsUsingAngles(route, 30, false);
		}
		
		// no dice - return empty list
		return new ArrayList<Transect>();
	}

	public static List<Transect> parseTransects(Route route) {
		// default
		return parseTransectsWithMethod(route, TransectParsingMethod.ANGLES_OVER_20_NO_DUPS);
	}

	public static Route getDefaultRouteFromList(List<Route> routes) {
		if (routes != null) {
			if (!routes.isEmpty()) {
				return routes.get(0);
			}
		}

		// no dice
		return null;
	}

	public static Route getDefaultRouteFromFile(File gpxFileObj) {
		if (gpxFileObj != null) {
		   return getDefaultRouteFromList(GPSUtils.parseRoute(gpxFileObj));
		}
		
		// no dice
		return null;
	}

	public static Route findRouteByName(String targetRouteName, List<Route>routesList) {
		if ((targetRouteName != null) && (routesList != null)) {
		    
			Iterator<Route> iterator = routesList.iterator();
			while (iterator.hasNext()) {
				Route routeItem = iterator.next();
				if (routeItem.matchesByName(targetRouteName)) {
					// winner!
					return routeItem;
				}
			}
		}
		
		// no dice
		return null;
	}

	public static Route findRouteInFile(String targetRouteName, File gpxFileObj) {
		if (gpxFileObj != null) {
		    List<Route> routes = GPSUtils.parseRoute(gpxFileObj);
		    
		    if (routes != null) {
			   if (!routes.isEmpty())
					   return routes.get(0);
		    }
		}
		
		// no dice
		return null;
	}

	public static Transect findTransectInList(String targetTransectName, List<Transect> transects) {
		if (transects != null) {
			Iterator<Transect> iterator = transects.iterator();
			while (iterator.hasNext()) {
				Transect transItem = iterator.next();
				if (transItem.matchesByName(targetTransectName)) {
					// winner!
					return transItem;
				}
			}
		}
		
		// no dice
		return null;
	}

	public static Transect findTransectInRoute(String targetTransectName, Route route) {
		if (route != null) {
			return findTransectInList(targetTransectName, GPSUtils.parseTransects(route));
		}
		
		// no dice
		return null;
	}

	public static Route getDefaultRouteFromFilename(String gpxFilename) {
		if (gpxFilename != null) {
			return getDefaultRouteFromFile(new File(gpxFilename));
		}
		
		// no dice
		return null;
	}

	public static Transect getDefaultTransectFromList(List<Transect> transects) {
		if (transects != null) {
			if (!transects.isEmpty())
				return transects.get(0);
		}

		// no dice
		return null;
	}

	public static Transect getDefaultTransectFromRoute(Route route) {
		if (route != null) {
		    return getDefaultTransectFromList(GPSUtils.parseTransects(route));
		}
		
		// no dice
		return null;
	}

}
