package com.brizztv.mcube;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		if(Log.DEBUG) Log.v("AlarmReceiver: onReceive");
		Intent pinger = new Intent(context, PingerService.class);
		context.startService(pinger);
	}
}
