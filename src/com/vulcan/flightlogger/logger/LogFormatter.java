package com.vulcan.flightlogger.logger;

public class LogFormatter {

	public String writeCommaSeparatedLine(String... values) {
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
		return builder.toString();
	}
}
