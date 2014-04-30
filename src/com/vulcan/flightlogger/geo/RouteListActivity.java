package com.vulcan.flightlogger.geo;

import java.io.File;
import java.util.List;

import com.vulcan.flightlogger.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RouteListActivity extends ListActivity {
	
	public void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    
	    Intent intent = getIntent();
	    String gpxFile = intent.getExtras().getString("gpxfile");
	    File gpxFileObj = new File(gpxFile);
	    List<Route> routes = GPXParser.parseRoutePoints(gpxFileObj);
	    ArrayAdapter<Route> adapter = new ArrayAdapter<Route>(this,
	        R.layout.route_list_row, R.id.route_name, routes);
	    setListAdapter(adapter);
	  }
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}

}
