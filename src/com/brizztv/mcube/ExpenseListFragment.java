package com.brizztv.mcube;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

public class ExpenseListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	SimpleCursorAdapter mCursorAdapter;
	
	static final String[] noExpenseRandomText = new String[] 
			{
				"No expenses! Why don\'t you buy that shiny thing for the love of your live?",
				"Still nothing!! Don't be that frugal guy. Splurge a little.",
				"I know, money can't buy happiness. But it can buy things which can buy happiness ;)",
				"Don't angry your spouse! Get that gift."
			};
	
	   // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[] 
    		{
    			DataProvider.KEY_EXPENSE_ID,
    			DataProvider.KEY_EXPENSE_AMOUNT,
    			DataProvider.KEY_EXPENSE_BANK_NAME,
    			DataProvider.KEY_EXPENSE_MERCHANT,
    			DataProvider.KEY_EXPENSE_LOCATION,
    			DataProvider.KEY_EXPENSE_DATE,
    			DataProvider.KEY_EXPENSE_NOTES,
		    	DataProvider.KEY_EXPENSE_CATEGORY_NAME,
				DataProvider.KEY_EXPENSE_DAY,
				DataProvider.KEY_EXPENSE_MONTH,
				DataProvider.KEY_EXPENSE_SMS_ID,
				DataProvider.KEY_EXPENSE_SMS_THREAD_ID,
				DataProvider.KEY_EXPENSE_SMS_BODY,
				DataProvider.KEY_EXPENSE_TIME_STAMP,
				DataProvider.KEY_EXPENSE_YEAR
            };
    static final String SELECTION = "*";
//    static final String sortOrder = DataProvider.KEY_EXPENSE_YEAR + "," + DataProvider.KEY_EXPENSE_MONTH + "," + DataProvider.KEY_EXPENSE_DAY + " DESC";
//    static final String sortOrder = DataProvider.KEY_EXPENSE_DATE + " DESC";
    static final String sortOrder = DataProvider.KEY_EXPENSE_TIME_STAMP + " DESC";
    // ListFragment is a very useful class that provides a simple ListView inside of a Fragment.
    // This class is meant to be sub-classed and allows you to quickly build up list interfaces
    // in your app.
    protected Object mActionMode;
  	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
//		if(Log.DEBUG) Log.v("ExpenseListFragment: onCreateView");
        View mRoot = inflater.inflate(R.layout.list_expenses, null);
        
        return mRoot;
    }
	
	private void fillData() {
//		if(Log.DEBUG) Log.v("ExpenseListFragment: fillData");
		
		setNoExpenseText();
		
		String[] from = new String[]{
				DataProvider.KEY_EXPENSE_AMOUNT, 
				DataProvider.KEY_EXPENSE_CATEGORY_NAME,
				DataProvider.KEY_EXPENSE_BANK_NAME.toLowerCase(),
				DataProvider.KEY_EXPENSE_DAY,
				DataProvider.KEY_EXPENSE_MONTH,
				DataProvider.KEY_EXPENSE_YEAR
			};

	    // and an array of the fields we want to bind those fields to (in this case just label)
	    int[] to = new int[]{R.id.amountText, R.id.categoryText, R.id.bankText, R.id.dayText, R.id.monthText, R.id.yearText};
	    
	    getLoaderManager().initLoader(1, null, this);
	    
		mCursorAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.expense_row, null, from, to, 0);
        setListAdapter(mCursorAdapter);
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Activity activity = getActivity();

        getListView().setEmptyView(activity.findViewById(R.id.expenseListEmpty));
