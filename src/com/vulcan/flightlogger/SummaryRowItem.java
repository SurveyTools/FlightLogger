package com.vulcan.flightlogger;

// CUSTOM_LIST_IN_DIALOG
// row item wrapper to hold the details

public class SummaryRowItem {
	public String mLabelText;
	public String mDetailsText;

	public SummaryRowItem(String label, String details) {
		super();
		mLabelText = label;
		mDetailsText = details;
	}

	// copy constructor
	public SummaryRowItem(SummaryRowItem srcData) {
		super();
		if (srcData != null) {
			mLabelText = srcData.mLabelText;
			mDetailsText = srcData.mDetailsText;
		}
	}

	// convenience method for UI display when using ArrayAdapters
	public String toString() {
		return mLabelText;
	}
}
