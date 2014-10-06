package com.vulcan.flightlogger.altimeter;

import android.content.res.Resources.NotFoundException;

import com.vulcan.flightlogger.altimeter.AltimeterService.RangefinderDriverType;

public class AltimeterUtils {
	
	public static RangefinderDriverType getRangefinderDriverForKey(String key) 
	throws NotFoundException
	{
		if (key != null) {
			if (key.equalsIgnoreCase("rangefinder_aglaser"))
				return RangefinderDriverType.AGLASER;
			else if (key.equalsIgnoreCase("rangefinder_sf03xlr"))
				return RangefinderDriverType.LIGHTWARE;
		}
		
		throw new NotFoundException("driver key not found");
		
	}

}
