package com.brizztv.mcube;

public class Log {
	 public final static String LOGTAG = "MCUBE";

	  public static final boolean DEBUG = true;

	  public static void v(String msg) {
	    android.util.Log.v(LOGTAG, msg);
	  }

	  public static void e(String msg) {
	    android.util.Log.e(LOGTAG, msg);
	  }
	  
	  public static void d(String msg) {
		  android.util.Log.d(LOGTAG, msg);
	  }
}
