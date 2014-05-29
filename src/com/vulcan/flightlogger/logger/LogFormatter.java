package com.vulcan.flightlogger.logger;

public class LogFormatter {

	public String writeCSVRecord(String... values) {
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
}
