package com.vulcan.flightlogger;

import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class USBAwareActivity extends Activity {
	
private final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
protected final String LOGGER_TAG = this.getClass().getSimpleName();

// listens for attachment events
private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (USB_DEVICE_ATTACHED.equals(action)) {
            synchronized (this) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device != null){
                	initUsbDevice(device);
                } 
                else {
                	Log.d(LOGGER_TAG, "permission denied for device " + device);
                }
            }
        }
    }
};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		IntentFilter filter = new IntentFilter(USB_DEVICE_ATTACHED);
		registerReceiver(mUsbReceiver, filter); 
	}

	/**
	 * Inteneded to be a template method - the expectation is that in the derived class we'll do something of interest 
	 * with the device passed in to this method
	 * @param device
	 */
	protected void initUsbDevice(UsbDevice device) {
		Log.d(LOGGER_TAG, "init USB device: " + device);
		String deviceInfo = String.format(Locale.US, "Connecting device: %s vendor id: %d prod id: %d", device.getDeviceName(), device.getVendorId(), device.getProductId());
		showToast(deviceInfo);
	}
	
	public void showToast(String message) {
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
}
