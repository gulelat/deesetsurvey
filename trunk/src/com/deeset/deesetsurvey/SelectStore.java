package com.deeset.deesetsurvey;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import com.deeset.deesetsurvey.model.DBAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class SelectStore extends Activity implements OnItemSelectedListener {

	private DBAdapter mDB;
	private Spinner mSpinChain;
	private Spinner mSpinStore;
	private ArrayAdapter<String> mArrAdapterChain;
	private ArrayAdapter<String> mArrAdapterStore;
	private ArrayList<String> mArrLstChain;
	private ArrayList<String> mArrLstChainId;
	private ArrayList<String> mArrLstStore;
	private ArrayList<String> mArrLstStoreId;
	private String mStrUserId;
	private String mStrStoreId;
	private String mStrChainId;

	private SharedPreferences mSharedPref;
	private int mIntVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get full screen and no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.selectchainstore);

		initialConnectDB();
		initialViews();
		getIntentValues();
	}

	private void getIntentValues() {
		mStrUserId = getIntent().getStringExtra("userid");
	}

	private void initialViews() {
		mArrLstChain = new ArrayList<String>();
		mArrLstChainId = new ArrayList<String>();
		mSpinChain = (Spinner) findViewById(R.id.spinSelectChain);
		loadDataChain();
		mArrAdapterChain = new ArrayAdapter<String>(SelectStore.this,
				android.R.layout.simple_spinner_item, mArrLstChain);
		mArrAdapterChain
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinChain.setAdapter(mArrAdapterChain);
		mSpinChain.setOnItemSelectedListener(this);

		mArrLstStore = new ArrayList<String>();
		mArrLstStoreId = new ArrayList<String>();
		mSpinStore = (Spinner) findViewById(R.id.spinSelectStore);
		loadDataStore(0);
		mArrAdapterStore = new ArrayAdapter<String>(SelectStore.this,
				android.R.layout.simple_spinner_item, mArrLstStore);
		mArrAdapterStore
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinStore.setAdapter(mArrAdapterStore);
		mSpinStore.setOnItemSelectedListener(this);
	}

	private void loadDataStore(int intIndex) {
		mArrLstStore.add("Select Store");
		mArrLstStoreId.add("0");
		ArrayList<ContentValues> arrlstStores = mDB
				.queryStoresByChain(mArrLstChainId.get(intIndex));
		for (int i = 0; i < arrlstStores.size(); i++) {
			mArrLstStore.add(arrlstStores.get(i).getAsString("StoreName"));
			mArrLstStoreId.add(arrlstStores.get(i).getAsString("StoreID"));
		}
	}

	private void loadDataChain() {
		mArrLstChain.add("Select Chain");
		mArrLstChainId.add("-1");
		ArrayList<ContentValues> arrlstChains = mDB.queryChains();
		for (int i = 0; i < arrlstChains.size(); i++) {
			mArrLstChain.add(arrlstChains.get(i).getAsString("ChainName"));
			mArrLstChainId.add(arrlstChains.get(i).getAsString("ChainID"));
		}
	}

	private void initialConnectDB() {
		mDB = new DBAdapter(SelectStore.this);
		mDB.open();
		mSharedPref = getSharedPreferences("UpdateDBPref", Context.MODE_PRIVATE);
	}

	private void putIntentValues(Intent intent) {
		intent.putExtra("chainid", mStrChainId);
		intent.putExtra("storeid", mStrStoreId);
		intent.putExtra("storename", mSpinStore.getSelectedItem().toString());
		intent.putExtra("userid", mStrUserId);
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

	public void resetChainSurvey(View v) {
		mSpinChain.setSelection(0);
		mArrLstStore.clear();
		loadDataStore(0);
		mArrAdapterStore.notifyDataSetChanged();
		mSpinStore.setSelection(0);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		switch (arg0.getId()) {
		case R.id.spinSelectChain:
			mStrChainId = mArrLstChainId.get(arg2);
			mArrLstStore.clear();
			loadDataStore(arg2);
			mArrAdapterStore.notifyDataSetChanged();
			break;
		case R.id.spinSelectStore:
			mStrStoreId = mArrLstStoreId.get(arg2);
			Log.i("storeid", mArrLstStoreId.get(arg2));
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			new Download().execute();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public int getIntPref(SharedPreferences sharedPref, String name, int valrtn) {
		return sharedPref.getInt(name, valrtn);
	}

	public void setIntPref(SharedPreferences sharedPref, String name, int value) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(name, value);
		editor.commit();
	}

	private class Download extends AsyncTask<Void, Integer, String> {

		private ProgressDialog mProDialog = new ProgressDialog(SelectStore.this);
		private String strURL = "http://m.deeset.co.uk/surveysql/deeset_survey.sql";

		@Override
		protected String doInBackground(Void... params) {
			int intCount;
			try {
				URL url = new URL(strURL);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int intFileLength = conexion.getContentLength();

				InputStream input = new BufferedInputStream(url.openStream());
				File file = new File(
						"data/data/com.deeset.deesetsurvey/deeset_survey.sql");
				if (file.exists()) {
					file.delete();
					Log.i("Delete", "OK");
				}
				OutputStream output = new FileOutputStream(file);

				byte data[] = new byte[1024];

				long lngTotal = 0;

				while ((intCount = input.read(data)) != -1) {
					lngTotal += intCount;
					publishProgress((int) ((lngTotal * 100) / intFileLength));
					output.write(data, 0, intCount);
				}

				output.flush();
				output.close();
				input.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			mProDialog.dismiss();
			new WebServiceTask(SelectStore.this).execute();
		}

		@Override
		protected void onPreExecute() {
			mProDialog = new ProgressDialog(SelectStore.this);
			mProDialog.setTitle("Downd file database");
			mProDialog.setMessage("Downloading...");
			mProDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProDialog.setCancelable(false);
			mProDialog.setCanceledOnTouchOutside(false);
			mProDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mProDialog.setProgress(values[0]);
		}
	}

	private class WebServiceTask extends AsyncTask<String, Integer, Boolean> {

		private ProgressDialog mProDialog = new ProgressDialog(SelectStore.this);
		private Context mCtx;
		
		public WebServiceTask(Context ctx) {
			mCtx = ctx;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			mDB.close();
			mIntVersion = getIntPref(mSharedPref, "versionDB", 1);
			mIntVersion++;
			setIntPref(mSharedPref, "versionDB", mIntVersion);
			mDB.open();
			return true;
		}

		@Override
		protected void onPostExecute(Boolean strResult) {
			mProDialog.dismiss();
			mCtx.startActivity(new Intent(mCtx, Login.class));
			finish();
		}

		@Override
		protected void onPreExecute() {
			mProDialog.setTitle("Upgrade database");
			mProDialog.setMessage("Upgrading...");
			mProDialog.setCancelable(false);
			mProDialog.setCanceledOnTouchOutside(false);
			mProDialog.show();
		}

	}
}
