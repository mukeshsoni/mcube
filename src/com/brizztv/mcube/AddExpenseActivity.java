package com.brizztv.mcube;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

public class AddExpenseActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, android.view.GestureDetector.OnGestureListener {
	private String mExpenseAmount = null, mNotes = null, mCategoryName = DataProvider.CATEGORY_UNCATEGORIZED, mBankName = DataProvider.BANK_OTHERS, 
			mState=null, mMerchant=null, mLocation=null, mExpenseDate=null, mSmsBody=null;

	private int mYear;
	private int mMonth;
	private int mDay;
	private Long mRowId = null;
	private long mTimeStamp=System.currentTimeMillis();
	private Long mReminderId = null;
	private boolean mFromReminder = false;
	private Cursor mExpenseCursor;
	final int CATEGORY_SPINNER = 0;
	final int BANK_SPINNER = 1;
	final int ADD_CATEGORY=2;
	// RANDOM CATEGORY ID FOR UNCATEGORIZED ITEMS. NEED TO TAKE CARE OF THIS IN
	// THE DATABASE

	private EditText expenseAmountEditText;
	private TextView expenseDateText, smsText;
	private EditText notesEditText;
	private Spinner mCategorySpinner, mBankSpinner;
	private SimpleCursorAdapter mCategorySpinnerAdapter, mBankSpinnerAdapter;
	private boolean expenseToEdit = false;

	static final String[] CATEGORY_PROJECTION = new String[] { DataProvider.KEY_CATEGORY_ID, DataProvider.KEY_CATEGORY_NAME, };
	private String categorySortOrder = DataProvider.KEY_CATEGORY_NAME + " ASC";
	
	static final String[] BANK_PROJECTION = new String[] { DataProvider.KEY_BANK_ID, DataProvider.KEY_BANK_NAME, DataProvider.KEY_BANK_DEFAULT };
	private String bankSortOrder = DataProvider.KEY_BANK_PRIORITY + " ASC, " + DataProvider.KEY_BANK_NAME + " ASC";
	
	static final String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_AMOUNT,
			DataProvider.KEY_EXPENSE_BANK_NAME, DataProvider.KEY_EXPENSE_MERCHANT, 
			DataProvider.KEY_EXPENSE_LOCATION, DataProvider.KEY_EXPENSE_NOTES,
			DataProvider.KEY_EXPENSE_CATEGORY_NAME, DataProvider.KEY_EXPENSE_DAY, 
			DataProvider.KEY_EXPENSE_MONTH, DataProvider.KEY_EXPENSE_YEAR,
			DataProvider.KEY_EXPENSE_DATE, DataProvider.KEY_EXPENSE_STATE, DataProvider.KEY_EXPENSE_SMS_BODY,
			DataProvider.KEY_EXPENSE_TIME_STAMP};
	private static final int AMOUNT_BLANK_DIALOG_ID = 1;

	// for swipe gesture detection
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (Log.DEBUG)
		// Log.v("AddExpenseActivity: onCreate");
		
		// so that the keyboard does not auto pop up when the activity starts. Only pops once the users clicks on an editable field
