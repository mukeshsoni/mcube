package com.brizztv.mcube;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brizztv.mcube.AddExpenseActivity;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;
//import com.actionbarsherlock.app.ActionBar;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuItem;
//import com.actionbarsherlock.view.MenuInflater;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/*
 * This is where things start. The main activity which takes the user to the home screen. It has three tabs, each of which has a fragment.
 * It also does a lot of stuff which is to be done one time during install.
 * And then we handle all the menu item clicks in this activity. 
 */
public class ExpenseOverviewActivity extends SherlockFragmentActivity {
	private static final int ACTIVITY_ADD_EXPENSE = 0, ACTIVITY_CATEGORY_LIST = 1;
	protected Object mActionMode;
	private SharedPreferences mPrefs;
	final String welcomeScreenShownPref = "welcomeScreenShown";

	static final String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_AMOUNT,
			DataProvider.KEY_EXPENSE_BANK_NAME, DataProvider.KEY_EXPENSE_DATE, DataProvider.KEY_EXPENSE_LOCATION, DataProvider.KEY_EXPENSE_MERCHANT,
			DataProvider.KEY_EXPENSE_NOTES, DataProvider.KEY_EXPENSE_CATEGORY_NAME, DataProvider.KEY_EXPENSE_DAY, DataProvider.KEY_EXPENSE_MONTH,
			DataProvider.KEY_EXPENSE_YEAR };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// lets get the action bar reference. Only possible with a fragment activity.
		ActionBar actionBar = getSupportActionBar();

		// lets do some customization on our action bar. well just a little.
		actionBar.setBackgroundDrawable(new ColorDrawable(0x0000FF));
		actionBar.setDisplayHomeAsUpEnabled(false); 
		actionBar.setDisplayShowTitleEnabled(false); 
		// Don't show app name on action bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab expenseSummaryTab = actionBar.newTab().setText("Summary");
		ActionBar.Tab expenseTab = actionBar.newTab().setText("Expenses");
		ActionBar.Tab uncategorizedExpenseTab = actionBar.newTab().setText("To File");
		
		expenseSummaryTab.setTabListener(new MyTabsListener<ExpenseSummaryFragment2>(this, "expenseSummaryTab", ExpenseSummaryFragment2.class));
		expenseTab.setTabListener(new MyTabsListener<ExpenseListFragment>(this, "expenseTab", ExpenseListFragment.class));
		uncategorizedExpenseTab.setTabListener(new MyTabsListener<UncategorizedExpenseListFragment>(this, "reminderTab", UncategorizedExpenseListFragment.class));
		
		actionBar.addTab(expenseSummaryTab);
		actionBar.addTab(expenseTab);
		actionBar.addTab(uncategorizedExpenseTab);

		// Get details like device_id and phone model and insert into db
		insertUserProfile();
		// Alart for data pinging
		setAlarm();
		showWelcomeDialog();
		AppRater.app_launched(this);
	}

	@Override
	public void onPause() {
//		if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onPause");
		super.onPause();
	}
	
	@Override
	public void onStop() {
//		if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onStop");
		super.onStop();
	}
	
	/*
	 * Schedule our pinger service
	 */
	private void setAlarm() {
		//start ping service
		Intent pinger = new Intent(this, AlarmReceiver.class);
		// set alarm only if not already set
		if(PendingIntent.getBroadcast(this, 0, pinger, PendingIntent.FLAG_NO_CREATE) == null) {
//			if(Log.DEBUG) Log.v("About to start ping service");
			
			PendingIntent recurringPing = PendingIntent.getBroadcast(this, 0, pinger, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			long interval = AlarmManager.INTERVAL_HOUR*6; // ping every six hours
			int randomMillisToAdd = 1000*60*5 + (int)(Math.random() * ((1000*60*60 - 1000*60) + 1));
			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+randomMillisToAdd, interval, recurringPing);
		} else {
//			if(Log.DEBUG) Log.v("Ping service already active");
		}
	}
	
	/*
	 * Shows a welcome dialog on first install. Also asks if the user wants to
	 * scan their past smses and auto file expenses.
	 */
	private void showWelcomeDialog() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String whatsNewTitle = getResources().getString(R.string.whatsNewTitle);
		String whatsNewText = getResources().getString(R.string.whatsNewText);

		// second argument is the default to use if the preference can't be
		// found
		Boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);

		boolean inEmulator = "generic".equals(Build.BRAND.toLowerCase());
		if (inEmulator) {
//			welcomeScreenShown = false;
		}

