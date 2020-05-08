package com.brizztv.mcube;

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
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

public class CategoryActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int ACTIVITY_ADD_CATEGORY = 0;
	SimpleCursorAdapter mCursorAdapter;
	static final String[] CATEGORY_PROJECTION = new String[] { DataProvider.KEY_CATEGORY_ID, DataProvider.KEY_CATEGORY_NAME, };
	private String categorySortOrder = DataProvider.KEY_CATEGORY_NAME + " ASC";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (Log.DEBUG)
		// Log.v("AddCategoryActivity: onCreate");

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		setUpList();
	}

	private void setUpList() {
		setContentView(R.layout.list_category);

		// Find the ListView resource
		ListView categoryListView = (ListView) findViewById(R.id.listCategory);

		// accordingly query dataprovider
		String[] from = new String[] { DataProvider.KEY_CATEGORY_NAME };

		// and an array of the fields we want to bind those fields to (in this
		// case just label)
		int[] to = new int[] { android.R.id.text1 };

		getSupportLoaderManager().initLoader(0, null, this);

		mCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, from, to, 0);
		categoryListView.setAdapter(mCursorAdapter);
		registerForContextMenu(categoryListView);
	}

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.category_list, menu);
		return true;
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
		case R.id.add_category:
			intent = new Intent(this, AddCategoryActivity.class);
			startActivityForResult(intent, ACTIVITY_ADD_CATEGORY);
			return true;
		default:
			toast = Toast.makeText(this, "You clicked on a menu item", Toast.LENGTH_SHORT);
			toast.show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(this, DataProvider.CATEGORY_URI, CATEGORY_PROJECTION, null, null, categorySortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		mCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mCursorAdapter.swapCursor(null);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// menu.add(UNIQUE_FRAGMENT_GROUP_ID, MENU_OPTION_1, 0, R.string.src1);
		menu.add(Menu.NONE, R.id.delete_expense, Menu.NONE, "Delete");
		menu.add(Menu.NONE, R.id.edit_expense, Menu.NONE, "Edit");
		// menu.add(Menu.NONE, R.id.go_to_sms, Menu.NONE, "Go to SMS");
		// MenuInflater inflater = getSupportMenuInflater();
		// inflater.inflate(R.menu.context_menu, menu);
	}

	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		// Cursor cc =
		// ((SimpleCursorAdapter)getListView().getAdapter()).getCursor();
		Cursor cc = mCursorAdapter.getCursor();
		cc.moveToPosition(info.position);

		long categoryId = cc.getLong(cc.getColumnIndexOrThrow(DataProvider.KEY_CATEGORY_ID));
		String categoryName = cc.getString(cc.getColumnIndexOrThrow(DataProvider.KEY_CATEGORY_NAME));

		switch (item.getItemId()) {
		case R.id.delete_expense:
			if(Utils.isBasicCategory(categoryName)) {
				new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Can't do this")
				.setMessage("Sorry, can't delete pre loaded categories :(")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
			} else {
				getContentResolver().delete(DataProvider.CATEGORY_URI, DataProvider.KEY_CATEGORY_ID + "=" + categoryId, null);
				Toast.makeText(this, "Category Deleted", Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.edit_expense:
			if(Utils.isBasicCategory(categoryName)) {
				new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Can't do this")
				.setMessage("Sorry, can't edit pre loaded categories :(")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
			} else {
				Intent intent = new Intent(this, AddCategoryActivity.class);
				intent.putExtra(DataProvider.KEY_CATEGORY_ID, categoryId);
				startActivity(intent);
			}
			return true;
		case R.id.go_to_sms:
			// Intent defineIntent = new Intent(Intent.ACTION_VIEW);
			// defineIntent.setData(Uri.parse("content://mms-sms/conversations/"+threadId));
			// myActivity.startActivity(defineIntent);
			return true;
		}
		return super.onContextItemSelected(item);
	}

}
