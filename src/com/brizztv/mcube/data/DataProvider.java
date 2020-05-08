package com.brizztv.mcube.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.brizztv.mcube.Log;

public class DataProvider extends ContentProvider {
	private static final String AUTHORITY = "com.brizztv.mcube.data";
	public static final int EXPENSES = 100;
	public static final int EXPENSES_ID = 101;
	public static final int EXPENSES_BY_CATEGORY = 102;
	public static final int EXPENSES_BY_CATEGORY_FILTER = 103;
	public static final int EXPENSES_UNAUDITED = 104;
	public static final int CATEGORIES = 200;
	public static final int CATEGORIES_ID = 201;
	public static final int BANKS = 300;
	public static final int BANK_ID = 301;
	public static final int REMINDERS = 400;
	public static final int REMINDERS_ID = 401;
	public static final int INCOMES = 500;
	public static final int INCOMES_ID = 501;
	public static final int USER_PROFILES = 601;
	public static final int USER_PROFILES_ID = 602;
	public static final int SMS_CAT_MAPS = 701;
	public static final int SMS_CAT_MAPS_ID = 702;

	private static final String EXPENSE_BASE_PATH = "expense";
	private static final String CATEGORY_BASE_PATH = "category";
	private static final String REMINDER_BASE_PATH = "reminder";
	private static final String BANK_BASE_PATH = "bank";
	private static final String USER_PROFILE_BASE_PATH = "user_profile";
	private static final String SMS_CAT_MAP_BASE_PATH = "sms_category_mapping";

