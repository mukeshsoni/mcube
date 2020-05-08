package com.brizztv.mcube;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.brizztv.mcube.data.DataProvider;

public class Utils {
	static final String[] CATEGORY_PROJECTION = new String[] { DataProvider.KEY_CATEGORY_ID, DataProvider.KEY_CATEGORY_NAME,
			DataProvider.KEY_CATEGORY_CREATED };
	
	static final String[] SMS_CATEGORY_MAPPING_PROJECTION = new String[] { DataProvider.KEY_SMS_CAT_MAP_ID, DataProvider.KEY_SMS_CAT_MAP_SERVER_ID, 
			DataProvider.KEY_SMS_CAT_MAP_BANK, DataProvider.KEY_SMS_CAT_MAP_MERCHANT, DataProvider.KEY_SMS_CAT_MAP_CATEGORY, 
			DataProvider.KEY_SMS_CAT_MAP_NOTES, DataProvider.KEY_SMS_CAT_MAP_LOCAL_RULE, DataProvider.KEY_SMS_CAT_MAP_PRIORITY };

	static final String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_AMOUNT,
			DataProvider.KEY_EXPENSE_BANK_NAME, DataProvider.KEY_EXPENSE_DATE, DataProvider.KEY_EXPENSE_LOCATION,
			DataProvider.KEY_EXPENSE_MERCHANT, DataProvider.KEY_EXPENSE_NOTES, DataProvider.KEY_EXPENSE_CATEGORY_NAME,
			DataProvider.KEY_EXPENSE_DAY, DataProvider.KEY_EXPENSE_MONTH, DataProvider.KEY_EXPENSE_YEAR, DataProvider.KEY_EXPENSE_STATE,
			DataProvider.KEY_EXPENSE_SMS_BODY };

	public static boolean isDebitMessage(String messageBody) {
		/*
		 * ICICI -> ATM Withdrawal -> Your account xxxxx888888 is debited with
		 * INR4,1212.00 12 aug. Info:NFS*CASH WDL*12-09-12. Total Avbl. Bal is
		 * INR123.909.
		 * 
		 * HDFC Credit Card -> Card purchase -> Thank you for using your HDFC
		 * bank credit card ending 9011 for Rs.1234.11 in GURGAON at IBIBO2121
		 * on 2012-09-12: 12:33:01. Debit Card -> Thank you for using your
		 * 
		 * HDFC Bank Debit/ATM Card ending 5622 for Rs.1000.00 towards ATM WDL
		 * in GBANGALORE at on 2012-09-12: 12:33:01. Net Banking online transfer
		 * -> An amount of Rs.2,000.00 has been debited from your account number
		 * xxxxx23232 for TPT txn done using HDFC Bank Net banking
		 * 
		 * HSBC ATM Withdrawal - Your account 123-421***-002 has been debited
		 * with INR 300.11 on 03AUG as cash withdrawal Credit card purchase -
		 * Thank you for using your
		 * 
		 * HSBC Credit Card No. xxxxx6636 for INR 333.00 on 18/08/12 StanC ->
		 * 'Dear Customer, your account 444****1212 has been debited on 22/02/12
		 * for INR 200.12 towards purchase of your debit card -
		 * 
		 * stanchart
		 * 
		 * Citi Bank -> ATM Withdrawal -> Rs. xxx was withdrawn at an ATM from
		 * a/c xx9999. The balance is now rs xxxx. We invite you to one of
		 * citibank's blah blue online payment -> airtel-broadband telephones
		 * bill for 21312121 of Rs.1600.00 has been processed successfully on
		 * your citibank card/account xxxxxx1234. Thank you. SBI -> Credit Card
		 * -> Transaction of Rs.1,144.00 made on SBI Credit Card XX2341 at MANGO
		 * on 29 Jul 12.
		 */
		// if (Log.DEBUG)
		// Log.v("Utils: isDebitMessage: Message body: " + messageBody);
		return messageBody.matches(".*[Dd]ebited.*") || messageBody.matches(".*[Ss]pent.*")
				|| messageBody.matches(".*using\\syour\\s[Hh][Dd][Ff][Cc]\\s[Bb]ank.*")
				|| (messageBody.toLowerCase().indexOf("using your HSBC Credit Card".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("using your HSBC Debit Card".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("using your StanChart Credit Card".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("using your StanChart Debit Card".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("using StanChart Credit card".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("using StanChart Debit card".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("Debit card purchase".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("credit card purchase".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("towards purchase".toLowerCase()) >= 0)
				|| messageBody.matches("(.*)using your HSBC Credit Card(.*)")
				|| (messageBody.toLowerCase().indexOf("was withdrawn at an ATM".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("processed successfully on your Citibank".toLowerCase()) >= 0)
				|| (messageBody.toLowerCase().indexOf("transaction of".toLowerCase()) >= 0) 
				|| messageBody.matches(".*using your HSBC Debit Card.*")
				|| (messageBody.toLowerCase().indexOf("tranx of USD".toLowerCase()) >= 0);
	}

	public static String getAmount(String messageBody) {
		// regular expression to find substrings like INR2,000 or INR 2,000 or
		// Inr 2,000
		Pattern patternINR = Pattern.compile("\\s?[Ii][Nn][Rr]\\.?\\s*\\d+.*\\s*");
		// some banks might send Rs/Rs. instead of INR
		Pattern patternRs = Pattern.compile("\\s?[Rr][Ss]\\.?\\s*\\d+.*\\s*");
		Pattern patternUSD = Pattern.compile("\\s?[Uu][Ss][Dd]\\s*\\.?\\d+.*\\s*");
		Pattern patternEUR = Pattern.compile("\\s?[Ee][Uu][Rr]\\s*\\.?\\d+.*\\s*");
		
		Matcher matcherINR, matcherRs, matcherUSD, matcherEUR;

		matcherINR = patternINR.matcher(messageBody);
		matcherRs = patternRs.matcher(messageBody);
		matcherUSD = patternUSD.matcher(messageBody);
		matcherEUR = patternEUR.matcher(messageBody);
		
		if(matcherUSD.find()) {
			String usd = trimLeadingChars(matcherUSD.group(0).trim().split("[Uu][Ss][Dd]\\s*")[1].split("\\s")[0].replace(",", ""), '.');
//			if(Log.DEBUG) Log.v("Utils: getAmount: USD: " + usd);
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			Double usdToInr = Double.valueOf(usd)*55;
			return twoDForm.format(usdToInr);			
		}  if(matcherEUR.find()) {
			String euro = trimLeadingChars(matcherEUR.group(0).trim().split("[Ee][Uu][Rr]\\s*")[1].split("\\s")[0].replace(",", ""), '.');
//			if(Log.DEBUG) Log.v("Utils: getAmount: EUR: " + euro);
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			Double euroToInr = Double.valueOf(euro)*69;
			return twoDForm.format(euroToInr);
		} else if (matcherINR.find()) {
			return trimLeadingChars(matcherINR.group(0).trim().split("[Ii][Nn][Rr]\\.?\\s*")[1].split("\\s")[0].replace(",", ""), '.');

		} else if (matcherRs.find()) {
			return trimLeadingChars(matcherRs.group(0).trim().split("[Rr][Ss]\\.?\\s*")[1].split("\\s")[0].replace(",", ""), '.');
		}
		// TODO - what if we are returning null?
		return null;
	}

	private static String trimLeadingChars(String inputString, char ch) {
		int count = inputString.length();
		int len = inputString.length();
		int st = 0;
		int off = 0;
		char[] val = inputString.toCharArray();

		// while ((st < len) && (val[off + st] <= ch)) {
		// st++;
		// }
		while ((st < len) && (val[off + len - 1] <= ch)) {
			len--;
		}
		// if(Log.DEBUG) Log.v("Input String: " + inputString +
		// ". Returned String: " + inputString.substring(st, len));
		return ((st > 0) || (len < count)) ? inputString.substring(st, len) : inputString;
	}

	public static String getBankFromSenderId(ContentResolver cr, String senderAddress) {
		final String[] BANK_PROJECTION = new String[] { DataProvider.KEY_BANK_ID, DataProvider.KEY_BANK_NAME, DataProvider.KEY_BANK_SENDER_ID,
				DataProvider.KEY_BANK_DEFAULT };
		int i;
		String where;
		String bankSenderId;
		String bankName = null;

		// where =
		// "'%"+senderAddress.toLowerCase(Locale.US)+"%' like lower("+DataProvider.KEY_BANK_SENDER_ID+")";
		where = null;
		// get the banks into our cursor from the bank uri
		Cursor cursor = cr.query(DataProvider.BANK_URI, BANK_PROJECTION, where, null, null);

		// Do the below work by adding a where clause in the query... much
		// faster and convenient
		int count = cursor.getCount();
		for (i = 0; i < count; i++) {
			cursor.moveToNext();
			bankSenderId = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_BANK_SENDER_ID));
			if (senderAddress.toLowerCase(Locale.US).indexOf(bankSenderId.toLowerCase()) >= 0) {
				bankName = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_BANK_NAME));
				break;
			}
		}
		return bankName;
	}

	public static boolean haveNetworkConnection(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

	public static void pingUnauditedExpenses(ContentResolver cr) {
		int i;

		Cursor cursor = cr.query(Uri.parse(DataProvider.EXPENSE_URI + "/unauditedExpenses"), EXPENSE_PROJECTION, null, null, null);
		String where = null;
		String expenseAmount, expenseDate, bankName, categoryName, merchant, location, deviceId, phoneModel, osVersion, email, phoneNumber, expenseState, smsBody, senderId;
		Long expenseId;

		int count = cursor.getCount();
//		if (Log.DEBUG)
//			Log.v("Utils: pingUnauditedExpenses: Record count: " + count);
		for (i = 0; i < count; i++) {
			cursor.moveToNext();

			expenseId = cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
			expenseAmount = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_AMOUNT));
			expenseDate = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_DATE));
			bankName = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_BANK_NAME));
			categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME));
			merchant = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_MERCHANT));
			location = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_LOCATION));
			expenseState = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_STATE));
			smsBody = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SMS_BODY));
			senderId = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SENDER_ID));
