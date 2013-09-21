package com.deeset.deesetsurvey.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {

	private final Context ctx;
	private DatabaseHelper mDBHelper;
	private SQLiteDatabase mDB;
	private static final String DATABASE_NAME = "dbdeesetsurvey";
	private static final int VERSION = 1;
	private ContentValues content;

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

	public void insertData(String strTable, ContentValues content) {
		mDB.insert(strTable, null, content);
	}

	public ArrayList<ContentValues> queryData(String strTable, String strQuery) {
		ArrayList<ContentValues> alstData = new ArrayList<ContentValues>();
		Cursor cur = mDB
				.query(strTable, null, strQuery, null, null, null, null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			content = new ContentValues();
			getSubmitSurveys(cur);
			alstData.add(content);
			cur.moveToNext();
		}
		cur.close();
		return alstData;
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
