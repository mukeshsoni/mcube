package com.brizztv.mcube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DbAdapter {
	public static final Uri CONTENT_URI = 
		    Uri.parse("content://com.brizztv.mcube.smsExpense/expense");
	
    public static final String KEY_EXPENSE_AMOUNT = "amount";
    public static final String KEY_EXPENSE_NOTES = "notes";
    public static final String KEY_EXPENSE_CATEGORY_ID = "category_id";
    public static final String KEY_EXPENSE_CATEGORY_NAME = "category_name";
    public static final String KEY_EXPENSE_DATE = "date";
    public static final String KEY_EXPENSE_DAY = "day";
    public static final String KEY_EXPENSE_MONTH = "month";
    public static final String KEY_EXPENSE_YEAR = "year";
    public static final String KEY_EXPENSE_ID = "_id";
    
    public static final String KEY_CATEGORY_ID = "_id";
    public static final String KEY_CATEGORY_NAME = "name";
    
    public static final String KEY_REMINDER_ID = "_id";
    public static final String KEY_REMINDER_DATE = "expense_date";
    public static final String KEY_REMINDER_AMOUNT = "amount";
    public static final String KEY_REMINDER_DAY = "day";
    public static final String KEY_REMINDER_MONTH = "month";
    public static final String KEY_REMINDER_YEAR = "year";

    public static final String KEY_INCOME_ID = "_id";
    public static final String KEY_INCOME_AMOUNT = "amount";
    public static final String KEY_INCOME_DAY = "day";
    public static final String KEY_INCOME_MONTH = "month";
    public static final String KEY_INCOME_YEAR = "year";
    public static final String KEY_INCOME_NOTES = "notes";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_EXPENSE_TABLE =
        "CREATE TABLE expense (_id integer primary key autoincrement, amount integer not null, notes text, category_id integer, "
        		+"expense_date date default CURRENT_DATE, day int, month int, year int, category_name text);";

    private static final String DATABASE_CREATE_CATEGORY_TABLE = 
    		"create table category (_id integer primary key autoincrement, "
    		+ "name text not null);";
    
    private static final String DATABASE_CREATE_REMINDER_TABLE = 
    		"CREATE TABLE reminder (_id integer primary key autoincrement, amount integer not null, "
    		+"expense_date date default CURRENT_DATE, day int, month int, year int);";
    				
    private static final String DATABASE_CREATE_INCOME_TABLE = 
    		"CREATE TABLE income (_id integer primary key autoincrement, amount integer not null, "
    		+"day int, month int, year int);";
    
    private static final String DATABASE_NAME = "sms_expense";
    private static final String DATABASE_TABLE_EXPENSE = "expense";
    private static final String DATABASE_TABLE_CATEGORY = "category";
    private static final String DATABASE_TABLE_REMINDER = "reminder";
    private static final String DATABASE_TABLE_INCOME = "income";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	if(Log.DEBUG) Log.v("trying to create database tables");
            db.execSQL(DATABASE_CREATE_EXPENSE_TABLE);
            db.execSQL(DATABASE_CREATE_CATEGORY_TABLE);
            db.execSQL(DATABASE_CREATE_REMINDER_TABLE);
            db.execSQL(DATABASE_CREATE_INCOME_TABLE);
            
            // TODO - insert some default categories here
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	// TODO - have to remove this code and think about how upgrade will work. Current upgrade will remove all user data!!
            if(Log.DEBUG) Log.v("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_EXPENSE);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CATEGORY);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_REMINDER);
            onCreate(db);
        }
    }
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the sms_expenser database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public long addExpense(long expenseAmount, String categoryName, int day, int month, int year, String notes) {
    	ContentValues initialValues = new ContentValues();
    	
//    	long category_id = getCategoryIdForName(categoryName);
    	initialValues.put(KEY_EXPENSE_AMOUNT, expenseAmount);
    	initialValues.put(KEY_EXPENSE_CATEGORY_NAME, categoryName);
    	initialValues.put(KEY_EXPENSE_NOTES, notes);
    	initialValues.put(KEY_EXPENSE_DAY, day);
    	initialValues.put(KEY_EXPENSE_MONTH, month);
    	initialValues.put(KEY_EXPENSE_YEAR, year);
    	
    	return mDb.insert(DATABASE_TABLE_EXPENSE, null, initialValues);
    }
    
    /*
     * returns true if the update was successful
     */
    public boolean updateExpense(long id, long expenseAmount, String categoryName, int day, int month, int year, String notes) {
    	ContentValues updatedValues = new ContentValues();
    	
    	updatedValues.put(KEY_EXPENSE_AMOUNT, expenseAmount);
    	updatedValues.put(KEY_EXPENSE_CATEGORY_NAME, categoryName);
    	updatedValues.put(KEY_EXPENSE_NOTES, notes);
    	updatedValues.put(KEY_EXPENSE_DAY, day);
    	updatedValues.put(KEY_EXPENSE_MONTH, month);
    	updatedValues.put(KEY_EXPENSE_YEAR, year);
    	
    	return mDb.update(DATABASE_TABLE_EXPENSE, updatedValues, KEY_EXPENSE_ID+"="+id, null) > 0;
    }
    
    public boolean deleteExpense(long id) {
    	return (mDb.delete(DATABASE_TABLE_EXPENSE, KEY_EXPENSE_ID+" = "+id, null)==1);
    }
 
    /*
     * need a single record when editing an expense entry
     */
    public Cursor getExpenseForId(long id){
    	String[] projection = new String[] {
    								KEY_EXPENSE_AMOUNT, 
    								KEY_EXPENSE_CATEGORY_NAME, 
    								KEY_EXPENSE_DAY, 
    								KEY_EXPENSE_MONTH, 
    								KEY_EXPENSE_YEAR, 
    								KEY_EXPENSE_NOTES
    							};
    	return mDb.query(true, DATABASE_TABLE_EXPENSE, projection, KEY_EXPENSE_ID+"="+id, null, null, null, null, null);
    }
    
    public Cursor getAllExpenses(){
    	String[] projection = new String[] {
    								KEY_EXPENSE_ID,
									KEY_EXPENSE_AMOUNT, 
									KEY_EXPENSE_CATEGORY_NAME, 
									KEY_EXPENSE_DAY, 
									KEY_EXPENSE_MONTH, 
									KEY_EXPENSE_YEAR, 
									KEY_EXPENSE_NOTES
								};
    	
    	String orderBy = KEY_EXPENSE_YEAR+","+KEY_EXPENSE_MONTH+","+KEY_EXPENSE_DAY+" DESC";
    	return mDb.query(DATABASE_TABLE_EXPENSE, projection, null, null, null, null, orderBy);
    }
    
    public Cursor getAllIncome() {
    	String[] projection = new String[] {
    								KEY_INCOME_ID,
    								KEY_INCOME_AMOUNT,
    								KEY_INCOME_DAY, 
									KEY_INCOME_MONTH, 
									KEY_INCOME_YEAR, 
									KEY_INCOME_NOTES
    							};
    	String orderBy = KEY_INCOME_YEAR+","+KEY_INCOME_MONTH+","+KEY_INCOME_DAY+" DESC";
    	return mDb.query(DATABASE_TABLE_INCOME, projection, null, null, null, null, orderBy);
    }
    
    public Cursor getIncomeForId(long id) {
    	String[] projection = new String[] {
				KEY_INCOME_ID,
				KEY_INCOME_AMOUNT,
				KEY_INCOME_DAY, 
				KEY_INCOME_MONTH, 
				KEY_INCOME_YEAR, 
				KEY_INCOME_NOTES
			};
    	return mDb.query(DATABASE_TABLE_INCOME, projection, KEY_INCOME_ID+"="+id, null, null, null, null);
    }
    
    public long addIncome(long incomeAmount, int day, int month, int year, String notes){
    	ContentValues initialValues = new ContentValues();
    	
//    	long category_id = getCategoryIdForName(categoryName);
    	initialValues.put(KEY_INCOME_AMOUNT, incomeAmount);
//    	initialValues.put(KEY_INCOME_CATEGORY_NAME, categoryName);
    	initialValues.put(KEY_INCOME_NOTES, notes);
    	initialValues.put(KEY_INCOME_DAY, day);
    	initialValues.put(KEY_INCOME_MONTH, month);
    	initialValues.put(KEY_INCOME_YEAR, year);
    	
    	return mDb.insert(DATABASE_TABLE_INCOME, null, initialValues);	
    }
   
    /*
     * returns true if the update was successful
     */
    public boolean updateIncome(long id, long incomeAmount, int day, int month, int year, String notes) {
    	ContentValues updatedValues = new ContentValues();
    	
    	updatedValues.put(KEY_INCOME_AMOUNT, incomeAmount);
//    	updatedValues.put(KEY_INCOME_CATEGORY_NAME, categoryName);
    	updatedValues.put(KEY_INCOME_NOTES, notes);
    	updatedValues.put(KEY_INCOME_DAY, day);
    	updatedValues.put(KEY_INCOME_MONTH, month);
    	updatedValues.put(KEY_INCOME_YEAR, year);
    	
    	return mDb.update(DATABASE_TABLE_INCOME, updatedValues, KEY_INCOME_ID+"="+id, null) > 0;
    }
    
    public boolean deleteIncome(long id) {
    	return (mDb.delete(DATABASE_TABLE_INCOME, KEY_INCOME_ID+" = "+id, null)==1);
    }
    
    public long getCategoryIdForName(String categoryName) {
    	Cursor cursor = mDb.query(true, DATABASE_TABLE_CATEGORY, new String[]{KEY_CATEGORY_ID}, KEY_CATEGORY_NAME+"="+"'"+categoryName+"'", null, null, null, null, null);
    	cursor.moveToFirst();
    	return cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ID));
    }
    
    public boolean deleteCategory(String categoryName) {
    	return (mDb.delete(DATABASE_TABLE_CATEGORY, KEY_CATEGORY_NAME+" = "+categoryName, null)==1);
    }
    
    public boolean deleteCategory(long id) {
    	return (mDb.delete(DATABASE_TABLE_CATEGORY, KEY_CATEGORY_ID+" = "+id, null)==1);
    }
    
    public long addReminder(long expenseAmount, int day, int month, int year) {
    	ContentValues cv = new ContentValues();
    	
    	cv.put(KEY_REMINDER_AMOUNT, expenseAmount);
    	cv.put(KEY_EXPENSE_DAY, day);
    	cv.put(KEY_EXPENSE_MONTH, month);
    	cv.put(KEY_EXPENSE_YEAR, year);
    	
    	return mDb.insert(DATABASE_TABLE_REMINDER, null, cv);
    }
    
    public boolean deleteReminder(long id) {
    	return (mDb.delete(DATABASE_TABLE_REMINDER, KEY_REMINDER_ID+" = "+id, null)==1);
    }

    public Cursor getReminderForId(long reminderId) {
    	String[] projection = new String[] {
				KEY_REMINDER_ID,
				KEY_REMINDER_AMOUNT,
				KEY_REMINDER_DAY,
				KEY_REMINDER_MONTH,
				KEY_REMINDER_YEAR,
			};
    	return mDb.query(true, DATABASE_TABLE_REMINDER, projection, KEY_REMINDER_ID+"="+reminderId, null, null, null, null, null);
    }
    
    public Cursor getAllReminders() {
    	String[] projection = new String[] {
    								KEY_REMINDER_ID,
    								KEY_REMINDER_AMOUNT,
    								KEY_REMINDER_DAY,
    								KEY_REMINDER_MONTH,
    								KEY_REMINDER_YEAR,
    							};
    	String orderBy = KEY_REMINDER_YEAR+","+KEY_REMINDER_MONTH+","+KEY_REMINDER_DAY+" DESC";
    	return mDb.query(DATABASE_TABLE_REMINDER, projection, null, null, null, null, orderBy);
    }
    
    public void truncateExpenseTable() {
    	mDb.execSQL("DELETE FROM "+DATABASE_TABLE_EXPENSE);
    }

    public void truncateReminderTable() {
    	mDb.execSQL("DELETE FROM "+DATABASE_TABLE_REMINDER);
    }
    
    public String getCategoryNameForId(long id){
    	Cursor cursor = mDb.query(true, DATABASE_TABLE_CATEGORY, new String[]{KEY_CATEGORY_NAME}, KEY_CATEGORY_ID+"="+id, null, null, null, null, null);
    	cursor.moveToFirst();
    	return cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME));
    }
    

    public Cursor getAllCategories() {
    	return mDb.query(DATABASE_TABLE_CATEGORY, new String[]{KEY_CATEGORY_ID, KEY_CATEGORY_NAME}, null, null, null, null, null);
    }
    
    public long addCategory(String categoryName) {
    	ContentValues values = new ContentValues();
    	
    	values.put(KEY_CATEGORY_NAME, categoryName);
    	return mDb.insert(DATABASE_TABLE_CATEGORY, null, values);
    }
    
    public void close() {
    	if(mDbHelper != null)
    		mDbHelper.close();
    }
    
    public void alterTable() throws SQLException{
    	//    	mDb.execSQL("ALTER TABLE " + DATABASE_TABLE_REMINDER + " ADD COLUMN " + KEY_REMINDER_DAY + " int");
//    	mDb.execSQL("ALTER TABLE " + DATABASE_TABLE_REMINDER + " ADD COLUMN " + KEY_REMINDER_MONTH + " int");
//    	mDb.execSQL("ALTER TABLE " + DATABASE_TABLE_REMINDER + " ADD COLUMN " + KEY_REMINDER_YEAR + " int");
//    	mDb.execSQL("ALTER TABLE " + DATABASE_TABLE_EXPENSE + " ADD COLUMN " + KEY_EXPENSE_CATEGORY_NAME + " text");
    	mDb.execSQL("ALTER TABLE " + DATABASE_TABLE_INCOME + " ADD COLUMN " + KEY_INCOME_NOTES + " text");
    }
}