//		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
		
		setContentView(R.layout.add_expense);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		extractBundleValues();
		setupViews();
		
		// setup gesture detection
		gestureDetector = new GestureDetector(this, this);
		// if add expense is not calling from within app, ask user to rate app if the app is launched more than 10 times or has been in use for more than 5 days.
		if(!getIntent().hasExtra("callingActivity")) {
			AppRater.app_launched(this);
		}
	}

	
	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_add_expense, menu);
		return true;
	}

	/*
	 * this activity can be called from various places - 1. notification 2.
	 * popup dialog 3. reminder 4. Edit Expense 5. Add Expense and some extra
	 * values will not be present in some intents - e.g. no amount when coming
	 * from home screen
	 */
	private void extractBundleValues() {
		final Calendar c = Calendar.getInstance();
		Bundle extras = getIntent().getExtras();
		// no extras, when the intent comes from "Add Expense" button
		// just set the date as current date
		if (extras == null) {
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);
		} else {
			// have to check if the extra is already present sing
			// extras.getLong returns primitive type long which can never be
			// null and so returns a value of 0 if the key is not found
			if (getIntent().hasExtra(DataProvider.KEY_EXPENSE_ID)) {
				mRowId = extras.getLong(DataProvider.KEY_EXPENSE_ID);
			}

			// if mRowId!=null, get values from the database (case 4. Edit Expense)
			// else, the remaining cases are 1. notification 2. popup dialog
			if (mRowId != null) {
				mExpenseCursor = getContentResolver().query(Uri.parse(DataProvider.EXPENSE_URI + "/" + mRowId), EXPENSE_PROJECTION, null, null, null);
				mExpenseCursor.moveToFirst();
				mExpenseAmount = mExpenseCursor.getString(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_AMOUNT));
				mCategoryName = mExpenseCursor.getString(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME));
				mBankName = mExpenseCursor.getString(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_BANK_NAME));
				mState = mExpenseCursor.getString(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_STATE));
				mDay = mExpenseCursor.getInt(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_DAY));
				// IMP - hack to take care of android starting months from 0
				mMonth = mExpenseCursor.getInt(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_MONTH)) - 1;
				mYear = mExpenseCursor.getInt(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_YEAR));
				mNotes = mExpenseCursor.getString(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_NOTES));
				mSmsBody = mExpenseCursor.getString(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SMS_BODY));
				mTimeStamp = mExpenseCursor.getLong(mExpenseCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_TIME_STAMP));
				expenseToEdit = true;
			} else {
				// in all the remaining cases, the extras will have nothing
//				if(Log.DEBUG) Log.v("AddExpenseActivity: extractBundleValues: date received is: "
				// + date);
				try {
					c.setTimeInMillis(System.currentTimeMillis());
//					c.setTime(format.parse(date));
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
				} catch (Exception pe) {
//					if (Log.DEBUG)
//						Log.v("Could not parse date from string: " + pe);
				}
			}
		}
	}

	/*
	 * setup the various views in the screen - date picker, spinner etc and get
	 * attach handles to required ui elements
	 */
	private void setupViews() {
		// get view references
//		deleteButton = (Button) findViewById(R.id.deleteExpenseEditButton);
		expenseDateText = (TextView) findViewById(R.id.expenseDateText);
		expenseAmountEditText = (EditText) findViewById(R.id.amountEditText);
		notesEditText = (EditText) findViewById(R.id.notesEditText1);
		smsText = (TextView) findViewById(R.id.smsBodyText);
		
		// populate amount and notes
		expenseAmountEditText.setText(mExpenseAmount);
		notesEditText.setText(mNotes);
		
		// Only show sms if one exists
		if(mSmsBody == null || mSmsBody.trim().equals("")) {
			findViewById(R.id.smsLabel).setVisibility(View.GONE);
			smsText.setVisibility(View.GONE);
		} else {
			smsText.setText(mSmsBody);
		}

		// setup Date view
		updateDateDisplay();
		setUpBankSpinner();
		setUpCategorySpinner();
	}

	private void setUpCategorySpinner() {
//		if(Log.DEBUG) Log.v("AddExpenseActivity: setUpCategorySpinner:");
		// setup category spinner
		mCategorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		String[] from = new String[] { DataProvider.KEY_CATEGORY_NAME };
		int[] to = new int[] { android.R.id.text1 };

		// get the categories into our cursor from the category uri
//		Cursor cursor = getContentResolver().query(DataProvider.CATEGORY_URI, CATEGORY_PROJECTION, null, null, categorySortOrder);

		// setup spinner cursor adapter
		mCategorySpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, from, to, 0);
		// setup the line item view for the spinner
		mCategorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attach spinner view to spinner adapter
		mCategorySpinner.setAdapter(mCategorySpinnerAdapter);
		getSupportLoaderManager().initLoader(CATEGORY_SPINNER, null, this);
		
		// set initial value for mCategoryName
