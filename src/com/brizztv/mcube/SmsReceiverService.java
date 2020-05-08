package com.brizztv.mcube;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.brizztv.mcube.data.DataProvider;
import com.brizztv.mcube.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class SmsReceiverService extends Service {
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	private Context context;
	private ServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	String mMessageBody, mMessageSender, mExpenseState, mExpenseAmount, 
		mCategoryName=DataProvider.CATEGORY_UNCATEGORIZED, mBankName, mExpenseDate, mNotes;
	long mTimeStamp, mMessageId, mThreadId;
	
	private static final Object mStartingServiceSync = new Object();
	private static PowerManager.WakeLock mStartingService;
	private static int mNotificationId = 0;

	public void onCreate() {
//		if (Log.DEBUG)
//			Log.v("SMSReceiverService: onCreate()");
		HandlerThread thread = new HandlerThread(Log.LOGTAG, Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public void onStart(Intent intent, int startId) {
//		if (Log.DEBUG)
//			Log.v("SMSReceiverService: onStart()");

		// mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;

		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
//		if (Log.DEBUG)
//			Log.v("SMSReceiverService: onDestroy()");
		mServiceLooper.quit();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
//			if (Log.DEBUG)
//				Log.v("SMSReceiverService: handleMessage()");

			int serviceId = msg.arg1;
			Intent intent = (Intent) msg.obj;
			String action = intent.getAction();
//			if (Log.DEBUG)
//				Log.v("In handleMessage() of SmsReceiverService");
			if (ACTION_SMS_RECEIVED.equalsIgnoreCase(action)) {
				if(intent != null) {
					// TODO - BUG FIX - Throws a null pointer exception many a times at this point... don't know why
					handleSmsReceived(intent);
				}
			}

			// NOTE: We MUST not call stopSelf() directly, since we need to
			// make sure the wake lock acquired by AlertReceiver is released.
			finishStartingService(SmsReceiverService.this, serviceId);
		}

		/**
		 * Handle receiving a SMS message
		 */
		private void handleSmsReceived(Intent intent) {
			if(Log.DEBUG) Log.v("SmsReceiverService: handleSmsReceived");
			Bundle bundle = intent.getExtras();
			long expenseId;
			mCategoryName = DataProvider.CATEGORY_UNCATEGORIZED;
			if (bundle != null) {
				SmsMessage[] messages = getMessagesFromIntent(intent);
				if (messages != null) {
					for (int i = 0; i < messages.length; i++) {
						//get the message sender
//						mMessageSender = messages[i].getDisplayOriginatingAddress().toString();
						mMessageSender = messages[i].getOriginatingAddress().toString();
						mMessageBody = messages[i].getMessageBody().toString();
						//try to see if the message is from one of our recognized banks
						mBankName = Utils.getBankFromSenderId(getContentResolver(), mMessageSender);
						//get time stamp
						mTimeStamp = messages[i].getTimestampMillis();
						
						setSmsMessageId();
						//if from recognized bank, make an entry to our expense table with expense state as caught_not_filed
						if (!Utils.isDuplicateSms(mMessageBody, mTimeStamp, getContentResolver()) && Utils.messageIsFromCompany(mMessageSender) && Utils.isDebitMessage(mMessageBody) && (mBankName != null || mMessageSender.equals("111") || mMessageSender.toLowerCase().equals("your") || mMessageSender.toLowerCase().equals("9845787038") || mMessageSender.toLowerCase().equals("+919845787038") || mMessageSender.toLowerCase().equals("+918792643045") || mMessageSender.toLowerCase().equals("+918792643041") || mMessageSender.toLowerCase().equals("Amarendra Sahu".toLowerCase()))) {
//						if (Utils.isDebitMessage(mMessageBody) && (mBankName != null || mMessageSender.equals("111") || mMessageSender.toLowerCase().equals("your") || mMessageSender.toLowerCase().equals("9845787038") || mMessageSender.toLowerCase().equals("+919845787038") || mMessageSender.toLowerCase().equals("Amarendra Sahu".toLowerCase()))) {
//							if (Log.DEBUG)
//								Log.v("SmsReceiverService: handleSmsReceived: Purchase found");
							mExpenseState = "caught_not_filed";
							mExpenseAmount = Utils.getAmount(mMessageBody);
							mCategoryName = Utils.getCategory(getApplicationContext(), mMessageBody, mBankName);
							if(!Utils.categoryExists(getApplicationContext(), mCategoryName)) {
								ContentValues values = new ContentValues();
								values.put(DataProvider.KEY_CATEGORY_NAME, mCategoryName);
								getApplicationContext().getContentResolver().insert(DataProvider.CATEGORY_URI, values);
							}
							
							if(mExpenseAmount != null && mCategoryName != null) {
								if(Utils.isAtmWithdrawal(mMessageBody)) {
									mNotes = "ATM Withdrawal";
									mCategoryName = DataProvider.CATEGORY_ATM_WITHDRAWAL;
								}
								expenseId = saveExpenseFromMessage(messages[i]);
								notifyUser(expenseId, i);
							}
						}
						// TODO - for later releases
						//if the message is not from a recognized bank, but still from some TD, LM like sender, ask the user if it might be an expense 
//						else if (Utils.messageIsFromCompany(mMessageSender) && (mExpenseAmount = Utils.getAmount(mMessageBody)) != null){ 
//							mExpenseState = "partial_not_filed";
////							mExpenseAmount = Utils.getAmount(mMessageBody);
//							expenseId = saveExpenseFromMessage(messages[i]);
//							notifyUser(expenseId);
//						} 
						else {
//							if (Log.DEBUG)
//								Log.v("SmsReceiverService: handleSmsReceived: No purchase found: " + mMessageBody + " . "
//										+ Utils.isDebitMessage(mMessageBody));
						}
					}
				}
			}
		}
		
		private void setSmsMessageId() {
			Uri SMS_INBOX_CONTENT_URI = Uri.parse("content://sms/inbox");
			String WHERE_CONDITION = "type=1 AND body=? AND date > "+mTimeStamp+"-10000";
			String[] selectionArgs = new String[] {mMessageBody};
			
			Cursor cursor = getContentResolver().query(SMS_INBOX_CONTENT_URI, new String[] { "_id", "thread_id", "address", "person", "date", "body" },
					WHERE_CONDITION, selectionArgs, "date desc");
			if(cursor.moveToFirst()) {
				mMessageId = cursor.getLong(0);
				mThreadId = cursor.getLong(1);
//				if(Log.DEBUG) Log.v("Caught the message");
			} else {
				mMessageId = -1;
				mThreadId = -1;
			}
//			if(Log.DEBUG) Log.v("Message id on sms inbox: " + mMessageId + ". Thread id: " + mThreadId);
		}
		
		private long saveExpenseFromMessage(SmsMessage sms) {
			ContentValues values = new ContentValues();
			
			String expenseAmount = Utils.getAmount(mMessageBody);
			long timeStamp = sms.getTimestampMillis();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timeStamp);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			String expenseDate = Integer.toString(year) + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
			
			String notes = null;
			
			values.put(DataProvider.KEY_EXPENSE_AMOUNT, expenseAmount);
			values.put(DataProvider.KEY_EXPENSE_CATEGORY_NAME, mCategoryName);
			values.put(DataProvider.KEY_EXPENSE_BANK_NAME, mBankName);
			values.put(DataProvider.KEY_EXPENSE_DAY, day);
			values.put(DataProvider.KEY_EXPENSE_MONTH, month + 1);
			values.put(DataProvider.KEY_EXPENSE_YEAR, year);
			values.put(DataProvider.KEY_EXPENSE_NOTES, notes);
			values.put(DataProvider.KEY_EXPENSE_DATE, expenseDate);
			values.put(DataProvider.KEY_EXPENSE_SMS_BODY, sms.getMessageBody());
			values.put(DataProvider.KEY_EXPENSE_TIME_STAMP, timeStamp);
			values.put(DataProvider.KEY_EXPENSE_STATE, mExpenseState);
			values.put(DataProvider.KEY_EXPENSE_SENDER_ID, mMessageSender);
			values.put(DataProvider.KEY_EXPENSE_NOTES, mNotes);
			values.put(DataProvider.KEY_EXPENSE_SMS_ID, mMessageId);
			values.put(DataProvider.KEY_EXPENSE_SMS_THREAD_ID, mThreadId);
			
			Uri uri = getContentResolver().insert(DataProvider.EXPENSE_URI, values);
//			if(Log.DEBUG) Log.v("SmsReceiverService: saveExpenseFromMessage: expenseId" + Long.valueOf(uri.getLastPathSegment()));
			return Long.valueOf(uri.getLastPathSegment());
		}
		
		/*
		 * shows the notification on the status bar TODO - notification alerts
		 * can vary. vibration or not. led light or not etc. add those.
		 */
		private void notifyOnStatusBar(String amount, String expenseDate, long expenseId) {
			Context context = getApplicationContext();
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

			int icon = R.drawable.ic_launcher_mcube;
			CharSequence tickerText = "New Expense";
			long when = System.currentTimeMillis();

			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

			CharSequence contentTitle = "New expense";
			CharSequence contentText = "You spent Rs. " + amount + " on " + expenseDate + ". File it now.";

			Intent notificationIntent = new Intent(SmsReceiverService.this, AddExpenseActivity.class);
			notificationIntent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);
			PendingIntent contentIntent = PendingIntent.getActivity(SmsReceiverService.this, 0, notificationIntent, 0);

			notificationBuilder.setContentIntent(contentIntent).setContentTitle(contentTitle).setContentText(contentText).setSmallIcon(icon)
					.setWhen(when).setTicker(tickerText);
			Notification notification = notificationBuilder.getNotification();

			// add vibration on notification
			// TODO - for future, get vibration pattern from user preference and
			// use notification.vibrate to set it
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			// add flashing LEDS!!
			// TODO - for future, get LED flash pref and pattern from user
			// preference and use notification.vibrate to set it
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			// to automatically cancel the notification after it is selected
			// from the notifications window
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// notification.setLatestEventInfo(context, contentTitle,
			// contentText, contentIntent);
			mNotificationId = (mNotificationId+1)%50;
			mNotificationManager.notify(mNotificationId++, notification);
		}

		/*
		 * will show the pop up notification if the user so desires (we think, it's best for them)
		 */
		private void popupNotification(long expenseId) {
			// TODO why not just use AlertDialog.Builder as we have done for
			// addExpense to show this pop up?
			Intent intent = new Intent(getBaseContext(), SmsExpenserPopupActivity.class);
			intent.putExtra(DataProvider.KEY_EXPENSE_ID, expenseId);

			// ManageWakeLock.acquireFull(context);
			// this is really important. To get an activity started from within
			// a service. don't know why.
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(intent);
		}

		private void notifyUser(long expenseId, int messageNumber) {
//			if(Log.DEBUG) Log.v("SmsExpenserService: notifyUser");
			// Fetch call state, if the user is in a call or the phone is
			// ringing we don't want to show the popup
			TelephonyManager mTM = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			boolean callStateIdle = mTM.getCallState() == TelephonyManager.CALL_STATE_IDLE;

			// get the expense date from the time stamp
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");			 
			Date resultdate = new Date(mTimeStamp);
			String expenseDate = sdf.format(resultdate);
			
			//get the user preference for pop up notification
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Boolean showPopUp = sharedPref.getBoolean("pref_pop_up", true);

//			if(Log.DEBUG) Log.v("SmsReceiverService: notifyUser: Popup activity running state: " + SmsExpenserPopupActivity.isRunning);
			// if either the user is on call or has turned on the preference for popup, use the notification bar
			if (!callStateIdle || showPopUp == false || messageNumber > 0 || SmsExpenserPopupActivity.isRunning) {
				notifyOnStatusBar(mExpenseAmount, expenseDate, expenseId);
			} else {
				popupNotification(expenseId);
			}
		}

		/**
		 * Read the PDUs out of an {@link #SMS_RECEIVED_ACTION} or a
		 * {@link #DATA_SMS_RECEIVED_ACTION} intent.
		 * 
		 * @param intent
		 *            the intent to read from
		 * @return an array of SmsMessages for the PDUs
		 */
		private final SmsMessage[] getMessagesFromIntent(Intent intent) {
			Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
			if (messages == null) {
				return null;
			}
			if (messages.length == 0) {
				return null;
			}

			byte[][] pduObjs = new byte[messages.length][];

			for (int i = 0; i < messages.length; i++) {
				pduObjs[i] = (byte[]) messages[i];
			}
			byte[][] pdus = new byte[pduObjs.length][];
			int pduCount = pdus.length;
			SmsMessage[] msgs = new SmsMessage[pduCount];
			for (int i = 0; i < pduCount; i++) {
				pdus[i] = pduObjs[i];
				msgs[i] = SmsMessage.createFromPdu(pdus[i]);
			}
			return msgs;
		}
	}

	/**
	 * Start the service to process the current event notifications, acquiring
	 * the wake lock before returning to ensure that the service will run.
	 */
	public static void beginStartingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
//			if (Log.DEBUG)
//				Log.v("SMSReceiverService: beginStartingService()");
			if (mStartingService == null) {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				// mStartingService =
				// pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						Log.LOGTAG + ".SmsReceiverService");
				mStartingService.setReferenceCounted(false);
			}
			mStartingService.acquire();
			context.startService(intent);
		}
	}

	/**
	 * Called back by the service when it has finished processing notifications,
	 * releasing the wake lock if the service is now stopping.
	 */
	public static void finishStartingService(Service service, int startId) {
		synchronized (mStartingServiceSync) {
//			if (Log.DEBUG)
//				Log.v("SMSReceiverService: finishStartingService()");
			if (mStartingService != null) {
				if (service.stopSelfResult(startId)) {
					mStartingService.release();
				}
			}
		}
	}

}
