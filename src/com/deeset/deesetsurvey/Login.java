package com.deeset.deesetsurvey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import org.ksoap2.serialization.SoapObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.deeset.deesetsurvey.controller.ConnectionDetector;
import com.deeset.deesetsurvey.model.DBAdapter;
import com.deeset.deesetsurvey.model.JDBCAdapter;
import com.deeset.deesetsurvey.profile.GlobalInfo;

public class Login extends Activity {

	private ConnectionDetector conDetect;
	private EditText mEdtUser;
	private EditText mEdtPass;
	private DBAdapter mDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);

		mEdtUser = (EditText) findViewById(R.id.edtLoginUser);
		mEdtPass = (EditText) findViewById(R.id.edtLoginPass);
		conDetect = new ConnectionDetector(this);
		mDB = new DBAdapter(this);
		mDB.open();
		if (!conDetect.isConnectingToInternet()) {
			conDetect.showSettingsAlert();
		}
	}

	@Override
	protected void onStop() {
		super.onPause();
		if (mDB.isOpen()) {
			mDB.close();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mDB.isOpen()) {
			mDB.open();
		}
	}

	public void loginDEESETSurvey(View v) {
		String strUsername = mEdtUser.getText().toString();
		String strPassword = mEdtPass.getText().toString();
		if (!strUsername.equals("") || !strPassword.equals("")) {
			InteractServer actServer = new InteractServer(Login.this,
					"Check Account Login", JDBCAdapter.METHOD_CHECKUSERLOGIN,
					conDetect.isConnectingToInternet());
			actServer
					.addParam(JDBCAdapter.TYPE_STRING, "Username", strUsername);
			actServer
					.addParam(JDBCAdapter.TYPE_STRING, "Password", strPassword);
			actServer.execute();
		} else {
			GlobalInfo.showToast(this, "Please fill username and password!");
		}
	}

	private class InteractServer extends AsyncTask<String, Integer, String> {

		private String mStrTitle = "";
		private String mStrMethod = "";
		private ArrayList<String> mAlstWS;

		private Context mCtx;
		private JDBCAdapter mJDBC;
		private ProgressDialog proDialog;
		private boolean mBlnOnline = false;

		public InteractServer(Context ctx, String strTitle, String strMethod,
				boolean blnOnline) {
			mBlnOnline = blnOnline;
			mCtx = ctx;
			mStrTitle = strTitle;
			mStrMethod = strMethod;
			mJDBC = new JDBCAdapter();
			mAlstWS = new ArrayList<String>();
		}

		public void addParam(String strType, String strName, String strValue) {
			mAlstWS.add(strType);
			mAlstWS.add(strName);
			mAlstWS.add(strValue);
		}

		@Override
		protected String doInBackground(String... params) {
			if (!mBlnOnline) {
				ArrayList<ContentValues> alst = mDB.queryData(
						DBAdapter.TABLE_USER, "Username=? AND Password=?",
						new String[] { mAlstWS.get(2), mAlstWS.get(5) });
				if (alst.size() != 0) {
					return alst.get(0).getAsString("UserID");
				} else {
					return "Inconnect";
				}
			} else {
				SoapObject soap = mJDBC.interactServer(mAlstWS, mStrMethod);
				if (soap != null) {
					return soap.getPropertyAsString("UserID");
				} else {
					return "Inconnect";
				}
			}
		}

		@Override
		protected void onPostExecute(String strResult) {
			proDialog.dismiss();
			if (mStrMethod.equals(JDBCAdapter.METHOD_CHECKUSERLOGIN)) {
				if (strResult.equals("Inconnect")) {
					Toast.makeText(mCtx, "Username or password incorrect!",
							Toast.LENGTH_SHORT).show();
				} else {
					if (mBlnOnline) {
						ContentValues content = new ContentValues();
						content.put("UserID", strResult);
						content.put("Username", mAlstWS.get(2));
						content.put("Password", mAlstWS.get(5));
						mDB.insertUser(content);
					} else {
						Toast.makeText(
								mCtx,
								"You're offline! Survey data can not appropriate! Please connect internet!",
								Toast.LENGTH_SHORT).show();
					}
					startSurvey(mCtx, strResult);
				}
			}
		}

		@Override
		protected void onPreExecute() {
			proDialog = new ProgressDialog(mCtx);
			proDialog.setTitle(mStrTitle);
			proDialog.setMessage("Processing...");
			proDialog.setCanceledOnTouchOutside(false);
			proDialog.setCancelable(false);
			proDialog.show();
		}
	}

	private void startSurvey(Context ctx, String strResult) {
		GlobalInfo.setUserId(strResult);
		ctx.startActivity(new Intent(ctx, SelectStore.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			backupToExternal();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void backupToExternal() {
		// backup db from internal to external
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//com.deeset.deesetsurvey//databases//dbdeesetsurvey";
				String backupDBPath = "dbdeesetsurvey";
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB)
							.getChannel();
					FileChannel dst = new FileOutputStream(backupDB)
							.getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