//			if (Log.DEBUG)
//				Log.v("Sms body: " + smsBody);
			deviceId = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_USER_PROFILE_DEVICE_ID));
			phoneModel = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_USER_PROFILE_PHONE_MODEL));
			osVersion = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_USER_PROFILE_OS_VERSION));
			email = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_USER_PROFILE_EMAIL));
			phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_USER_PROFILE_PHONE_NUMBER));

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
			// Add your data
			nameValuePairs.add(new BasicNameValuePair("user_expense_id", expenseId.toString()));
			nameValuePairs.add(new BasicNameValuePair("amount", expenseAmount));
			nameValuePairs.add(new BasicNameValuePair("category", categoryName));
			nameValuePairs.add(new BasicNameValuePair("bank", bankName));
			nameValuePairs.add(new BasicNameValuePair("date", expenseDate));
			nameValuePairs.add(new BasicNameValuePair("expense_state", expenseState));
			nameValuePairs.add(new BasicNameValuePair("sms_body", smsBody));
			nameValuePairs.add(new BasicNameValuePair("sender_id", senderId));
			nameValuePairs.add(new BasicNameValuePair("phone_model", phoneModel));
			nameValuePairs.add(new BasicNameValuePair("os_version", osVersion));
			nameValuePairs.add(new BasicNameValuePair("email", email));
			nameValuePairs.add(new BasicNameValuePair("phone_number", phoneNumber));
			nameValuePairs.add(new BasicNameValuePair("merchant", merchant));
			nameValuePairs.add(new BasicNameValuePair("location", location));
			nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));

			if (Utils.postData(nameValuePairs)) {
				// change audited value in the expense
				// if (Log.DEBUG)
				// Log.v("About to update audited status of expense with id: " +
				// cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID)));
				ContentValues values = new ContentValues();
				values.put(DataProvider.KEY_EXPENSE_AUDITED, 1);
				where = DataProvider.KEY_EXPENSE_ID + "=" + cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
				cr.update(DataProvider.EXPENSE_URI, values, where, null);
			}
		}
		cursor.close();
	}

	public static boolean postData(List<NameValuePair> nameValuePairs) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.examontv.com/smsExpenserDump/create");
		Boolean recordSaved = false;

		try {
			// set post data
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();

			JSONTokener tokener = new JSONTokener(json);

			// TODO - fix the casting bug...
			// java.lang.ClassCastException: java.lang.String
			// at com.brizztv.mcube.Utils.postData(Utils.java:251)
			// at com.brizztv.mcube.Utils.pingUnauditedExpenses(Utils.java:222)
			// at com.brizztv.mcube.PingerService$1.run(PingerService.java:21)
			// at java.lang.Thread.run(Thread.java:1019)
			JSONObject jsonObj = (JSONObject) tokener.nextValue();

			recordSaved = !jsonObj.getBoolean("error");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
//			if (Log.DEBUG)
//				Log.v("Problem in decoding json");
			e.printStackTrace();
		}
		return recordSaved;
	}

	public static boolean messageIsFromCompany(String messageSender) {
		// return ((messageSender.length() == 9 && messageSender.indexOf("-") ==
		// 2) || messageSender.equals("your") || messageSender.equals("111") ||
		// messageSender.toLowerCase().equals("+919845787038") ||
		// messageSender.toLowerCase().equals("+918792643045") ||
		// messageSender.toLowerCase().equals("+918792643041"));
		return (messageSender.indexOf("-") == 2 || messageSender.equals("your") || messageSender.equals("111")
				|| messageSender.toLowerCase().equals("+919845787038") || messageSender.toLowerCase().equals("+918792643045") || messageSender
				.toLowerCase().equals("+918792643041"));
	}

	public static boolean hasCurrencySymbol(String messageBody) {
		return messageBody.indexOf("Rs.") >= 0 || messageBody.indexOf("INR") >= 0;
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean isAtmWithdrawal(String messageBody) {
		return ((messageBody.toLowerCase().indexOf("atm") >= 0) && (messageBody.toLowerCase().indexOf("wdl") >= 0
				|| messageBody.toLowerCase().indexOf("withdrawal") >= 0 || messageBody.toLowerCase().indexOf("withdrawn") >= 0))
				|| messageBody.toLowerCase().indexOf("cash wdl") >= 0 || messageBody.toLowerCase().indexOf("cash withdrawn") >= 0 
				|| messageBody.toLowerCase().indexOf("cash-atm") >= 0 || messageBody.toLowerCase().indexOf("thru atm") >= 0 ;
	}

	public static boolean isBasicCategory(String categoryName) {
		for (String s : DataProvider.CATEGORY_LIST) {
			if (s.equals(categoryName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean inEmulator() {
		return "generic".equals(Build.BRAND.toLowerCase());
	}

	public static void insertTestExpense(ContentResolver cr) {
		String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID };
		Cursor cursor = cr.query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, null, null, null);

		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(DataProvider.KEY_EXPENSE_AMOUNT, "00000");
			values.put(DataProvider.KEY_EXPENSE_CATEGORY_NAME, "test_category");
			values.put(DataProvider.KEY_EXPENSE_STATE, "initial_test_expense");

			cr.insert(DataProvider.EXPENSE_URI, values);
		}
	}

	public static boolean isDuplicateSms(String mMessageBody, long mTimeStamp, ContentResolver cr) {
		String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_TIME_STAMP,
				DataProvider.KEY_EXPENSE_SMS_BODY };

		String where = DataProvider.KEY_EXPENSE_TIME_STAMP + "> (" + mTimeStamp + " - 60000) AND " + DataProvider.KEY_EXPENSE_SMS_BODY + "=?";

		// where = DataProvider.KEY_EXPENSE_SMS_BODY + "='"+mMessageBody+"'";
		Cursor cursor = cr.query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, new String[] { mMessageBody }, null);
		return cursor.getCount() > 0;
	}

	public static File exportToCSV(Context ctx) {
		String combinedString = "";
		
		File file = null;
		File root = Environment.getExternalStorageDirectory();
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File dir = new File(root.getAbsolutePath() + "/com.brizztv.mcube/exported_csv");
			if (!dir.exists()) {
				dir.mkdirs();
			}

			// append current date to file name
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			String dateNow = formatter.format(currentDate.getTime());
			file = new File(dir, "mcube_expenses_" + dateNow + ".csv");

			combinedString = getDataForCSV(ctx);

			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				out.write(combinedString.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//			if(Log.DEBUG) Log.v("sdcard mounted readonly");
		} else {
//			if (Log.DEBUG)
//				Log.v("Could not get write permission");
		}

		return file;
	}

	private static String getDataForCSV(Context ctx) {
		String expenseAmount, bank, location, merchant, notes, category, expenseDate;
		long timeStamp;
		
		String[] EXPENSE_PROJECTION = new String[] { DataProvider.KEY_EXPENSE_ID, DataProvider.KEY_EXPENSE_AMOUNT,
				DataProvider.KEY_EXPENSE_BANK_NAME, DataProvider.KEY_EXPENSE_LOCATION, DataProvider.KEY_EXPENSE_DATE,
				DataProvider.KEY_EXPENSE_MERCHANT, DataProvider.KEY_EXPENSE_NOTES, DataProvider.KEY_EXPENSE_CATEGORY_NAME,
				DataProvider.KEY_EXPENSE_DAY, DataProvider.KEY_EXPENSE_MONTH, DataProvider.KEY_EXPENSE_YEAR, DataProvider.KEY_EXPENSE_TIME_STAMP };

		String where = DataProvider.KEY_EXPENSE_STATE + " LIKE '%saved%'";
		String sortOrder = DataProvider.KEY_EXPENSE_TIME_STAMP + " DESC";
		Cursor cursor = ctx.getContentResolver().query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, null, sortOrder);
		
		int count = cursor.getCount();
		String stringToWrite = "\"Bank\", Category, Date, Amount, Notes, Merchant, Location";

		for (int i = 0; i < count; i++) {
			cursor.moveToNext();
			expenseAmount = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_AMOUNT));
