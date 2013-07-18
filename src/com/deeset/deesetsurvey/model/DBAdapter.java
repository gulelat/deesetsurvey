package com.deeset.deesetsurvey.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import de.greenrobot.dao.DaoLog;

public class DBAdapter {
	private int mIntVersion;
	private final Context mCtx;
	private DatabaseHelper mDBHelper;
	public static SQLiteDatabase mDB;
	private static final String DATABASE_NAME = "dbdeesetsurvey";
	private SharedPreferences mSharedPref;
	private ContentValues content;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private Context ctx;
		private int mIntVersion;

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			this.ctx = context;
			mIntVersion = version;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				executeSqlScript(ctx, db, "deeset_survey.sql", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public int executeSqlScript(Context context, SQLiteDatabase db,
				String assetFilename, boolean transactional) throws IOException {
			byte[] bytes;
			if (mIntVersion == 1) {
				bytes = readAsset(context, assetFilename);
				Log.i("1", "OK");
			} else {
				Log.i("2", "OK");
				File file = new File(
						"data/data/com.deeset.deesetsurvey/deeset_survey.sql");
				if (file.exists()) {
					Log.i("3", "OK");
					FileInputStream input = new FileInputStream(file);
					bytes = readAllBytes(input);
				} else {
					return 0;
				}
			}
			String sql = new String(bytes, "UTF-8");
			String[] lines = sql.split(";(\\s)*[\n\r]");
			int count;
			if (transactional) {
				count = executeSqlStatementsInTx(db, lines);
			} else {
				count = executeSqlStatements(db, lines);
			}
			DaoLog.i("Executed " + count + " statements from SQL script '"
					+ assetFilename + "'");
			return count;
		}

		public int executeSqlStatementsInTx(SQLiteDatabase db,
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

		public int executeSqlStatements(SQLiteDatabase db, String[] statements) {
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
		public int copyAllBytes(InputStream in, OutputStream out)
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

		public byte[] readAllBytes(InputStream in) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copyAllBytes(in, out);
			return out.toByteArray();
		}

		public byte[] readAsset(Context context, String filename)
				throws IOException {
			InputStream in = context.getResources().getAssets().open(filename);
			try {
				return readAllBytes(in);
			} finally {
				in.close();
			}
		}

		/*
		 * public static void logTableDump(SQLiteDatabase db, String tablename)
		 * { Cursor cursor = db.query(tablename, null, null, null, null, null,
		 * null); try { String dump = DatabaseUtils.dumpCursorToString(cursor);
		 * DaoLog.d(dump); } finally { cursor.close(); } }
		 */

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}
	}

	public DBAdapter(Context ctx) {
		this.mCtx = ctx;
		mSharedPref = ctx.getSharedPreferences("UpdateDBPref",
				Context.MODE_PRIVATE);
	}