//        if(Log.DEBUG) Log.v("ExpenseListFragment: onActivityCreated");
        
        if (activity != null) {
        	fillData();
        	registerForContextMenu(getListView());
        }
    }

	/*
	 * Randomly selects a text message from an array of strings to show when the list is empty
	 */
    private void setNoExpenseText() {
//    	TextView noExpenseText = (TextView) getListView().getEmptyView();
    	TextView noExpenseText = (TextView) getActivity().findViewById(R.id.emptyListMessage);
    	if(noExpenseText != null)
    		noExpenseText.setText(noExpenseRandomText[(int)(Math.random() * noExpenseRandomText.length)]);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Activity activity = getActivity();
        
        if (activity != null) {   
        	super.onListItemClick(l, v, position, id);
    		Cursor cc = ((SimpleCursorAdapter)l.getAdapter()).getCursor();
    		cc.moveToPosition(position);
    		long expenseId = cc.getLong(cc.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
//    		if(Log.DEBUG) Log.v("ListExpenses: Id of selected expense: "+expenseId);
    		Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
    		intent.putExtra("callingActivity", ActivityConstants.ExpenseOverViewActivity);
    		intent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);
    		startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
//    	menu.add(UNIQUE_FRAGMENT_GROUP_ID, MENU_OPTION_1, 0, R.string.src1);
    	menu.add(Menu.NONE, R.id.delete_expense, Menu.NONE, "Delete");
    	menu.add(Menu.NONE, R.id.edit_expense, Menu.NONE, "Edit");
    	menu.add(Menu.NONE, R.id.go_to_sms, Menu.NONE, "Show related SMS");
//        MenuInflater inflater = getSupportMenuInflater();
//        inflater.inflate(R.menu.context_menu, menu);
    }
    
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	 AdapterView.AdapterContextMenuInfo info = 
    		        (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
     	Cursor cc = ((SimpleCursorAdapter)getListView().getAdapter()).getCursor();
 		cc.moveToPosition(info.position);
 		long expenseId = cc.getLong(cc.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
 		Long messageId = cc.getLong(cc.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SMS_ID));
 		
        switch (item.getItemId()) {
            case R.id.delete_expense:
        		getActivity().getContentResolver().delete(DataProvider.EXPENSE_URI, DataProvider.KEY_EXPENSE_ID+"="+expenseId, null);
        		Toast.makeText(getActivity().getApplicationContext(), "Expense Deleted", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.edit_expense:
	    		Intent intent = new Intent(getActivity().getApplicationContext(), AddExpenseActivity.class);
	    		intent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);
	    		intent.putExtra("callingActivity", ActivityConstants.ExpenseOverViewActivity);
	    		startActivity(intent);
                return true;
            case R.id.go_to_sms:
            	if(messageId != null && messageId != -1) {
            		String smsBody = cc.getString(cc.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SMS_BODY));
            		if(smsBody == null || smsBody.equals("")) {
                		new AlertDialog.Builder(getActivity())
        				.setTitle("Bummer!")
        				.setMessage("This expense has no related SMS!")
        				.setIcon(R.drawable.ic_alert_mcube)
        				.setPositiveButton(R.string.alertPopupCloseButton,
        						new DialogInterface.OnClickListener() {
        							public void onClick(DialogInterface dialog,
        									int whichButton) {
        								// don't do nothing
//        								finish();
        							}
        						}).show();
            		} else {
                		new AlertDialog.Builder(getActivity())
        				.setTitle("Related SMS")
        				.setMessage(smsBody)
        				.setIcon(R.drawable.ic_alert_mcube)
        				.setPositiveButton(R.string.alertPopupCloseButton,
        						new DialogInterface.OnClickListener() {
        							public void onClick(DialogInterface dialog,
        									int whichButton) {
        								// don't do nothing
        							}
        						}).show();
            		}
//            		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//                	smsIntent.setData(Uri.parse("content://mms-sms/conversations"));
////            		smsIntent.setData(Uri.parse("content://sms/inbox"));
//            		smsIntent.putExtra("_id", messageId);
//                	startActivity(smsIntent);
            	} else {
            		Toast.makeText(getActivity().getApplicationContext(), "Could not find the original SMS", Toast.LENGTH_SHORT).show();
            	}
            	
            	return true;
        }
        return super.onContextItemSelected(item);
    }
    
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
//		if(Log.DEBUG) Log.v("ExpenseListFragment: onCreateLoader");
//		String where = "expense_date>=date('now','-2 days')";
		String where = DataProvider.KEY_EXPENSE_STATE+" LIKE '%saved%'";
		return new CursorLoader(getActivity(), DataProvider.EXPENSE_URI, PROJECTION, where, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//		if(Log.DEBUG) Log.v("ExpenseListFragment: onLoadFinished");
//		if(Log.DEBUG) Log.v("No. of rows returned: "+data.getCount());
		mCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}
}
