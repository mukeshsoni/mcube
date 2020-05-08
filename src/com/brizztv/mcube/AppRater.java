package com.brizztv.mcube;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {
    private final static String APP_TITLE = "MCUBE";
    private final static String APP_PNAME = "com.brizztv.mcube";
    
    private final static int DAYS_UNTIL_PROMPT = 5;
    private final static int LAUNCHES_UNTIL_PROMPT = 10;
    
    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch or the last time Later was clicked
        Long lastLaterClicked = prefs.getLong("last_later_clicked", 0);
        if (lastLaterClicked == 0) {
            lastLaterClicked = System.currentTimeMillis();
            editor.putLong("last_later_clicked", lastLaterClicked);
        }
        
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= lastLaterClicked + 
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }
        
        editor.commit();
    }   
    
    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
    	String message = "If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!";
        Dialog dialog = new Dialog(mContext);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message)
        		.setTitle("Rate " + APP_TITLE)
        		.setIcon(mContext.getApplicationInfo().icon)
        		.setCancelable(false)
        		.setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(editor != null) {
		            		editor.putBoolean("dontshowagain", true);
		                    editor.commit();
		            	}
		                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
		                dialog.dismiss();
					}
				})
				.setNeutralButton("Later", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putLong("launch_count", 0);
						editor.putLong("last_later_clicked", System.currentTimeMillis());
						editor.commit();
						dialog.dismiss();
					}
				})
				.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
		                if (editor != null) {
		                    editor.putBoolean("dontshowagain", true);
		                    editor.commit();
		                }
		                dialog.dismiss();
					}
				});
        
        dialog = builder.create();
        dialog.show();
    }
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirate