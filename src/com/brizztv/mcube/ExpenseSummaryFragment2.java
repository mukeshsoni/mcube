package com.brizztv.mcube;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

public class ExpenseSummaryFragment2 extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	TextView mTotalExpense, mCategoryNameText2;
	SimpleCursorAdapter mCursorAdapter, mCursorAdapterDetail;
	protected int mCategoryId;
	protected String mCategoryName;
	private Spinner timeFilterSpinner;
	private String mTimeFilter = null;
	final int AGGREGATE_VIEW = 1;
	final int DETAIL_VIEW = 2;
	private myCursorAdapter mCursorAdapter2;
	
	static final String[] EXPENSE_PROJECTION_CAT = new String[] { DataProvider.KEY_EXPENSE_ID, 
		DataProvider.KEY_EXPENSE_BANK_NAME, 
		DataProvider.KEY_EXPENSE_CATEGORY_NAME,
		DataProvider.KEY_EXPENSE_DATE,
		DataProvider.KEY_EXPENSE_STATE,
		"sum_amount" };

    static final String[] EXPENSE_PROJECTION = new String[] 
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
				DataProvider.KEY_EXPENSE_YEAR,
				DataProvider.KEY_EXPENSE_SMS_ID,
				DataProvider.KEY_EXPENSE_SMS_THREAD_ID,
				DataProvider.KEY_EXPENSE_TIME_STAMP,
				DataProvider.KEY_EXPENSE_SMS_BODY
            };
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onCreateView");
		return inflater.inflate(R.layout.expense_summary2, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onActivityCreated");
		
		Activity activity = getActivity();
		Button scanSmsButton = (Button)getActivity().findViewById(R.id.scanSmsButton);
		
		Button recategorizeExpensesButton = (Button)activity.findViewById(R.id.recategorizeExpensesButton); 
        
		// I have no idea why i have put the check for nullability of activity :O
		if (activity != null) {
			// Back button is shown in detail view. On click of that button, we should go back to category/aggregate view
			Button backButton  = (Button)getActivity().findViewById(R.id.backButton);
			backButton.setOnClickListener(new OnClickListener() {
				Activity activity = getActivity();
			    public void onClick(View v)
			    {
					getLoaderManager().restartLoader(AGGREGATE_VIEW, null, ExpenseSummaryFragment2.this);
					((ListView)activity.findViewById(R.id.listViewByCategory)).setVisibility(View.VISIBLE);
					
					if(Utils.expenseCount(activity) != 0) {
						activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.VISIBLE);
					} else {
						activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.INVISIBLE);
					}
						
					activity.findViewById(R.id.expenseListContainer).setVisibility(View.INVISIBLE);
			    }
			});
			
			// setup the scan sms button click
			scanSmsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onScanButtonClick");
					new FileExpensesFromHistoryTask().execute();
					final ProgressDialog pDialog = ProgressDialog.show(getActivity(), "", "Churning data. Please wait...", true);

					new Thread(new Runnable() {
						public void run() {
							Utils.fileExpenseFromHistory(getActivity());
							// Toast.makeText(
							// ExpenseOverviewActivity.this,
							// "Automatic expense filing done! You can now categorize them.",
							// Toast.LENGTH_LONG).show();
							if (pDialog != null) {
								pDialog.dismiss();
							}							
						}
					}).start();
				}
			});
				
			// setup the recategorize expenses button click
			recategorizeExpensesButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onRecategorizeExpensesClick");					
					new RecategorizeExpensesTask().execute();
				}
			});

			mCategoryNameText2 = (TextView)getActivity().findViewById(R.id.categoryName2);
			
			fillData();
			if(Utils.expenseCount(activity) != 0) {
				activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.VISIBLE);
			} else {
				activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.INVISIBLE);
			}

//			ListView categoryView = (ListView)getActivity().findViewById(R.id.listViewByCategory);
//			if(categoryView.getVisibility() == View.VISIBLE) {
//				
//			}
			// registerForContextMenu(getListView());
			
		}
	}

	@Override
	public void onDestroyView() {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onDestroyView");
		super.onDestroyView();
	}
	
	@Override
	public void onDetach() {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onDetach");
		super.onDetach();
	}
	
	private void fillData() {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: fillData");

		// Setup filter spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.expense_filter,
				R.layout.time_filter_spinner_row);
//				android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeFilterSpinner = (Spinner) getActivity().findViewById(R.id.timeSortSpinner);
		timeFilterSpinner.setAdapter(adapter);

		timeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Object item = parent.getItemAtPosition(pos);
				mTimeFilter = item.toString();
				int detailExpenseViewVisibility = getActivity().findViewById(R.id.expenseListContainer).getVisibility();
				if(detailExpenseViewVisibility == View.VISIBLE) {
					getLoaderManager().restartLoader(DETAIL_VIEW, null, ExpenseSummaryFragment2.this);
				} else {
					getLoaderManager().restartLoader(AGGREGATE_VIEW, null, ExpenseSummaryFragment2.this);
				}
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		listByCategory();
		setupDetailView();
		// TODO - implement the total expense with cursor loader
//		setupTotalExpenseView();
	}
	
	/*
	 * Converts the string in time filter (like "last month") to number of days from today
	 */
	private int getDaysFromTimeFilter() {
		int days = 100000;
		Calendar c = Calendar.getInstance();

		if (mTimeFilter != null && !mTimeFilter.equalsIgnoreCase("All Time")) {
			if (mTimeFilter.equalsIgnoreCase("Last 7 days")) {
				days = 7;
			} else if (mTimeFilter.equalsIgnoreCase("Last 30 days")) {
				days = 30;
			} else if (mTimeFilter.equalsIgnoreCase("This Month")) {
				days = c.get(Calendar.DAY_OF_MONTH);
			} else if (mTimeFilter.equalsIgnoreCase("This Year")) {
				days = c.get(Calendar.DAY_OF_YEAR);
			}
		}
		return days;
	}

	// TODO - should fix this up with cursor loader
	private void calculateTotalExpense(int days) {
		mTotalExpense = (TextView) getActivity().findViewById(R.id.totalExpense);
		mTotalExpense.setText("100");
		String where = "expense_date>=date('now','-" + days + " days') AND " + DataProvider.KEY_EXPENSE_STATE+" LIKE '%saved%'"; 
		
		int detailExpenseViewVisibility = getActivity().findViewById(R.id.expenseListContainer).getVisibility();
		if(detailExpenseViewVisibility == View.VISIBLE) {
			where += " AND " + DataProvider.KEY_EXPENSE_CATEGORY_NAME + "='" + mCategoryName + "'";
		}
		
		Cursor totalExpenseCursor = getActivity().getContentResolver().query(DataProvider.EXPENSE_URI,
				new String[] { "sum(" + DataProvider.KEY_EXPENSE_AMOUNT + ") AS total_expense" }, where, null, null);
		try {
			totalExpenseCursor.moveToFirst();
			if (totalExpenseCursor.getCount() != 0 && totalExpenseCursor.getString(0) != null) {
				// using getLong instead of getString method fixes the problem of large values appearing in scientific notation
				mTotalExpense.setText("Rs. " + totalExpenseCursor.getLong(0));
			} else {
				mTotalExpense.setText("Rs. 0");
			}
		} finally {
			totalExpenseCursor.close();
		}
	}

	private void listByCategory() {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: listByCategory");
		// Find the ListView resource
		Activity activity = getActivity();
		final ListView categoryListView = (ListView) activity.findViewById(R.id.listViewByCategory);
		categoryListView.setEmptyView(activity.findViewById(R.id.emptyCatView));
        
		// get time filter value. accordingly query data provider
//		String[] from = new String[] { DataProvider.KEY_EXPENSE_CATEGORY_NAME, "sum_amount" };
		// and an array of the fields we want to bind those fields to (in this case just label)
//		int[] to = new int[] { R.id.categoryText, R.id.amountText };

//		mCursorAdapter = new SimpleCursorAdapter(activity, R.layout.expense_by_cat_row, null, from, to, 0);
		mCursorAdapter2 = new myCursorAdapter(activity, R.layout.expense_by_cat_row, null, 0);
		
//		categoryListView.setAdapter(mCursorAdapter);
		categoryListView.setAdapter(mCursorAdapter2);
		categoryListView.setOnItemClickListener(new ListView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//	        	if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: listByCategory: setOnItemClick");
	        	Activity activity = getActivity();
	    		Cursor cc = ((CursorAdapter) parent.getAdapter()).getCursor();
	    		cc.moveToPosition(position);
	    		mCategoryName = cc.getString(cc.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME));
	    		getLoaderManager().restartLoader(DETAIL_VIEW, null, ExpenseSummaryFragment2.this);
	    		((ListView)activity.findViewById(R.id.listViewByCategory)).setVisibility(View.INVISIBLE);
	    		activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.INVISIBLE);
	    		activity.findViewById(R.id.expenseListContainer).setVisibility(View.VISIBLE);
	    		// we should not close this cursor since it's not a newly created one. We are picking it from the adapter
	        }
		});

		getLoaderManager().initLoader(AGGREGATE_VIEW, null, this);	   
	}

	private void setupDetailView() {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: setupDetailView");
		Activity activity = getActivity();
		ListView expenseListView = (ListView) activity.findViewById(R.id.listViewByExpense);
		// for the detail view
		String[] from_detail = new String[]{
				DataProvider.KEY_EXPENSE_AMOUNT, 
				DataProvider.KEY_EXPENSE_BANK_NAME,
				DataProvider.KEY_EXPENSE_DAY,
				DataProvider.KEY_EXPENSE_MONTH,
				DataProvider.KEY_EXPENSE_YEAR
			};
	    // and an array of the fields we want to bind those fields to (in this case just label)
	    int[] to_detail = new int[]{R.id.amountText, R.id.bankText, R.id.dayText, R.id.monthText, R.id.yearText};
		mCursorAdapterDetail = new SimpleCursorAdapter(activity.getApplicationContext(), R.layout.expense_row_lite, null, from_detail, to_detail, 0);
	    expenseListView.setAdapter(mCursorAdapterDetail);
		
	    expenseListView.setOnItemClickListener(new ListView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//	        	if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: listByExpense: setOnItemClick");
	    		Cursor cc = ((CursorAdapter) parent.getAdapter()).getCursor();
	    		cc.moveToPosition(position);
	    		long expenseId = cc.getLong(cc.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
	    		Intent intent = new Intent(getActivity().getApplicationContext(), AddExpenseActivity.class);
	    		intent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);
	    		intent.putExtra("callingActivity", ActivityConstants.ExpenseOverViewActivity);
	    		intent.putExtra("categoryConstraint", mCategoryName);
	    		startActivity(intent);
	        }
		});
	    
