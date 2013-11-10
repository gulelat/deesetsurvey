package com.deeset.deesetsurvey;

import java.util.ArrayList;
import java.util.Calendar;

import org.ksoap2.serialization.SoapObject;

import com.deeset.deesetsurvey.controller.ConnectionDetector;
import com.deeset.deesetsurvey.controller.DynamicViews;
import com.deeset.deesetsurvey.model.DBAdapter;
import com.deeset.deesetsurvey.model.JDBCAdapter;
import com.deeset.deesetsurvey.profile.GlobalInfo;

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
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StaticSurveys extends Activity {

	private LinearLayout mLiLayStaticSurveys;
	private LinearLayout mLiLayStaticSurveysCur;
	private String mStrStoreId;
	private String mStrUserId;
	private String mStrStoreName;
	private TextView mTxtNameStore;
	private DynamicViews mDynaViews;
	public ArrayList<Integer> mAlstStaticSurveyId;
	public ArrayList<String> mAlstStaticSurveyTitle;
	public ArrayList<Integer> mAlstCurrentSurveyId;
	public ArrayList<String> mAlstCurrentSurveyTitle;
	public DBAdapter mDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.staticsurveys);

		initialViews();
		getIntentValues();
		loadStaticSurveysQuestion();
	}

	private void initialViews() {
		mLiLayStaticSurveys = (LinearLayout) findViewById(R.id.llayStaticSurveyQuestions);
		mLiLayStaticSurveysCur = (LinearLayout) findViewById(R.id.llayStaticSurveyCurrent);
		mTxtNameStore = (TextView) findViewById(R.id.txtStaticSurveyNameStore);
	}

	private void getIntentValues() {
		mStrUserId = GlobalInfo.getUserId();
		mStrStoreId = GlobalInfo.getStoreId();
		mStrStoreName = GlobalInfo.getStoreName();
		mTxtNameStore.setText(mStrStoreName);
		mDB = new DBAdapter(this);
		mDB.open();
	}

	private void loadStaticSurveysQuestion() {
		mAlstStaticSurveyId = new ArrayList<Integer>();
		mAlstStaticSurveyTitle = new ArrayList<String>();
		mAlstCurrentSurveyId = new ArrayList<Integer>();
		mAlstCurrentSurveyTitle = new ArrayList<String>();
		if (mStrStoreId != null && mStrUserId != null) {
			InteractServer actServer = new InteractServer(this,
					"Get static surveys",
					JDBCAdapter.METHOD_GETSTATICSURVEYDATA, true, false);
			actServer
					.addParam(JDBCAdapter.TYPE_INTEGER, "StoreID", mStrStoreId);
			actServer.addParam(JDBCAdapter.TYPE_STRING, "startDate",
					getDateStaticSurvey(2, " 00:00:00"));
			actServer.addParam(JDBCAdapter.TYPE_STRING, "endDate",
					getDateStaticSurvey(7, " 23:59:59"));
			actServer.execute();
		}
	}

	private String getDateStaticSurvey(int intOrder, String strTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(Calendar.getInstance().getTime());
		calendar.set(Calendar.DAY_OF_WEEK, intOrder);
		StringBuffer strDate = new StringBuffer();
		strDate.append(calendar.get(Calendar.YEAR) + "-");
		strDate.append((Integer.valueOf(calendar.get(Calendar.MONTH)) + 1)
				+ "-");
		strDate.append(calendar.get(Calendar.DAY_OF_MONTH) + strTime);
		return strDate.toString();
	}

	public void changeStore(View v) {
		onBackPressed();
	}

	public void logoutApp(View v) {
		startActivity(new Intent(this, Login.class));
	}

	private class InteractServer extends AsyncTask<String, Integer, Integer> {

		private String mStrTitle = "";
		private String mStrMethod = "";

		private Context mCtx;
		private JDBCAdapter mJDBC;
		private ProgressDialog proDialog;
		private ConnectionDetector conDetect;

		private ArrayList<String> mAlstWS;
		private boolean mBlnUpdateData = false;
		private boolean mBlnShowProDialog = true;
		private long longTimeExipred = 0;

		public InteractServer(Context ctx, String strTitle, String strMethod,
				boolean blnShowProDialog, boolean blnUpdateData) {
			mBlnShowProDialog = blnShowProDialog;
			mBlnUpdateData = blnUpdateData;
			mCtx = ctx;
			mStrTitle = strTitle;
			mStrMethod = strMethod;
			mJDBC = new JDBCAdapter();
			conDetect = new ConnectionDetector(mCtx);
			mAlstWS = new ArrayList<String>();
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
				if (querySurvey()) {
					return JDBCAdapter.RESULT_OK;
				}
				return JDBCAdapter.RESULT_NOTDATA;
			} else {
				SoapObject soap = mJDBC.interactServer(mAlstWS, mStrMethod);
				if (soap != null) {
					if (soap.getPropertyCount() != 0) {
						if (mStrMethod
								.equals(JDBCAdapter.METHOD_GETSTATICSURVEYDATA)) {
							mDB.insertSurvey(soap, mAlstWS.get(2), "1",
									longTimeExipred);
						}
						if (mStrMethod
								.equals(JDBCAdapter.METHOD_GETCURRENTSURVEYDATA)) {
							mDB.insertSurvey(soap, mAlstWS.get(2), "0",
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
				longTimeExipred = mDB.queryTimeExpired(DBAdapter.LOG_STORE,
						mAlstWS.get(2));
				if (conDetect.isConnectingToInternet()
						&& Calendar.getInstance().getTimeInMillis()
								- longTimeExipred > JDBCAdapter.TIME_OUT) {
					mBlnUpdateData = true;
					Log.i("update Data", longTimeExipred + "");
				}
			}
		}

		private boolean querySurvey() {
			ArrayList<ContentValues> alstStaticSurvey = mDB.queryDatas(
					DBAdapter.TABLE_SURVEY, "StoreID = ? AND Type = 1",
					new String[] { mAlstWS.get(2) });
			ArrayList<ContentValues> alstCurrentSurvey = mDB.queryDatas(
					DBAdapter.TABLE_SURVEY, "StoreID = ? AND Type = 0",
					new String[] { mAlstWS.get(2) });
			if (alstStaticSurvey.size() > 0 || alstCurrentSurvey.size() > 0) {
				for (ContentValues contentValues : alstStaticSurvey) {
					mAlstStaticSurveyTitle.add(contentValues
							.getAsString("SurveyName"));
					mAlstStaticSurveyId.add(contentValues
							.getAsInteger("SurveyID"));
				}
				for (ContentValues contentValues : alstCurrentSurvey) {
					mAlstCurrentSurveyTitle.add(contentValues
							.getAsString("SurveyName"));
					mAlstCurrentSurveyId.add(contentValues
							.getAsInteger("SurveyID"));
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
						startUpdateStaticSurvey(true);
					} else {
						GlobalInfo.showToast(mCtx,
								JDBCAdapter.STR_NODATAOFFLINE);
					}
				} else {
					initialStaticSurveys();
					initialCurrentSurveys();
					if (conDetect.isConnectingToInternet()
							&& Calendar.getInstance().getTimeInMillis()
									- longTimeExipred > JDBCAdapter.TIME_UPDATE) {
						Log.i("update Data", longTimeExipred + "");
						startUpdateStaticSurvey(false);
					}
				}
			} else {
				if (result == JDBCAdapter.RESULT_OK) {
					if (mStrMethod
							.equals(JDBCAdapter.METHOD_GETSTATICSURVEYDATA)) {
						if (mBlnShowProDialog) {
							if (!querySurvey()) {
								GlobalInfo.showToast(mCtx,
										JDBCAdapter.STR_NOTLOADDATA);
							} else {
								initialStaticSurveys();
							}
							startUpdateCurrentSurvey(true);
						} else {
							startUpdateCurrentSurvey(false);
						}
					}
					if (mStrMethod
							.equals(JDBCAdapter.METHOD_GETCURRENTSURVEYDATA)) {
						if (mBlnShowProDialog) {
							if (!querySurvey()) {
								GlobalInfo.showToast(mCtx,
										JDBCAdapter.STR_NOTLOADDATA);
							} else {
								initialCurrentSurveys();
							}
						}
					}
				} else {
					if (result == JDBCAdapter.RESULT_EMPTYDATA) {
						if (mStrMethod
								.equals(JDBCAdapter.METHOD_GETSTATICSURVEYDATA)) {
							GlobalInfo.showToast(mCtx,
									JDBCAdapter.STR_EMPTYDATA + "store "
											+ mStrStoreName + "!");
						}
					} else {
						GlobalInfo.showToast(mCtx, JDBCAdapter.STR_NOCONNECT);
					}
				}
			}
			proDialog.dismiss();
		}

		private void startUpdateStaticSurvey(boolean blnShowProDialog) {
			InteractServer actServer = new InteractServer(mCtx,
					"Get static surveys",
					JDBCAdapter.METHOD_GETSTATICSURVEYDATA, blnShowProDialog,
					true);
			actServer.addParam(JDBCAdapter.TYPE_INTEGER, "StoreID",
					mAlstWS.get(2));
			actServer.addParam(JDBCAdapter.TYPE_STRING, "startDate",
					mAlstWS.get(5));
			actServer.addParam(JDBCAdapter.TYPE_STRING, "endDate",
					mAlstWS.get(8));
			actServer.execute();
		}

		private void startUpdateCurrentSurvey(boolean blnShowProDialog) {
			InteractServer actServer = new InteractServer(mCtx,
					"Get current surveys",
					JDBCAdapter.METHOD_GETCURRENTSURVEYDATA, blnShowProDialog,
					true);
			actServer.addParam(JDBCAdapter.TYPE_INTEGER, "StoreID",
					mAlstWS.get(2));
			actServer.execute();
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

	public void initialStaticSurveys() {
		mDynaViews = new DynamicViews(StaticSurveys.this, null);
		View vi;
		TextView txt;
		for (int i = 0; i < mAlstStaticSurveyId.size(); i++) {
			vi = mDynaViews.initialBorder();
			txt = mDynaViews.initialTextView(mAlstStaticSurveyTitle.get(i),
					true);
			txt.setId(mAlstStaticSurveyId.get(i));
			txt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startSurvey(v, mAlstStaticSurveyId, mAlstStaticSurveyTitle);
				}
			});
			mLiLayStaticSurveys.addView(vi);
			mLiLayStaticSurveys.addView(txt);
			mLiLayStaticSurveys.setVisibility(View.VISIBLE);
		}
	}

	public void initialCurrentSurveys() {
		mDynaViews = new DynamicViews(StaticSurveys.this, null);
		View vi;
		TextView txt;
		for (int i = 0; i < mAlstCurrentSurveyId.size(); i++) {
			vi = mDynaViews.initialBorder();
			txt = mDynaViews.initialTextView(mAlstCurrentSurveyTitle.get(i),
					true);
			txt.setId(mAlstCurrentSurveyId.get(i));
			txt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startSurvey(v, mAlstCurrentSurveyId,
							mAlstCurrentSurveyTitle);
				}
			});
			mLiLayStaticSurveysCur.addView(vi);
			mLiLayStaticSurveysCur.addView(txt);
			mLiLayStaticSurveysCur.setVisibility(View.VISIBLE);
		}
	}

	public void startSurvey(View vi, ArrayList<Integer> alstInt,
			ArrayList<String> alstStr) {
		Intent intent = new Intent(StaticSurveys.this, FieldSurvey.class);
		for (int i = 0; i < alstInt.size(); i++) {
			if (vi.getId() == alstInt.get(i)) {
				GlobalInfo.setSurveyId(alstInt.get(i).toString());
				GlobalInfo.setSurveyName(alstStr.get(i));
			}
		}
		startActivity(intent);
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
