package com.deeset.deesetsurvey.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import org.ksoap2.serialization.SoapObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	private final Context ctx;
	private DatabaseHelper mDBHelper;
	private SQLiteDatabase mDB;
	private static final String DATABASE_NAME = "dbdeesetsurvey";
	private static final int VERSION = 1;
	private ContentValues content;

	public static final String TABLE_USER = "TblUser";
	public static final String TABLE_CHAIN = "TblChain";
	public static final String TABLE_STORE = "TblStore";
	public static final String TABLE_TIMEUPDATE = "TblTimeUpdate";
	public static final String TABLE_SURVEY = "TblSurvey";
	public static final String TABLE_QUESTION = "TblQuestion";
	public static final String TABLE_ANSWER = "TblAnswer";
	public static final String TABLE_RESULT = "TblResult";

	public static final String LOG_ALL_CHAIN = "ALLCHAIN";
	public static final String LOG_CHAIN = "CHAIN";
	public static final String LOG_STORE = "STORE";
	public static final String LOG_SURVEY = "SURVEY";
	public static final String LOG_QUESTION = "QUESTION";
	public static final String LOG_ANSWER = "ANSWER";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private Context ctx;

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			this.ctx = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				executeSqlScript(ctx, db, "deeset_survey.sql", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static int executeSqlScript(Context context, SQLiteDatabase db,
				String assetFilename, boolean transactional) throws IOException {
			byte[] bytes = readAsset(context, assetFilename);
			String sql = new String(bytes, "UTF-8");
			String[] lines = sql.split(";(\\s)*[\n\r]");
			int count;
			if (transactional) {
				count = executeSqlStatementsInTx(db, lines);
			} else {
				count = executeSqlStatements(db, lines);
			}
			return count;
		}

		public static int executeSqlStatementsInTx(SQLiteDatabase db,
				String[] statements) {
			db.beginTransaction();
			try {
				int count = executeSqlStatements(db, statements);
				db.setTransactionSuccessful();
				return count;
			} finally {
				db.endTransaction();
			}
		}

		public static int executeSqlStatements(SQLiteDatabase db,
				String[] statements) {
			int count = 0;
			for (String line : statements) {
				line = line.trim();
				if (line.length() > 0) {
					db.execSQL(line);
					count++;
				}
			}
			return count;
		}

		/**
		 * Copies all available data from in to out without closing any stream.
		 * 
		 * @return number of bytes copied
		 */

		public static int copyAllBytes(InputStream in, OutputStream out)
				throws IOException {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			while (true) {
				int read = in.read(buffer);
				if (read == -1) {
					break;
				}
				out.write(buffer, 0, read);
				byteCount += read;
			}
			return byteCount;
		}

		public static byte[] readAllBytes(InputStream in) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copyAllBytes(in, out);
			return out.toByteArray();
		}

		public static byte[] readAsset(Context context, String filename)
				throws IOException {
			InputStream in = context.getResources().getAssets().open(filename);
			try {
				return readAllBytes(in);
			} finally {
				in.close();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}
	}

	public DBAdapter(Context ctx) {
		this.ctx = ctx;
	}

	public DBAdapter open() {
		mDBHelper = new DatabaseHelper(ctx, DATABASE_NAME, null, VERSION);
		mDB = mDBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDBHelper.close();
	}
	
	public boolean isOpen() {
		if (mDB.isOpen()) {
			return true;
		}
		return false;
	}

	public void insertUser(ContentValues content) {
		try {
			mDB.beginTransaction();
			String deleteUser = "DELETE FROM " + TABLE_USER + " WHERE UserID='"
					+ content.getAsString("UserID") + "'";
			mDB.execSQL(deleteUser);
			String insertUser = "INSERT INTO " + TABLE_USER
					+ "(UserID, Username, Password) VALUES('"
					+ content.getAsString("UserID") + "','"
					+ content.getAsString("Username") + "','"
					+ content.getAsString("Password") + "');";
			mDB.execSQL(insertUser);
			mDB.setTransactionSuccessful();
		} catch (Exception e) {
			mDB.endTransaction();
		} finally {
			mDB.endTransaction();
		}
	}

	public void insertChain(SoapObject soap, long longTimeUpdate) {
		try {
			mDB.beginTransaction();
			String deleteChains = "DELETE FROM " + TABLE_CHAIN;
			mDB.execSQL(deleteChains);
			int total = soap.getPropertyCount();
			int row = 0;
			while (row < total) {
				SoapObject object = (SoapObject) soap.getProperty(row);
				if (Boolean.valueOf(object.getPropertyAsString("isActive"))) {
					String insertChain = "INSERT INTO " + TABLE_CHAIN
							+ "(ChainID, ChainName) VALUES('"
							+ object.getPropertyAsString("id") + "','"
							+ object.getPropertyAsString("name") + "');";
					mDB.execSQL(insertChain);
				}
				row++;
			}
			if (row == total) {
				String timeUpdate;
				if (longTimeUpdate == -1) {
					timeUpdate = "INSERT INTO " + TABLE_TIMEUPDATE
							+ "(LogID, TypeID, Time) VALUES('" + LOG_ALL_CHAIN
							+ "', '0', '"
							+ Calendar.getInstance().getTimeInMillis() + "')";
				}

				else {
					timeUpdate = "UPDATE " + TABLE_TIMEUPDATE + " SET TIME='"
							+ Calendar.getInstance().getTimeInMillis()
							+ "' WHERE LogID='" + LOG_ALL_CHAIN
							+ "' AND TypeID = 0";
				}
				mDB.execSQL(timeUpdate);
				mDB.setTransactionSuccessful();
			}
		} catch (Exception e) {
			mDB.endTransaction();
		} finally {
			mDB.endTransaction();
		}
	}

	public void insertStore(SoapObject soap, String chainId, long longTimeUpdate) {
		try {
			mDB.beginTransaction();
			String deleteStores = "DELETE FROM " + TABLE_STORE
					+ " WHERE ChainID='" + chainId + "'";
			mDB.execSQL(deleteStores);
			int total = soap.getPropertyCount();
			int row = 0;
			while (row < total) {
				SoapObject object = (SoapObject) soap.getProperty(row);
				if (Boolean.valueOf(object.getPropertyAsString("IsActive"))) {
					String insertStore = "INSERT INTO " + TABLE_STORE
							+ "(StoreID, StoreName, ChainID) VALUES('"
							+ object.getPropertyAsString("fld_lng_ID") + "','"
							+ object.getPropertyAsString("fld_str_Name")
							+ "','" + chainId + "')";
					mDB.execSQL(insertStore);
				}
				row++;
			}
			if (row == total) {
				String timeUpdate;
				if (longTimeUpdate == -1) {
					timeUpdate = "INSERT INTO " + TABLE_TIMEUPDATE
							+ "(LogID, TypeID, Time) VALUES('" + LOG_CHAIN
							+ "', '" + chainId + "', '"
							+ Calendar.getInstance().getTimeInMillis() + "')";
				} else {
					timeUpdate = "UPDATE " + TABLE_TIMEUPDATE + " SET TIME='"
							+ Calendar.getInstance().getTimeInMillis()
							+ "' WHERE LogID='" + LOG_CHAIN + "' AND TypeID='"
							+ chainId + "'";
				}
				mDB.execSQL(timeUpdate);
				mDB.setTransactionSuccessful();
			}
		} catch (Exception e) {
			mDB.endTransaction();
		} finally {
			mDB.endTransaction();
		}
	}

	public void insertSurvey(SoapObject soap, String storeId,
			String type, long longTimeUpdate) {
		try {
			mDB.beginTransaction();
			String deleteSurvey = "DELETE FROM " + TABLE_SURVEY
					+ " WHERE StoreID='" + storeId + "' AND Type='"
					+ type + "'";
			mDB.execSQL(deleteSurvey);
			int total = soap.getPropertyCount();
			int row = 0;
			while (row < total) {
				SoapObject object = (SoapObject) soap.getProperty(row);
				String insertSurvey = "INSERT INTO " + TABLE_SURVEY
						+ "(SurveyID, SurveyName, StoreID, Type) VALUES('"
						+ object.getPropertyAsString("SurveyID") + "','"
						+ object.getPropertyAsString("Survey_Name") + "','"
						+ storeId + "', '" + type + "')";
				mDB.execSQL(insertSurvey);
				row++;
			}
			if (row == total) {
				String timeUpdate;
				if (longTimeUpdate == -1) {
					timeUpdate = "INSERT INTO " + TABLE_TIMEUPDATE
							+ "(LogID, TypeID, Time) VALUES('" + LOG_STORE
							+ "', '" + storeId + "', '"
							+ Calendar.getInstance().getTimeInMillis() + "')";
				} else {
					timeUpdate = "UPDATE " + TABLE_TIMEUPDATE + " SET TIME='"
							+ Calendar.getInstance().getTimeInMillis()
							+ "' WHERE LogID='" + LOG_STORE + "' AND TypeID='"
							+ storeId + "'";
				}
				mDB.execSQL(timeUpdate);
				mDB.setTransactionSuccessful();
			}
		} catch (Exception e) {
			mDB.endTransaction();
		} finally {
			mDB.endTransaction();
		}
	}

	public ArrayList<ContentValues> queryData(String strTable, String strQuery,
			String[] strParams) {
		ArrayList<ContentValues> alstData = new ArrayList<ContentValues>();
		Cursor cur = mDB.query(strTable, null, strQuery, strParams, null, null,
				null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getDataSurveys(strTable, cur);
			alstData.add(content);
			cur.moveToNext();
		}
		cur.close();
		return alstData;
	}

	public long queryTimeExpired(String logId, String typeId) {
		long longTimeExpired = -1;
		Cursor cur = mDB.query(TABLE_TIMEUPDATE, null, "LogID='" + logId
				+ "' AND TypeID='" + typeId + "'", null, null, null, null);
		if (cur != null) {
			if (cur.moveToFirst()) {
				longTimeExpired = cur.getLong(cur.getColumnIndex("Time"));
			}
		}
		cur.close();
		Log.i("timeupdate", longTimeExpired + "");
		return longTimeExpired;
	}

	private void getDataSurveys(String strTable, Cursor cur) {
		if (strTable.equals(DBAdapter.TABLE_USER)) {
			getAccountSurveys(cur);
		}
		if (strTable.equals(DBAdapter.TABLE_TIMEUPDATE)) {
			getLogSurveys(cur);
		}
		if (strTable.equals(DBAdapter.TABLE_CHAIN)) {
			getChainSurveys(cur);
		}
		if (strTable.equals(DBAdapter.TABLE_STORE)) {
			getStoreSurveys(cur);
		}
		if (strTable.equals(DBAdapter.TABLE_SURVEY)) {
			getSurveys(cur);
		}
		if (strTable.equals(DBAdapter.TABLE_RESULT)) {
			getSubmitSurveys(cur);
		}
	}

	public void getAccountSurveys(Cursor cur) {
		content.put("UserID",
				cur.getString(cur.getColumnIndexOrThrow("UserID")));
		content.put("Username",
				cur.getString(cur.getColumnIndexOrThrow("Username")));
		content.put("Password",
				cur.getString(cur.getColumnIndexOrThrow("Password")));
	}

	public void getLogSurveys(Cursor cur) {
		content.put("LogID", cur.getString(cur.getColumnIndexOrThrow("LogID")));
		content.put("TypeID",
				cur.getString(cur.getColumnIndexOrThrow("TypeID")));
		content.put("Time", cur.getString(cur.getColumnIndexOrThrow("Time")));
	}

	public void getChainSurveys(Cursor cur) {
		content.put("ChainID",
				cur.getString(cur.getColumnIndexOrThrow("ChainID")));
		content.put("ChainName",
				cur.getString(cur.getColumnIndexOrThrow("ChainName")));
	}

	public void getStoreSurveys(Cursor cur) {
		content.put("StoreID",
				cur.getString(cur.getColumnIndexOrThrow("StoreID")));
		content.put("StoreName",
				cur.getString(cur.getColumnIndexOrThrow("StoreName")));
		content.put("ChainID",
				cur.getString(cur.getColumnIndexOrThrow("ChainID")));
	}
	
	public void getSurveys(Cursor cur) {
		content.put("SurveyID",
				cur.getString(cur.getColumnIndexOrThrow("SurveyID")));
		content.put("SurveyName",
				cur.getString(cur.getColumnIndexOrThrow("SurveyName")));
		content.put("StoreID",
				cur.getString(cur.getColumnIndexOrThrow("StoreID")));
		content.put("Type",
				cur.getString(cur.getColumnIndexOrThrow("Type")));
	}

	public void getSubmitSurveys(Cursor cur) {
		content.put("UserID",
				cur.getString(cur.getColumnIndexOrThrow("UserID")));
		content.put("StoreID",
				cur.getString(cur.getColumnIndexOrThrow("StoreID")));
		content.put("SurveyID",
				cur.getString(cur.getColumnIndexOrThrow("SurveyID")));
		content.put("QuestionOrder",
				cur.getString(cur.getColumnIndexOrThrow("QuestionOrder")));
		content.put("Question",
				cur.getString(cur.getColumnIndexOrThrow("Question")));
		content.put("Answer",
				cur.getString(cur.getColumnIndexOrThrow("Answer")));
	}

}