//	    expenseListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
//			
//			@Override
//			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//				// TODO Auto-generated method stub
////				super.onCreateContextMenu(menu, v, menuInfo);
////		    	menu.add(UNIQUE_FRAGMENT_GROUP_ID, MENU_OPTION_1, 0, R.string.src1);
//		    	menu.add(Menu.NONE, R.id.delete_expense, Menu.NONE, "Delete");
//		    	menu.add(Menu.NONE, R.id.edit_expense, Menu.NONE, "Edit");
//		    	menu.add(Menu.NONE, R.id.go_to_sms, Menu.NONE, "Show related SMS");
//		    	
//			}
//		});
	   
	    registerForContextMenu(expenseListView);
	    
		getLoaderManager().initLoader(DETAIL_VIEW, null, this);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, R.id.delete_expense, Menu.NONE, "Delete");
    	menu.add(Menu.NONE, R.id.edit_expense, Menu.NONE, "Edit");
    	menu.add(Menu.NONE, R.id.go_to_sms, Menu.NONE, "Show related SMS");
	}
	
    public boolean onContextItemSelected(android.view.MenuItem item) {
   	 AdapterView.AdapterContextMenuInfo info = 
   		        (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
   	 	ListView expenseListView = (ListView) getActivity().findViewById(R.id.listViewByExpense);
    	Cursor cc = ((SimpleCursorAdapter)expenseListView.getAdapter()).getCursor();
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
//       								finish();
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
           	} else {
           		Toast.makeText(getActivity().getApplicationContext(), "Could not find the original SMS", Toast.LENGTH_LONG).show();
           	}
           	
           	return true;
       }
       return super.onContextItemSelected(item);
   }
    
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment: onCreateLoader: loader id: " + id);
		CursorLoader cl = null;
		Uri uri;
		int days = getDaysFromTimeFilter();
		String where = null;
		String sortOrder = DataProvider.KEY_EXPENSE_TIME_STAMP + " DESC";
		
		if(id == AGGREGATE_VIEW) { //id 1 belongs to category view
//			if(Log.DEBUG) Log.v("about to initiate bycategory view");
			uri = Uri.parse(DataProvider.EXPENSE_URI + "/groupByCategory");
			uri = Uri.parse(uri + "/" + days);
			cl = new CursorLoader(getActivity(), uri, EXPENSE_PROJECTION_CAT, where, null, null);
		} else if(id == DETAIL_VIEW){
//			if(Log.DEBUG) Log.v("about to initiate bydetail view");
			uri = DataProvider.EXPENSE_URI;
			where =  DataProvider.KEY_EXPENSE_STATE + " LIKE '%saved%'";
			where += " AND " + DataProvider.KEY_EXPENSE_CATEGORY_NAME+"='"+mCategoryName+"'";
			where += " AND expense_date>=date('now','-" + days + " days')";
			cl = new CursorLoader(getActivity(), uri, EXPENSE_PROJECTION, where, null, sortOrder);
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int loaderId = loader.getId();
//		Log.v("ExpenseSummaryFragment2: onLoadFinished: loader id: " + loaderId);
		Activity activity = getActivity();
		
		if(loaderId == AGGREGATE_VIEW) {
//			mCursorAdapter.swapCursor(data);
			mCursorAdapter2.swapCursor(data);
		} else if(loaderId == DETAIL_VIEW) {
			mCategoryNameText2.setText(mCategoryName);
			mCursorAdapterDetail.swapCursor(data);
		}
		
		// Really a poor hack to set the visibility of the aggregate and detail view. I could not find any other way to do it after 2 hours of intense research :( 
		int detailExpenseViewVisibility = getActivity().findViewById(R.id.expenseListContainer).getVisibility();
		if(detailExpenseViewVisibility == View.VISIBLE) {
			((ListView)activity.findViewById(R.id.listViewByCategory)).setVisibility(View.INVISIBLE);
			activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.INVISIBLE);
			activity.findViewById(R.id.expenseListContainer).setVisibility(View.VISIBLE);			
		} else {
			((ListView)activity.findViewById(R.id.listViewByCategory)).setVisibility(View.VISIBLE);
			
			if(Utils.expenseCount(activity) != 0) {
				activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.VISIBLE);
			} else {
				activity.findViewById(R.id.recategorizeExpensesButton).setVisibility(View.INVISIBLE);
			}
				
			activity.findViewById(R.id.expenseListContainer).setVisibility(View.INVISIBLE);
		}
		
		calculateTotalExpense(getDaysFromTimeFilter());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