	public static Uri EXPENSE_URI = Uri.parse("content://" + AUTHORITY + "/" + EXPENSE_BASE_PATH);
	public static Uri CATEGORY_URI = Uri.parse("content://" + AUTHORITY + "/" + CATEGORY_BASE_PATH);
	public static Uri REMINDER_URI = Uri.parse("content://" + AUTHORITY + "/" + REMINDER_BASE_PATH);
	public static Uri BANK_URI = Uri.parse("content://" + AUTHORITY + "/" + BANK_BASE_PATH);
	public static Uri USER_PROFILE_URI = Uri.parse("content://" + AUTHORITY + "/" + USER_PROFILE_BASE_PATH);
	public static Uri SMS_CAT_MAP_URI = Uri.parse("content://" + AUTHORITY + "/" + SMS_CAT_MAP_BASE_PATH);

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, EXPENSE_BASE_PATH, EXPENSES);
		sUriMatcher.addURI(AUTHORITY, EXPENSE_BASE_PATH + "/#", EXPENSES_ID);
		sUriMatcher.addURI(AUTHORITY, EXPENSE_BASE_PATH + "/groupByCategory", EXPENSES_BY_CATEGORY);
		sUriMatcher.addURI(AUTHORITY, EXPENSE_BASE_PATH + "/groupByCategory/#", EXPENSES_BY_CATEGORY_FILTER);
		sUriMatcher.addURI(AUTHORITY, EXPENSE_BASE_PATH + "/unauditedExpenses", EXPENSES_UNAUDITED);
		sUriMatcher.addURI(AUTHORITY, CATEGORY_BASE_PATH, CATEGORIES);
		sUriMatcher.addURI(AUTHORITY, CATEGORY_BASE_PATH + "/#", CATEGORIES_ID);
		sUriMatcher.addURI(AUTHORITY, BANK_BASE_PATH, BANKS);
		sUriMatcher.addURI(AUTHORITY, BANK_BASE_PATH + "/#", BANK_ID);
		sUriMatcher.addURI(AUTHORITY, REMINDER_BASE_PATH, REMINDERS);
		sUriMatcher.addURI(AUTHORITY, REMINDER_BASE_PATH + "/#", REMINDERS_ID);
		sUriMatcher.addURI(AUTHORITY, USER_PROFILE_BASE_PATH, USER_PROFILES);
		sUriMatcher.addURI(AUTHORITY, USER_PROFILE_BASE_PATH + "/#", USER_PROFILES_ID);
		sUriMatcher.addURI(AUTHORITY, SMS_CAT_MAP_BASE_PATH, SMS_CAT_MAPS);
		sUriMatcher.addURI(AUTHORITY, SMS_CAT_MAP_BASE_PATH + "/#", SMS_CAT_MAPS_ID);
	}

	public static final String EXPENSE_TABLE = "expense";
	public static final String REMINDER_TABLE = "reminder";
	public static final String CATEGORY_TABLE = "category";
	public static final String BANK_TABLE = "bank";
	public static final String INCOME_TABLE = "income";
	public static final String USER_PROFILE_TABLE = "user_profile";
	public static final String SMS_CAT_MAP_TABLE = "sms_category_mapping";

	public static final String KEY_EXPENSE_AMOUNT = "amount";
	public static final String KEY_EXPENSE_MERCHANT = "merchant";
	public static final String KEY_EXPENSE_LOCATION = "location";
	public static final String KEY_EXPENSE_NOTES = "notes";
	public static final String KEY_EXPENSE_BANK_NAME = "bank_name";
	public static final String KEY_EXPENSE_CATEGORY_ID = "category_id";
	public static final String KEY_EXPENSE_CATEGORY_NAME = "category_name";
	public static final String KEY_EXPENSE_DATE = "expense_date";
	public static final String KEY_EXPENSE_DAY = "day";
	public static final String KEY_EXPENSE_MONTH = "month";
	public static final String KEY_EXPENSE_YEAR = "year";
	public static final String KEY_EXPENSE_SMS_ID = "sms_id";
	public static final String KEY_EXPENSE_SMS_THREAD_ID = "sms_thread_id";
	public static final String KEY_EXPENSE_ID = "_id";
	public static final String KEY_EXPENSE_AUDITED = "audited";
	public static final String KEY_EXPENSE_STATE = "state";
	public static final String KEY_EXPENSE_SMS_BODY = "sms_body";
	public static final String KEY_EXPENSE_TIME_STAMP = "time_stamp";
	public static final String KEY_EXPENSE_SENDER_ID = "sender_id";

	public static final String KEY_CATEGORY_ID = "_id";
	public static final String KEY_CATEGORY_NAME = "name";
	public static final String KEY_CATEGORY_CREATED = "created";

	public static final String KEY_REMINDER_ID = "_id";
	public static final String KEY_REMINDER_DATE = "expense_date";
	public static final String KEY_REMINDER_BANK_NAME = "bank_name";
	public static final String KEY_REMINDER_MERCHANT = "merchant";
	public static final String KEY_REMINDER_LOCATION = "location";
	public static final String KEY_REMINDER_AMOUNT = "amount";
	public static final String KEY_REMINDER_DAY = "day";
	public static final String KEY_REMINDER_MONTH = "month";
	public static final String KEY_REMINDER_YEAR = "year";
	public static final String KEY_REMINDER_AUDITED = "audited";

	public static final String KEY_INCOME_ID = "_id";
	public static final String KEY_INCOME_AMOUNT = "amount";
	public static final String KEY_INCOME_DAY = "day";
	public static final String KEY_INCOME_MONTH = "month";
	public static final String KEY_INCOME_YEAR = "year";
	public static final String KEY_INCOME_NOTES = "notes";
	public static final String KEY_INCOME_AUDITED = "audited";

	public static final String KEY_BANK_ID = "_id";
	public static final String KEY_BANK_NAME = "bank_name";
	public static final String KEY_BANK_SENDER_ID = "bank_sender_id";
	public static final String KEY_BANK_DEFAULT = "default_bank";
	public static final String KEY_BANK_PRIORITY = "priority";

	public static final String KEY_USER_PROFILE_ID = "_id";
	public static final String KEY_USER_PROFILE_EMAIL = "email";
	public static final String KEY_USER_PROFILE_PHONE_NUMBER = "phone_number";
	public static final String KEY_USER_PROFILE_PHONE_MODEL = "phone_model";
	public static final String KEY_USER_PROFILE_OS_VERSION = "os_version";
	public static final String KEY_USER_PROFILE_DEVICE_ID = "device_id";

	public static final String KEY_SMS_CAT_MAP_ID = "_id";
	public static final String KEY_SMS_CAT_MAP_SERVER_ID = "server_id";
	public static final String KEY_SMS_CAT_MAP_BANK = "bank";
	public static final String KEY_SMS_CAT_MAP_MERCHANT = "merchant";
	public static final String KEY_SMS_CAT_MAP_CATEGORY = "category";
	public static final String KEY_SMS_CAT_MAP_NOTES = "notes";
	public static final String KEY_SMS_CAT_MAP_LOCAL_RULE = "local_rule";
	public static final String KEY_SMS_CAT_MAP_PRIORITY = "notes";

	public static final int AUDITED = 1;
	public static final int UNAUDITED = 0;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE_EXPENSE_TABLE = "CREATE TABLE expense (_id integer primary key autoincrement, amount text not null, notes text, bank_name text, category_id integer, "
			+ "expense_date date default CURRENT_DATE, merchant text, location text, day int, "
			+ "month int, year int, category_name text, sms_id integer, sms_thread_id integer, "
			+ "audited integer, state text, sms_body text, time_stamp integer NOT NULL DEFAULT (strftime('%s','now')*1000), sender_id text);";

	private static final String DATABASE_CREATE_CATEGORY_TABLE = "create table category (_id integer primary key autoincrement, "
			+ "name text not null unique, created integer NOT NULL DEFAULT (strftime('%s','now')));";

	private static final String DATABASE_CREATE_BANK_TABLE = "create table bank (_id integer primary key autoincrement, "
			+ "bank_name text not null, bank_sender_id text, default_bank integer, priority integer default 10000);";

	private static final String DATABASE_CREATE_REMINDER_TABLE = "CREATE TABLE reminder (_id integer primary key autoincrement, bank_name text, amount text not null, "
			+ "merchant text, location text, expense_date date default CURRENT_DATE, day int, month int, year int, audited integer);";

	private static final String DATABASE_CREATE_INCOME_TABLE = "CREATE TABLE income (_id integer primary key autoincrement, amount text not null, "
			+ "day int, month int, year int, audited integer);";

	private static final String DATABASE_CREATE_USER_PROFILE_TABLE = "CREATE TABLE user_profile (_id integer primary key autoincrement, "
			+ "email text, phone_number text, phone_model text, os_version text, device_id text)";

	private static final String DATABASE_CREATE_SMS_CATEGORY_MAPPING_TABLE = "CREATE TABLE sms_category_mapping (_id integer primary key autoincrement, "
			+ "server_id integer, bank text, merchant text, category text, notes text, local_rule integer default 0, priority integer default 0)";

	private static final String DATABASE_NAME = "sms_expense";
	private static final String DATABASE_TABLE_EXPENSE = "expense";
	private static final String DATABASE_TABLE_CATEGORY = "category";
	private static final String DATABASE_TABLE_REMINDER = "reminder";
	private static final String DATABASE_TABLE_BANK = "bank";
	private static final String DATABASE_TABLE_SMS_CATEGORY_MAPPING = "sms_category_mapping";

	private static final int DATABASE_VERSION = 5;
	public static final String CATEGORY_UNCATEGORIZED = "Uncategorized";
	public static final String CATEGORY_ATM_WITHDRAWAL = "ATM Withdrawal";
	public static final String[] CATEGORY_LIST = { CATEGORY_UNCATEGORIZED, CATEGORY_ATM_WITHDRAWAL, "Petrol/Diesel", "Clothing/Accessory",
			"Eating Out", "Food and Grocery", "Medical", "Insurance", "Household", "Cable/DTH", "Mobile Bill", "Internet", "Loan Payment",
			"Travel/Accomodation", "Investment", "Rent" };
	public static final String BANK_OTHERS = "Cash";
	public static final String[][] BANKS_LIST = { { "HDFC", "HDFC", "00" }, { "ICICI", "ICICI", "01" }, { "STANC", "STAN", "02" },
			{ "SBI", "SBI", "03" }, { "HSBC", "HSBC", "04" }, { "CITI", "CITI", "05" }, { "AXIS", "AXIS", "06" } };
	public static final String EXPENSE_DISCARDED = "discarded";
	public static final String EXPENSE_STATE_SAVED = "saved";
	public static final String EXPENSE_STATE_USER_ADDED = "user_added";

	// private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			if (Log.DEBUG)
				Log.v("DataProvider: DatabaseHelper");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (Log.DEBUG)
				Log.v("DataProvider: DatabaseHelper: onCreate: trying to create database tables");
			db.execSQL(DATABASE_CREATE_EXPENSE_TABLE);
			db.execSQL(DATABASE_CREATE_CATEGORY_TABLE);
			db.execSQL(DATABASE_CREATE_BANK_TABLE);
			db.execSQL(DATABASE_CREATE_REMINDER_TABLE);
			db.execSQL(DATABASE_CREATE_INCOME_TABLE);
			db.execSQL(DATABASE_CREATE_USER_PROFILE_TABLE);
			db.execSQL(DATABASE_CREATE_SMS_CATEGORY_MAPPING_TABLE);

			// delete current rows from sms category mapping table
			// db.delete(DATABASE_TABLE_SMS_CATEGORY_MAPPING, null, null);
			populateSmsCategoryMappingTable(db);

			String sql;
			// nasty bug because ppl with older database has a null or 0
			// timestamp
			// sql =
			// "update expense set time_stamp = (julianday(expense_date) - 2440587.5)*86400.0";
			// db.execSQL(sql);

			// Lets add the initial categories here too
			Cursor c = db.query(DATABASE_TABLE_CATEGORY, null, null, null, null, null, null);
			// TODO - remove this piece of code... oncreate is never called for
			// upgrade. only the first time app is installed
			if (c.getCount() == 0) {
				sql = "insert into " + DATABASE_TABLE_CATEGORY + " (" + KEY_CATEGORY_NAME + ") ";
				sql += "select '" + CATEGORY_LIST[0] + "' as " + KEY_CATEGORY_NAME;

				int categoryCount = CATEGORY_LIST.length;
				for (int i = 1; i < categoryCount; i++) {
					sql += " union select '" + CATEGORY_LIST[i] + "' as " + KEY_CATEGORY_NAME;
				}
				db.execSQL(sql);

				// db.execSQL("insert into " + DATABASE_TABLE_CATEGORY + " (" +
				// KEY_CATEGORY_NAME + ") "
				// + "select 'Uncategorized' as " + KEY_CATEGORY_NAME
				// + " union select 'Fuel' as " + KEY_CATEGORY_NAME
				// + " union select '" + CATEGORY_ATM_WITHDRAWAL + "' as " +
				// KEY_CATEGORY_NAME
				// + " union select 'Clothing' as " + KEY_CATEGORY_NAME
				// + " union select 'Eating Out/Movies' as " + KEY_CATEGORY_NAME
				// + " union select 'Food and Grocery' as " + KEY_CATEGORY_NAME
				// + " union select 'Medical' as " + KEY_CATEGORY_NAME
				// + " union select 'Insurance' as " + KEY_CATEGORY_NAME
				// + " union select 'Household' as " + KEY_CATEGORY_NAME
				// + " union select 'Cable/DTH' as " + KEY_CATEGORY_NAME
				// + " union select 'Mobile Bill' as " + KEY_CATEGORY_NAME
				// + " union select 'Internet' as " + KEY_CATEGORY_NAME
				// + " union select 'Loan Payment' as " + KEY_CATEGORY_NAME
				// + " union select 'Vacation' as " + KEY_CATEGORY_NAME
				// + " union select 'Investment' as " + KEY_CATEGORY_NAME
				// + " union select 'Rent' as " + KEY_CATEGORY_NAME);
			}

			// Now lets add some banks
			c = db.query(DATABASE_TABLE_BANK, null, null, null, null, null, null);
			if (c.getCount() == 0) {
				// ContentValues values = new ContentValues();
				// // values.put(key, value)
				// db.insert(table, nullColumnHack, values);
				populateBanks(db);
			}
		}

		private void populateBanks(SQLiteDatabase db) {
			db.execSQL("insert into " + DATABASE_TABLE_BANK + " (" + KEY_BANK_NAME + "," + KEY_BANK_SENDER_ID + "," + KEY_BANK_PRIORITY + ") "
					+ " select 'Cash' as " + KEY_BANK_NAME + ", 'Cash' as " + KEY_BANK_SENDER_ID + ", -1 as " + KEY_BANK_PRIORITY
					+ " union select 'HDFC' as " + KEY_BANK_NAME + ", 'HDFC' as " + KEY_BANK_SENDER_ID + ", 0 as " + KEY_BANK_PRIORITY
					+ " union select 'ICICI' as " + KEY_BANK_NAME + ", 'ICICI' as " + KEY_BANK_SENDER_ID + ", 1 as " + KEY_BANK_PRIORITY
					+ " union select 'SBI' as " + KEY_BANK_NAME + ", 'SBI' as " + KEY_BANK_SENDER_ID + ", 2 as " + KEY_BANK_PRIORITY
					+ " union select 'Citi Bank' as " + KEY_BANK_NAME + ", 'CITI' as " + KEY_BANK_SENDER_ID + ", 3 as " + KEY_BANK_PRIORITY
					+ " union select 'Standard Chartered' as " + KEY_BANK_NAME + ", 'STANC' as " + KEY_BANK_SENDER_ID + ", 4 as " + KEY_BANK_PRIORITY
					+ " union select 'Axis' as " + KEY_BANK_NAME + ", 'AXIS' as " + KEY_BANK_SENDER_ID + ", 5 as " + KEY_BANK_PRIORITY
					+ " union select 'HSBC' as " + KEY_BANK_NAME + ", 'HSBC' as " + KEY_BANK_SENDER_ID + ", 6 as " + KEY_BANK_PRIORITY
					+ " union select 'American Express' as " + KEY_BANK_NAME + ", 'AMEX' as " + KEY_BANK_SENDER_ID + ", 7 as " + KEY_BANK_PRIORITY
					+ " union select 'Punjab National Bank' as " + KEY_BANK_NAME + ", 'PNB' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Bank of India' as " + KEY_BANK_NAME + ", 'BOI' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Bank of Baroda' as " + KEY_BANK_NAME + ", 'BOB' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Andhra Bank' as " + KEY_BANK_NAME + ", 'ANDHRB' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Canara Bank' as " + KEY_BANK_NAME + ", 'CANARA' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Indian Bank' as " + KEY_BANK_NAME + ", 'INDSMS' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'United Bank of India' as " + KEY_BANK_NAME + ", 'UBI' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Syndicate Bank' as " + KEY_BANK_NAME + ", 'SYND' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Corporation Bank' as " + KEY_BANK_NAME + ", 'CORP' as "
					+ KEY_BANK_SENDER_ID + ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Central Bank of India' as " + KEY_BANK_NAME
					+ ", 'CBI' as " + KEY_BANK_SENDER_ID + ", 10000 as " + KEY_BANK_PRIORITY + " union select 'State Bank of Hyderabad' as "
					+ KEY_BANK_NAME + ", 'SBH' as " + KEY_BANK_SENDER_ID + ", 10000 as " + KEY_BANK_PRIORITY
					+ " union select 'State Bank of Travancore' as " + KEY_BANK_NAME + ", 'SBT' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Kotak Mahindra Bank' as " + KEY_BANK_NAME + ", 'KOTAK' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'IDBI' as " + KEY_BANK_NAME + ", 'IDBI' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'UCO Bank' as " + KEY_BANK_NAME + ", 'UCO' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Yes Bank' as " + KEY_BANK_NAME + ", 'YES' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Tamilnad Mercantile' as " + KEY_BANK_NAME + ", 'TMB' as "
					+ KEY_BANK_SENDER_ID + ", 10000 as " + KEY_BANK_PRIORITY + " union select 'State Bank of Mysore' as " + KEY_BANK_NAME
					+ ", 'SBM' as " + KEY_BANK_SENDER_ID + ", 10000 as " + KEY_BANK_PRIORITY + " union select 'State Bank of Bikaner and Jaipur' as "
					+ KEY_BANK_NAME + ", 'SBBJ' as " + KEY_BANK_SENDER_ID + ", 10000 as " + KEY_BANK_PRIORITY
					+ " union select 'Oriental Bank of Commerce' as " + KEY_BANK_NAME + ", 'OBC' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Karur Vysya Bank' as " + KEY_BANK_NAME + ", 'KVB' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'ING Vysya' as " + KEY_BANK_NAME + ", 'ING' as " + KEY_BANK_SENDER_ID + ", 10000 as "
					+ KEY_BANK_PRIORITY + " union select 'Indian Overseas Bank' as " + KEY_BANK_NAME + ", 'IOB' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Dena' as " + KEY_BANK_NAME + ", 'DENA' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Allahabad Bank' as " + KEY_BANK_NAME + ", 'ALLA' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY + " union select 'Deutsche Bank' as " + KEY_BANK_NAME + ", 'dbAlrt' as " + KEY_BANK_SENDER_ID
					+ ", 10000 as " + KEY_BANK_PRIORITY);
		}

		private void populateSmsCategoryMappingTable(SQLiteDatabase db) {
			if (Log.DEBUG)
				Log.v("populating sms category mapping table");
			String sql;
			sql = "INSERT INTO sms_category_mapping (_id, merchant, category, notes) "
					+ " select 4 as _id, 'EASY MOBILE RECHARGE' as merchant, 'Mobile Bill' as category, '' as notes"
					+ " union select 5, 'SHIVAM WINES', 'Eating Out', ''" + " union select 8, 'LANDMARK GARDENS SHOP', 'Shopping', ''"
					+ " union select 9, 'SHRI MAHESH MEDICAL', 'Medical', ''" + " union select 10, 'BAGZONE LIFESTYLES', 'Shopping', ''"
					+ " union select 14, 'TATA SKY', 'Cable/DTH', ''" + " union select 15, 'MAKEMYTRIP', 'Travel/Accomodation', ''"
					+ " union select 18, 'INFINITI WINES', 'Eating Out', ''" + " union select 19, 'WOOD STOC', 'Eating Out', 'Bangalore'"
					+ " union select 22, 'SAI SILKS', 'Clothing/Accessory', ''" + " union select 23, 'HYPER CITY', 'Shopping', ''"
					+ " union select 27, 'DEWAN SERVICE STA HPCL', 'Petrol/Diesel', ''" + " union select 28, 'DIGIWORLD', 'Electronics', ''"
					+ " union select 31, 'BRIX GYM', 'Beauty/Health Care', ''" + " union select 32, 'firstcry.com', 'Childcare', ''"
					+ " union select 33, 'HESTOFT FOODS PVT LTD', 'Food and Grocery', ''"
					+ " union select 35, 'SADHU VASWANI INTERNATIONAL SC', 'Social Welfare', ''"
					+ " union select 36, 'united india', 'Insurance', ''" + " union select 41, 'PRAKASH WINES', 'Eating Out', ''"
					+ " union select 45, 'ONLINE RECHARGE', 'Mobile Bill', ''" + " union select 48, 'WANGS DYNASTY CHINESE', 'Eating Out', ''"
					+ " union select 50, 'REGENT RESTAURANT ', 'Eating Out', ''" + " union select 51, 'SUMAN MEGA BAZAR', 'Shopping', ''"
					+ " union select 53, 'DMART AVENUE SUPER', 'Shopping', ''" + " union select 54, 'M SACHCHIYAY. COM', 'Electronics', ''"
					+ " union select 57, 'SOWBHAGYA AND CO', 'Food and Grocery', ''" + " union select 59, 'VEEKES & THOMAS', 'Eating Out', ''"
					+ " union select 60, 'AMAR SERVICE STATION', 'Petrol/Diesel', ''" + " union select 61, 'BESCOM', 'Electricity Bill', ''"
					+ " union select 62, 'VAYUPUTHRA ENTERPRISES', 'Travel/Accomodation', ''" + " union select 65, 'CALLISTA ', 'Eating Out', ''"
					+ " union select 66, 'SYRA VENTURES', 'Eating Out', ''" + " union select 69, 'STATUS FAMILY RESTUURA', 'Eating Out', ''"
					+ " union select 71, 'VRL LOGIS', 'Travel/Accomodation', ''" + " union select 72, 'ADIGAS NALAPAKA å ', 'Eating Out', ''"
					+ " union select 75, 'AIRTEL-BROADB AND TELEPHONES', 'internet bill', ''"
					+ " union select 76, 'BSNL-BANGALORE TELEPHONES', 'Mobile Bill', ''"
					+ " union select 77, 'AIRTEL - BANGALORE bill', 'Mobile Bill', ''" + " union select 78, 'BWSSB', 'Utility', ''"
					+ " union select 79, 'M.K. FOOD BAZAAR', 'Food and Grocery', ''"
					+ " union select 80, 'SHREE HARI SERVICE STATIO', 'Petrol/Diesel', ''"
					+ " union select 81, 'MADURA GARMENTS', 'Clothing/Accessory', ''" + " union select 82, 'ANGEETI', 'Eating Out', ''"
					+ " union select 84, 'EDEN PARK RESTAURANT', 'Eating Out', ''" + " union select 85, 'THE NESARA GRAND', 'Eating Out', ''"
					+ " union select 87, 'LIFELINE FARMACIA', 'Medical', ''" + " union select 88, 'VISABILLPAY-VODAFONE', 'Mobile Bill', ''"
					+ " union select 89, 'VISABILLPAY-BSNL-BIL', 'Mobile Bill', ''"
					+ " union select 90, 'WS RETAIL SERVICES', 'Online Shopping', 'Flipkart'"
					+ " union select 93, 'SOLITAIRE FOOD & BEV', 'Eating Out', ''" + " union select 94, 'M K RETAIL', 'Food and Grocery', ''"
					+ " union select 96, 'HEALTH AND GLOW', 'Beauty/Health Care', ''" + " union select 97, 'GRABMORE.IN', 'Shopping', ''"
					+ " union select 98, 'IRCTC', 'Travel/Accomodation', ''" + " union select 99, 'KAIRALI HERBALS', 'Beauty/Health Care', ''"
					+ " union select 100, 'G.R.T. JEWELLERS', 'Jewellery', ''" + " union select 101, 'THULP FOOD PVT LTD', 'Food and Grocery', ''"
					+ " union select 104, 'AUTO CARE CENTRE', 'Vehicle', ''" + " union select 111, 'Veekes J P Nagar ', 'Eating Out', ''"
					+ " union select 112, 'BRAND CALCULUS ICE CRE', 'Eating Out', ''" + " union select 115, 'EBSCHARGE.COM', 'Mobile Bill', ''"
					+ " union select 116, 'Croma J P NAGAR', 'Electronics', ''" + " union select 119, 'KRISHNA PETRO JUNCT', 'Petrol/Diesel', ''"
					+ " union select 122, 'SRI SATHYA SAI KEERT', 'Petrol/Diesel', ''"
					+ " union select 127, 'FEET COLLECTION -MAR', 'Clothing/Accessory', ''" + " union select 128, 'KIDS CITY', 'Childcare', ''"
					+ " union select 129, 'R K. NOVELTIES BAGS', 'Shopping', ''" + " union select 130, 'FOOD BAZAAR', 'Food and Grocery', ''"
					+ " union select 131, 'G R THANGA MALIGAI', 'Jewellery', ''" + " union select 133, 'SNAPDEAL.COM', 'Shopping', ''"
					+ " union select 135, 'PEARL HONDA', 'Vehicle', ''" + " union select 138, 'NEEDS SUPERMARKET', 'Household', ''"
					+ " union select 139, 'THE MASTERS FAST FOO', 'Eating Out', ''" + " union select 140, 'GRAND MOTORS', 'Vehicle', ''"
					+ " union select 141, 'RAGAM MEGA STORES ', 'Shopping', ''" + " union select 144, 'GIRI TRADING AGENCY ', 'Dharma', ''"
					+ " union select 145, 'NEW WOODLANDS HOTEL', 'Travel/Accomodation', ''" + " union select 147, 'APOLLO PHARMACY', 'Medical', ''"
					+ " union select 148, 'FRESH CHICKEN ', 'Food and Grocery', ''" + " union select 150, 'CURRYS', 'Eating Out', ''"
					+ " union select 151, 'LIQUOR BOUTIQUE ', 'Eating Out', ''"
					+ " union select 153, 'COLLARS CASUALS WEAR', 'Clothing/Accessory', ''"
					+ " union select 155, 'F R FOODS', 'Food and Grocery', ''" + " union select 157, 'THE APOLLO CLINIC', 'Medical', ''"
					+ " union select 158, 'RYAN MEDICAL & GENERAL ', 'Medical', ''" + " union select 162, 'AISHWARYA DEPARTMENTAL', 'Shopping', ''"
					+ " union select 163, 'TOTAL MALL', 'Household', ''" + " union select 175, 'G. K. VALE', 'Photography', 'Photography'"
					+ " union select 176, 'VILLAGE HYPER MARKET', 'Food and Grocery', ''"
					+ " union select 179, 'MADHULOKA LIQUOR', 'Eating Out', 'Spirit'"
					+ " union select 180, 'ANAND SWEETS', 'Food and Grocery', 'sweets'" + " union select 182, 'MATS FUEL PARK', 'Petrol/Diesel', ''"
					+ " union select 184, 'BHARTI INFOTEL', 'Mobile Bill', ''" + " union select 185, 'MADHULOKA', 'Eating Out', 'Spirit'"
					+ " union select 186, 'UTH FASHION', 'Clothing/Accessory', ''" + " union select 187, 'BHARTHIS', 'Shopping', ''"
					+ " union select 188, 'SANGEETHA', 'Electronics', ''" + " union select 189, 'V CARE', 'Beauty/Health Care', ''"
					+ " union select 190, 'HOUSE OF SPIRIT', 'Food and Grocery', 'Spirit'"
					+ " union select 191, 'HOTEL AIDA', 'Travel/Accomodation', ''" + " union select 192, 'PAWANS KIDS ZONE', 'Shopping', ''"
					+ " union select 193, 'VLCC HEALTH CARE', 'Beauty/Health Care', ''" + " union select 194, 'Medicals', 'Medical', ''"
					+ " union select 195, 'AUSTRALIAN FOODS', 'Food and Grocery', ''" + " union select 196, 'Indian Health Org.', 'Medical', ''"
					+ " union select 197, 'Paytm.com', 'Mobile Bill', ''" + " union select 201, 'LATHANGI FORD SALES', 'Vehicle', ''"
					+ " union select 203, 'TOYS N TOYS', 'Childcare', ''" + " union select 204, 'SRI VARSHATH', 'Petrol/Diesel', ''"
					+ " union select 206, 'FOOD SQUARE SUPER', 'Food and Grocery', ''"
					+ " union select 207, 'SOWMYA HERBAL', 'Beauty/Health Care', ''"
					+ " union select 208, '7 DAYS SUPER MARKET', 'Food and Grocery', ''"
					+ " union select 209, 'fashionandyou.com', 'Clothing/Accessory', ''" + " union select 211, 'LIFE INSURANCE', 'Insurance', ''"
					+ " union select 212, 'RELIANCE FOOTPRINT', 'Shopping', ''" + " union select 213, 'K LOUNGE', 'Eating Out', ''"
					+ " union select 214, 'INNOVATION MALL', 'Shopping', ''" + " union select 215, 'Aditya Birla Retail', 'Food and Grocery', ''"
					+ " union select 216, 'COTTON DESIGN', 'Clothing/Accessory', ''" + " union select 217, 'BIG BAZAAR', 'Food and Grocery', ''"
					+ " union select 219, 'SAIRAS HOLDINGS', 'Clothing/Accessory', ''"
					+ " union select 220, 'TATA TELE SERVICES LTD', 'Mobile Bill', ''" + " union select 222, 'DECATHLON SPORTS', 'Sports', ''"
					+ " union select 224, 'SOFT N SUPPLE', 'Beauty/Health Care', ''" + " union select 225, 'MANDARIN TRIAL', 'Eating Out', ''"
					+ " union select 226, 'PINDI RESTAURANT', 'Eating Out', ''" + " union select 227, 'THE BOWLING', 'Movies/Entertainment', ''"
					+ " union select 229, 'SPICE RETAIL LTD', 'Electronics', ''" + " union select 230, 'RISTORANTE PREGO', 'Eating Out', ''"
					+ " union select 231, 'INDIGO', 'Travel/Accomodation', ''" + " union select 233, 'MASTAKALANDAR', 'Eating Out', ''"
					+ " union select 234, 'INOX', 'Movies/Entertainment', ''" + " union select 235, 'ROADTRIP', 'Travel/Accomodation', ''"
					+ " union select 236, 'PRINTO', 'Photography', ''" + " union select 238, 'JAGRITI -THE ART FOUNDATI', 'Social Welfare', ''"
					+ " union select 239, 'KARNATAKA STATE ROAD', 'Travel/Accomodation', ''" + " union select 240, 'BBMP', 'Utility', ''"
					+ " union select 242, 'RELIANCE DIGITAL', 'Electronics', ''" + " union select 243, 'MANIPAL HOSPITAL', 'Medical', ''"
					+ " union select 244, 'MANIPAL HEALTH ENTERPR', 'Medical', ''" + " union select 245, 'LANDMARK LIMITED', 'Shopping', ''"
					+ " union select 247, 'HINDUSTAN PETROLEUM', 'Petrol/Diesel', ''" + " union select 248, 'FOOD WORLD', 'Food and Grocery', ''"
					+ " union select 249, 'ARSHIS RESTAURANTEURS', 'Eating Out', ''"
					+ " union select 250, 'JUST BLUES HIGH SPIRIT', 'Eating Out', ''" + " union select 251, 'THOMSUN MUSIC HOUSE', 'Music', ''"
					+ " union select 252, 'CROMA', 'Electronics', ''" + " union select 255, 'SPECIALISTS DENTAL CLI', 'Medical', ''"
					+ " union select 256, 'NKP EMPIRE VENTURES', 'Travel/Accomodation', ''"
					+ " union select 257, 'RELIANCE TRENDS', 'Clothing/Accessory', ''"
					+ " union select 258, 'BHARATHI SERVICE STATI', 'Petrol/Diesel', ''" + " union select 259, 'HERBS AND SPICE', 'Eating Out', ''"
					+ " union select 261, 'MALLIGE MEDICAL CENTRE', 'Medical', ''" + " union select 262, 'MALLIGE PHARMACY', 'Medical', ''"
					+ " union select 264, 'FAME CINEMAS', 'Movies/Entertainment', ''" + " union select 267, 'DROPS TOTAL SPIRITS', 'Eating Out', ''"
					+ " union select 268, 'NIKHIL FILLING', 'Petrol/Diesel', ''" + " union select 269, 'PLANET M RETAIL', 'Shopping', ''"
					+ " union select 270, 'COPPER CHIMNEY', 'Eating Out', ''" + " union select 271, 'SUNCORP LIFESTYLES', 'Clothing/Accessory', ''"
					+ " union select 272, 'OPEN MEDIA NETWORK PVT', 'Books/Education', ''"
					+ " union select 274, 'Book My Show', 'Movies/Entertainment', ''"
					+ " union select 275, 'CHINA PEARL ENTERPRISES', 'Eating Out', ''" + " union select 276, 'INDIAN OIL', 'Petrol/Diesel', ''"
					+ " union select 277, 'EKBOTES LOGS AND', 'Furniture', ''" + " union select 278, 'MAX NEW YORK LIFE', 'Insurance', ''"
					+ " union select 279, 'Tata Indicom', 'Mobile Bill', ''" + " union select 281, 'RELIANCE FOOT PRINT', 'Shopping', ''"
					+ " union select 283, 'SPAGHETTI KITCHEN', 'Eating Out', ''"
					+ " union select 286, 'NYSA ENTERPRISES P', 'Clothing/Accessory', ''"
					+ " union select 287, 'PETER ENGLAND', 'Clothing/Accessory', ''" + " union select 288, 'HPCL', 'Petrol/Diesel', ''"
					+ " union select 289, 'D MART ', 'Shopping', ''" + " union select 291, 'HEALTH & GLOW', 'Beauty/Health Care', ''"
					+ " union select 293, 'BATA INDIA', 'Clothing/Accessory', 'Shoes'"
					+ " union select 294, 'TOTAL HYBERMARKET', 'Food and Grocery', ''"
					+ " union select 296, 'YATRA DOMESTIC', 'Travel/Accomodation', ''"
					+ " union select 297, 'BANGALORE CENTRAL', 'Clothing/Accessory', ''"
					+ " union select 298, 'INTERGLOBE AVIATION', 'Travel/Accomodation', ''" + " union select 300, 'HALLMARK.COM', 'Shopping', ''"
					+ " union select 301, 'SHARMA TRANSPORTS', 'Misc', ''" + " union select 302, 'MANAPPURAM JEWELLERY', 'Jewellery', ''"
					+ " union select 304, 'Q CINEMAS ', 'Movies/Entertainment', ''" + " union select 305, 'SOCH', 'Clothing/Accessory', ''"
					+ " union select 306, 'Coffee Day', 'Eating Out', ''" + " union select 307, 'PANTALOONS', 'Clothing/Accessory', ''"
					+ " union select 308, 'METRO SHOES', 'Shopping', ''" + " union select 309, 'MAX SHOPPEE', 'Household', ''"
					+ " union select 310, 'SHELL ANISH ENTERPRISES', 'Vehicle', ''"
					+ " union select 311, 'SONAS FAVOURITE SHOP', 'Clothing/Accessory', ''"
					+ " union select 312, 'SAPNA BOOK HOUSE', 'Books/Education', ''" + " union select 313, 'LIFE STYLE', 'Shopping', ''"
					+ " union select 316, 'DELICACY RESTAURANT', 'Eating Out', ''" + " union select 317, 'STAR ENTERPRISES.', 'Vehicle', ''"
					+ " union select 318, 'KHADI BHANDAR', 'Clothing/Accessory', ''" + " union select 319, 'CAKE WALK', 'Food and Grocery', ''"
					+ " union select 321, 'ASIAN CUISINE', 'Eating Out', ''" + " union select 323, 'MEDICINE HOUSE', 'Medical', ''"
					+ " union select 324, 'METRO CASH AND CARRY', 'Household', ''" + " union select 325, 'NEW TANDOOR RESTAURANT', 'Eating Out', ''"
					+ " union select 326, ' JABONG', 'Shopping', ''" + " union select 327, 'THE BIRYANIS', 'Eating Out', ''"
					+ " union select 328, 'TIKONA', 'Internet bill', ''" + " union select 329, 'FUTURE BAZAAR', 'Shopping', ''"
					+ " union select 332, 'J R FUELS', 'Petrol/Diesel', ''" + " union select 333, 'GRT GRAND ESTANCIA', 'Travel/Accomodation', ''"
					+ " union select 334, 'MEDICINE PALACE', 'Medical', ''" + " union select 335, 'RELIANCE AUTOZONE', 'Vehicle', ''"
					+ " union select 336, 'TYRE EMPIRE', 'Vehicle', ''" + " union select 337, 'LATHANGI MOTORS', 'Vehicle', ''"
					+ " union select 338, 'HI TECH AUTO', 'Vehicle', ''" + " union select 339, 'MY BABY STORE', 'Childcare', ''"
					+ " union select 341, 'LITTLE BOSS', 'Clothing/Accessory', ''" + " union select 342, 'FUELS', 'Petrol/Diesel', ''"
					+ " union select 343, 'SAI FOTOS', 'Photography', ''" + " union select 344, 'THE PUNJABI RASOI', 'Eating Out', ''"
					+ " union select 347, 'BAKE N CAKE', 'Food and Grocery', ''" + " union select 348, 'THE BODY SHOP', 'Beauty/Health Care', ''"
					+ " union select 349, 'MAHINDRA RETAIL.', 'Shopping', ''" + " union select 351, 'APOLLO HOSPITALS', 'Medical', ''"
					+ " union select 352, 'Hospital', 'Medical', ''" + " union select 353, 'MEDICARE', 'Medical', ''"
					+ " union select 354, 'NALLI SILK SAREES', 'Clothing/Accessory', ''" + " union select 355, 'SHOPPERSSTOP', 'Shopping', ''"
					+ " union select 356, 'CROSSWORD', 'Books/Education', ''" + " union select 359, 'LULU COLLECTION', 'Clothing/Accessory', ''"
					+ " union select 360, 'SAPTHAGIRI DIGITAL IMA', 'Photography', ''"
					+ " union select 362, 'KALYAN SAREES', 'Clothing/Accessory', ''" + " union select 363, 'KALYAN SILKS', 'Clothing/Accessory', ''"
					+ " union select 364, 'ARVIND BRANDS', 'Clothing/Accessory', ''"
					+ " union select 365, 'PRETTY COLLECTION', 'Clothing/Accessory', ''" + " union select 366, 'MAMA MIA', 'Eating Out', ''"
					+ " union select 369, 'OXFORD BOOK STORE', 'Books/Education', ''" + " union select 371, 'THE WORLD OF FUN TOYS', 'Childcare', ''"
					+ " union select 374, 'CRIMSON FOODS', 'Food and Grocery', ''"
					+ " union select 376, 'UNDER WEAR WORLD', 'Clothing/Accessory', ''"
					+ " union select 378, 'INDIAN RAILWAY CATERIN', 'Travel/Accomodation', ''"
					+ " union select 381, 'NILGIRIS', 'Food and Grocery', ''" + " union select 382, 'AASTHA PETROL PUMP', 'Petrol/Diesel', ''"
					+ " union select 383, 'BHARAT PETROLIUM', 'Petrol/Diesel', ''"
					+ " union select 384, 'NEW LAXMI CHASMA', 'Clothing/Accessory', ''"
					+ " union select 386, 'GEETANJALI SALON', 'Beauty/Health care', ''" + " union select 387, 'BIKANER VALA', 'Food and Grocery', ''"
					+ " union select 388, 'homeshop18.com', 'Shopping', ''" + " union select 391, 'Petrol', 'Petrol/Diesel', ''"
					+ " union select 392, 'MAX SUPER SPECIALITY', 'Medical', ''"
					+ " union select 393, 'The Gateway Hotel', 'Travel/Accomodation', ''" + " union select 394, 'UMESH SERVICE', 'Vehicle', ''"
					+ " union select 396, 'LORD VENKATESWARA ENTE', 'Social Welfare', ''" + " union select 398, 'MURUGAN AUTO', 'Vehicle', ''"
					+ " union select 399, 'GANAPATHY FASHION POON', 'Clothing/Accessory', ''"
					+ " union select 402, 'MY CINEMAS', 'Movies/Entertainment', ''" + " union select 403, 'MANSUKHS SWEETS', 'Eating Out', ''"
					+ " union select 404, 'SARAVANA SELVARATHINAM', 'Shopping', ''" + " union select 405, 'POTHYS', 'Shopping', ''"
					+ " union select 406, 'EMI on your HDFC Loan acct', 'Loan', ''"
					+ " union select 407, 'PARDESI GENERAL STORE', 'Food and Grocery', ''" + " union select 408, 'RETAIL', 'Food and Grocery', ''"
					+ " union select 409, 'AUTOCARE', 'vehicle', ''" + " union select 410, 'SAPRA PAINTS PVT LTD', 'Household', ''"
					+ " union select 411, 'SPAR HYPERMARKET', 'Food and Grocery', ''" + " union select 412, 'RELIANCE FRESH', 'Food and Grocery', ''"
					+ " union select 413, 'IBP', 'Petrol/Diesel', ''" + " union select 414, 'HIGHWAY SERVICE STATIO', 'Petrol/Diesel', ''"
					+ " union select 415, 'AGGARWAL SUPER', 'Food and Grocery', ''" + " union select 418, 'RTGS / NEFT.', 'Internet Txns', ''"
					+ " union select 423, 'TITAN INDUS', 'Clothing/Accessory', ''" + " union select 427, 'BHAGINI ', 'Eating Out', ''"
					+ " union select 428, 'PIZZAHUT', 'Eating Out', ''" + " union select 429, 'THE LALIT THE GRILL', 'Eating Out', ''"
					+ " union select 430, 'ENTERTAINMENT PVT LTD', 'Movies/Entertainment', ''"
					+ " union select 433, 'KADAMBA THE SPORTS', 'Beauty/Health Care', ''" + " union select 436, 'HOME TOWN', 'Furniture', ''"
					+ " union select 438, 'REGAL SHOES', 'Shopping', ''" + " union select 439, 'SHERATON', 'Travel/Accomodation', ''"
					+ " union select 440, 'BABITHA FUELS HP', 'Petrol/Diesel', ''" + " union select 441, 'POPULAR MOTOR', 'Vehicle', ''"
					+ " union select 443, 'AIRCEL', 'Mobile Bill', ''" + " union select 447, 'CHENNAI FUELS', 'Petrol/Diesel', ''"
					+ " union select 448, 'ICICIPRULIFE', 'Insurance', ''" + " union select 449, 'Haneefa Super Market', 'Food and Grocery', ''"
					+ " union select 450, 'KUN HONDA MOTOR CYCLE', 'Vehicle', ''" + " union select 451, 'SRI KRISHNA SWEETS', 'Eating Out', ''"
					+ " union select 454, 'HANEEFA SHOE PARK', 'Shopping', ''" + " union select 456, 'SIMRANS AAPPAKADAI', 'Eating Out', ''"
					+ " union select 457, 'GANAPATHY STORES', 'Shopping', ''" + " union select 458, 'NALAS AAPPAKADAI', 'Eating Out', ''"
					+ " union select 459, 'PHARMACY', 'Medical', ''" + " union select 465, 'HOSMAT HOSP', 'Medical', ''"
					+ " union select 466, 'TATA DOCOMO', 'Mobile Bill', ''" + " union select 467, 'FFN.COM', 'Gifts', ''"
					+ " union select 473, 'www.redbus.in', 'Travel/Accomodation', ''" + " union select 474, 'TRINETHRA SUPER', 'Shopping', ''"
					+ " union select 475, 'BOOKMYSHOW', 'Movies/entertainment', ''" + " union select 476, 'VISABILLPAY-BES', 'Electricity Bill', ''"
					+ " union select 477, 'AIRCOM TRAVELS.', 'Travel/Accomodation', ''" + " union select 478, 'PUNJAB CROCKERY', 'Household', ''"
					+ " union select 479, 'HYDERABAD HOUSE.', 'Eating Out', ''" + " union select 480, 'MIRAH HOSPITALI.', 'Medical', ''"
					+ " union select 481, 'PILANI SOFT LAB.', 'Travel/Accomodation', ''" + " union select 482, 'HONEY DROPS.', 'Eating Out', ''"
					+ " union select 483, 'VODAFONE', 'Mobile Bill', ''" + " union select 484, 'FEATHERLITE', 'Furniture', ''"
					+ " union select 485, 'PVR LTD', 'Movies/entertainment', ''" + " union select 486, 'DOMINOS', 'Eating Out', ''"
					+ " union select 487, 'THE SOUL FOOD.', 'Eating Out', ''" + " union select 488, 'Cafe Coffee Day.', 'Eating Out', ''"
					+ " union select 489, 'DALLY BREADS', 'Food and Grocery', ''" + " union select 493, 'KFC', 'Eating Out', ''"
					+ " union select 494, 'KARANATAKA STAT.', 'Travel/Accomodation', ''" + " union select 495, 'MC DONALDS', 'Eating Out', ''"
					+ " union select 496, 'NANUTEL - MARGA', 'Travel/Accomodation', ''" + " union select 497, 'Click and Buy', 'Shopping', ''"
					+ " union select 499, 'BARBEQUE NATION.', 'Eating Out', ''" + " union select 500, 'THE BIERE CLUB', 'Eating Out', ''"
					+ " union select 503, 'TPT txn', 'Internet Txns', ''" + " union select 504, 'NEFT txn', 'Internet Txns', ''"
					+ " union select 505, 'GOLDEN BIRD', 'Food and Grocery', ''" + " union select 507, 'LAL RAK', 'Sports', ''"
					+ " union select 508, 'CINEMAX', 'Movies/Entertainment', ''" + " union select 510, 'Fair choice', 'Food and Grocery', ''"
					+ " union select 511, 'ROYALE SENATE', 'Eating Out', ''" + " union select 512, 'FLIPKAR', 'Shopping', ''"
					+ " union select 513, 'cleartrip', 'Travel/Accomodation', ''" + " union select 514, 'STAPLES', 'Office Supply', ''"
					+ " union select 515, 'Anand World', 'Clothing/Accessory', ''" + " union select 516, 'linode.com', 'Web Server', ''"
					+ " union select 517, 'bigrock.in', 'Web Server', ''" + " union select 519, 'MAHADISCOM', 'Electricity Bill', ''"
					+ " union select 520, 'AIRCELL', 'Mobile Bill', ''" + " union select 522, 'HOT SHANGHAI RESTAUR', 'Eating Out', ''"
					+ " union select 523, 'TIRUMALA TIRUPATI DEVASTH', 'Social Welfare', ''" + " union select 524, 'HYUNDAI MOTORS', 'Vehicle', ''"
					+ " union select 525, 'BAJAJ ELECTRONICS', 'Electronics', ''" + " union select 526, 'COST TO COST DHAMAKA', 'Shopping', ''"
					+ " union select 528, 'RESELLERCLUB.COM', 'Web Server', ''" + " union select 529, 'MOTHER CARE', 'Childcare', ''"
					+ " union select 530, 'COTTON COTTAGE', 'Clothing/Accessory', ''"
					+ " union select 531, 'PVR LIMITED', 'Movies/Entertainment', ''" + " union select 532, 'Stick Sports', 'Sports', ''"
					+ " union select 533, 'ONLINE RECHARGE SERVICES', 'Mobile Bill', ''" + " union select 534, 'NSDL DATABASE', 'Business Cost', ''"
					+ " union select 535, 'INDIAIDEAS-SCB', 'Utility', ''" + " union select 536, 'B S DWARKA', 'Grocery', ''"
					+ " union select 537, 'Directi Internet', 'Web Server', ''" + " union select 538, 'MUMBAI at ALFA', 'Electronics', ''"
					+ " union select 539, 'BILLDESK', 'Utility', ''" + " union select 540, 'SUVIDHA COMPLETE', 'Clothing/Accessory', ''"
					+ " union select 541, 'YES BOSS', 'Eating Out', ''" + " union select 544, 'MF-RELIANCE', 'Investment', ''"
					+ " union select 545, 'FLIGHT RAJA', 'Travel/Accomodation', ''" + " union select 546, 'PAI INTERNA', 'Electronics', ''"
					+ " union select 547, 'MF-FRANKLIN', 'Investment', ''" + " union select 548, 'at HONEY DROPS', 'Movies/Entertainment', ''"
					+ " union select 549, 'MF-FIDELITY', 'Investment', ''" + " union select 550, 'MF-RELIGARE', 'Investment', ''"
					+ " union select 551, 'TIRUMALA TIRUPA', 'Household', ''" + " union select 552, 'BHAWAR LIFE STY', 'Electronics', ''"
					+ " union select 553, 'MARKS AND SPENCER', 'Household', ''" + " union select 554, 'VISHAL MEGA MART', 'Household', ''"
					+ " union select 555, 'at MAA ANAND MAI', 'Petrol/Diesel', ''" + " union select 556, 'AISHI RAM BATRA', 'Petrol/Diesel', ''"
					+ " union select 557, 'Poorvika Mo', 'Electronics', ''" + " union select 558, 'GEOJIT BNP', 'Investment', ''"
					+ " union select 559, 'in MISYS', 'Investment', ''" + " union select 560, 'at BHAWAR LIFE', 'Clothing/Accessory', ''"
					+ " union select 561, 'Spencers Re', 'Clothing/Accessory', ''"
					+ " union select 562, 'CINEPLEX PRIVATE', 'Movies/Entertainment', ''" + " union select 563, 'FRIENDS RENDEZVO', 'Eating Out', ''"
					+ " union select 564, 'SANKARS THE BOOK', 'Books/Education', ''" + " union select 565, 'JIMIS CAFE', 'Eating Out', ''"
					+ " union select 566, 'STAR BAZAAR', 'Food and Grocery', ''" + " union select 567, 'FOCAL POINT', 'Photography', ''"
					+ " union select 568, 'HOTEL A.S. RESIDENCY', 'Travel/Accomodation', ''"
					+ " union select 569, 'SAPHIRE SERVICE STATIO', 'Petrol/Diesel', ''"
					+ " union select 570, 'HOTEL SITARA ROYAL', 'Travel/Accomodation', ''" + " union select 571, 'APSRTC', 'Travel/Accomodation', ''"
					+ " union select 572, 'Santosh Service Station', 'Petrol/Diesel', ''"
					+ " union select 574, 'SAI SHANTHI FILLING', 'Petrol/Diesel', ''" + " union select 575, '4 SEASONS', 'Eating Out', ''"
					+ " union select 576, 'Four SEASONS', 'Travel/Accomodation', ''" + " union select 577, 'VAMSHI FUEL POINT', 'Vehicle', ''"
					+ " union select 578, 'HOTEL SWAGATH', 'Travel/Accomodation', ''"
					+ " union select 579, 'SREE SAI VEERA SERVICE', 'Petrol/Diesel', ''" + " union select 580, 'A K R BAR AND RES', 'Eating Out', ''"
					+ " union select 581, 'SREE KRISHNA JEWELLERS', 'Jewellery', ''" + " union select 582, 'BRAND FACT', 'Clothing/Accessory', ''"
					+ " union select 583, 'S S LIQUOR', 'Movies/Entertainment', ''"
					+ " union select 584, 'HABIB SERVICE STATION', 'Petrol/Diesel', ''" + " union select 585, 'MEDPLUS', 'Medical', ''"
					+ " union select 586, 'SIVAM AUTO', 'Vehicle', ''" + " union select 587, 'LAXMI TYRE AND AUTO', 'Vehicle', ''"
					+ " union select 588, 'THE CHENNAI SHOPPING M', 'Clothing/Accessory', ''"
					+ " union select 589, 'SRI KRISHNA AUTO', 'Vehicle', ''" + " union select 590, 'CD WORLD', 'Movies/Entertainment', ''"
					+ " union select 591, 'RISHI FUEL', 'Petrol/Diesel', ''" + " union select 592, 'WEBSTORE PVT LTD', 'Internet', ''"
					+ " union select 593, 'VISHAL PERI', 'Electronics', ''" + " union select 594, 'CHANDANA BROTHERS', 'Jewellery', ''"
					+ " union select 595, 'PARVATHI MEDICAL HALL', 'Medical', ''" + " union select 597, 'MAYURI REST', 'Eating Out', ''"
					+ " union select 598, 'K S BAKERS', 'Eating Out', ''" + " union select 599, 'HOTEL SWAGA', 'Travel/Accomodation', ''"
					+ " union select 600, 'www.redbus', 'Travel/Accomodation', ''" + " union select 601, 'IDEA CELLUL', 'Mobile Bill', ''"
					+ " union select 602, 'R.S. BROTHE', 'Jewellery', ''" + " union select 603, 'J C BROTHER', 'Jewellery', ''"
					+ " union select 604, 'SANGOI SERVICE STATI', 'Petrol/Diesel', ''"
					+ " union select 605, 'KAMATS HOLI', 'Travel/Accomodation', ''" + " union select 606, 'MEHROFI TRA', 'Petrol/Diesel', ''"
					+ " union select 607, 'SMILE STONE', 'Travel/Accomodation', ''" + " union select 608, 'KALINGA RESTAURANT', 'Eating Out', ''"
					+ " union select 609, 'VERSANT TECHNOLOGIES', 'Business Cost', ''" + " union select 610, 'LOOKS N STY', 'Beauty/Health Care', ''"
					+ " union select 611, 'MAULI PETRO', 'Petrol/Diesel', ''" + " union select 612, 'PUNE CHICKE', 'Food and Grocery', ''"
					+ " union select 613, 'COSTA COFFE', 'Eating Out', ''" + " union select 614, 'NALLI CHINNASAMI CHE', 'Clothing/Accessory', ''"
					+ " union select 615, 'NALLI SILK GAR', 'Clothing/Accessory', ''"
					+ " union select 616, 'FOOD PALACE SUPER MARK', 'Food and Grocery', ''"
					+ " union select 617, 'BHAGWATI FILLING STATI', 'Petrol/Diesel', ''"
					+ " union select 619, 'DIVYA FUEL POINT', 'Petrol/Diesel', ''" + " union select 620, 'SVASTI HOSP', 'Medical', ''"
					+ " union select 621, 'PETS STOP', 'Childcare', ''" + " union select 622, 'SHOPPERS ST', 'Clothing/Accessory', ''"
					+ " union select 623, 'WRANGLER', 'Clothing/Accessory', ''" + " union select 624, 'RIOTGAMES', 'Movies/Entertainment', ''"
					+ " union select 625, 'SRI SAI MOTORS', 'Vehicle', ''" + " union select 626, 'GEM MOTORS', 'Vehicle', ''"
					+ " union select 627, 'SHWETA COMP', 'Electronics', ''" + " union select 628, 'BIKING TERMINAL', 'Vehicle', ''"
					+ " union select 629, 'ICICI LOMBARD', 'Insurance', ''" + " union select 630, 'RAMOJ FILM CI', 'Movies/Entertainment', ''"
					+ " union select 631, 'Vijetha Su', 'Household', ''" + " union select 632, 'Bajaj Au', 'Vehicle', ''"
					+ " union select 633, 'SANGEET SAG', 'Movies/Entertainment', ''"
					+ " union select 634, 'GRAND FRESH.. PER', 'Food and Grocery', ''"
					+ " union select 635, 'FABRIC LINE COCHIN', 'Clothing/Accessory', ''"
					+ " union select 636, 'DONUT BAKER', 'Food and Grocery', ''" + " union select 638, 'at BARISTA', 'Eating Out', ''"
					+ " union select 639, 'MF-UTI', 'Investment', ''" + " union select 640, 'MF-SBI FUND', 'Investment', ''"
					+ " union select 641, 'ALPS HOSPITAL', 'Medical', ''" + " union select 642, 'SREE TIRUMALA SUP', 'Petrol/Diesel', ''"
					+ " union select 643, 'INTERNATIONAL HOSPIT', 'Medical', ''" + " union select 644, 'DEVSHI NATHOOBHAI', 'Household', ''"
					+ " union select 645, 'UNIVERCELL TELE', 'Mobile Bill', ''" + " union select 646, 'PARAGON TELELINKS', 'Electronics', ''"
					+ " union select 647, 'VASANI SERVICE CENTR', 'Petrol/Diesel', ''" + " union select 648, 'UNIQUE FILLING', 'Petrol/Diesel', ''"
					+ " union select 649, 'PRATIBHA FOOTW', 'Clothing/Accessory', ''" + " union select 650, 'BSNL PAYMENTS', 'Mobile Bill', ''"
					+ " union select 651, 'INDIAN A AND C BAZAR', 'Clothing/Accessory', ''" + " union select 652, 'ACHINDRA', 'Eating Out', ''"
					+ " union select 653, 'R S Brothers', 'Jewellery', ''" + " union select 654, 'NEPTUNE STEEL', 'Household', ''"
					+ " union select 655, 'SAI KARUNA AGENCIES', 'Petrol/Diesel', ''" + " union select 656, 'FUTURE CAPITALS', 'Investment', ''"
					+ " union select 657, 'VITA LIFE', 'Medical', ''" + " union select 658, 'RELIANCE FR', 'Food and Grocery', ''"
					+ " union select 659, 'EURONET MOBILE RE', 'Mobile Bill', ''" + " union select 660, 'LIC HOUSING FIN', 'Loan', ''"
					+ " union select 661, 'HOTEL VIRUDHUNAGAR', 'Travel/Accomodation', ''"
					+ " union select 662, 'TWENTY FOUR SEVEN', 'Business Cost', ''" + " union select 663, 'BHARTI TELEVENTURE', 'Mobile Bill', ''"
					+ " union select 664, 'Tipitap Apps', 'Childcare', ''" + " union select 665, 'Indian Railways', 'Travel/Accomodation', ''"
					+ " union select 666, 'SAPPHIRE TOYS', 'Childcare', ''" + " union select 667, 'SANJAY UTSAV SPEED', 'Petrol/Diesel', ''"
					+ " union select 668, 'INTEL APPUP', 'Movies/Entertainment', ''" + " union select 669, 'MIRACLE TOUCH', 'Beauty/Health Care', ''"
					+ " union select 670, 'RANGANATHA SER STN', 'Petrol/Diesel', ''" + " union select 671, 'SHOPPERS CITY', 'Shopping', ''"
					+ " union select 672, 'SHELL R K R', 'Petrol/Diesel', ''" + " union select 673, 'VIVANTA BY TAJ', 'Travel/Accomodation', ''"
					+ " union select 674, 'at MY TOYZ', 'Childcare', ''" + " union select 675, 'INFINITEA', 'Food and Grocery', ''"
					+ " union select 676, 'Zynga Inc', 'Movies/Entertainment', 'Games'"
					+ " union select 677, 'Four Pixels', 'Movies/Entertainment', ''" + " union select 678, 'OMGPOP INC', 'Movies/Entertainment', ''"
					+ " union select 679, 'Loud Crow', 'Books/Education', ''" + " union select 680, 'androidSlide', 'Photography', ''"
					+ " union select 681, 'ALUKKAS WEDDING', 'Jewellery', ''" + " union select 683, 'KRIPANIKETHAN', 'Petrol/Diesel', ''"
					+ " union select 684, 'KRIPA NIKETHAN', 'Petrol/Diesel', ''" + " union select 685, 'H P AUTO SERVICE', 'Vehicle', ''"
					+ " union select 686, 'FABINDIA OVER', 'Clothing/Accessory', ''"
					+ " union select 687, 'VARADARAJA SERV STN', 'Petrol/Diesel', ''" + " union select 688, 'M K AHMED', 'Household', ''"
					+ " union select 689, 'SHELL ADARSH', 'Vehicle', ''" + " union select 690, 'BANGALORE ONE', 'Utility', ''"
					+ " union select 691, 'SRI RANGANATHA SER', 'Petrol/Diesel', ''" + " union select 692, 'KANTI SWEETS', 'Food and Grocery', ''"
					+ " union select 693, 'PAYTM MOBILE', 'Mobile Bill', ''" + " union select 694, 'OHRIS TOTAL', 'Eating Out', ''"
					+ " union select 695, 'DELL INDIA', 'Electronics', ''" + " union select 696, 'JALAHALLI SER', 'Petrol/Diesel', ''"
					+ " union select 697, 'SEA ROCK FAMILY RES', 'Eating Out', ''" + " union select 698, 'SUBWAY - BREAD BASKET', 'Eating Out', ''";

			db.execSQL(sql);

			sql = "INSERT INTO sms_category_mapping (_id, merchant, category, notes) "
					+ " select 699 as _id, 'at SKYYE' as merchant, 'Eating Out' as category, '' as notes"
					+ " union select 700, 'KING POWER DUTY', 'Eating Out', ''" + " union select 701, 'MAINLAND CHINA', 'Eating Out', ''"
					+ " union select 702, 'KINDLE-Fantasy', 'Books/Education', ''"
					+ " union select 703, 'SIAM OCEAN WOR', 'Movies/Entertainment', ''" + " union select 704, 'HMS HOST SER', 'Eating Out', ''"
					+ " union select 705, 'made at ANKUR', 'Medical', ''" + " union select 706, 'S S ENTERPRISES', 'Business Cost', ''"
					+ " union select 707, 'THE ATRIA HOTEL', 'Travel/Accomodation', ''"
					+ " union select 708, 'TRICHUR TOWERS', 'Travel/Accomodation', ''" + " union select 709, 'THE OBEROI', 'Travel/Accomodation', ''"
					+ " union select 710, 'KINDLE-Light', 'Books/Education', ''" + " union select 711, 'BOOSTER JUICE', 'Eating Out', ''"
					+ " union select 712, 'HOWARDS STORAGE', 'Furniture', ''" + " union select 713, 'Galibore', 'Travel/Accomodation', ''"
					+ " union select 714, 'KINDLE-Clark', 'Books/Education', ''" + " union select 715, 'LITTLE ITALY', 'Eating Out', ''"
					+ " union select 716, 'BANGALORE CARES', 'Social Welfare', ''" + " union select 717, 'DAILY BREAD GOU', 'Food and Grocery', ''"
					+ " union select 718, 'MSFT *XBOX', 'Movies/Entertainment', ''"
					+ " union select 719, 'ADYAR ANANDA', 'Food and Grocery', 'Sweets'" + " union select 720, 'HUMBLE BUND', 'Social Welfare', ''"
					+ " union select 721, 'ICCR BRITISH', 'Social Welfare', ''" + " union select 722, 'INDIA GARAGE', 'Vehicle', ''"
					+ " union select 723, 'WOODLANDS HOTEL', 'Travel/Accomodation', ''" + " union select 724, 'YADA DADA INTER', 'Eating Out', ''"
					+ " union select 725, 'CAFE'' TORINO', 'Eating Out', ''" + " union select 726, 'PEON SEA FOOD', 'Eating Out', ''"
					+ " union select 727, 'ALL SEASONS BAN', 'Eating Out', ''" + " union select 728, 'GEMS GALLERY', 'Jewellery', ''"
					+ " union select 729, 'SUNNYS INDIRA', 'Eating Out', ''" + " union select 730, 'GREEN PEACE ENV', 'Social Welfare', ''"
					+ " union select 731, 'AXIO PROFESSION', 'Business Cost', ''" + " union select 732, 'VINOD AUTO SERV', 'Petrol/Diesel', ''"
					+ " union select 733, 'BHIMA JEWELLERS', 'Jewellery', ''" + " union select 734, 'HERITAGE RESORT', 'Travel/Accomodation', ''"
					+ " union select 735, 'GURU RAGHAVENDR', 'Social Welfare', ''" + " union select 736, 'HOTEL JADE GAR', 'Travel/Accomodation', ''"
					+ " union select 737, 'MOCHI THE SHOE', 'Clothing/Accessory', ''" + " union select 738, 'BHATS FOODS', 'Food and Grocery', ''"
					+ " union select 739, 'BEIJING BITES', 'Eating Out', ''" + " union select 740, 'MUSEUM INN', 'Travel/Accomodation', ''"
					+ " union select 741, 'TAJ RESIDENCY', 'Travel/Accomodation', ''" + " union select 742, 'SPAGHETTI KITCH', 'Eating Out', ''"
					+ " union select 743, 'NAGA APPLIANCES', 'Electronics', ''" + " union select 744, 'CHEVRON HOT', 'Travel/Accomodation', ''"
					+ " union select 745, 'ADVAITH MOTORS', 'Vehicle', ''" + " union select 746, 'PRASAD PHARMA', 'Medical', ''"
					+ " union select 747, 'BHARTI CELL', 'Mobile Bill', ''" + " union select 748, 'GKB LENS', 'Clothing/Accessory', 'Spectacles'"
					+ " union select 749, 'at IGNOU', 'Books/Education', ''" + " union select 750, 'MALABAR GOLD', 'Jewellery', ''"
					+ " union select 751, 'ICICI PRUDENTIAL LIFE', 'Insurance', ''" + " union select 752, 'MORE-KUR', 'Household', ''"
					+ " union select 753, 'WEST SIDE', 'Clothing/Accessory', ''" + " union select 754, 'B H S R HIGHWAY SER', 'Petrol/Diesel', ''"
					+ " union select 755, 'PANTALOON-FRESH FAS', 'Clothing/Accessory', ''" + " union select 756, 'RELIANCE JEWELS', 'Jewellery', ''"
					+ " union select 757, 'a2zShopping', 'Electronics', ''" + " union select 758, 'TANGERINE', 'Eating Out', ''"
					+ " union select 759, 'VEEKES and THOMAS', 'Eating Out', ''" + " union select 760, 'RRCYCLE', 'Sports', ''"
					+ " union select 761, 'MAHA BAZAR', 'Household', ''" + " union select 762, 'KAKA HALWAI', 'Food and Grocery', 'Sweets'"
					+ " union select 764, 'GRT JEWEL', 'Jewellery', ''" + " union select 765, 'SUPREMOJ FITNESS', 'Sports', ''"
					+ " union select 766, 'VISHAL NIDHI', 'Clothing/Accessory', ''" + " union select 767, 'STELATOES SHOE', 'Clothing/Accessory', ''"
					+ " union select 768, 'SHOE SHOPPE', 'Clothing/Accessory', ''" + " union select 769, 'TRINETHRA SUPER MARK', 'Household', ''"
					+ " union select 770, 'DIVYASHREE ENTERPRIS', 'Gifts', ''" + " union select 771, 'HU SREERAM AGENCY', 'Petrol/Diesel', ''"
					+ " union select 772, 'ANJAPPAR CHETTINAD', 'Eating Out', ''" + " union select 773, 'AMMAN AGENCY', 'Electronics', 'Mobile'"
					+ " union select 774, 'DHL EXPRESS INDIA', 'Misc', 'Courier'" + " union select 775, 'SRI DEVI ENTERPRISES', 'Electronics', ''"
					+ " union select 776, 'Sri Vinayagamoorthy', 'Dharma', ''" + " union select 777, 'PACHAIYAPPAS SILKS', 'Clothing/Accessory', ''"
					+ " union select 778, 'BHARATI TELE', 'Mobile Bill', ''" + " union select 779, 'UNIT OF EUROPA', 'Insurance', ''"
					+ " union select 780, 'at ITALIA', 'Eating Out', ''" + " union select 781, 'MADHARSHA & SONS', 'Clothing/Accessory', ''"
					+ " union select 782, 'A.K.G. LODGING', 'Travel/Accomodation', ''" + " union select 783, 'R R Tyres LB', 'Vehicle', ''"
					+ " union select 784, 'XTREME COM', 'Electronics', ''" + " union select 785, 'CURRIES & PICKLES', 'Eating Out', ''"
					+ " union select 786, 'FUSION WINES', 'Eating Out', ''" + " union select 787, 'VIJAY SALES', 'Electronics', ''"
					+ " union select 788, 'E ZONE', 'Electronics', ''" + " union select 789, 'RELIANCE MEDIA', 'Marketing', ''"
					+ " union select 790, 'ORANGE HOTEL', 'Travel/Accomodation', ''" + " union select 791, 'at Rain Forest', 'Eating Out', ''"
					+ " union select 792, 'LIC HOUSING', 'Loan', ''" + " union select 793, 'RAINFOREST', 'Eating Out', ''"
					+ " union select 794, 'COBB APPARELS', 'Clothing/Accessory', ''" + " union select 795, 'DOCTORS DIAGNOSTIC', 'Medical', ''"
					+ " union select 796, 'DESIRE/MUMBAI', 'Eating Out', ''" + " union select 797, 'HOTEL GIRIJA', 'Travel/Accomodation', ''"
					+ " union select 798, 'WOODLAND', 'Clothing/Accessory', ''" + " union select 799, 'SHREE SUKHAKARTA', 'Clothing/Accessory', ''"
					+ " union select 800, 'BIJASON INFOTECH', 'Electronics', ''" + " union select 801, 'WORLD OF TITAN', 'Clothing/Accessory', ''"
					+ " union select 802, 'R P ENTERPRISES', 'Business Cost', ''"
					+ " union select 803, 'SHIVALIK SERVICE CENTER', 'Petrol/Diesel', ''" + " union select 804, 'SHREE RATNAM', 'Eating Out', ''"
					+ " union select 805, 'HALDIRAM PRODUCTS', 'Food and Grocery', ''" + " union select 806, 'at HALDIRAM', 'Food and Grocery', ''"
					+ " union select 807, 'KRISHNA STO', 'Clothing/Accessory', ''" + " union select 809, 'SCORPIO PETRO', 'Petrol/Diesel', ''"
					+ " union select 810, 'SHYAM SWAAD MITHAI', 'Food and Grocery', ''"
					+ " union select 811, 'PRIYANKA STEEL TUBES', 'Maintenance', ''" + " union select 812, 'SHUBHAM VALLEY RESTAU', 'Eating Out', ''"
					+ " union select 813, 'SAGAR RATNA', 'Eating Out', ''" + " union select 814, 'Endomon', 'Movies/Entertainment', ''"
					+ " union select 815, 'GOOGLE *Disney', 'Movies/Entertainment', ''"
					+ " union select 816, 'GLOBUS STORES', 'Clothing/Accessory', ''" + " union select 817, 'HAJIALLI JUICE', 'Eating Out', ''"
					+ " union select 818, 'NOODLE BAR', 'Eating Out', ''" + " union select 819, 'SAPHIRE HONDA', 'Vehicle', ''"
					+ " union select 820, 'IVORY TOWERS', 'Travel/Accomodation', ''" + " union select 821, 'RX DX HEALTH', 'Medical', ''"
					+ " union select 822, 'SHUBHAM FOODS', 'Eating Out', ''" + " union select 823, 'VENKATESH AUTOMOBILES', 'Vehicle', ''"
					+ " union select 824, 'TELEPHONES bill', 'Mobile Bill', ''"
					+ " union select 825, 'GOOGLE *Rovio', 'Movies/Entertainment', 'Mobile App'"
					+ " union select 826, 'SHARP LOGIC', 'Electronics', ''" + " union select 827, 'BHARTI AIRT', 'Mobile Bill', ''"
					+ " union select 828, 'goodteamstudio', 'Movies/Entertainment', ''" + " union select 829, 'BAR & RESTAU', 'Eating Out', ''"
					+ " union select 830, 'RM K VISVANATHA PILLAI', 'Clothing/Accessory', 'Silk Sarees'"
					+ " union select 831, 'ANISH ENTERPRISES', 'Maintenance', ''" + " union select 832, 'SHANKAR AND SONS', 'Petrol/Diesel', ''"
					+ " union select 833, 'HSR RICE TRADERS', 'Food and Grocery', ''"
					+ " union select 834, 'FOOD PALACE SUPER', 'Food and Grocery', ''" + " union select 835, 'MALAY AI', 'Travel/Accomodation', ''"
					+ " union select 836, 'ONUS PETRO SERVICES', 'Petrol/Diesel', ''" + " union select 837, 'GODADDY.COM', 'Web Server', ''"
					+ " union select 838, 'SRI JAI MANJUNATH TRAD ', 'Food and Grocery', ''" + " union select 839, 'DEALEXTREME', 'Electronics', ''"
					+ " union select 840, 'Bharti Telemedia', 'Cable/DTH', ''"
					+ " union select 841, 'GOOGLE *android', 'Movies/Entertainment', 'Apps'"
					+ " union select 842, 'NIKUNJA CAR FILL', 'Petrol/Diesel', ''" + " union select 843, 'at JAI HANUMAN', 'Petrol/Diesel', ''"
					+ " union select 844, 'BANGALORE TELEPHONES', 'Mobile Bill', ''" + " union select 845, 'SURE FERTILITY', 'Medical', ''"
					+ " union select 846, 'GOOGLE *ZeptoLa', 'Movies/Entertainment', 'Mobile App'"
					+ " union select 847, 'NEW TOWN SUPER MARKET', 'Food and Grocery', ''"
					+ " union select 848, 'RICE N FISH', 'Food and Grocery', ''" + " union select 849, 'SWEEPOUT FD', 'Investment', ''"
					+ " union select 850, 'MOBILE RECHAR', 'Mobile Bill', ''" + " union select 851, 'DEVRAJ ENTERPRISES', 'Petrol/Diesel', ''"
					+ " union select 852, 'ROYAL HOME NEEDS', 'Food and Grocery', ''" + " union select 853, 'MANIPAL HEALTH', 'Medical', ''"
					+ " union select 854, 'SHREE SIDDI VINAYAKA', 'Dharma', ''" + " union select 855, 'Kotak Opportunities', 'Investment', ''"
					+ " union select 856, 'MF Redum', 'Investment', ''" + " union select 857, 'HDFC Mid-Cap', 'Investment', ''"
					+ " union select 858, 'HDFC Top 200 Fund', 'Investment', ''" + " union select 859, 'TataMF', 'Investment', ''"
					+ " union select 860, 'BSLMF', 'Investment', ''" + " union select 861, 'at COOL CLUB', 'Clothing/Accessory', ''"
					+ " union select 862, 'HIBEAM DIAGNOST', 'Medical', ''" + " union select 863, 'NAGPALS GARAGE', 'Petrol/Diesel', ''"
					+ " union select 864, 'paytm mobil', 'Mobile Bill', ''" + " union select 865, 'SURYANSH HOTELS', 'Eating Out', ''"
					+ " union select 866, 'SILKS & SAREES', 'Clothing/Accessory', ''" + " union select 867, 'TRINETHRA TUMKUR', 'Household', ''"
					+ " union select 868, 'R.N. INTERNATIONAL', 'Clothing/Accessory', ''" + " union select 869, 'KAAYAL', 'Eating Out', ''"
					+ " union select 870, 'OJAS WELLNESS', 'Medical', ''" + " union select 871, 'CITY SUPER MARKET', 'Food and Grocery', ''"
					+ " union select 872, 'MARUTHI SERVICE', 'Petrol/Diesel', ''" + " union select 873, 'CATMOSS BA', 'Clothing/Accessory', ''"
					+ " union select 874, 'HDFC TaxSaver', 'Investment', ''" + " union select 875, 'PPF transfer', 'Investment', ''"
					+ " union select 876, 'at MOBILE WORLD', 'Electronics', ''" + " union select 877, 'at AFFECTION', 'Childcare', ''"
					+ " union select 878, 'at SUB WAY', 'Eating Out', ''" + " union select 879, 'at SUBWAY', 'Eating Out', ''"
					+ " union select 880, 'Vashi Central', 'Clothing/Accessory', ''"
					+ " union select 881, 'NATURES BASKET LTD', 'Food and Grocery', ''"
					+ " union select 882, 'DALVI HEIGHTS', 'Household', 'LPG Cylinder'"
					+ " union select 883, 'ARASAPPAR CHETTINADU', 'Eating Out', ''" + " union select 884, 'GANGA PHARMA', 'Medical', ''"
					+ " union select 885, 'CAPTO LIGHT', 'Household', ''" + " union select 886, 'ASHIRWAD HERITAGE', 'Travel/Accomodation', ''"
					+ " union select 887, 'THE CAKE WO', 'Food and Grocery', ''" + " union select 888, 'HDFC Top 200', 'Investment', ''"
					+ " union select 889, 'POSH FOOTWEAR', 'Clothing/Accessory', ''"
					+ " union select 890, 'SANGHANI CORPORATION', 'Food and Grocery', ''"
					+ " union select 891, 'J B PATEL & CO', 'Petrol/Diesel', ''" + " union select 892, 'TRUST CHEMISTS', 'Medical', ''"
					+ " union select 893, 'PATIL AUTOMOBILES', 'Petrol/Diesel', ''" + " union select 894, 'DOMINO''S', 'Eating Out', ''"
					+ " union select 895, 'GARDEN COURT REST', 'Eating Out', ''" + " union select 896, 'APR SPIRIT ZONE', 'Eating Out', ''"
					+ " union select 897, 'VISHAL MEDICAL', 'Medical', ''" + " union select 898, 'HIRANANDANI GARDEN P', 'Eating Out', ''"
					+ " union select 899, 'MAXUS CINEMAS SAKINA', 'Movies/Entertainment', ''"
					+ " union select 900, 'BATA SHOE', 'Clothing/Accessory', ''" + " union select 901, 'HEALTH NEST', 'Medical', ''"
					+ " union select 902, 'AVIGNA ENTERPRISES', 'Eating Out', ''" + " union select 903, 'HDFC Equity Fund', 'Investment', ''"
					+ " union select 904, 'FEET WORLD', 'Clothing/Accessory', ''" + " union select 905, 'TRISTAR MOT', 'Vehicle', ''"
					+ " union select 906, 'SAPNA BOOK HOUS', 'Books/Education', ''" + " union select 907, 'FAVOURITE SHOP', 'Clothing/Accessory', ''"
					+ " union select 908, 'FARICO SILK', 'Clothing/Accessory', ''" + " union select 909, 'at TATASKY', 'Cable/DTH', ''"
					+ " union select 910, 'COLUMBIA ASIA HOSP', 'Mobile Bill', ''" + " union select 911, 'SHALINI MACHADO BDS', 'Medical', ''"
					+ " union select 912, 'GOOGLE *TeslaCo', 'Movies/Entertainment', 'Mobile App'"
					+ " union select 913, 'SUPERMARTS PVT LTD', 'Food and Grocery', ''"
					+ " union select 914, 'PURUSHARTH AUTO SER', 'Petrol/Diesel', ''" + " union select 915, 'VISABILLPAY-SPICE', 'Mobile Bill', ''"
					+ " union select 916, 'AIRTELCELL', 'Mobile Bill', ''" + " union select 917, 'PIZZA HUT', 'Eating Out', ''"
					+ " union select 918, 'bigbasket.com', 'Food and Grocery', ''"
					+ " union select 919, 'LEMON TREE HOTEL', 'Travel/Accomodation', ''"
					+ " union select 920, 'FOODWORLD SUPER MKTS', 'Food and Grocery', ''"
					+ " union select 921, 'GANGAR OPTICIANS', 'Clothing/Accessory', ''"
					+ " union select 922, 'TicketNew.com', 'Movies/Entertainment', ''"
					+ " union select 923, 'KANNAN CATERING', 'Food and Grocery', ''"
					+ " union select 924, 'CHENNAI at WINGS', 'Travel/Accomodation', ''"
					+ " union select 925, 'AT WESTSIDE', 'Clothing/Accessory', ''" + " union select 926, 'ADIGAS FAST FOO', 'Eating Out', ''"
					+ " union select 927, 'PUJARA & COMPANY', 'Clothing/Accessory', ''"
					+ " union select 928, 'PUJARA AND COMPANY', 'Clothing/Accessory', ''"
					+ " union select 929, 'AT NANAK SONS', 'Clothing/Accessory', ''"
					+ " union select 930, 'BUDDHULAL DEVCHAND', 'Clothing/Accessory', ''" + " union select 931, 'BSNL BILLDE', 'Mobile Bill', ''"
					+ " union select 932, 'SUKH SAGAR', 'Eating Out', ''" + " union select 933, 'SHELL GANESH ENTER', 'Petrol/Diesel', ''"
					+ " union select 934, 'Yatra Online Pvt', 'Travel/Accomodation', ''"
					+ " union select 935, 'BONBON SERVICE CE', 'Petrol/Diesel', ''"
					+ " union select 936, 'FOODLINK SERVICES INDIA', 'Eating Out', ''" + " union select 937, 'SHREE JAGDISH', 'Electronics', ''"
					+ " union select 938, 'at tbz', 'Jewellery', ''" + " union select 939, 'HOTEL SARAVANA', 'Eating Out', ''"
					+ " union select 940, 'GARDEN CITY SERVICE', 'Petrol/Diesel', ''" + " union select 941, 'DRAPES VIEW', 'Furniture', ''"
					+ " union select 942, 'GOPI SERVICE STATION', 'Petrol/Diesel', ''"
					+ " union select 943, 'LIFE OFC MGMT ASSOC', 'Books/Education', ''"
					+ " union select 944, 'BIRLA SUN LIFE TAX RELIEF 96 GROWTH', 'Investment', ''"
					+ " union select 945, 'ROYAL HOSPI', 'Medical', ''" + " union select 946, 'Snapfish by HP', 'Photography', ''"
					+ " union select 947, 'VODAFONE-BILLDESK', 'Mobile Bill', ''" + " union select 948, 'MURUGAN STORES', 'Household', ''"
					+ " union select 949, 'SIDDHI VINAYAK MOTORS', 'Vehicle', ''" + " union select 950, 'HOTEL MANDOVI', 'Travel/Accomodation', ''"
					+ " union select 951, 'UK DRY FRUIT', 'Food and Grocery', ''" + " union select 952, 'LATTA SUPER MARKET', 'Food and Grocery', ''"
					+ " union select 953, 'PAZHAMUDHIR CHOLAI', 'Food and Grocery', ''" + " union select 954, 'CARZ N DOLL', 'Childcare', ''"
					+ " union select 955, 'HOTTENTOTS', 'Food and Grocery', ''";

			db.execSQL(sql);

			try {
				db.execSQL(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql;
			switch (oldVersion) {
			case 1:
				if (!isTableExists(SMS_CAT_MAP_TABLE, db)) {
					db.execSQL(DATABASE_CREATE_SMS_CATEGORY_MAPPING_TABLE);
				}
			case 3:
				// nasty bug because ppl with older database has a null or 0
				// timestamp
				sql = "update expense set time_stamp = (julianday(expense_date) - 2440587.5)*86400.0*1000";
				db.execSQL(sql);
			case 4:
				if (Log.DEBUG)
					Log.v("Upgrading stuff");
				// delete current rows from sms category mapping table
				db.delete(DATABASE_TABLE_SMS_CATEGORY_MAPPING, null, null);
				// insert new ones
				populateSmsCategoryMappingTable(db);

				// Replace Travel Category with Travel/Accomodation
				ContentValues values = new ContentValues();
				values.put(KEY_CATEGORY_NAME, "Travel/Accomodation");
				String where = KEY_CATEGORY_NAME + "=?";
				String[] whereArgs = new String[] { "Travel" };
				db.update(DATABASE_TABLE_CATEGORY, values, where, whereArgs);
				// do the replacement in expense table
				values.clear();
				values.put(KEY_EXPENSE_CATEGORY_NAME, "Travel/Accomodation");
				where = KEY_EXPENSE_CATEGORY_NAME + "=?";
				db.update(DATABASE_TABLE_EXPENSE, values, where, whereArgs);

				// Replace Clothing category with Clothing/Accessory
				values.clear();
				values.put(KEY_CATEGORY_NAME, "Clothing/Accessory");
				where = KEY_CATEGORY_NAME + "=?";
				whereArgs[0] = "Clothing";
				db.update(DATABASE_TABLE_CATEGORY, values, where, whereArgs);
				// do the replacement in expense table
				values.clear();
				values.put(KEY_EXPENSE_CATEGORY_NAME, "Clothing/Accessory");
				where = KEY_EXPENSE_CATEGORY_NAME + "=?";
				db.update(DATABASE_TABLE_EXPENSE, values, where, whereArgs);

			default:
				if (!isTableExists(SMS_CAT_MAP_TABLE, db)) {
					db.execSQL(DATABASE_CREATE_SMS_CATEGORY_MAPPING_TABLE);
					populateSmsCategoryMappingTable(db);
				}
				break;
			}
		}

		public boolean isTableExists(String tableName, SQLiteDatabase db) {
			Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					cursor.close();
					return true;
				}
				cursor.close();
			}
			return false;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// if(Log.DEBUG) Log.v("DataProvider: delete");
		// Using SQLiteQueryBuilder instead of query() method
		int uriType = sUriMatcher.match(uri);
		int rowsDeleted = 0;
		String id = null;

		switch (uriType) {
		case EXPENSES:
			rowsDeleted = mDb.delete(EXPENSE_TABLE, selection, selectionArgs);
			break;
		case EXPENSES_ID:
			id = uri.getLastPathSegment();
			rowsDeleted = mDb.delete(EXPENSE_TABLE, KEY_EXPENSE_ID + "=" + id, null);
			break;
		case CATEGORIES:
			rowsDeleted = mDb.delete(CATEGORY_TABLE, selection, selectionArgs);
			break;
		case CATEGORIES_ID:
			id = uri.getLastPathSegment();
			rowsDeleted = mDb.delete(CATEGORY_TABLE, KEY_CATEGORY_ID + "=" + id, null);
			break;
		case BANKS:
			rowsDeleted = mDb.delete(BANK_TABLE, selection, selectionArgs);
			break;
		case BANK_ID:
			id = uri.getLastPathSegment();
			rowsDeleted = mDb.delete(BANK_TABLE, KEY_BANK_ID + "=" + id, null);
			break;
		case REMINDERS:
			rowsDeleted = mDb.delete(REMINDER_TABLE, selection, selectionArgs);
			break;
		case REMINDERS_ID:
			id = uri.getLastPathSegment();
			rowsDeleted = mDb.delete(REMINDER_TABLE, KEY_REMINDER_ID + "=" + id, null);
			break;
		case USER_PROFILES:
			rowsDeleted = mDb.delete(USER_PROFILE_TABLE, selection, selectionArgs);
			break;
		case USER_PROFILES_ID:
			id = uri.getLastPathSegment();
			rowsDeleted = mDb.delete(USER_PROFILE_TABLE, KEY_USER_PROFILE_ID + "=" + id, null);
			break;
		case SMS_CAT_MAPS:
			rowsDeleted = mDb.delete(SMS_CAT_MAP_TABLE, selection, selectionArgs);
			break;
		case SMS_CAT_MAPS_ID:
			id = uri.getLastPathSegment();
			rowsDeleted = mDb.delete(SMS_CAT_MAP_TABLE, KEY_SMS_CAT_MAP_ID + "=" + id, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// if(Log.DEBUG) Log.v("DataProvider: insert");
		int uriType = sUriMatcher.match(uri);
		long id = 0;

		switch (uriType) {
		case EXPENSES:
			id = mDb.insert(EXPENSE_TABLE, null, values);
			if (id > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				// if(Log.DEBUG)
				// Log.v("DataProvider: insert: Expense inserted successfully");
			} else {
				// if(Log.DEBUG)
				// Log.v("DataProvider: insert: Could not insert expense");
			}
			return Uri.parse(EXPENSE_BASE_PATH + "/" + id);
		case REMINDERS:
			id = mDb.insert(REMINDER_TABLE, null, values);
			if (id > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
			} else {
				// if(Log.DEBUG)
				// Log.v("DataProvider: insert: Could not insert reminder");
			}
			return Uri.parse(CATEGORY_BASE_PATH + "/" + id);
		case CATEGORIES:
			id = mDb.insert(CATEGORY_TABLE, null, values);
			if (id > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
			} else {
				// if(Log.DEBUG)
				// Log.v("DataProvider: insert: Could not insert category");
			}
			return Uri.parse(CATEGORY_BASE_PATH + "/" + id);
		case BANKS:
			id = mDb.insert(BANK_TABLE, null, values);
			if (id > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
			} else {
				if (Log.DEBUG)
					Log.v("DataProvider: insert: Could not insert bank");
			}
			return Uri.parse(CATEGORY_BASE_PATH + "/" + id);
		case USER_PROFILES:
			id = mDb.insert(USER_PROFILE_TABLE, null, values);
			if (id > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
			} else {
				if (Log.DEBUG)
					Log.v("DataProvider: insert: Could not insert bank");
			}
			return Uri.parse(CATEGORY_BASE_PATH + "/" + id);
		case SMS_CAT_MAPS:
			id = mDb.insert(SMS_CAT_MAP_TABLE, null, values);
			if (id > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
			} else {
				if (Log.DEBUG)
					Log.v("DataProvider: insert: Could not insert sms_cat_map row");
			}
			return Uri.parse(CATEGORY_BASE_PATH + "/" + id);

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		// if(Log.DEBUG) Log.v("DataProvider: onCreate");
		// TODO Auto-generated method stub
		mCtx = getContext();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		final String welcomeScreenShownPref = "welcomeScreenShown";
		Boolean welcomeScreenShown = prefs.getBoolean(welcomeScreenShownPref, false);
		boolean inEmulator = "generic".equals(Build.BRAND.toLowerCase());
		if (inEmulator) {
			welcomeScreenShown = false;
		}
		if (welcomeScreenShown == false) {
			// getContext().deleteDatabase(DATABASE_NAME);
		}

		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return false;
	}

	public Cursor getExpensesByCategory() {
		// if(Log.DEBUG) Log.v("DataProvider: getExpensesByCategory");
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// if(Log.DEBUG) Log.v("DataProvider: query: "+uri.toString());
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		Cursor cursor = null;
		String lastPathSegment;

		int uriType = sUriMatcher.match(uri);

		switch (uriType) {
		case EXPENSES:
			queryBuilder.setTables(EXPENSE_TABLE);
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case EXPENSES_ID:
			queryBuilder.setTables(EXPENSE_TABLE);
			queryBuilder.appendWhere(KEY_EXPENSE_ID + "=" + uri.getLastPathSegment());
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case EXPENSES_BY_CATEGORY:
			cursor = mDb.rawQuery("select _id, category_name, sum(amount) as sum_amount from expense where " + KEY_EXPENSE_STATE
					+ " like '%saved%' group by category_name", selectionArgs);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case EXPENSES_BY_CATEGORY_FILTER:
			// if (Log.DEBUG)
			// Log.v("DataProvider: query: bycategory: filter: "
			// + uri.getLastPathSegment());
			int days;
			lastPathSegment = uri.getLastPathSegment();
			days = Integer.parseInt(lastPathSegment);
			cursor = mDb.rawQuery("select _id, category_name, sum(amount) as sum_amount from expense where " + "expense_date>=date('now', '-" + days
					+ " days') and " + KEY_EXPENSE_STATE + " like '%saved%' group by category_name order by sum_amount DESC", selectionArgs);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case EXPENSES_UNAUDITED:
			// Log.v("DataProvider: query: unaudited");
			cursor = mDb.rawQuery("select expense._id, expense.amount, expense.bank_name, expense.category_name, expense.expense_date, "
					+ "expense.merchant, expense.location, expense.state, expense.sms_body, expense.sender_id, "
					+ " user_profile.device_id, user_profile.phone_model, user_profile.os_version, user_profile.email, user_profile.phone_number"
					+ " from expense cross join user_profile where expense.audited=0 or expense.audited is null", selectionArgs);
			// Log.v("Number of unaudited expenses: " + cursor.getCount());
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case CATEGORIES:
			queryBuilder.setTables(CATEGORY_TABLE);
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case CATEGORIES_ID:
			queryBuilder.setTables(CATEGORY_TABLE);
			queryBuilder.appendWhere(KEY_CATEGORY_ID + "=" + uri.getLastPathSegment());
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case BANKS:
			queryBuilder.setTables(BANK_TABLE);
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case BANK_ID:
			queryBuilder.setTables(BANK_TABLE);
			queryBuilder.appendWhere(KEY_BANK_ID + "=" + uri.getLastPathSegment());
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case REMINDERS:
			queryBuilder.setTables(REMINDER_TABLE);
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case REMINDERS_ID:
			queryBuilder.setTables(REMINDER_TABLE);
			queryBuilder.appendWhere(KEY_REMINDER_ID + "=" + uri.getLastPathSegment());
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case USER_PROFILES:
			queryBuilder.setTables(SMS_CAT_MAP_TABLE);
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case SMS_CAT_MAPS:
			queryBuilder.setTables(SMS_CAT_MAP_TABLE);
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		case SMS_CAT_MAPS_ID:
			queryBuilder.setTables(CATEGORY_TABLE);
			queryBuilder.appendWhere(KEY_SMS_CAT_MAP_ID + "=" + uri.getLastPathSegment());
			cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(mCtx.getContentResolver(), uri);
			break;
		default:
			// throw expception if we can't recognize the uri
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// if(Log.DEBUG) Log.v("DataProvider: update");
		int uriType = sUriMatcher.match(uri);
		int rowsUpdated = 0;
		String id = null;

		switch (uriType) {
		case EXPENSES:
			rowsUpdated = mDb.update(EXPENSE_TABLE, values, selection, selectionArgs);
			break;
		case EXPENSES_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = mDb.update(EXPENSE_TABLE, values, KEY_EXPENSE_ID + "=" + id, null);
			} else {
				rowsUpdated = mDb.update(EXPENSE_TABLE, values, KEY_EXPENSE_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case CATEGORIES:
			rowsUpdated = mDb.update(CATEGORY_TABLE, values, selection, selectionArgs);
			break;
		case CATEGORIES_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = mDb.update(CATEGORY_TABLE, values, KEY_CATEGORY_ID + "=" + id, null);
			} else {
				rowsUpdated = mDb.update(CATEGORY_TABLE, values, KEY_CATEGORY_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case BANKS:
			rowsUpdated = mDb.update(BANK_TABLE, values, selection, selectionArgs);
			break;
		case BANK_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = mDb.update(BANK_TABLE, values, KEY_BANK_ID + "=" + id, null);
			} else {
				rowsUpdated = mDb.update(BANK_TABLE, values, KEY_BANK_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case REMINDERS:
			rowsUpdated = mDb.update(REMINDER_TABLE, values, selection, selectionArgs);
			break;
		case REMINDERS_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = mDb.update(REMINDER_TABLE, values, KEY_REMINDER_ID + "=" + id, null);
			} else {
				rowsUpdated = mDb.update(REMINDER_TABLE, values, KEY_REMINDER_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case USER_PROFILES:
			rowsUpdated = mDb.update(USER_PROFILE_TABLE, values, selection, selectionArgs);
			break;
		case USER_PROFILES_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = mDb.update(USER_PROFILE_TABLE, values, KEY_USER_PROFILE_ID + "=" + id, null);
			} else {
				rowsUpdated = mDb.update(USER_PROFILE_TABLE, values, KEY_USER_PROFILE_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		case SMS_CAT_MAPS:
			rowsUpdated = mDb.update(SMS_CAT_MAP_TABLE, values, selection, selectionArgs);
			break;
		case SMS_CAT_MAPS_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = mDb.update(SMS_CAT_MAP_TABLE, values, KEY_SMS_CAT_MAP_ID + "=" + id, null);
			} else {
				rowsUpdated = mDb.update(SMS_CAT_MAP_TABLE, values, KEY_SMS_CAT_MAP_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
}
