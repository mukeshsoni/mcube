package com.brizztv.mcube;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class SmsExpenserPopupActivity extends Activity {
	private String mPopupMessage=null, mExpenseAmount, mExpenseState, mSmsBody, mCategoryName;
	private static final Map<String, String> messageMap;
	
	static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("NEFT", "You seem to have made an online transfer of Rs. ");
        aMap.put("CASH-ATM", "You seem to have made an ATM withdrawal of Rs.");
        aMap.put("ICONN", "You seem to have made an online transaction of Rs. ");
        aMap.put("M-Banking", "You seem to have used mobile banking for a transaction of Rs. ");
        aMap.put("Info: PUR", "You seem to have made a purchase of Rs. ");
        aMap.put("ECS", "An ECS has been cleared. Amount - Rs. ");
        aMap.put("online transaction", "You seem to have made an online transfer of Rs. ");
        aMap.put("Chq No.", "Your cheque has been debited. Amount - Rs. ");
        
        aMap.put("RTGS / NEFT", "You seem to have made an online transaction of Rs. ");
        aMap.put("through Net Banking", "You seem to have made an online transaction of Rs. ");
        aMap.put("withdrawal from ATM", "You seem to have made an ATM withdrawal of Rs. ");
        aMap.put("Folio & NAV", "You seem to have made an investment of Rs. ");
        aMap.put("thru ATM", "you seem to have made an ATM withdrawal of Rs. ");
        aMap.put("thru Internet Bkg", "You seem to have made an online transaction of Rs. ");
        aMap.put("Txn at ATM", "You seem to have made an ATM withdrawal of Rs. ");
        aMap.put("TPT Txn", "You seem to have made an investment of Rs. ");
        aMap.put("NEFT Txn", "You seem to have made an investment of Rs. ");
        aMap.put("online payment", "You seem to have made an investment of Rs. ");
        aMap.put("BillPay/Credit Card payment", "You seem to have made an investment of Rs. ");
        aMap.put("Purchase in folio, NAV, successful", "You seem to have made an investment of Rs. ");
        
        aMap.put("withdrawn at an ATM", "you seem to have made an ATM withdrawal of Rs. ");
       
        messageMap = Collections.unmodifiableMap(aMap);
    }
	
	public static boolean isRunning = false;
	long mExpenseId;
	Cursor mCursor;
	private AlertDialog ad;
	
	static final String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_AMOUNT,
		DataProvider.KEY_EXPENSE_BANK_NAME, DataProvider.KEY_EXPENSE_DATE, DataProvider.KEY_EXPENSE_LOCATION, DataProvider.KEY_EXPENSE_MERCHANT,
		DataProvider.KEY_EXPENSE_NOTES, DataProvider.KEY_EXPENSE_CATEGORY_NAME, DataProvider.KEY_EXPENSE_DAY, DataProvider.KEY_EXPENSE_MONTH,
		DataProvider.KEY_EXPENSE_YEAR, DataProvider.KEY_EXPENSE_STATE, DataProvider.KEY_EXPENSE_SMS_BODY, DataProvider.KEY_EXPENSE_TIME_STAMP };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if(Log.DEBUG) Log.v("SmsExpensePopupActivity: onCreate");
		
		Bundle extras = getIntent().getExtras();
		mExpenseId = extras.getLong(DataProvider.KEY_EXPENSE_ID);
		
		// get the expense from database
		String where = DataProvider.KEY_EXPENSE_ID+"="+mExpenseId;
		mCursor = getContentResolver().query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, null, null);
		mCursor.moveToFirst();
		mExpenseAmount = mCursor.getString(mCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_AMOUNT));
		mExpenseState = mCursor.getString(mCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_STATE));
		mSmsBody = mCursor.getString(mCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SMS_BODY));
		mCategoryName = mCursor.getString(mCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME));
		
		
		// default message in case nothing matched in our 'smsbody-to-message-map'
		mPopupMessage = "You seem to have made an expense of Rs. "
				+ mExpenseAmount + ". Want to file it?\n\nCategory: " + mCategoryName;

		if(mCategoryName.equals(DataProvider.CATEGORY_ATM_WITHDRAWAL)) {
			mPopupMessage = "You seem to have made an ATM withdrawal of Rs. "
					+ mExpenseAmount + ". Want to file it?\n\nCategory: " + mCategoryName;
		} else {
			// iterate through our hashmap of custom messages and pick the appropriate one (if any)
			Iterator<Entry<String, String>> it = messageMap.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, String> pairs = it.next();
				String key = pairs.getKey();
				if(mSmsBody.toLowerCase().indexOf(key.toLowerCase()) >= 0) {
					mPopupMessage = pairs.getValue() + mExpenseAmount + ". Want to file it?\n\nCategory: " + mCategoryName;
					break;
				}
			}
		}

		createAlert();
		if(!ad.isShowing()) {
			ad.show();
		} else {
			ad.dismiss();
			createAlert();
			ad.show();
		}
	}

	private void createAlert() {
		ad = new AlertDialog.Builder(this)
				.setTitle("New Expense")
				.setMessage(mPopupMessage)
				.setIcon(R.drawable.ic_alert_mcube)
				.setPositiveButton(R.string.alertPopupDiscardButton,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								//update the state of expense to discarded
								String expenseState = mCursor.getString(mCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_STATE)) + "_" + DataProvider.EXPENSE_DISCARDED;
								ContentValues values = new ContentValues();
								values.put(DataProvider.KEY_EXPENSE_STATE, expenseState);
								String where = DataProvider.KEY_EXPENSE_ID+"="+mExpenseId;
								getContentResolver().update(DataProvider.EXPENSE_URI, values, where, null);
								finish();
							}
						})
//				.setNeutralButton(R.string.alertPopupLaterButton,
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog,
//									int whichButton) {
//								addReminder();
//								finish();
//								/* User clicked Later so do some stuff */
//							}
//						})
				.setNegativeButton(R.string.alertPopupOkButton,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Append 'saved' to expense state
								ContentValues values = new ContentValues();
								String where = DataProvider.KEY_EXPENSE_ID+"="+mExpenseId;
								if(!mExpenseState.contains("saved")){
									values.put(DataProvider.KEY_EXPENSE_STATE, mExpenseState+"_saved");
								}
								getContentResolver().update(DataProvider.EXPENSE_URI, values, where, null);
								
								// let us start the add expense activity... just send it the expense id
								Intent intent = new Intent(getBaseContext(), AddExpenseActivity.class);
								intent.putExtra(DataProvider.KEY_EXPENSE_ID, mExpenseId);
								startActivity(intent);
								finish();
							}
						}).create();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isRunning = false;
		if(ad != null && ad.isShowing())
			ad.dismiss();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isRunning = true;
		if(ad != null && !ad.isShowing())
			ad.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ad.dismiss();
		finish();
	}
}