//		if (mCategoryName != null) {
//			for (int i = 0; i < mCategorySpinner.getCount(); i++) {
//				Cursor value = (Cursor) mCategorySpinner.getItemAtPosition(i);
//				String cName = value.getString(value.getColumnIndex(DataProvider.KEY_CATEGORY_NAME));
//				if (cName.equalsIgnoreCase(mCategoryName)) {
//					mCategorySpinner.setSelection(i);
//				}
//			}
//		} else {
//			mCategorySpinner.setSelection(0); // if nothing was set
//			mCategoryName = mCategorySpinner.getSelectedItem().toString();
//		}

		// catch what has been selected in the category spinner
		mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Cursor spinnerCursor = (Cursor) (mCategorySpinner.getSelectedItem());
				mCategoryName = spinnerCursor.getString(spinnerCursor.getColumnIndexOrThrow(DataProvider.KEY_CATEGORY_NAME));
			}

			public void onNothingSelected(AdapterView<?> parent) {
				mCategoryName = DataProvider.CATEGORY_UNCATEGORIZED;
			}
		});
	}

	private void setUpBankSpinner() {
		mBankSpinner = (Spinner) findViewById(R.id.bankNameSpinner);
		String[] from = new String[] { DataProvider.KEY_BANK_NAME };
		int[] to = new int[] { android.R.id.text1 };

		// get the banks into our cursor from the bank uri
		Cursor cursor = getContentResolver().query(DataProvider.BANK_URI, BANK_PROJECTION, null, null, bankSortOrder);
		// setup spinner cursor adapter
		mBankSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to, 0);

		mBankSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attach spinner view to spinner adapter
		mBankSpinner.setAdapter(mBankSpinnerAdapter);

		// set initial selection
		if (mBankName != null) {
			for (int i = 0; i < mBankSpinner.getCount(); i++) {
				Cursor value = (Cursor) mBankSpinner.getItemAtPosition(i);
				String name = value.getString(value.getColumnIndex(DataProvider.KEY_BANK_NAME));
				if (name.equalsIgnoreCase(mBankName)) {
					mBankSpinner.setSelection(i);
				}
			}
		} else {
			mBankSpinner.setSelection(0);
			// set bank name in case it was null
			mBankName = mBankSpinner.getSelectedItem().toString();
		}

		// catch what has been selected
		mBankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Cursor spinnerCursor = (Cursor) (mBankSpinner.getSelectedItem());
				mBankName = spinnerCursor.getString(spinnerCursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_BANK_NAME));
				// if(Log.DEBUG)
				// Log.v("category selected is: "+mCategoryName);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				mBankName = "Uncategorized";
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// saveState();
	}

	@Override
	protected void onResume() {
		// repopulate category spinner
		setUpCategorySpinner();
		super.onResume();
		// populateFields();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DataProvider.KEY_EXPENSE_ID, mRowId);
	}

	public void onClickAddCategory(View view) {
		Intent intent = new Intent(this, AddCategoryActivity.class);
		startActivityForResult(intent, ADD_CATEGORY);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
		case ADD_CATEGORY:
			if(resultCode == Activity.RESULT_OK) {
				mCategoryName = data.getStringExtra(DataProvider.KEY_CATEGORY_NAME);
				setCategorySpinnerValue();
			}
			break;
		default:
		}
	}
	
	// If the user comes to this screen from the pop up or the notification screen, he/she should not go out of the app when back is pressed
	@Override
    public void onBackPressed() {
		Intent intent;
		finish();
		// If the user comes to this screen from the pop up or the notification screen, he/she should not go out of the app when back is pressed
		if(getIntent().hasExtra("callingActivity") && getIntent().getExtras().getInt("callingActivity") == ActivityConstants.ExpenseOverViewActivity) {
			// do nothing
		} else {
			intent = new Intent(this, ExpenseOverviewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
	 
	private void saveExpense() {
		ContentValues values = new ContentValues();
		Uri expenseUri;
		String where;

		if (expenseAmountEditText.getText().length() == 0) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("No amount").setMessage("Amount box is blank")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();
		} else {
			mExpenseAmount = expenseAmountEditText.getText().toString();
			mNotes = notesEditText.getText().toString();
			
			// if the state is blank, it means the expense has been explicitly created by the user
			if(mState == null || mState.equals("")) {
				mState = DataProvider.EXPENSE_STATE_USER_ADDED + "_" + DataProvider.EXPENSE_STATE_SAVED; 
			} // else check if this is not an edit of a previously saved expense 
			else if(!mState.contains("saved")){
				// append a "saved" substring to the state if it came from a status bar notification or a pop up notification
				mState = mState + "_" + DataProvider.EXPENSE_STATE_SAVED;
			}
			
			mExpenseDate = Integer.toString(mYear) + "-" + String.format("%02d", mMonth + 1) + "-" + String.format("%02d", mDay);
			
			values.put(DataProvider.KEY_EXPENSE_AMOUNT, mExpenseAmount);
			values.put(DataProvider.KEY_EXPENSE_CATEGORY_NAME, mCategoryName);
			values.put(DataProvider.KEY_EXPENSE_BANK_NAME, mBankName);
			values.put(DataProvider.KEY_EXPENSE_DAY, mDay);
			values.put(DataProvider.KEY_EXPENSE_MONTH, mMonth + 1);
			values.put(DataProvider.KEY_EXPENSE_YEAR, mYear);
			values.put(DataProvider.KEY_EXPENSE_NOTES, mNotes);
			values.put(DataProvider.KEY_EXPENSE_MERCHANT, mMerchant);
			values.put(DataProvider.KEY_EXPENSE_LOCATION, mLocation);
			values.put(DataProvider.KEY_EXPENSE_STATE, mState);
			// TODO - do not overwrite an existing time stamp
			values.put(DataProvider.KEY_EXPENSE_TIME_STAMP, mTimeStamp);
			values.put(DataProvider.KEY_EXPENSE_DATE, mExpenseDate);

			where = DataProvider.KEY_EXPENSE_ID + "=" + mRowId;

			if (expenseToEdit) {
				// values.put(DataProvider.KEY_EXPENSE_ID, mRowId);
				getContentResolver().update(DataProvider.EXPENSE_URI, values, where, null);
			} else {
				expenseUri = getContentResolver().insert(DataProvider.EXPENSE_URI, values);
				mRowId = Long.valueOf(expenseUri.getLastPathSegment());

				// if the expense came from a reminder, delete the reminder
				if (mFromReminder) {
					where = DataProvider.KEY_REMINDER_ID + "=" + mReminderId;
					getContentResolver().delete(DataProvider.REMINDER_URI, where, null);
				}
			}

			if (mRowId == -1) {
//				if (Log.DEBUG)
//					Log.v("AddExpenseActivity: onClickSaveExpense: Could not save expense");
			} else {
				Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
			}
		}
	}

	private void deleteExpense() {
		if (mRowId != null) {
			if (getContentResolver().delete(Uri.parse(DataProvider.EXPENSE_URI + "/" + mRowId), null, null) > 0) {
				mRowId = null;
			} else {
//				if (Log.DEBUG)
//					Log.v("Could not delete expense with id: " + "mRowId");
			}
		}
	}

	public void onClickDatePicker(View view) {
		showDatePickerDialog(view);
	}

	public void showDatePickerDialog(View v) {
		SherlockDialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		// case DATE_DIALOG_ID:
		// return new DatePickerDialog(this,
		// mDateSetListener,
		// mYear, mMonth, mDay);
		case AMOUNT_BLANK_DIALOG_ID:
			return new AlertDialog.Builder(this).setTitle("Mandatory field").setMessage("Please enter an amount to save")
					.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// continue with delete
						}
					}).create();
		}
		return null;
	}

	// updates the date in the TextView
	private void updateDateDisplay() {
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(mTimeStamp);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Month is 0 based so add 1
		expenseDateText.setText(new StringBuilder()
				.append(day).append("-").append(new DateFormatSymbols().getShortMonths()[month]).append("-").append(year).append(" "));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader cl = null;
		Uri uri;
		String where = null;

		if(id == CATEGORY_SPINNER) {
			uri = DataProvider.CATEGORY_URI;
			cl = new CursorLoader(this, uri, CATEGORY_PROJECTION, where, null, categorySortOrder);
		} else if(id == BANK_SPINNER) {
			uri = DataProvider.BANK_URI;
			cl = new CursorLoader(this, uri, BANK_PROJECTION, where, null, bankSortOrder);
		}
		return cl;
	}

	private void setCategorySpinnerValue() {
		// set initial value for mCategoryName
		if (mCategoryName != null) {
			for (int i = 0; i < mCategorySpinner.getCount(); i++) {
				Cursor value = (Cursor) mCategorySpinner.getItemAtPosition(i);
				String cName = value.getString(value.getColumnIndex(DataProvider.KEY_CATEGORY_NAME));
				if (cName.equalsIgnoreCase(mCategoryName)) {
					mCategorySpinner.setSelection(i);
				}
			}
		} else {
			mCategorySpinner.setSelection(0); // if nothing was set
			mCategoryName = mCategorySpinner.getSelectedItem().toString();
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		if(id == CATEGORY_SPINNER) {
			mCategorySpinnerAdapter.swapCursor(data);
			setCategorySpinnerValue();
		} else if(id == BANK_SPINNER) {
			mBankSpinnerAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		if(id == CATEGORY_SPINNER) {
			mCategorySpinnerAdapter.swapCursor(null);
		} else if(id == BANK_SPINNER) {
			mBankSpinnerAdapter.swapCursor(null);
		}
	}

	// @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, ExpenseOverviewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.save_expense:
			saveExpense();
			finish();
			// If the user comes to this screen from the pop up or the notification screen, he/she should not go out of the app when back is pressed
			if(getIntent().hasExtra("callingActivity") && getIntent().getExtras().getInt("callingActivity") == ActivityConstants.ExpenseOverViewActivity) {
				// do nothing
			} else {
				intent = new Intent(this, ExpenseOverviewActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return true;
		case R.id.delete_expense:
			deleteExpense();
			finish();
			// If the user comes to this screen from the pop up or the notification screen, he/she should not go out of the app when back is pressed
			if(getIntent().hasExtra("callingActivity") && getIntent().getExtras().getInt("callingActivity") == ActivityConstants.ExpenseOverViewActivity) {
				// do nothing
			} else {
				intent = new Intent(this, ExpenseOverviewActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return true;
		default:
//			toast = Toast.makeText(this, "You clicked on a menu item", Toast.LENGTH_SHORT);
//			toast.show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me){
//		if(Log.DEBUG) Log.v("AddExpenseActivity: onTouchEvent"); 
		return gestureDetector.onTouchEvent(me);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	// IMP: this is required when using scroll view.
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
	    super.dispatchTouchEvent(ev);    
	    return gestureDetector.onTouchEvent(ev); 
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		String where = "";
		String sortOrder = DataProvider.KEY_EXPENSE_TIME_STAMP + " ASC";
		Cursor cursor;
		String categoryConstraint = null;
		
		if(mRowId != null) {
			try {
				if(getIntent().hasExtra("categoryConstraint")) {
            		categoryConstraint = getIntent().getExtras().getString("categoryConstraint");
            		if(Log.DEBUG) Log.v("AddExpenseActivity: onFling: category Constraint: " + categoryConstraint);
            		where += DataProvider.KEY_EXPENSE_CATEGORY_NAME + "='" + categoryConstraint + "' AND "; 
            	} else {
            		if(Log.DEBUG) Log.v("AddExpenseActivity: onFling: category Constraint: none");
            	}
				
	            if(e1.getX() > e2.getX() && Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	where += DataProvider.KEY_EXPENSE_TIME_STAMP + ">" + mTimeStamp;
	            	cursor = getContentResolver().query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, null, sortOrder);
	            	if(cursor.moveToFirst()) { 
						// let us start the add expense activity... just send it the expense id
	            		Long expenseId = cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
						Intent intent = new Intent(getBaseContext(), AddExpenseActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);
						if(categoryConstraint != null) {
							intent.putExtra("categoryConstraint", categoryConstraint);
						}
						startActivity(intent);
	            	} else {
	            		Toast.makeText(this.getApplicationContext(), "No more expenses", Toast.LENGTH_SHORT).show();
	            	}
	            }else if (e1.getX() < e2.getX() && e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	where += DataProvider.KEY_EXPENSE_TIME_STAMP + "<" + mTimeStamp;
	            	sortOrder = DataProvider.KEY_EXPENSE_TIME_STAMP + " DESC";
	            	cursor = getContentResolver().query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, null, sortOrder);
	            	if(cursor.moveToFirst()) { 
						// let us start the add expense activity... just send it the expense id
	            		Long expenseId = cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
						Intent intent = new Intent(getBaseContext(), AddExpenseActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);
						if(categoryConstraint != null) {
							intent.putExtra("categoryConstraint", categoryConstraint);
						}
						startActivity(intent);
	            	} else {
	            		Toast.makeText(this.getApplicationContext(), "No more expenses", Toast.LENGTH_SHORT).show();
	            	}
	            }
	        } catch (Exception e) {
	            // nothing
	        }
		}
		
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	// creating a date picker using DialogFragment (SherlockDialogFragment in
	// this case)
	public class DatePickerFragment extends SherlockDialogFragment implements DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(mTimeStamp);
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			mYear = year;
			// Ultimate shitty implementation... android starts month from 0
			mMonth = month; 
			mDay = day;
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);
			mTimeStamp = c.getTimeInMillis();
			
			updateDateDisplay();
		}
	}
}
