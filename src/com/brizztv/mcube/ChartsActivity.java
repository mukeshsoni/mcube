package com.brizztv.mcube;

import java.text.DecimalFormat;
import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brizztv.mcube.data.DataProvider;

public class ChartsActivity extends SherlockFragmentActivity {
	static final String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_AMOUNT,
		DataProvider.KEY_EXPENSE_BANK_NAME, DataProvider.KEY_EXPENSE_MERCHANT, 
		DataProvider.KEY_EXPENSE_LOCATION, DataProvider.KEY_EXPENSE_NOTES,
		DataProvider.KEY_EXPENSE_CATEGORY_NAME, DataProvider.KEY_EXPENSE_DAY, 
		DataProvider.KEY_EXPENSE_MONTH, DataProvider.KEY_EXPENSE_YEAR,
		DataProvider.KEY_EXPENSE_DATE, DataProvider.KEY_EXPENSE_STATE,
		DataProvider.KEY_EXPENSE_TIME_STAMP};
	
	static final String[] EXPENSE_PROJECTION_CAT = new String[] { 
		DataProvider.KEY_EXPENSE_ID, 
		DataProvider.KEY_EXPENSE_BANK_NAME, 
		DataProvider.KEY_EXPENSE_CATEGORY_NAME,
		DataProvider.KEY_EXPENSE_DATE,
		DataProvider.KEY_EXPENSE_STATE,
		"sum_amount" };
	
	private Spinner timeFilterSpinner;
	private String mTimeFilter = null;
	GraphicalView mChartView = null;
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private CategorySeries mSeries = new CategorySeries("Expenses");
	private static int[] COLORS = new int[] { Color.parseColor("#d19190"), Color.parseColor("#6785c2"), Color.parseColor("#ffb492"), 
									Color.parseColor("#676770"), Color.parseColor("#c37cc0"), Color.parseColor("#67c267"), 
									Color.parseColor("#c1b7c7"), Color.YELLOW, Color.RED, Color.BLACK};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (Log.DEBUG)
		// Log.v("AddExpenseActivity: onCreate");
		setContentView(R.layout.charts);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		setupViews();
	}
	
	private void setupViews() {
		setupTimeFilter();
		setupChartsView();
	}

	private void setupChartsView() {
		// setup some global values for the chart UI
		mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.WHITE);
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(15);
	    mRenderer.setLegendTextSize(15);
	    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
	    mRenderer.setZoomButtonsVisible(true);
	    mRenderer.setStartAngle(90);
	    mRenderer.setLabelsColor(Color.BLACK);
	    mRenderer.setShowLegend(false);
	    mRenderer.setShowAxes(true);
	    mRenderer.setShowGrid(true);
	    mRenderer.setChartTitle("");
	    
	    // DO NOT DELETE! - for later, when we make the chart clickable 
//	    mRenderer.setClickEnabled(true);
//	    mRenderer.setSelectableBuffer(10);
	    
	    // setup the data for the chart
		setChartData();
		
		// DO NOT DELETE! - for later, when we make the chart clickable
//		mChartView.setOnClickListener(new View.OnClickListener() {
//		    @Override
//		    public void onClick(View v) {
//		      SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
//		      if (seriesSelection == null) {
//		        Toast
//		            .makeText(PieChartBuilder.this, "No chart element was clicked",
//		      Toast.LENGTH_SHORT)
//		            .show();
//		      } else {
//		        Toast.makeText(
//		            PieChartBuilder.this,
//		            "Chart element data point index " + seriesSelection.getPointIndex()
//		                + " was clicked" + " point value=" + seriesSelection.getValue(),
//		            Toast.LENGTH_SHORT).show();
//		      }
//		    }
//		  });
		
		// now add the chart to our layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, findViewById(R.id.timeSortSpinnerCharts).getId());
		mChartView.setLayoutParams(params);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.chartsRelativeLayout);
		layout.addView(mChartView);
	}
	
	private void setupTimeFilter() {
		// Setup filter spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.expense_filter,
				R.layout.time_filter_spinner_row);
//				android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeFilterSpinner = (Spinner) findViewById(R.id.timeSortSpinnerCharts);
		timeFilterSpinner.setAdapter(adapter);

		timeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Object item = parent.getItemAtPosition(pos);
				mTimeFilter = item.toString();
				redrawChart();
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	private void redrawChart() {
		// clear the current values
		mSeries.clear();
		// populate series with new values according to the selected time filter
		setChartData();
		// repaint the chart
		mChartView.repaint();
	}
	
	/*
	 * Handles menu item clicks
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	// @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, ExpenseOverviewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
//			toast = Toast.makeText(this, "You clicked on a menu item", Toast.LENGTH_SHORT);
//			toast.show();
		}
		return super.onOptionsItemSelected(item);
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
	
	private void setChartData() {
		int days = getDaysFromTimeFilter();
		Uri uri = Uri.parse(DataProvider.EXPENSE_URI + "/groupByCategory");
		uri = Uri.parse(uri + "/" + days);
		Cursor cursor = getContentResolver().query(uri, EXPENSE_PROJECTION_CAT, null, null, null);
		int count = cursor.getCount();
		double[] values = new double[count];
		double total=0;
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		int i;
		
		String[] categoryNames = new String[count];
		
		for(i = 0; i < count; i++) {
			cursor.moveToNext();
			values[i] = cursor.getDouble(2);
			total += values[i];
			categoryNames[i] = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME));
			// If we have more than 9 categories, club all other values into a single value
			if(i >= 9) {
				values[9] += values[i];
				categoryNames[9] = "Others";
			}
		}
		
		// calculate percentage share of each category and append to category labels
		for(i=0; i<count; i++) {
			categoryNames[i] += " - " + twoDForm.format(values[i]*100/total) + "%";
		}
		
		i = 0;	
		// Add the category and their respective values to the chart
		for(double value: values) {
			// we are not going to add more than 10 categories. The 10th category will be called "Others" 
			if(i > 9) break;
//			if(Log.DEBUG) Log.v(categoryNames[i] + ": " + value);
			mSeries.add(categoryNames[i], value);
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
	        renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
	        renderer.setDisplayChartValues(true);
	        renderer.setChartValuesTextSize(15);
	        renderer.setChartValuesSpacing(10);
	        renderer.setChartValuesTextAlign(Align.RIGHT);
	        mRenderer.addSeriesRenderer(renderer);

	        i++;
		}
		
		if(mChartView == null) {
			mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
		}
	}
}
