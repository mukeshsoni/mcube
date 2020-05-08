package com.brizztv.mcube;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brizztv.mcube.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;

public class FeedbackActivity extends SherlockFragmentActivity {
	ProgressDialog progressDialog;
	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Bundle extras = msg.getData();
			progressDialog.dismiss();
			new AlertDialog.Builder(FeedbackActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("")
			.setMessage(extras.getString("ourFeedback"))
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		setContentView(R.layout.feedback);
	}

	// @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, ExpenseOverviewActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.send_feedback:
			sendFeedback();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_feedback, menu);
		return true;
	}

	public void onClickSendFeedback(View view) {
		sendFeedback();
	}

	private void sendFeedback() {
		final EditText feedbackText = (EditText) findViewById(R.id.feedbackEditText);
		final EditText emailText = (EditText) findViewById(R.id.emailText);
		if (feedbackText.getText().toString().trim().equals("")) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("No feedback entered")
					.setMessage("Feedback box is empty. Please enter something")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();
		} else if (emailText.getText().toString().trim().equals("")){
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Email required")
			.setMessage("Please enter your email so that we get back to you.")
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
		}else if (!Utils.haveNetworkConnection(this)) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("No network connnection")
					.setMessage("Seems you are not conencted to internet. Please enable internet connection to send feedback.")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();
		} else {
			progressDialog = ProgressDialog.show(this, "", "Sending Feedback...");
			new Thread(new Runnable() {
				public void run() {
					Bundle extras = new Bundle();
					Message msg = new Message();
					TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					// encrypt the device id before sending to the server
					String deviceId = Utils.md5(telephonyManager.getDeviceId());
					
					// don't know why i am doing the network double check
					if (Utils.haveNetworkConnection(getApplicationContext())) {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
						// Add your data
						nameValuePairs.add(new BasicNameValuePair("expense_state", "feedback"));
						nameValuePairs.add(new BasicNameValuePair("feedback", feedbackText.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("email", emailText.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
						
						// TODO - need to create a handler for communication
						// between this thread and the caller activity
						 if(Utils.postData(nameValuePairs)) {
							 extras.putString("ourFeedback", "Thank you for your feedback. We will surely look into it.");
							 msg.setData(extras);
						 } else {
							 extras.putString("ourFeedback", "Sorry, could not post your feedback at this moment. Please try again later.");
							 msg.setData(extras);
						 }
						 messageHandler.sendMessage(msg);
					}
				}
			}).start();
		}
	}
}
