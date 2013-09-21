package com.deeset.deesetsurvey;

import java.util.ArrayList;
import org.ksoap2.serialization.SoapObject;
import com.deeset.deesetsurvey.controller.ConnectionDetector;
import com.deeset.deesetsurvey.model.JDBCAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

public class Login extends Activity {

	private ConnectionDetector conDetect;
	private EditText mEdtUser;
	private EditText mEdtPass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);

		initialViews();
		initialControllers();
		initialChecks();
	}

	private void initialViews() {
		mEdtUser = (EditText) findViewById(R.id.edtLoginUser);
		mEdtPass = (EditText) findViewById(R.id.edtLoginPass);
	}

	private void initialControllers() {
		conDetect = new ConnectionDetector(this);
	}

	private void initialChecks() {
		if (!conDetect.isConnectingToInternet()) {
			conDetect.showSettingsAlert();
		}
	}

	public void loginDEESETSurvey(View v) {
		String strUsername = mEdtUser.getText().toString();
		String strPassword = mEdtPass.getText().toString();
		if (!strUsername.equals("") || !strPassword.equals("")) {
			InteractServer actServer = new InteractServer(Login.this,
					"Check Account Login", JDBCAdapter.METHOD_CHECKUSERLOGIN);
			actServer
					.addParam(JDBCAdapter.TYPE_STRING, "Username", strUsername);
			actServer
					.addParam(JDBCAdapter.TYPE_STRING, "Password", strPassword);
			actServer.execute();
		} else {
			Toast.makeText(Login.this, "Please fill username or password!",
					Toast.LENGTH_SHORT).show();
		}
	}

	private class InteractServer extends AsyncTask<String, Integer, String> {

		private String mStrTitle = "";
		private String mStrMethod = "";
		private ArrayList<String> mAlstWS;

		private Context mCtx;
		private JDBCAdapter mJDBC;
		private ProgressDialog proDialog;
		private ConnectionDetector conDetect;

		public InteractServer(Context ctx, String strTitle, String strMethod) {
			mCtx = ctx;
			mStrTitle = strTitle;
			mStrMethod = strMethod;
			mJDBC = new JDBCAdapter();
			conDetect = new ConnectionDetector(mCtx);
			mAlstWS = new ArrayList<String>();
		}

		public void addParam(String strType, String strName, String strValue) {
			mAlstWS.add(strType);
			mAlstWS.add(strName);
			mAlstWS.add(strValue);
		}

		@Override
		protected String doInBackground(String... params) {
			if (!conDetect.isConnectingToInternet()) {
				return "Error";
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
			if (strResult.equals("Error")) {
				Toast.makeText(mCtx, "Can't connect to server!",
						Toast.LENGTH_SHORT).show();
			} else {
				if (mStrMethod.equals(JDBCAdapter.METHOD_CHECKUSERLOGIN)) {
					if (strResult.equals("Inconnect")) {
						Toast.makeText(mCtx, "Username or password incorrect!",
								Toast.LENGTH_SHORT).show();
					} else {
						Log.i("SurveyeeID", strResult);
						mCtx.startActivity(new Intent(mCtx, SelectStore.class)
								.putExtra("userid", strResult));
					}
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

}
