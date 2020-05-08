package com.brizztv.mcube;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

public class AddCategoryActivity extends SherlockFragmentActivity {
	private EditText categoryEditText;
	static final String[] CATEGORY_PROJECTION = new String[] { DataProvider.KEY_CATEGORY_ID, DataProvider.KEY_CATEGORY_NAME, DataProvider.KEY_CATEGORY_CREATED};
	Long mCategoryId = null;
	String mCategoryName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (Log.DEBUG)
		// Log.v("AddExpenseActivity: onCreate");
		setContentView(R.layout.add_category);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		Bundle extras = getIntent().getExtras();
		// in case of editing an existing category
		if (getIntent().hasExtra(DataProvider.KEY_CATEGORY_ID)) {
			mCategoryId = extras.getLong(DataProvider.KEY_CATEGORY_ID);
			String where = DataProvider.KEY_CATEGORY_ID + "=" + mCategoryId;
			Cursor c = getContentResolver().query(DataProvider.CATEGORY_URI, CATEGORY_PROJECTION, where, null, null);
			c.moveToFirst();
			mCategoryName = c.getString(c.getColumnIndexOrThrow(DataProvider.KEY_CATEGORY_NAME));
			categoryEditText = (EditText) findViewById(R.id.categoryEditText);
			categoryEditText.setText(mCategoryName);
			c.close();
		}
	}

	public void onClickSaveCategory(View view) {
		addCategory();
	}

	private void addCategory() {
		categoryEditText = (EditText) findViewById(R.id.categoryEditText);
		String categoryName = categoryEditText.getText().toString();
		String where = "upper(" + DataProvider.KEY_CATEGORY_NAME + ")=upper(?)";
		String[] selectionArgs = new String[] {categoryName};
		Long otherCatId=null;
		
		ContentValues values = new ContentValues();
		ContentResolver cr = getContentResolver();
		Intent intent = new Intent(this, AddExpenseActivity.class);
		
		mCategoryName = categoryEditText.getText().toString();
		values.put(DataProvider.KEY_CATEGORY_NAME, mCategoryName);
		
		Cursor c = cr.query(DataProvider.CATEGORY_URI, CATEGORY_PROJECTION, where, selectionArgs, null);
		int rowCount = c.getCount();
		if(c.moveToNext()) {
			otherCatId = c.getLong(c.getColumnIndexOrThrow(DataProvider.KEY_CATEGORY_ID));
		}
		
		if(mCategoryName == null || mCategoryName.equals("")) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Blank text box")
			.setMessage("Please enter something").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		} else if(rowCount > 0 && (mCategoryId == null || otherCatId != mCategoryId)) { 
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Duplicate entry")
			.setMessage("Category " + categoryName + " already exists").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		}
		// if we have a category id, then update existing category record
		else if(mCategoryId != null) {
			if(c.getCount() > 1) {
			} else {
				cr.update(DataProvider.CATEGORY_URI, values, DataProvider.KEY_CATEGORY_ID + "=" + mCategoryId, null);
				intent.putExtra(DataProvider.KEY_CATEGORY_NAME, mCategoryName);
				this.setResult(Activity.RESULT_OK, intent);
				Toast.makeText(this, "Category " + mCategoryName + " saved", Toast.LENGTH_SHORT).show();
				finish();
			}
			
		} else {
			// TODO - try catching by the duplicate entry try by setting category
			// name to unique and catching the exception thrown when trying to make
			// a database entry
			cr.insert(DataProvider.CATEGORY_URI, values);
			intent.putExtra(DataProvider.KEY_CATEGORY_NAME, mCategoryName);
			this.setResult(Activity.RESULT_OK, intent);
			c.close();
			Toast.makeText(this, "Category " + mCategoryName + " saved", Toast.LENGTH_SHORT).show();
			finish();
		}
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
		default:
			toast = Toast.makeText(this, "You clicked on a menu item", Toast.LENGTH_SHORT);
			toast.show();
		}
		return super.onOptionsItemSelected(item);
	}

}