//			expenseDate = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_DATE));
			timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_TIME_STAMP));
			expenseDate = new SimpleDateFormat("MM/dd/yyyy").format(timeStamp);
			
			bank = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_BANK_NAME));
			category = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_CATEGORY_NAME));
			merchant = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_MERCHANT));
			location = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_LOCATION));
			notes = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_NOTES));

			if(merchant == null) merchant = "";
			if(location == null) location = "";
			if(notes == null) notes = "";
			
			stringToWrite += "\n" + "\"" + bank + "\"" + ",\"" + category + "\"" + ",\"" + expenseDate + "\"" + ",\"" + expenseAmount + "\"" + ",\""
					+ notes + "\"" + ",\"" + merchant + "\"" + ",\"" + location + "\"";
		}
		// TODO Auto-generated method stub
		return stringToWrite;
	}

	public static String getCategory(Context context, String smsBody, String bankName) {
		String categoryName=null, merchant=null, notes=null;
		// prepare where condition and sort order for local query
		String where = "? LIKE '%' || merchant || '%'"; // Tip - for concatenating column with other string use '||'
		String sortOrder = DataProvider.KEY_SMS_CAT_MAP_PRIORITY + " DESC";
		String[] selectionArgs = new String[] {smsBody};
		
		Boolean tryLocal = false; // by default assume that we don't have to try the local database to get the category
		
		// lets get the local result and store it
		Cursor cursor = context.getContentResolver().query(DataProvider.SMS_CAT_MAP_URI, SMS_CATEGORY_MAPPING_PROJECTION, where, selectionArgs, sortOrder);
		
		if(isAtmWithdrawal(smsBody)) {
			categoryName = DataProvider.CATEGORY_ATM_WITHDRAWAL;
		} else if(haveNetworkConnection(context)) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
			// Add your data
			nameValuePairs.add(new BasicNameValuePair("smsBody", smsBody));
			nameValuePairs.add(new BasicNameValuePair("bankName", bankName));

			// TODO - will throw a network on main ui thread exception :(
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://www.examontv.com/McubeService/getCategory");

			try {
				// set post data
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
//				inputStream = response.getEntity().getContent();

				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				String json = reader.readLine();

				JSONTokener tokener = new JSONTokener(json);

				// TODO - fix the casting bug...
				// java.lang.ClassCastException: java.lang.String
				// at com.brizztv.mcube.Utils.postData(Utils.java:251)
				// at com.brizztv.mcube.Utils.pingUnauditedExpenses(Utils.java:222)
				// at com.brizztv.mcube.PingerService$1.run(PingerService.java:21)
				// at java.lang.Thread.run(Thread.java:1019)
				JSONObject jsonObj = (JSONObject) tokener.nextValue();
				categoryName = jsonObj.getString("category");
				merchant = jsonObj.getString("merchant");
				notes = jsonObj.getString("notes");
				
				// insert record to local database, only if the returned values is not 'Uncategorized' or null and the merchant name is also not blank (causes huge problem, matches every time)
				if(!categoryName.equals(DataProvider.CATEGORY_UNCATEGORIZED) && !categoryName.trim().equals("") && merchant != null && !merchant.trim().equals("")) {
					if(cursor.moveToFirst()) {
						ContentValues values = new ContentValues();
						String merchantLocal = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_SMS_CAT_MAP_MERCHANT));
						String categoryNameLocal = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_SMS_CAT_MAP_CATEGORY));
						int priority = cursor.getInt(cursor.getColumnIndexOrThrow(DataProvider.KEY_SMS_CAT_MAP_PRIORITY));
						
						// If the server returned some other category for the same merchant, lets update the local database
						if(merchant.equals(merchantLocal) && !categoryName.equalsIgnoreCase(categoryNameLocal)) {
							where = DataProvider.KEY_SMS_CAT_MAP_ID + "=" + cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_SMS_CAT_MAP_ID));
							values.put(DataProvider.KEY_SMS_CAT_MAP_MERCHANT, merchant);
							values.put(DataProvider.KEY_SMS_CAT_MAP_CATEGORY, categoryName);
							values.put(DataProvider.KEY_SMS_CAT_MAP_NOTES, notes);
							context.getContentResolver().update(DataProvider.SMS_CAT_MAP_URI, values, where, null);
						} else if(!merchant.equals(merchantLocal) && !categoryName.equalsIgnoreCase(categoryNameLocal)) {
							values.put(DataProvider.KEY_SMS_CAT_MAP_MERCHANT, merchant);
							values.put(DataProvider.KEY_SMS_CAT_MAP_CATEGORY, categoryName);
							values.put(DataProvider.KEY_SMS_CAT_MAP_NOTES, notes);
							values.put(DataProvider.KEY_SMS_CAT_MAP_PRIORITY, priority + 1);
							context.getContentResolver().insert(DataProvider.SMS_CAT_MAP_URI, values);
						}
					}
				}
			} catch (ClientProtocolException e) {
				tryLocal = true;
//				if (Log.DEBUG)
//					Log.v("Error connecting to examontv server: " + e.toString());
			} catch (IOException e) {
				tryLocal = true;
				e.printStackTrace();
			} catch (JSONException e) {
				tryLocal = true;
//				if (Log.DEBUG)
//					Log.v("Problem in decoding json");
				e.printStackTrace();
			} catch (Exception e) {
				tryLocal = true;
//				if (Log.DEBUG)
//					Log.v("Error connecting to examontv server: " + e.toString());
			}

