package com.deeset.deesetsurvey;

import java.util.ArrayList;
import com.deeset.deesetsurvey.model.DBAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;

public class Login extends Activity {

	private DBAdapter mDB;
	private EditText mEdtUser, mEdtPass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get full screen and no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.login);

		mEdtUser = (EditText) findViewById(R.id.edtLoginUser);
		mEdtPass = (EditText) findViewById(R.id.edtLoginPass);

		mDB = new DBAdapter(Login.this);
		mDB.open();
	}

	public void loginDEESETSurvey(View v) {
		
		if (mEdtUser.getText().toString().equals("")
				|| mEdtPass.getText().toString().equals("")) {
			Toast.makeText(Login.this, "Please fill username or password!",
					Toast.LENGTH_SHORT).show();
		} else {
			ArrayList<ContentValues> arlAllUser = new ArrayList<ContentValues>();
			arlAllUser = mDB.queryAllUser();
			int i = 0;
			for (; i < arlAllUser.size(); i++) {
				ContentValues content = arlAllUser.get(i);
				if (mEdtUser.getText().toString()
						.equals(content.getAsString("username")) == true
						&& mEdtPass.getText().toString()
								.equals(content.getAsString("password")) == true) {
					Intent intent = new Intent(Login.this, SelectStore.class);
					putIntentValues(intent, content.getAsString("userid"));
					if (mDB != null) {
						mDB.close();
					}
					startActivity(intent);
					break;
				}
			}
			if (i == arlAllUser.size()) {
				Toast.makeText(Login.this, "Username or password incorrect!",
						Toast.LENGTH_SHORT).show();
			}
			if (mDB != null) {
				mDB.close();
			}
		}
	}

	private void putIntentValues(Intent intent, String strUserId) {
		intent.putExtra("userid", strUserId);
	}

}
