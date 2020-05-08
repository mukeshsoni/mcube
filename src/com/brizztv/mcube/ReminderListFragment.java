package com.brizztv.mcube;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

public class ReminderListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	SimpleCursorAdapter mCursorAdapter;
	private String mExpenseAmount=null, mBankName=null;
	private int mYear;
    private int mMonth;
    private int mDay;
    private String mDate;
    
	// These are the Reminder rows that we will retrieve
	static final String[] PROJECTION = new String[] 
	 		{
	 			DataProvider.KEY_REMINDER_ID,
	 			DataProvider.KEY_REMINDER_BANK_NAME,
	 			DataProvider.KEY_REMINDER_AMOUNT,
	 			DataProvider.KEY_REMINDER_DATE,
				DataProvider.KEY_REMINDER_DAY,
				DataProvider.KEY_REMINDER_MONTH,
				DataProvider.KEY_REMINDER_YEAR
	     };
	static final String SELECTION = "*";
	static final String sortOrder = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
		if(Log.DEBUG) Log.v("ReminderListFragment: onCreateView");
		View mRoot = inflater.inflate(R.layout.list_reminders, null);
		return mRoot;
	}
	
	private void fillData() {
		if(Log.DEBUG) Log.v("ReminderListFragment: fillData");
		String[] from = new String[]{
				DataProvider.KEY_REMINDER_AMOUNT, 
				DataProvider.KEY_EXPENSE_DAY,
				DataProvider.KEY_EXPENSE_MONTH,
				DataProvider.KEY_REMINDER_YEAR
			};

	    // and an array of the fields we want to bind those fields to (in this case just label)
	    int[] to = new int[]{R.id.amountTextReminder, R.id.dayTextReminder, R.id.monthTextReminder, R.id.yearTextReminder};
	    
	    getLoaderManager().initLoader(1, null, this);
	    
		mCursorAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.reminder_row, null, from, to, 0);
		setListAdapter(mCursorAdapter);
	}
	
 @Override
 	public void onActivityCreated(Bundle savedInstanceState) {
	 	super.onActivityCreated(savedInstanceState);
     
	 	Activity activity = getActivity();
     
	 	if(Log.DEBUG) Log.v("ReminderListFragment: onActivityCreated");

	 	if (activity != null) {
	 		fillData();
	 		this.getListView().setDividerHeight(2);
     }
 }


	 @Override
	 public void onListItemClick(ListView l, View v, int position, long id) {
		Activity activity = getActivity();
	        
        if (activity != null) {   
        	super.onListItemClick(l, v, position, id);
    		Cursor cc = ((SimpleCursorAdapter)l.getAdapter()).getCursor();
    		cc.moveToPosition(position);
    		long reminderId = cc.getLong(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_ID));
    		
    		//get the amount, day, month and year to feed the extras
    		String selection = DataProvider.KEY_REMINDER_ID+"="+reminderId;
    		cc = getActivity().getApplicationContext().getContentResolver().query(
    				DataProvider.REMINDER_URI, 
    				PROJECTION, 
    				selection, 
    				null, 
    				null);
    		cc.moveToFirst();
    		mBankName = cc.getString(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_BANK_NAME));
    		mExpenseAmount = cc.getString(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_AMOUNT));
    		mDay = cc.getInt(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_DAY));
			mMonth = cc.getInt(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_MONTH));
			mYear = cc.getInt(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_YEAR));
			mDate = cc.getString(cc.getColumnIndexOrThrow(DataProvider.KEY_REMINDER_DATE));
			
    		if(Log.DEBUG) Log.v("ListReminders: onListItemClick: Id of selected reminder: "+reminderId);
    		Intent intent = new Intent(getActivity().getApplicationContext(), AddExpenseActivity.class);
    		intent.putExtra(DataProvider.KEY_REMINDER_BANK_NAME, mBankName);
    		intent.putExtra(DataProvider.KEY_EXPENSE_AMOUNT, mExpenseAmount);
    		intent.putExtra(DataProvider.KEY_EXPENSE_DATE, mDate);
			intent.putExtra(DataProvider.KEY_EXPENSE_DAY, mDay);
			intent.putExtra(DataProvider.KEY_EXPENSE_MONTH, mMonth);
			intent.putExtra(DataProvider.KEY_EXPENSE_YEAR, mYear);
			intent.putExtra("reminder"+DataProvider.KEY_REMINDER_ID, reminderId);
			cc.close();
    		startActivity(intent);
        }
	 }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if(Log.DEBUG) Log.v("ReminderListFragment: onCreateLoader");
		return new CursorLoader(getActivity(), DataProvider.REMINDER_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(Log.DEBUG) Log.v("ReminderListFragment: onLoadFinished: No. of reminder rows returned: "+data.getCount());
		mCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}
}
