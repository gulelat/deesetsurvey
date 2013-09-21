package com.deeset.deesetsurvey;

import java.util.ArrayList;
import java.util.Calendar;
import org.ksoap2.serialization.SoapObject;
import com.deeset.deesetsurvey.controller.ConnectionDetector;
import com.deeset.deesetsurvey.controller.DynamicViews;
import com.deeset.deesetsurvey.model.JDBCAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StaticSurveys extends Activity {

	private LinearLayout mLiLayStaticSurveys;
	private LinearLayout mLiLayStaticSurveysCur;
	private String mStrStoreId;
	private String mStrUserId;
	private String mStrStoreName;
	private TextView mTxtNameStore;
	private DynamicViews mDynaViews;
	public static ArrayList<Integer> mAlstStaticSurveyId;
	public static ArrayList<String> mAlstStaticSurveyTitle;
	public static ArrayList<Integer> mAlstCurrentSurveyId;
	public static ArrayList<String> mAlstCurrentSurveyTitle;

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
		mStrUserId = getIntent().getStringExtra("userid");
		mStrStoreId = getIntent().getStringExtra("storeid");
		mStrStoreName = getIntent().getStringExtra("storename");
		mTxtNameStore.setText(mStrStoreName);
	}

	private void loadStaticSurveysQuestion() {
		mAlstStaticSurveyId = new ArrayList<Integer>();
		mAlstStaticSurveyTitle = new ArrayList<String>();
		mAlstCurrentSurveyId = new ArrayList<Integer>();
		mAlstCurrentSurveyTitle = new ArrayList<String>();
		if (mStrStoreId != null && mStrUserId != null) {
			InteractServer actServer = new InteractServer(this,
					"Get static surveys",
					JDBCAdapter.METHOD_GETSTATICSURVEYDATA);
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

	private void putIntentValues(Intent intent, String strSurveyId,
			String strSurveyName) {
		Log.i("SurveyID", strSurveyId);
		intent.putExtra("userid", mStrUserId);
		intent.putExtra("storeid", mStrStoreId);
		intent.putExtra("storename", mStrStoreName);
		intent.putExtra("surveyid", strSurveyId);
		intent.putExtra("surveyname", strSurveyName);
	}

	private class InteractServer extends AsyncTask<String, Integer, String> {

		private String mStrTitle = "";
		private String mStrMethod = "";

		private Context mCtx;
		private JDBCAdapter mJDBC;
		private ProgressDialog proDialog;
		private ConnectionDetector conDetect;

		private ArrayList<String> mAlstWS;

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
					if (mStrMethod
							.equals(JDBCAdapter.METHOD_GETSTATICSURVEYDATA)) {
						getStaticSurveyData(soap);
					}
					if (mStrMethod
							.equals(JDBCAdapter.METHOD_GETCURRENTSURVEYDATA)) {
						getCurrentSurveyData(soap);
					}
					return "Connect";
				} else {
					return "Inconnect";
				}
			}
		}

		private void getCurrentSurveyData(SoapObject soap) {
			int intSize = soap.getPropertyCount();
			for (int i = 0; i < intSize; i++) {
				SoapObject object = (SoapObject) soap.getProperty(i);
				mAlstCurrentSurveyId.add(Integer.valueOf(object
						.getPropertyAsString("SurveyID")));
				mAlstCurrentSurveyTitle.add(object
						.getPropertyAsString("Survey_Name"));
			}
		}

		private void getStaticSurveyData(SoapObject soap) {
			int intSize = soap.getPropertyCount();
			for (int i = 0; i < intSize; i++) {
				SoapObject object = (SoapObject) soap.getProperty(i);
				mAlstStaticSurveyId.add(Integer.valueOf(object
						.getPropertyAsString("SurveyID")));
				mAlstStaticSurveyTitle.add(object
						.getPropertyAsString("Survey_Name"));
			}
		}

		@Override
		protected void onPostExecute(String strResult) {
			proDialog.dismiss();
			if (strResult.equals("Error")) {
				Toast.makeText(mCtx, "Can't connect to server!",
						Toast.LENGTH_SHORT).show();
			} else {
				if (strResult.equals("Connect")) {
					if (mStrMethod
							.equals(JDBCAdapter.METHOD_GETSTATICSURVEYDATA)) {
						initialStaticSurveys();
						InteractServer actServer = new InteractServer(mCtx,
								"Get current tasks",
								JDBCAdapter.METHOD_GETCURRENTSURVEYDATA);
						actServer.addParam(JDBCAdapter.TYPE_INTEGER, "StoreID",
								mStrStoreId);
						actServer.execute();
					}
					if (mStrMethod
							.equals(JDBCAdapter.METHOD_GETCURRENTSURVEYDATA)) {
						initialCurrentSurveys();
					}
				} else {
					Toast.makeText(mCtx, "Can't get data from server!",
							Toast.LENGTH_SHORT).show();
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
				putIntentValues(intent, alstInt.get(i).toString(),
						alstStr.get(i));
			}
		}
		startActivity(intent);
	}
}
