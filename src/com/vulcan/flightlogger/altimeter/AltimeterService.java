package com.vulcan.flightlogger.altimeter;

import tw.com.prolific.driver.pl2303.PL2303Driver;
import android.app.IntentService;
import android.content.Intent;

public class AltimeterService extends IntentService {
	
	private PL2303Driver mSerialDriver;

	public AltimeterService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		

	}

}
