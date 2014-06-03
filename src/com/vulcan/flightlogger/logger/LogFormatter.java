package com.vulcan.flightlogger.logger;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.location.Location;

// OK, OK, in OO this would typically by a 'Visitor' object. We may visit that
// approach if we need multiple formats, but for now, let's keep it simple-ish...

public class LogFormatter {

	private static final NumberFormat ELEVATION_FORMAT = NumberFormat
			.getInstance(Locale.US);
	private static final SimpleDateFormat ISO_8601_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

	static {
		ELEVATION_FORMAT.setMaximumFractionDigits(1);
		ELEVATION_FORMAT.setGroupingUsed(false);
		ISO_8601_DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public final String GPX_HEADER = new StringBuilder()
			.append("\"<?xml version=\"1.0\" encoding=\"UTF-8\"?> ")
			.append("<gpx version=\"1.0\"")
			.append("creator=\"Vulcan FlightLogger\"")
			.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
			.append("xmlns=\"http://www.topografix.com/GPX/1/0\"")
			.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\"> ")
			.toString();

	public final String GPX_Footer = "</gpx>";

	public String writeGenericCSVRecord(String... values) {
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (String value : values) {
			if (!isFirst) {
				builder.append(',');
			}
			isFirst = false;

			builder.append('"');
			if (value != null) {
				builder.append(value.replaceAll("\"", "\"\""));
			}
			builder.append('"');
		}
		// XXX for now, assuming Windows (CRLF) as terminator
		builder.append("\r\n");
		return builder.toString();
	}
	
	public String writeCSVTrackRecord(Location currLoc, float altitude, float airSpeed) {
		return writeGenericCSVRecord(
				ISO_8601_DATE_TIME_FORMAT.format(currLoc.getTime()),
				String.valueOf(currLoc.getLatitude()),
				String.valueOf(currLoc.getLongitude()),
				ELEVATION_FORMAT.format(altitude),
				ELEVATION_FORMAT.format(airSpeed));
	}

	public String writeGPSTrackRecord(Location currLoc, float altitude,
			float airSpeed) {
		StringBuilder builder = new StringBuilder();
		builder.append("<trkpt " + formatGeoLocation(currLoc) + ">");
		builder.append("<ele>" + ELEVATION_FORMAT.format(altitude) + "</ele>");
		builder.append("<speed>" + ELEVATION_FORMAT.format(airSpeed) + "</speed>");
		builder.append("<time>"
				+ ISO_8601_DATE_TIME_FORMAT.format(currLoc.getTime())
				+ "</time>");
		builder.append("</trkpt>");
		return builder.toString();
	}

	private String formatGeoLocation(Location location) {
		return "lat=\"" + String.valueOf(location.getLatitude()) + "\" lon=\""
				+ String.valueOf(location.getLongitude()) + "\"";
	}

}
