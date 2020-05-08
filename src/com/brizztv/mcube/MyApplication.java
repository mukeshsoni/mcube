package com.brizztv.mcube;

import android.app.Application;

public class MyApplication extends Application {
	
	public static int mNotificationId=0;
	
	  public static boolean isActivityVisible() {
	    return activityVisible;
	  }  

	  public static void activityResumed() {
	    activityVisible = true;
	  }

	  public static void activityPaused() {
	    activityVisible = false;
	  }

	  private static boolean activityVisible;
}