	public DBAdapter open() {
		mIntVersion = getIntPref(mSharedPref, "versionDB", 1);
		mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, mIntVersion);
		mDB = mDBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDBHelper.close();
	}

	public int getIntPref(SharedPreferences sharedPref, String name, int valrtn) {
		return sharedPref.getInt(name, valrtn);
	}

	public void setIntPref(SharedPreferences sharedPref, String name, int value) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(name, value);
		editor.commit();
	}

	public void insertAnswerDB(String strTable, ContentValues content) {
		mDB.insert(strTable, null, content);
	}

	public ArrayList<ContentValues> queryAllUser() {
		ArrayList<ContentValues> allTask = new ArrayList<ContentValues>();
		Cursor cur = mDB.query("TblLogin", null, null, null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getContentLogin(cur);
			allTask.add(content);
			cur.moveToNext();
		}
		cur.close();
		return allTask;
	}

	public int countSubmittedResult(String strUserId, String strChain,
			String strStore, String strSurvey) {
		int intCount = 0;
		Cursor cur = mDB.query("TblSubmitted", null, "userid=" + strUserId
				+ " AND chain='" + strChain + "' AND store='" + strStore
				+ "' AND surveyname='" + strSurvey + "'", null, null, null,
				null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			intCount++;
		}
		cur.close();
		return intCount;
	}

	public ArrayList<ContentValues> querySubmittedResult(String strUserId,
			String strChain, String strStore, String strSurvey, String strSeqNum) {
		ArrayList<ContentValues> arrlstResult = new ArrayList<ContentValues>();
		Cursor cur = mDB.query("TblSubmitted", null, "userid=" + strUserId
				+ " AND chain='" + strChain + "' AND store='" + strStore
				+ "' AND surveyname='" + strSurvey + "' AND seqnum='"
				+ strSeqNum + "'", null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getContentSubmittedResult(cur);
			arrlstResult.add(content);
			cur.moveToNext();
		}
		cur.close();
		return arrlstResult;
	}

	public ArrayList<ContentValues> queryChains() {
		ArrayList<ContentValues> arrlst = new ArrayList<ContentValues>();
		Cursor cur = mDB.query("TblChain", null, null, null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getChains(cur);
			arrlst.add(content);
			cur.moveToNext();
		}
		cur.close();
		return arrlst;
	}

	public String queryChainName(String strChainId) {
		String strChainName;
		Cursor cur = mDB.query("TblChain", null,
				"ChainID='" + strChainId + "'", null, null, null, null);
		cur.moveToFirst();
		strChainName = cur.getString(cur.getColumnIndexOrThrow("ChainName"));
		cur.close();
		return strChainName;
	}

	public ArrayList<ContentValues> queryStoresByChain(String strChainId) {
		ArrayList<ContentValues> arrlst = new ArrayList<ContentValues>();
		Cursor cur = mDB.query("TblStore", null,
				"ChainID='" + strChainId + "'", null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getStores(cur);
			arrlst.add(content);
			cur.moveToNext();
		}
		cur.close();
		return arrlst;
	}

	public ArrayList<String> queryStoreSurveys(String strStoreId) {
		ArrayList<String> arrlst = new ArrayList<String>();
		Cursor cur = mDB.query("TblSurvey_Store", null,
				"Survey_Store_StoreID='" + strStoreId + "'", null, null, null,
				null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			arrlst.add(cur.getString(cur
					.getColumnIndexOrThrow("Survey_Store_SurveyID")));
			cur.moveToNext();
		}
		cur.close();
		return arrlst;
	}

	public ArrayList<ContentValues> querySurveyContains(String strSurveyId) {
		ArrayList<ContentValues> arrlst = new ArrayList<ContentValues>();
		Cursor cur = mDB.query("TblQuestion", null, "Question_SurveyID='"
				+ strSurveyId + "'", null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getSurveyContains(cur);
			arrlst.add(content);
			cur.moveToNext();
		}
		cur.close();
		return arrlst;
	}

	public ContentValues querySurveys(String strSurveyId) {
		ContentValues cont = new ContentValues();
		Cursor cur = mDB.query("TblSurvey", null, "SurveyId='" + strSurveyId
				+ "'", null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			cont.put("Survey_Name",
					cur.getString(cur.getColumnIndexOrThrow("Survey_Name")));
			cont.put("Survey_StartDate", cur.getString(cur
					.getColumnIndexOrThrow("Survey_StartDate")));
			cont.put("Survey_FinalDate", cur.getString(cur
					.getColumnIndexOrThrow("Survey_FinalDate")));
			cont.put("Survey_IsStatic",
					cur.getString(cur.getColumnIndexOrThrow("Survey_IsStatic")));
			cur.moveToNext();
		}
		cur.close();
		return cont;
	}

	public ArrayList<ContentValues> querySurveyAnwers(String strQuestionId) {
		ArrayList<ContentValues> arrlst = new ArrayList<ContentValues>();
		Cursor cur = mDB.query("TblQuestion_Options", null, "Question_ID='"
				+ strQuestionId + "'", null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getSurveyAnswers(cur);
			arrlst.add(content);
			cur.moveToNext();
		}
		cur.close();
		return arrlst;
	}

	private void getContentSubmittedResult(Cursor cur) {
		content.put("question",
				cur.getString(cur.getColumnIndexOrThrow("question")));
		content.put("answer",
				cur.getString(cur.getColumnIndexOrThrow("answer")));
	}

	public void getContentLogin(Cursor cur) {
		content.put("userid",
				cur.getString(cur.getColumnIndexOrThrow("userid")));
		content.put("username",
				cur.getString(cur.getColumnIndexOrThrow("username")));
		content.put("password",
				cur.getString(cur.getColumnIndexOrThrow("password")));
	}

	private void getChains(Cursor cur) {
		content.put("ChainID",
				cur.getString(cur.getColumnIndexOrThrow("ChainID")));
		content.put("ChainName",
				cur.getString(cur.getColumnIndexOrThrow("ChainName")));
	}

	private void getStores(Cursor cur) {
		content.put("StoreID",
				cur.getString(cur.getColumnIndexOrThrow("StoreID")));
		content.put("StoreName",
				cur.getString(cur.getColumnIndexOrThrow("StoreName")));
	}

	private void getSurveyContains(Cursor cur) {
		content.put("Question_ID",
				cur.getString(cur.getColumnIndexOrThrow("Question_ID")));
		content.put("Question_Type",
				cur.getString(cur.getColumnIndexOrThrow("Question_Type")));
		content.put("Question_Order",
				cur.getString(cur.getColumnIndexOrThrow("Question_Order")));
		content.put("Question_Description", cur.getString(cur
				.getColumnIndexOrThrow("Question_Description")));
	}

	private void getSurveyAnswers(Cursor cur) {
		content.put("Question_Option_ID",
				cur.getString(cur.getColumnIndexOrThrow("Question_Option_ID")));
		content.put("Question_Answer_Value", cur.getString(cur
				.getColumnIndexOrThrow("Question_Answer_Value")));
		content.put("Question_Answer_Replacement_Value", cur.getString(cur
				.getColumnIndexOrThrow("Question_Answer_Replacement_Value")));
	}
}
