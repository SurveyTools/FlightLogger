package com.vulcan.flightlogger.geo;

import java.io.File;
import java.util.List;
import java.util.Iterator;

import com.vulcan.flightlogger.CourseInfoIntent;
import com.vulcan.flightlogger.R;
import com.vulcan.flightlogger.geo.data.Route;
import com.vulcan.flightlogger.geo.data.Transect;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TransectListActivity extends ListActivity {
	
	public static final String GPX_FILE_NAME_STRING_KEY = "gpxfile";
	public static final String ROUTE_NAME_STRING_KEY = "routeName";
	public static final String TRANSECT_NAME_STRING_KEY = "transectName";
	public static final String TRANSECT_DETAILS_STRING_KEY = "transectDetails";

	private String mOriginalTransectName;
	private String mOriginalTransectDetails;
	
	public void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    
	    Intent intent = getIntent();
	    
	    String gpxFile = intent.getExtras().getString(GPX_FILE_NAME_STRING_KEY);
	    String routeName = intent.getExtras().getString(ROUTE_NAME_STRING_KEY);
	    mOriginalTransectName = intent.getExtras().getString(TRANSECT_NAME_STRING_KEY);
	    mOriginalTransectDetails = intent.getExtras().getString(TRANSECT_DETAILS_STRING_KEY);
	    
	    File gpxFileObj = new File(gpxFile);
	    List<Route> routes = GPSUtils.parseRoute(gpxFileObj);
	    
	    // find the route
	    Route route = GPSUtils.findRouteByName(routeName, routes);
		List<Transect> transects = GPSUtils.parseTransects(route);
	    
	    ArrayAdapter<Transect> adapter = new ArrayAdapter<Transect>(this,
	        R.layout.transect_list_row, R.id.transect_name, transects);
	    setListAdapter(adapter);
	  }
	
	private void finishWithCancel()
	{
		Intent intent = getIntent();
		intent.putExtra(TRANSECT_NAME_STRING_KEY, mOriginalTransectName);
		intent.putExtra(mOriginalTransectDetails, mOriginalTransectDetails);
		this.setResult(RESULT_CANCELED, intent);
		finish();
	}

	private void finishWithDone(Transect transectItem)
	{
		Intent intent = getIntent();
		intent.putExtra(TRANSECT_NAME_STRING_KEY, transectItem.mName);
		intent.putExtra(TRANSECT_DETAILS_STRING_KEY, transectItem.getDetailsName());
		this.setResult(RESULT_OK, intent);
		finish();
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		Transect transectItem = (Transect) getListAdapter().getItem(position);
		// optional Toast.makeText(this, transectItem.mName + " selected", Toast.LENGTH_LONG).show();
		finishWithDone(transectItem);
	}
}
