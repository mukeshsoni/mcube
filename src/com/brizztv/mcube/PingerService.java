package com.brizztv.mcube;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PingerService extends Service {

	public void onCreate() {
	}

	@Override
	public void onStart(Intent intent, int startId) {
		new Thread(new Runnable() {
		    public void run() {
		    	if(Utils.haveNetworkConnection(getApplicationContext())) {
		    		if(!Utils.inEmulator()) {
		    			Utils.pingUnauditedExpenses(getContentResolver());
		    		}
		    	}
		    }
		  }).start();
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
