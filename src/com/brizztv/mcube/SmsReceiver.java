package com.brizztv.mcube;

import com.brizztv.mcube.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * this class will listen to all broadcasts about incoming smses. When a new Sms comes in, 
 * the onRecieve method will start SmsReceiverService, which will do the heavy duty work
 */
public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		intent.setClass(context, SmsReceiverService.class);
		intent.putExtra("result", getResultCode());
		
//		if(Log.DEBUG) Log.v("SmsReceiver: sms received. About to show notification service");
		/*
	     * This service will process the activity and show the popup (+ play notifications)
	     * after it's work is done the service will be stopped.
	     */
	    SmsReceiverService.beginStartingService(context, intent);
//		Toast toast = Toast.makeText(context, "gotcha", Toast.LENGTH_LONG);
//		toast.show();		
	}

}
