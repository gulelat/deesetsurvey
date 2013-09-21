package com.deeset.deesetsurvey;

import java.util.ArrayList;

import org.ksoap2.serialization.SoapObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.deeset.deesetsurvey.controller.ConnectionDetector;
import com.deeset.deesetsurvey.model.JDBCAdapter;

public class SelectStore extends Activity implements OnItemSelectedListener {

	private Spinner mSpinChain;
	private Spinner mSpinStore;

	private ArrayAdapter<String> mAdapterChain;
	private ArrayAdapter<String> mAdapterStore;

	private ArrayList<String> mAlstChain;
	private ArrayList<String> mAlstChainId;
	private ArrayList<String> mAlstStore;
	private ArrayList<String> mAlstStoreId;

	private String mStrUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.selectchainstore);

		initialViews();
		initialData();
		getIntentValues();
	}

	private void initialViews() {
		mSpinChain = (Spinner) findViewById(R.id.spinSelectChain);
		mSpinStore = (Spinner) findViewById(R.id.spinSelectStore);
	}

	private void initialData() {
		getIntentValues();

		mAlstChain = new ArrayList<String>();
		mAlstChainId = new ArrayList<String>();
		loadDataChain();
		mAdapterChain = new ArrayAdapter<String>(SelectStore.this,
				android.R.layout.simple_spinner_item, mAlstChain);
		mAdapterChain
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinChain.setAdapter(mAdapterChain);
		mSpinChain.setOnItemSelectedListener(this);

		mAlstStore = new ArrayList<String>();
		mAlstStoreId = new ArrayList<String>();
		loadDataStore(0);
		mAdapterStore = new ArrayAdapter<String>(SelectStore.this,
				android.R.layout.simple_spinner_item, mAlstStore);
		mAdapterStore
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinStore.setAdapter(mAdapterStore);
		mSpinStore.setOnItemSelectedListener(this);
	}

	private void getIntentValues() {
		mStrUserId = getIntent().getStringExtra("userid");
	}

	private void loadDataStore(int intIndex) {
		mAlstStore.add("Select Store");
		mAlstStoreId.add("-1");
		if (intIndex != 0 && mAlstChainId.size() > intIndex) {
			InteractServer actServer = new InteractServer(this,
					"Get store data", JDBCAdapter.METHOD_GETSTOREDATA);
			actServer.addParam(JDBCAdapter.TYPE_INTEGER, "ChainID",
					mAlstChainId.get(intIndex));
			actServer.execute();
		}
	}

	private void loadDataChain() {
		mAlstChain.add("Select Chain");
		mAlstChainId.add("-1");
		if (mStrUserId != null) {
			InteractServer actServer = new InteractServer(this,
					"Get chain data", JDBCAdapter.METHOD_GETCHAINDATA);
			actServer.execute();
		}
	}

	public void submitChainStoreSurvey(View v) {
		if ((mSpinChain.getSelectedItemPosition() == 0)
				|| (mSpinStore.getSelectedItemPosition() == 0)) {
			Toast.makeText(SelectStore.this,
					"Please choose Chain and Store specific!",
					Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent(SelectStore.this, StaticSurveys.class);
			putIntentValues(intent);
			startActivity(intent);
		}
	}

	private void putIntentValues(Intent intent) {
		Log.i("StoreID", mAlstStoreId.get(mSpinStore.getSelectedItemPosition()));
		intent.putExtra("storeid",
				mAlstStoreId.get(mSpinStore.getSelectedItemPosition()));
		intent.putExtra("storename", mSpinStore.getSelectedItem().toString());
		intent.putExtra("userid", mStrUserId);
	}

	public void resetChainSurvey(View v) {
		mSpinChain.setSelection(0);
		mAlstStore.clear();
		loadDataStore(0);
		mAdapterStore.notifyDataSetChanged();
		mSpinStore.setSelection(0);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		switch (arg0.getId()) {
		case R.id.spinSelectChain:
			mAlstStore.clear();
			loadDataStore(arg2);
			mAdapterStore.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

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
					if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
						getChainData(soap);
					}
					if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
						getStoreData(soap);
					}
					return "Connect";
				} else {
					return "Inconnect";
				}
			}
		}

		private void getChainData(SoapObject soap) {
			int intSize = soap.getPropertyCount();
			for (int i = 0; i < intSize; i++) {
				SoapObject object = (SoapObject) soap.getProperty(i);
				if (Boolean.valueOf(object.getPropertyAsString("isActive"))) {
					mAlstChain.add(object.getPropertyAsString("name"));
					mAlstChainId.add(object.getPropertyAsString("id"));
				}
			}
		}

		private void getStoreData(SoapObject soap) {
			int intSize = soap.getPropertyCount();
			for (int i = 0; i < intSize; i++) {
				SoapObject object = (SoapObject) soap.getProperty(i);
				if (Boolean.valueOf(object.getPropertyAsString("IsActive"))) {
					mAlstStore.add(object.getPropertyAsString("fld_str_Name"));
					mAlstStoreId.add(object.getPropertyAsString("fld_lng_ID"));
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
				if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)
						|| mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
					if (strResult.equals("Inconnect")) {
						Toast.makeText(mCtx, "Can't get data from server!",
								Toast.LENGTH_SHORT).show();
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
