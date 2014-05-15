package com.vulcan.flightlogger;

public class FlightDatum {

	public static final short FLIGHT_STATUS_UNKNOWN = 0; // UNKNOWN
	public static final short FLIGHT_STATUS_GREEN = 1;
	public static final short FLIGHT_STATUS_RED = 2;
	public static final short FLIGHT_STATUS_YELLOW = 3;
	public static final short FLIGHT_STATUS_IGNORE = 4;

	// todo - probably want this to be different for different data types
	static final long DATA_IS_EXPIRED_THRESHOLD_MILLIS = 8000;
	static final long DATA_IS_OLD_THRESHOLD_MILLIS = 4000;

	public boolean mIgnore;
	public boolean mDemoMode;
	public String mValueToDisplay; // ready for display
	public boolean mDataIsValid; // so we don't have to key off the raw value
	public long mDataTimestamp;

	public FlightDatum(boolean ignore, boolean demoMode) {
		mIgnore = ignore;
		mDemoMode = demoMode;
	}

	protected long curDataTimestamp() {
		return System.currentTimeMillis();
	}

	protected boolean dataIsExpired() {

		if (mDataTimestamp == 0)
			return false;

		long elapsedMillis = curDataTimestamp() - mDataTimestamp;

		if (elapsedMillis > DATA_IS_EXPIRED_THRESHOLD_MILLIS)
			return true;

		// aok
		return false;
	}

	protected boolean dataIsOld() {

		if (mDataTimestamp == 0)
			return true;

		long elapsedMillis = curDataTimestamp() - mDataTimestamp;

		if (elapsedMillis > DATA_IS_OLD_THRESHOLD_MILLIS)
			return true;

		// aok
		return false;
	}

	public short getStatusColor() {

		if (mIgnore)
			return FLIGHT_STATUS_IGNORE;
		else if (mDemoMode)
			return FLIGHT_STATUS_GREEN; // DEMO_MODE
		else if (!mDataIsValid)
			return FLIGHT_STATUS_RED;
		else if (dataIsExpired())
			return FLIGHT_STATUS_RED;
		else if (dataIsOld())
			return FLIGHT_STATUS_YELLOW;
		else
			return FLIGHT_STATUS_GREEN;
	}

	public void reset() {
		mValueToDisplay = null;
		mDataIsValid = false;
		mDataTimestamp = 0;
	}

	// TODO - move to a util
	protected float metersToFeet(float meters) {
		return meters * 3.2808399f;
	}
}
