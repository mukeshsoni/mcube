/**
 * 
 */
package com.brizztv.mcube;

import com.brizztv.mcube.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * @author mukesh
 *
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
	private final static String APP_PNAME = "com.brizztv.mcube";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor prefEditor = settings.edit();  
//        prefEditor.putBoolean("pref_pop_up", true);
//        prefEditor.putBoolean("pref_read_private_sms", true);
//        prefEditor.commit();
        settings.registerOnSharedPreferenceChangeListener(this);
        
        Preference appRatingPref = (Preference) findPreference("pref_app_rater");
        appRatingPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
//                    Toast.makeText(getBaseContext(),
//                                    "The custom preference has been clicked",
//                                    Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                    return true;
            }
        });
    }
	
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
//		if(Log.DEBUG) Log.v("Preference change caught");
		if(key.equals("pref_read_private_sms")) {
//			if(Log.DEBUG) Log.v("User changed private sms read pref");
			new AlertDialog.Builder(this)
			.setTitle("Can't change this")
			.setMessage("Can't change this preference")
			.setIcon(R.drawable.ic_alert_mcube)
			.setPositiveButton(R.string.alertPopupDiscardButton,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							
						}
					}).show();
			
			

		}
		
	}
}
