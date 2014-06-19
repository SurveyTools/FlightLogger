package com.vulcan.flightlogger.util;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextPreferenceShowSummary extends EditTextPreference {

	private final static String TAG = EditTextPreferenceShowSummary.class.getName();

	public EditTextPreferenceShowSummary(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EditTextPreferenceShowSummary(Context context) {
		super(context);
		init();
	}

	private void init() {

		setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				arg0.setSummary(getText());
				return true;
			}
		});
	}

	@Override
	public CharSequence getSummary() {
		return getText();
	}
}
