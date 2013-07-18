package com.deeset.deesetsurvey;

import java.util.ArrayList;
import com.deeset.deesetsurvey.controller.DynamicViews;
import com.deeset.deesetsurvey.model.DBAdapter;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StaticSurveys extends Activity implements OnClickListener {

	private DBAdapter mDB;
	private LinearLayout mLiLayStaticSurveys, mLiLayStaticSurveysCur;
	private String mStrChainId;
	private String mStrStoreId;
	private String mStrUserId;
	private String mStrStoreName;
	private TextView mTxtNameStore;
	private DynamicViews mDynaViews;
	private ArrayList<String> mArrLstSurvey;
	private ArrayList<String> mArrLstSurveyId;
	private ArrayList<String> mArrLstSurveyStatic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get full screen and no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.staticsurveys);

		initialConnectDB();
		initialViews();
		getIntentValues();
		loadStaticSurveysQuestion();
	}

	private void initialViews() {
		mLiLayStaticSurveys = (LinearLayout) findViewById(R.id.llayStaticSurveyQuestions);
		mLiLayStaticSurveysCur = (LinearLayout) findViewById(R.id.llayStaticSurveyCurrent);
		mTxtNameStore = (TextView) findViewById(R.id.txtStaticSurveyNameStore);
	}

	private void initialConnectDB() {
		mDB = new DBAdapter(StaticSurveys.this);
		mDB.open();
	}

	private void getIntentValues() {
		mStrUserId = getIntent().getStringExtra("userid");
		mStrChainId = getIntent().getStringExtra("chainid");
		mStrStoreId = getIntent().getStringExtra("storeid");
		mStrStoreName = getIntent().getStringExtra("storename");
		mTxtNameStore.setText(mStrStoreName);
	}

	private void loadStaticSurveysQuestion() {
		initialQuestion();
		mDynaViews = new DynamicViews(StaticSurveys.this, null);
		View vi;
		TextView txt;
		Log.i("Survey", mArrLstSurvey.size() + " " + mArrLstSurveyId.size());
		for (int i = 0; i < mArrLstSurvey.size(); i++) {
			vi = mDynaViews.initialBorder();
			txt = mDynaViews.initialTextView(mArrLstSurvey.get(i), true);
			txt.setId(i);
			txt.setOnClickListener(this);
			Log.i("Static", mArrLstSurveyStatic.get(i) + " - " + i);
			if (mArrLstSurveyStatic.get(i).equals("1")) {
				mLiLayStaticSurveys.addView(vi);
				mLiLayStaticSurveys.addView(txt);
			} else {
				mLiLayStaticSurveysCur.addView(vi);
				mLiLayStaticSurveysCur.addView(txt);
				mLiLayStaticSurveysCur.setVisibility(View.VISIBLE);
			}
		}
	}

	private void initialQuestion() {
		mArrLstSurvey = new ArrayList<String>();
		mArrLstSurveyStatic = new ArrayList<String>();
		mArrLstSurveyId = mDB.queryStoreSurveys(mStrStoreId);
		Log.i("StoreId", mStrStoreId);
		for (int i = 0; i < mArrLstSurveyId.size(); i++) {
			ContentValues contentSurvey = mDB.querySurveys(mArrLstSurveyId
					.get(i));
			Log.i("Survey", i + " - " + contentSurvey.toString());
			if (contentSurvey.size() != 0) {
				mArrLstSurvey.add(contentSurvey.getAsString("Survey_Name"));
				mArrLstSurveyStatic.add(contentSurvey
						.getAsString("Survey_IsStatic"));
			} else {
				mArrLstSurveyId.remove(i);
			}
		}
	}

	public void changeStore(View v) {
		if (mDB != null) {
			mDB.close();
		}
		startActivity(new Intent(StaticSurveys.this, SelectStore.class)
				.putExtra("userid", mStrUserId));
		finish();
	}

	public void logoutApp(View v) {
		if (mDB != null) {
			mDB.close();
		}
		startActivity(new Intent(this, Login.class));
	}

	@Override
	protected void onPause() {
		if (mDB != null) {
			mDB.close();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mDB.open();
		super.onResume();
	}

	@Override
	public void onClick(View vi) {
		Intent intent = new Intent(StaticSurveys.this, FieldSurvey.class);
		for (int i = 0; i < mArrLstSurvey.size(); i++) {
			if (vi.getId() == i) {
				putIntentValues(intent, mArrLstSurveyId.get(i),
						mArrLstSurvey.get(i));
			}
		}
		if (mDB != null) {
			mDB.close();
		}
		startActivity(intent);
	}

	private void putIntentValues(Intent intent, String strSurveyId,
			String strSurveyName) {
		intent.putExtra("userid", mStrUserId);
		intent.putExtra("chainid", mStrChainId);
		intent.putExtra("storeid", mStrStoreId);
		intent.putExtra("storename", mStrStoreName);
		intent.putExtra("surveyid", strSurveyId);
		intent.putExtra("surveyname", strSurveyName);
	}
}
