package com.brizztv.mcube;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class TutorialActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    /**
     * Processes splash screen touch events
     */
    @Override
    public boolean onTouchEvent(MotionEvent evt)
    {
//    	if(Log.DEBUG) Log.v("Touch x coordinate: " + evt.getX() + " y coordinate: " + evt.getY());
    	finish();
        return true;
    }   
    
	public void onClickTakeMeToTheApp(View view) {
		finish();
	}

}
