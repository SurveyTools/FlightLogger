package com.vulcan.flightlogger.altimeter;

import java.util.Arrays;
import android.util.Log;
import com.vulcan.flightlogger.util.KMPMatch;

public class LightwareDataValidator implements AltimeterValidator {
	
	final static int dataSampleBufferSize = 100;
	final static int dataSampleSize = 10;
	final byte[] terminatingPattern = { (byte)0x20, (byte)0x6d, (byte)0x0d, (byte)0x0a }; 

	@Override
	public float parseDataPayload(byte[] data) {
		float meters = 0;	
		// Note: when the laser starts up, it stream a buch of 0xff characters, typically
		// 30 bytes or so at a time. The serial library allocates 4K, which seems like a 
		// crazy large size for collecting serial data, but we extract a value from the 
		// beginning of the buffer. Later, we should probably extract a range of values, 
		// and average them.
		
		int end = 100 < dataSampleBufferSize ? 100 : data.length;
		byte[] sampleData = Arrays.copyOfRange(data, 0, end);
		// find the values between the patterns. The value may have up to 2 spaces (0x20) padding
		int start = KMPMatch.indexOf(sampleData, terminatingPattern);
		// if we have a match, extract a sample from the next value in the sequence
		if (start > 0 && (start + dataSampleSize + terminatingPattern.length) < sampleData.length )
		{
			int fromIndex = start + terminatingPattern.length;
			int toIndex = fromIndex + dataSampleSize + terminatingPattern.length;
			try {
				byte[] sampleSlice = Arrays.copyOfRange(sampleData, fromIndex, toIndex);
				int matchEnd = KMPMatch.indexOf(sampleSlice, terminatingPattern);
				if (matchEnd > 0)
				{
					byte[] sample = Arrays.copyOfRange(sampleSlice, 0, matchEnd);
					Log.d(this.getClass().getName(), "Meters: " + new String(sample));
					meters = Float.parseFloat(new String(sample));
					if (meters < 0)
						meters = AltimeterService.ALTIMETER_OUT_OF_RANGE_THRESHOLD;
				}	
			} 
			catch (ArrayIndexOutOfBoundsException aioobe)
			{
				Log.d("LightwareDataValidator.parseDataPayload", "start: " + start);
			}
			// TODO - throw an IllegalValidationError, instead of an int
			catch (IllegalArgumentException iae)
			{
				Log.d("LightwareDataValidator.parseDataPayload", "illegal argument fromIndex: " + fromIndex + " toIndex: " + toIndex);
			}
		}
		else {
			meters = AltimeterService.ALTIMETER_OUT_OF_RANGE_THRESHOLD;
		}
		return meters;
	}

}