//		if (Log.DEBUG)
//			Log.v("Welcome Screen Pref: " + welcomeScreenShown);

		Builder adb = new AlertDialog.Builder(this).setIcon(R.drawable.ic_alert_mcube).setTitle(whatsNewTitle).setMessage(whatsNewText)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// do some magic here

						final ProgressDialog pDialog = ProgressDialog.show(ExpenseOverviewActivity.this, "", "Churning data! Please wait...", true);

						new Thread(new Runnable() {
							public void run() {
								Utils.fileExpenseFromHistory(getApplicationContext());
								// Toast.makeText(
								// ExpenseOverviewActivity.this,
								// "Automatic expense filing done! You can now categorize them.",
								// Toast.LENGTH_LONG).show();
								if (pDialog != null) {
									pDialog.dismiss();
								}
								Intent intent = new Intent(ExpenseOverviewActivity.this, TutorialActivity.class);
//								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						}).start();
						dialog.cancel();
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// do nothing. user not comfortable with us reading their smses
						dialog.cancel();
					}
				});
		AlertDialog ad = adb.create();
		
		// welcomeScreenShown = false;
		if (!welcomeScreenShown && !ad.isShowing()) {
			// insert dummy expense record if none exists
			Utils.insertTestExpense(getContentResolver());
			// now ping unaudited data
			new Thread(new Runnable() {
			    public void run() {
			    	if(Utils.haveNetworkConnection(getApplicationContext())) {
			    		if(!Utils.inEmulator()) {
			    			Utils.pingUnauditedExpenses(getContentResolver());
			    		}
			    	}
			    }
			  }).start();
			// now show the scanning message
			ad.show();
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean(welcomeScreenShownPref, true);
			editor.commit(); // Very important to save the preference
		} else {
//			Utils.insertTestExpense(getContentResolver());
//			new Thread(new Runnable() {
//			    public void run() {
//			    	if(Utils.haveNetworkConnection(getApplicationContext())) {
//			    		if(!Utils.inEmulator()) {
//			    			Utils.pingUnauditedExpenses(getContentResolver());
//			    		}
//			    	}
//			    }
//			  }).start();
		}
	}
	
	private void insertUserProfile() {
		// get users primary email
//		final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
//		Pattern emailPattern = Pattern.compile(EMAIL_PATTERN); // API level
//																// 8+
//		Account[] accounts = AccountManager.get(this).getAccounts();
//		String possibleEmail=null;
//		for (Account account : accounts) {
//			if (emailPattern.matcher(account.name).matches()) {
//				possibleEmail = account.name;
//				break; //we just need the first email
//				if(Log.DEBUG) Log.v("Possible Email: " + possibleEmail);
//			}
//		}

		// get phone build and os version
		// Device model
		String phoneModel = android.os.Build.MODEL;

		// Android version
		String androidVersion = android.os.Build.VERSION.RELEASE;

		// get user's phone number
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = telephonyManager.getLine1Number();

		// encrypt the device id before sending to the server
		String deviceId = Utils.md5(telephonyManager.getDeviceId());

		// insert user profile info in database
		ContentValues values = new ContentValues();
//		values.put(DataProvider.KEY_USER_PROFILE_EMAIL, possibleEmail);
		values.put(DataProvider.KEY_USER_PROFILE_PHONE_MODEL, phoneModel);
		values.put(DataProvider.KEY_USER_PROFILE_OS_VERSION, androidVersion);
		values.put(DataProvider.KEY_USER_PROFILE_PHONE_NUMBER, phoneNumber);
		values.put(DataProvider.KEY_USER_PROFILE_DEVICE_ID, deviceId);
		values.put(DataProvider.KEY_USER_PROFILE_ID, 1); //sending the id, so that primary key constraints will not allow any multiple insertions
		
		// TODO - change this way of ensuring only one record in user profile table. make one of the columns unique and then put the insert in try catch block
		// or else, put this code in the section where we check if this is the first install for the user
		getContentResolver().delete(DataProvider.USER_PROFILE_URI, null, null);
		getContentResolver().insert(DataProvider.USER_PROFILE_URI, values);
	}
	
	protected class MyTabsListener<T extends Fragment> implements ActionBar.TabListener {

		// private SherlockListFragment mFragment;
		private Fragment mFragment;
		private final SherlockFragmentActivity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public MyTabsListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
//			if(Log.DEBUG) Log.v("ExpenseOverviewActivity: MyTabsListener");
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			// mArgs = args;

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			mFragment = (Fragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
			if (mFragment != null) { // && !mFragment.isDetached()) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				ft.detach(mFragment);
				// ft.remove(mFragment);
				ft.commit();
			}
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
//			 if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onTabSelected");

			// Check if the fragment is already initialized
			if (mFragment == null) {
				// If not, instantiate and add it to the activity
				mFragment = (Fragment) Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
				// ft.add(android.R.id.content, mFragment, mTag);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
//			if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onTabSelected");
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				// ft.remove(mFragment);
				ft.detach(mFragment);
			}
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
//			if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onTabSelected");
		}
	}

	@Override
	protected void onDestroy() {
//		if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onDestroy");
		super.onDestroy();
	}

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_expense_overview, menu);
		return true;
	}

	@Override
	public void onResume() {
//		if(Log.DEBUG) Log.v("ExpenseOverviewActivity: onResume");
		super.onResume();
	}
	
	// @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast toast;
		Intent intent;
		
		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, ExpenseOverviewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_charts:
			intent = new Intent(ExpenseOverviewActivity.this, ChartsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_add_expense:
			intent = new Intent(ExpenseOverviewActivity.this, AddExpenseActivity.class);
			startActivityForResult(intent, ACTIVITY_ADD_EXPENSE);
			return true;
		case R.id.show_categories:
			intent = new Intent(ExpenseOverviewActivity.this, CategoryActivity.class);
			startActivityForResult(intent, ACTIVITY_CATEGORY_LIST);
			return true;
		case R.id.export_to_csv:
			
			final CharSequence[] exportChoices = {"Write to SD Card", "Send via Email/Dropbox/others"};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Choose an export method");
			builder.setItems(exportChoices, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	Intent intent;
			    	File exportedFile;
			        switch(item) {
			        	case 0:
			    			exportedFile = Utils.exportToCSV(ExpenseOverviewActivity.this);
			    			if(exportedFile == null) {
			    				Toast.makeText(ExpenseOverviewActivity.this, "Could not save file to sd card. May not have write permission.", Toast.LENGTH_SHORT).show();
			    			} else {
			    				Toast.makeText(ExpenseOverviewActivity.this, "CSV file saved to sd card", Toast.LENGTH_SHORT).show();
			    			}
			        		break;
			        	case 1:
			        		exportedFile = Utils.exportToCSV(ExpenseOverviewActivity.this);
			        		
			        		if(exportedFile == null) {
			    				Toast.makeText(ExpenseOverviewActivity.this, "Could not save file to sd card. May not have write permission.", Toast.LENGTH_SHORT).show();
			    			} else {
				        		// this part is required if the user wants to email the file as attachment. we prepare the relevant intent here
				        		Uri u1  =   null;
				        		if(exportedFile != null)
				        			u1  =   Uri.fromFile(exportedFile);			

				        		intent = new Intent(Intent.ACTION_SEND);
				        		intent.putExtra(Intent.EXTRA_SUBJECT, "MCUBE - Expenses till date");
				        		intent.putExtra(Intent.EXTRA_STREAM, u1);
				        		intent.setType("text/html");
				    			startActivity(intent);
			    			}
			        		break;
			        	default:
			        		break;
			        }
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
			


			return true;
		case R.id.menu_preferences:
			intent = new Intent(ExpenseOverviewActivity.this, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_about:
			intent = new Intent(ExpenseOverviewActivity.this, AboutActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_feedback:
			intent = new Intent(ExpenseOverviewActivity.this, FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_backup_database:
			new ExportDatabaseFileTask().execute();
			break;
		case R.id.menu_restore_database:
			new RestoreDatabaseTask().execute();
			break;
		default:
			toast = Toast.makeText(this, "You clicked on a menu item", Toast.LENGTH_SHORT);
			toast.show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class RestoreDatabaseTask extends AsyncTask<String, Void, Boolean> {
		Context ctx = ExpenseOverviewActivity.this;
        private final ProgressDialog dialog = new ProgressDialog(ctx);

        // can use UI thread here
        protected void onPreExecute() {
           this.dialog.setMessage("Restoring database from SD Card. Please Wait...");
           this.dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {

           File backupDbFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.brizztv.mcube/databases/sms_expense");
//           File exportDir = new File(Environment.getExternalStorageDirectory(), "");
           // The directory where we will save our database file
           File importDir = new File(Environment.getDataDirectory() + "/data/com.brizztv.mcube/databases");

//           exportDir = new File(getExternalFilesDir(null).getAbsolutePath());
           
           if (!importDir.exists()) {
              importDir.mkdirs();
           }
           
           File file = new File(importDir, backupDbFile.getName());
           file.getParentFile().mkdirs();
           
           try {
              file.createNewFile();
              this.copyFile(backupDbFile, file);
              return true;
           } catch (IOException e) {
//              if(Log.DEBUG) Log.v(e.getMessage());
              return false;
           }
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
           if (this.dialog.isShowing()) {
              this.dialog.dismiss();
           }
           if (success) {
              Toast.makeText(ctx, "Restored database from sd card back up.", Toast.LENGTH_SHORT).show();
              // The next two method calls are to refresh the activity. pretty sleek ;)
              finish();
              startActivity(getIntent());
           } else {
              Toast.makeText(ctx, "Could not restore database from sd card :(", Toast.LENGTH_SHORT).show();
           }
        }

        void copyFile(File src, File dst) throws IOException {
           FileChannel inChannel = new FileInputStream(src).getChannel();
           FileChannel outChannel = new FileOutputStream(dst).getChannel();
           try {
              inChannel.transferTo(0, inChannel.size(), outChannel);
           } finally {
              if (inChannel != null)
                 inChannel.close();
              if (outChannel != null)
                 outChannel.close();
           }
        }

     }
	
	private class ExportDatabaseFileTask  extends AsyncTask<String, Void, Boolean> {
		Context ctx = ExpenseOverviewActivity.this;
        private final ProgressDialog dialog = new ProgressDialog(ctx);

        // can use UI thread here
        protected void onPreExecute() {
           this.dialog.setMessage("Taking Backup. Please Wait...");
           this.dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {

           File dbFile = new File(Environment.getDataDirectory() + "/data/com.brizztv.mcube/databases/sms_expense");

//           File exportDir = new File(Environment.getExternalStorageDirectory(), "");
           // The directory where we will save our database file
           File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.brizztv.mcube/databases");
           
           if (!exportDir.exists()) {
              exportDir.mkdirs();
           }
           
           File file = new File(exportDir, dbFile.getName());

           try {
              file.createNewFile();
              this.copyFile(dbFile, file);
              return true;
           } catch (IOException e) {
//              if(Log.DEBUG) Log.v(e.getMessage());
              return false;
           }
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
           if (this.dialog.isShowing()) {
              this.dialog.dismiss();
           }
           if (success) {
              Toast.makeText(ctx, "Backup successful!", Toast.LENGTH_SHORT).show();
           } else {
              Toast.makeText(ctx, "Backup failed", Toast.LENGTH_SHORT).show();
           }
        }

        void copyFile(File src, File dst) throws IOException {
           FileChannel inChannel = new FileInputStream(src).getChannel();
           FileChannel outChannel = new FileOutputStream(dst).getChannel();
           try {
              inChannel.transferTo(0, inChannel.size(), outChannel);
           } finally {
              if (inChannel != null)
                 inChannel.close();
              if (outChannel != null)
                 outChannel.close();
           }
        }
     }
	
	@Override
	 public void onBackPressed() { 
		new AlertDialog.Builder(this)
        .setMessage("Are you sure you want to exit?")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExpenseOverviewActivity.this.finish();
            }
        })
        .setNegativeButton("No", null)
        .show();
	 }
}
