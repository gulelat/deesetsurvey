package com.deeset.deesetsurvey;

import java.util.ArrayList;
import java.util.Calendar;

import org.ksoap2.serialization.SoapObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.deeset.deesetsurvey.controller.ConnectionDetector;
import com.deeset.deesetsurvey.model.DBAdapter;
import com.deeset.deesetsurvey.model.JDBCAdapter;
import com.deeset.deesetsurvey.profile.GlobalInfo;

public class SelectStore extends Activity implements OnItemSelectedListener {

	private Spinner mSpinChain;
	private Spinner mSpinStore;

	private ArrayAdapter<String> mAdapterChain;
	private ArrayAdapter<String> mAdapterStore;

	private ArrayList<String> mAlstChain;
	private ArrayList<String> mAlstChainId;
	private ArrayList<String> mAlstStore;
	private ArrayList<String> mAlstStoreId;

	private DBAdapter mDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.selectchainstore);

		mSpinChain = (Spinner) findViewById(R.id.spinSelectChain);
		mSpinStore = (Spinner) findViewById(R.id.spinSelectStore);
		mDB = new DBAdapter(this);
		mDB.open();
		initialData();
	}

	private void initialData() {
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

	private void loadDataStore(int intIndex) {
		mAlstStore.add("Select Store");
		mAlstStoreId.add("-1");
		if (intIndex != 0 && mAlstChainId.size() > intIndex) {
			InteractServer actServer = new InteractServer(this,
					"Get store data", JDBCAdapter.METHOD_GETSTOREDATA, true,
					false);
			actServer.addParam(JDBCAdapter.TYPE_INTEGER, "ChainID",
					mAlstChainId.get(intIndex));
			actServer.execute();
		}
	}

	private void loadDataChain() {
		mAlstChain.add("Select Chain");
		mAlstChainId.add("-1");
		if (GlobalInfo.getUserId() != null) {
			InteractServer actServer = new InteractServer(this,
					"Get chain data", JDBCAdapter.METHOD_GETCHAINDATA, true,
					false);
			actServer.execute();
		}
	}

	public void submitChainStoreSurvey(View v) {
		if ((mSpinChain.getSelectedItemPosition() == 0)
				|| (mSpinStore.getSelectedItemPosition() == 0)) {
			GlobalInfo.showToast(this,
					"Please choose Chain and Store specific!");
		} else {
			Intent intent = new Intent(SelectStore.this, StaticSurveys.class);
			Log.i("StoreID",
					mAlstStoreId.get(mSpinStore.getSelectedItemPosition()));
			GlobalInfo.setStoreId(mAlstStoreId.get(mSpinStore
					.getSelectedItemPosition() + 1));
			GlobalInfo.setStoreName(mSpinStore.getSelectedItem().toString());
			startActivity(intent);
		}
	}

	public void resetChainSurvey(View v) {
		mSpinChain.setSelection(0);
		mAlstStore.clear();
		mAlstStoreId.clear();
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
			mAlstStoreId.clear();
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

	private class InteractServer extends AsyncTask<String, Integer, Integer> {

		private String mStrTitle = "";
		private String mStrMethod = "";
		private ArrayList<String> mAlstWS;

		private Context mCtx;
		private JDBCAdapter mJDBC;
		private ProgressDialog proDialog;
		private boolean mBlnUpdateData = false;
		private boolean mBlnShowProDialog;
		private ConnectionDetector conDetect;
		private long longTimeExipred = 0;

		public InteractServer(Context ctx, String strTitle, String strMethod,
				boolean blnShowProDialog, boolean blnUpdateData) {
			mBlnShowProDialog = blnShowProDialog;
			mBlnUpdateData = blnUpdateData;
			mCtx = ctx;
			mStrTitle = strTitle;
			mStrMethod = strMethod;
			mJDBC = new JDBCAdapter();
			mAlstWS = new ArrayList<String>();
			conDetect = new ConnectionDetector(mCtx);
			proDialog = new ProgressDialog(mCtx);
		}

		public void addParam(String strType, String strName, String strValue) {
			mAlstWS.add(strType);
			mAlstWS.add(strName);
			mAlstWS.add(strValue);
		}

		@Override
		protected Integer doInBackground(String... params) {
			getUpdateTime();
			if (!mBlnUpdateData) {
				if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
					if (queryChains()) {
						return JDBCAdapter.RESULT_OK;
					}
				}
				if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
					if (queryStores()) {
						return JDBCAdapter.RESULT_OK;
					}
				}
				return JDBCAdapter.RESULT_NOTDATA;
			} else {
				SoapObject soap = mJDBC.interactServer(mAlstWS, mStrMethod);
				if (soap != null) {
					if (soap.getPropertyCount() != 0) {
						if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
							mDB.insertChain(soap, longTimeExipred);
						}
						if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
							mDB.insertStore(soap, mAlstWS.get(2),
									longTimeExipred);
						}
						return JDBCAdapter.RESULT_OK;
					}
					return JDBCAdapter.RESULT_EMPTYDATA;
				} else {
					return JDBCAdapter.RESULT_NOTCONNECT;
				}
			}
		}

		private void getUpdateTime() {
			if (mBlnShowProDialog) {
				if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
					longTimeExipred = mDB.queryTimeExpired(
							DBAdapter.LOG_ALL_CHAIN, "0");
				}
				if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
					longTimeExipred = mDB.queryTimeExpired(DBAdapter.LOG_CHAIN,
							mAlstWS.get(2));
				}
				if (conDetect.isConnectingToInternet()
						&& Calendar.getInstance().getTimeInMillis()
								- longTimeExipred > JDBCAdapter.TIME_OUT) {
					mBlnUpdateData = true;
					Log.i("update Data", longTimeExipred + "");
				}
			}
		}

		private boolean queryStores() {
			ArrayList<ContentValues> alstStores = mDB.queryDatas(
					DBAdapter.TABLE_STORE, "ChainID=?",
					new String[] { mAlstWS.get(2) });
			if (alstStores.size() > 0) {
				mAlstStore.clear();
				mAlstStoreId.clear();
				mAlstStore.add("Select Store");
				mAlstStoreId.add("-1");
				for (ContentValues contentValues : alstStores) {
					mAlstStore.add(contentValues.getAsString("StoreName"));
					mAlstStoreId.add(contentValues.getAsString("StoreID"));
				}
				return true;
			}
			return false;
		}

		private boolean queryChains() {
			ArrayList<ContentValues> alstChains = mDB.queryDatas(
					DBAdapter.TABLE_CHAIN, null, null);
			if (alstChains.size() > 0) {
				mAlstChain.clear();
				mAlstChainId.clear();
				mAlstChain.add("Select Store");
				mAlstChainId.add("-1");
				for (ContentValues contentValues : alstChains) {
					mAlstChainId.add(contentValues.getAsString("ChainID"));
					mAlstChain.add(contentValues.getAsString("ChainName"));
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (!mBlnUpdateData) {
				if (result == JDBCAdapter.RESULT_NOTDATA) {
					if (conDetect.isConnectingToInternet()) {
						startUpdateSurvey(true);
					} else {
						GlobalInfo.showToast(mCtx,
								JDBCAdapter.STR_NODATAOFFLINE);
					}
				} else {
					if (conDetect.isConnectingToInternet()
							&& Calendar.getInstance().getTimeInMillis()
									- longTimeExipred > JDBCAdapter.TIME_UPDATE) {
						Log.i("update Data", longTimeExipred + "");
						startUpdateSurvey(false);
					}
				}
			} else {
				if (result == JDBCAdapter.RESULT_OK) {
					if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
						if (!queryChains() && mBlnShowProDialog) {
							GlobalInfo.showToast(mCtx,
									JDBCAdapter.STR_NOTLOADDATA);
						}
					}
					if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
						if (!queryStores() && mBlnShowProDialog) {
							GlobalInfo.showToast(mCtx,
									JDBCAdapter.STR_NOTLOADDATA);
						}
					}
				} else {
					if (result == JDBCAdapter.RESULT_EMPTYDATA) {
						if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
							GlobalInfo.showToast(mCtx,
									JDBCAdapter.STR_EMPTYDATA + "chains!");
						}
						if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
							GlobalInfo.showToast(mCtx,
									JDBCAdapter.STR_EMPTYDATA
											+ "chain "
											+ mSpinChain.getSelectedItem()
													.toString() + "!");
						}
					} else {
						GlobalInfo.showToast(mCtx, JDBCAdapter.STR_NOCONNECT);
					}
				}
			}
			mAdapterChain.notifyDataSetChanged();
			mAdapterStore.notifyDataSetChanged();
			proDialog.dismiss();
		}

		private void startUpdateSurvey(boolean blnShowProDialog) {
			if (mStrMethod.equals(JDBCAdapter.METHOD_GETCHAINDATA)) {
				InteractServer actServer = new InteractServer(mCtx,
						"Get chain data", JDBCAdapter.METHOD_GETCHAINDATA,
						blnShowProDialog, true);
				actServer.execute();
			}
			if (mStrMethod.equals(JDBCAdapter.METHOD_GETSTOREDATA)) {
				InteractServer actServer = new InteractServer(mCtx,
						"Get store data", JDBCAdapter.METHOD_GETSTOREDATA,
						blnShowProDialog, true);
				actServer.addParam(JDBCAdapter.TYPE_INTEGER, "ChainID",
						mAlstWS.get(2));
				actServer.execute();
			}
		}

		@Override
		protected void onPreExecute() {
			if (mBlnShowProDialog) {
				proDialog.setTitle(mStrTitle);
				proDialog.setMessage("Processing...");
				proDialog.setCanceledOnTouchOutside(false);
				proDialog.setCancelable(false);
				proDialog.show();
			}

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mDB.isOpen()) {
			mDB.open();
		}
	}
	
	public int getValuePref(SharedPreferences sharedPref, String name,
			int valrtn) {
		return sharedPref.getInt(name, valrtn);
	}

	public void setValuePref(SharedPreferences sharedPref, String name,
			int value) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(name, value);
		editor.commit();
	}

}
