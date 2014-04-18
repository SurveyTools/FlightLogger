package com.vulcan.flightlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, FlightLogger.class);  
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);  
    }

}