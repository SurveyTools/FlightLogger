package com.vulcan.flightlogger;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import java.util.List;
import java.util.ArrayList;

import com.vulcan.flightlogger.SummaryRowItem;
import com.vulcan.flightlogger.SummaryArrayAdapter;
import com.vulcan.flightlogger.logger.TransectSummary;

// FRAGMENTS_BAD_IN_ACTIVITY_RESULTS
// Note: this was not done as a Fragment due to the state
// problems involved with putting up a fragment-based dialog
// from onActivityResult

// CUSTOM_LIST_IN_DIALOG
// here's where it comes together

public class TransectSummaryAlert {
	
	public static void showSummary(Context context, TransectSummary summary) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);

		builder.setCancelable(true);
		builder.setTitle(R.string.transectsummary_title);

		// raw list
		List<SummaryRowItem> summaryList = new ArrayList<SummaryRowItem>();
		
		// TODO_TRANSECT_SUMMARY_STUFF - real data
		summaryList.add(new SummaryRowItem(context.getString(R.string.transectsummary_average_speed_label), "fast"));
		summaryList.add(new SummaryRowItem(context.getString(R.string.transectsummary_average_altitude_label), "high"));
		summaryList.add(new SummaryRowItem(context.getString(R.string.transectsummary_total_duration_label), "forever"));

		final SummaryArrayAdapter adapter = new SummaryArrayAdapter(context, R.layout.transect_summary_row, summaryList.toArray(new SummaryRowItem[summaryList.size()]));

		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ignore
			}
		});

		builder.setNegativeButton(R.string.modal_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.show();
	
		}
	
}