//		if(Log.DEBUG) Log.v("ExpenseSummaryFragment2: onLoaderReset: loader id: " + loader.getId());
		if(loader.getId() == AGGREGATE_VIEW) {
//			mCursorAdapter.swapCursor(null);
			mCursorAdapter2.swapCursor(null);
		} else if(loader.getId() == DETAIL_VIEW){
			mCursorAdapterDetail.swapCursor(null);
		}
	}
	
	public void onClickScanSms(View view) {
		final ProgressDialog pDialog = ProgressDialog.show(getActivity(), "", "Processsing SMS. Please wait...", true);

		new Thread(new Runnable() {
			public void run() {
				Utils.fileExpenseFromHistory(getActivity());
				// Toast.makeText(
				// ExpenseOverviewActivity.this,
				// "Automatic expense filing done! You can now categorize them.",
				// Toast.LENGTH_LONG).show();
				if (pDialog != null) {
					pDialog.dismiss();
				}
			}
		}).start();
	}
	
	public class myCursorAdapter extends ResourceCursorAdapter {
		Cursor c;
		Context context;
		Activity activity;

		public myCursorAdapter(Context context, int layout, Cursor c, int flags) {
		    super(context, layout, c, flags);
		}

		@Override
		public void bindView(View view , Context context, Cursor cursor){
			TextView amountText = (TextView) view.findViewById(R.id.amountText),
					categoryText = (TextView) view.findViewById(R.id.categoryText);
		    String expenseAmount = null;
			
			if(cursor != null) {
				expenseAmount = Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("sum_amount"))).toString();
				amountText.setText(expenseAmount);
				categoryText.setText(cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME)));
			} else {
//				if(Log.DEBUG) Log.v("Cursor is null");
			}
		}
	}
	
	private class RecategorizeExpensesTask  extends AsyncTask<String, Void, Boolean> {
		Context ctx = getActivity();
        private final ProgressDialog dialog = new ProgressDialog(ctx);
        private int countExpensesCategorized;
        
        // can use UI thread here
        protected void onPreExecute() {
           this.dialog.setMessage("Trying to categorize uncategorized expenses. Please Wait...");
           this.dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {
           countExpensesCategorized = Utils.recategorizeExpenses(ctx);
           return true;
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
           if (this.dialog.isShowing()) {
              this.dialog.dismiss();
           }
           if (success) {
        	  if(countExpensesCategorized > 0) { 
        		  if(countExpensesCategorized == 1) {
        			  Toast.makeText(ctx, countExpensesCategorized + " expense recategorized!", Toast.LENGTH_LONG).show();
        		  } else {
        			  Toast.makeText(ctx, countExpensesCategorized + " expenses recategorized!", Toast.LENGTH_LONG).show();
        		  }
        	  } else {
        		  if(!Utils.haveNetworkConnection(ctx)) {
        			  Toast.makeText(ctx, "Could not categorize any uncategorized expense. Please check if you internet connection in on.", Toast.LENGTH_LONG).show();
        		  } else {
        			  Toast.makeText(ctx, "We reached the end of our super powers. Do let us know through our in app feedback if we are missing something.", Toast.LENGTH_LONG).show();
        		  }
        	  }
           } else {
              Toast.makeText(ctx, "Recategorization failed", Toast.LENGTH_LONG).show();
           }
        }
     }
	
	private class FileExpensesFromHistoryTask  extends AsyncTask<String, Void, Boolean> {
		Context ctx = getActivity();
        private final ProgressDialog dialog = new ProgressDialog(ctx);

        // can use UI thread here
        protected void onPreExecute() {
           this.dialog.setMessage("Scanning past SMS. Please wait...");
           this.dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {
           Utils.fileExpenseFromHistory(ctx);
           return true;
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
           if (this.dialog.isShowing()) {
              this.dialog.dismiss();
           }
           if (success) {
              Toast.makeText(ctx, "SMS scan and expense auto filing complete!", Toast.LENGTH_LONG).show();
           } else {
              Toast.makeText(ctx, "SMS scan and expense auto filing failed", Toast.LENGTH_LONG).show();
           }
        }
     }

}
