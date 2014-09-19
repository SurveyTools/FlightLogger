package com.vulcan.flightlogger.altimeter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import android.util.Log;
import com.vulcan.flightlogger.util.KMPMatch;

public class LightwareDataValidator implements AltimeterValidator {
	
	final int dataSampleSize = 15;
	final byte[] terminatingPattern = { (byte)0x20, (byte)0x6d, (byte)0x0d, (byte)0x0a }; 

	@Override
	public float parseDataPayload(byte[] data) {
		float meters = 0;	
		// Note: when the laser starts up, it stream a buch of 0xff characters, typically
		// 30 bytes or so at a time. The serial library allocates 4K, which seems like a 
		// crazy large size for collecting serial data, but we extract a value from the 
		// beginning of the buffer. Later, we should probably extract a range of values, 
		// and average them.
		
		if (data.length > 70)
		{
			byte[] sampleData = Arrays.copyOfRange(data, 0, 100);
			// find the values between the patterns. The value may have up to 2 spaces (0x20) padding
			int start = KMPMatch.indexOf(sampleData, terminatingPattern);
			// if we have a match, extract a sample from the next value in the sequence
			if (start > 0 && (start + dataSampleSize + terminatingPattern.length) < sampleData.length )
			{
				try {
					byte[] sampleSlice = Arrays.copyOfRange(sampleData, (start + terminatingPattern.length), dataSampleSize + terminatingPattern.length);
					int matchEnd = KMPMatch.indexOf(sampleSlice, terminatingPattern);
					if (matchEnd > 0)
					{
						byte[] sample = Arrays.copyOfRange(sampleSlice, 0, matchEnd);
						Log.d(this.getClass().getName(), "Meters: " + new String(sample));
						meters = Float.parseFloat(new String(sample));
					}	
				} 
				catch (IllegalArgumentException iae)
				{
					Log.d("LightwareDataValidator.parseDataPayload", "start: " + start);
				}
			}
		}
		else Log.d(this.getClass().getName(), "No data");
		return meters;
	}

}
