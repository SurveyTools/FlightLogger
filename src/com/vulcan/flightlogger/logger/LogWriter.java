package com.vulcan.flightlogger.logger;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.location.Location;

// OK, OK, in OO this would typically by a 'Visitor' object. We may visit that
// approach if we need multiple formats, but for now, let's keep it simple-ish...

public class LogWriter {
	
	// what do we log for log entries 
	public enum LogFields {
		TIMESTAMP, LAT, LON, ALTITUDE, SPEED, GPS_ALT
	}

	private static final NumberFormat ELEVATION_FORMAT = NumberFormat
			.getInstance(Locale.US);
	private static final SimpleDateFormat ISO_8601_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

	static {
		ELEVATION_FORMAT.setMaximumFractionDigits(1);
		ELEVATION_FORMAT.setGroupingUsed(false);
		ISO_8601_DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public String writeGenericCSVRecord(String... values) 
	{
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (String value : values) {
			if (!isFirst) {
				builder.append(',');
			}
			isFirst = false;

			builder.append('"');
			if (value != null) 
			{
				builder.append(value.replaceAll("\"", "\"\""));
			}
			
			builder.append('"');
		}
		// XXX for now, assuming Windows (CRLF) as terminator
		builder.append("\r\n");
		return builder.toString();
	}
	
	public String writeCSVTrackTitle() 
	{
		final String[] titles = {"Title", "Timestamp", "Lat", "Lon", "Alt", "Airspeed", "GPS Alt"};
		return writeGenericCSVRecord(titles);
	}
	
	public String writeCSVTrackRecord(String transectName, Location currLoc, float laserAlt, float airSpeed, double gpsAlt) 
	{
		return writeGenericCSVRecord(
				transectName,
				ISO_8601_DATE_TIME_FORMAT.format(currLoc.getTime()),
				String.valueOf(currLoc.getLatitude()),
				String.valueOf(currLoc.getLongitude()),
				ELEVATION_FORMAT.format(laserAlt),
				ELEVATION_FORMAT.format(airSpeed),
				ELEVATION_FORMAT.format(gpsAlt));
	}

}
