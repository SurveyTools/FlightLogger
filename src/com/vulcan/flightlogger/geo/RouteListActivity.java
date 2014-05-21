package com.vulcan.flightlogger.geo;

import java.io.File;
import java.util.List;

import com.vulcan.flightlogger.CourseInfoIntent;
import com.vulcan.flightlogger.R;
import com.vulcan.flightlogger.geo.data.Route;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RouteListActivity extends ListActivity {
	
	public static final String GPX_FILE_NAME_STRING_KEY = "gpxfile";
	public static final String ROUTE_NAME_STRING_KEY = "routeName";

	private String mOriginalRouteName;
	
	public void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    
	    Intent intent = getIntent();
	    String gpxFile = intent.getExtras().getString(GPX_FILE_NAME_STRING_KEY);
	    mOriginalRouteName = intent.getExtras().getString(ROUTE_NAME_STRING_KEY);
	    File gpxFileObj = new File(gpxFile);
	    List<Route> routes = GPSUtils.parseRoute(gpxFileObj);
	    ArrayAdapter<Route> adapter = new ArrayAdapter<Route>(this,
	        R.layout.route_list_row, R.id.route_name, routes);
	    setListAdapter(adapter);
	  }
	
	private void finishWithCancel()
	{
		Intent intent = getIntent();
		intent.putExtra(ROUTE_NAME_STRING_KEY, mOriginalRouteName);
		this.setResult(RESULT_CANCELED, intent);
		finish();
	}

	private void finishWithDone(Route routeItem)
	{
		Intent intent = getIntent();
		intent.putExtra(ROUTE_NAME_STRING_KEY, routeItem.mName);
		this.setResult(RESULT_OK, intent);
		finish();
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		Route routeItem = (Route) getListAdapter().getItem(position);
		Toast.makeText(this, routeItem.mName + " selected", Toast.LENGTH_LONG).show();
		finishWithDone(routeItem);
	}

}