//			if (Log.DEBUG)
//				Log.v("Category from server: " + categoryName);
		} else {
//			if (Log.DEBUG)
//				Log.v("Utils: getCategory: no network connection");

			tryLocal = true;
		} 

		// if not an ATM withdrawal and could not get category from the server for some reason, try getting it from local database
		if(tryLocal) {
			if(cursor.moveToFirst()) {
				categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_SMS_CAT_MAP_CATEGORY));
			}
//			if(Log.DEBUG) Log.v("SMS: " + smsBody + ". Category from local db: " + categoryName);
		}
		
		if(categoryName == null || categoryName.trim().equals("")) {
			categoryName = DataProvider.CATEGORY_UNCATEGORIZED;
		}
		
		return changeTitleCaseforEachWord(categoryName);
	}

	public static boolean categoryExists(Context context, String mCategoryName) {
		Cursor cursor = context.getContentResolver().query(DataProvider.CATEGORY_URI, CATEGORY_PROJECTION, "name=?", new String[] { mCategoryName },
				null);
		return cursor.getCount() > 0;
	}

	/*
	 * Function to check if an SMS is actually an expense
	 */
	public static boolean shouldFileExpense(String body, String address, long timestamp, Context ctx) {
		ContentResolver cr = ctx.getContentResolver();
		Boolean firstPass=false, secondPass=false;
		
		firstPass = (Utils.getBankFromSenderId(cr, address) != null || address.equals("your") || address.equals("111") || address.equals("+919845787038") || address.equals("+918792643041"))
				&& isDebitMessage(body) 
				&& !Utils.isDuplicateSms(body, timestamp, cr);
		
		secondPass = !isFalsePositive(body); 
		return firstPass && secondPass;
	}
	
	/* 
	 * to catch the false positive cases
	 */
	private static Boolean isFalsePositive(String smsBody) {
		String[] falsePositiveIndicators = new String[] 
				{ 
					"converted into EMI",
					"EMI on your",
					"declined due to insufficient limit",
					"debited to your",
					"will be debited from your",
				};
		
		for(String falsePositiveIndicator: falsePositiveIndicators) {
			if(smsBody.toLowerCase().indexOf(falsePositiveIndicator.toLowerCase()) >= 0) {
//				if(Log.DEBUG) Log.v("Found a false positive. SMS: " + smsBody + ". False positive Indicator: " + falsePositiveIndicator);
				return true;
			}
		}
		return false;
	}

	public static void fileExpenseFromHistory(Context ctx) {
		String WHERE_CONDITION = null;
		String SORT_ORDER = "date ASC";
		int count = 0, i;
		Uri SMS_INBOX_CONTENT_URI = Uri.parse("content://sms/inbox");

		int year, month, day;
		String expenseAmount;
		String expenseDate, notes;
		String bankName, categoryName = DataProvider.CATEGORY_UNCATEGORIZED;
		Calendar c = Calendar.getInstance();

		// pick only last 30 days sms... trying to file everything would be overkill
		// determines if the message was received and not sent...not very much required in our case, but will take care of forwarded bank messages
//		WHERE_CONDITION = "type=1 AND date>=(julianday('now', '-20 days') - 2440587.5)*86400000.0";
		// type = 1 will ensure we only get messages sent to us and not message sent from us
		WHERE_CONDITION = "type=1";

		Cursor cursor = ctx.getContentResolver().query(SMS_INBOX_CONTENT_URI, new String[] { "_id", "thread_id", "address", "person", "date", "body" },
				WHERE_CONDITION, null, SORT_ORDER);

		if (cursor != null) {
			try {
				count = cursor.getCount();
				for (i = 0; i < count; i++) {
					cursor.moveToNext();
					bankName = DataProvider.BANK_OTHERS;
					categoryName = DataProvider.CATEGORY_UNCATEGORIZED;
					notes = null;
					long messageId = cursor.getLong(0);
					long threadId = cursor.getLong(1);
					String address = cursor.getString(2);
					
					// long contactId = cursor.getLong(3);
					// String contactId_string = String.valueOf(contactId);
					long timestamp = cursor.getLong(4);
					
					Log.v("Time stamp: " + timestamp);
					
					String body = cursor.getString(5);
					
					// only file expense if the sender id can be mapped to a bank. first stage of filter
					if(Utils.shouldFileExpense(body, address, timestamp, ctx)) {
						// get the bank Name from the address
						if(address.equals("+919845787038") || address.equals("+918792643041")) {
							bankName = "ICICI";
						} else {
							bankName = Utils.getBankFromSenderId(ctx.getContentResolver(), address);
						}

						// get expense date
						c.setTimeInMillis(timestamp);
						year = c.get(Calendar.YEAR);
						month = c.get(Calendar.MONTH);
						day = c.get(Calendar.DAY_OF_MONTH);
						expenseDate = Integer.toString(year) + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
						// get amount
						expenseAmount = Utils.getAmount(body);
						categoryName = getCategory(ctx, body, bankName);

						// create a new category in case this category does not exist in this mobile phone
						if(!Utils.categoryExists(ctx, categoryName)) {
							ContentValues values = new ContentValues();
							values.put(DataProvider.KEY_CATEGORY_NAME, categoryName);
							ctx.getContentResolver().insert(DataProvider.CATEGORY_URI, values);
						}
						
						if(Utils.isAtmWithdrawal(body)) {
							notes = "ATM Withdrawal";
							categoryName = DataProvider.CATEGORY_ATM_WITHDRAWAL;
						}
						// setup the values to be inserted
						ContentValues values = new ContentValues();
						values.put(DataProvider.KEY_EXPENSE_AMOUNT, expenseAmount);
						values.put(DataProvider.KEY_EXPENSE_CATEGORY_NAME, categoryName);
						values.put(DataProvider.KEY_EXPENSE_BANK_NAME, bankName);
						values.put(DataProvider.KEY_EXPENSE_DAY, day);
						values.put(DataProvider.KEY_EXPENSE_MONTH, month + 1);
						values.put(DataProvider.KEY_EXPENSE_YEAR, year);
						values.put(DataProvider.KEY_EXPENSE_DATE, expenseDate);
						values.put(DataProvider.KEY_EXPENSE_SMS_ID, messageId);
						values.put(DataProvider.KEY_EXPENSE_SMS_THREAD_ID, threadId);
						values.put(DataProvider.KEY_EXPENSE_STATE, "scanned_saved");
						values.put(DataProvider.KEY_EXPENSE_SMS_BODY, body);
						values.put(DataProvider.KEY_EXPENSE_SENDER_ID, address);
						values.put(DataProvider.KEY_EXPENSE_NOTES, notes);
						values.put(DataProvider.KEY_EXPENSE_TIME_STAMP, timestamp);
						
						// trigger the insert query if we could parse the amount... else goto next SMS
						// adding the second condition because many a times the same sms is being inserted again.
						if (expenseAmount != null && !expenseExists(ctx, body, messageId)) {
							ctx.getContentResolver().insert(DataProvider.EXPENSE_URI, values);
						}
					} else {// end if(shouldFileExpense)
						// do nothing
					}
				} // end for loop to get every message
			} finally {
				cursor.close();
			}
		}
	}

	private static boolean expenseExists(Context ctx, String smsBody, long smsId) {
		String where = DataProvider.KEY_EXPENSE_SMS_BODY + "=? AND " + DataProvider.KEY_EXPENSE_SMS_ID + "=" + smsId;
		String[] selectionArgs = new String[] {smsBody};
		Cursor cursor = ctx.getContentResolver().query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, selectionArgs, null);

		return cursor.getCount() > 0;
	}

	public static int recategorizeExpenses(Context ctx) {
		String where = DataProvider.KEY_EXPENSE_CATEGORY_NAME + "='" + DataProvider.CATEGORY_UNCATEGORIZED + "'";
		Cursor cursor = ctx.getContentResolver().query(DataProvider.EXPENSE_URI, EXPENSE_PROJECTION, where, null, null);
		String categoryName = null;
		String smsBody = null;
		String bankName = null;
		Long expenseId = null;
//		if(Log.DEBUG) Log.v("Utils: recategorizeExpenses: record count: " + cursor.getCount());
		int count = 0;
		
		while(cursor.moveToNext()) {
			smsBody = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_SMS_BODY));
			bankName = cursor.getString(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_BANK_NAME));
			
			if(smsBody != null && !smsBody.equals(""))  {
				categoryName = getCategory(ctx, smsBody, bankName);
				if(categoryName != null && !categoryName.equals(DataProvider.CATEGORY_UNCATEGORIZED) && !categoryName.equals("")) {
					expenseId = cursor.getLong(cursor.getColumnIndexOrThrow(DataProvider.KEY_EXPENSE_ID));
					where = DataProvider.KEY_EXPENSE_ID + "=" + expenseId;
					
					ContentValues values = new ContentValues();
					values.put(DataProvider.KEY_EXPENSE_CATEGORY_NAME, categoryName);
					ctx.getContentResolver().update(DataProvider.EXPENSE_URI, values, where, null);
					count += 1;
				}
			}
		}
		return count;
	}

	public static int expenseCount(Context ctx) {
		String where = DataProvider.KEY_EXPENSE_STATE + " LIKE '%saved%'"; 
		Cursor countCursor = ctx.getContentResolver().query(DataProvider.EXPENSE_URI,
                new String[] {"count(*) AS count"},
                where,
                null,
                null);

        countCursor.moveToFirst();
        return countCursor.getInt(0);
	}
	
	 private static String changeTitleCaseforEachWord(String str) {  
	        StringTokenizer parser = new StringTokenizer(str);
	        String tokenTemp;
	        String word = "";
	        
	        while (parser.hasMoreTokens()) {  
	        	tokenTemp = parser.nextToken();
	        	if(tokenTemp.equals("and")) {
	        		word = word + " " + tokenTemp.toLowerCase();
	        	} else {
	        		word = word +" "+ tokenTemp.substring(0, 1).toUpperCase() + tokenTemp.substring(1);
	        	}
	        }
	        return word.trim();        
	    } 
}
