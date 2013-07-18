package com.deeset.deesetsurvey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.deeset.deesetsurvey.controller.DynamicViews;
import com.deeset.deesetsurvey.model.DBAdapter;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubmittedResult extends Activity {

	private DBAdapter mDB;
	private LinearLayout mLiLaySubmittedResultQuestion,
			mLiLaySubmittedResultAnswer;
	private String mStrChainId;
	private String mStrStoreId;
	private String mStrSurveyId;
	private String mStrSurveyName;
	private String mStrStoreName;
	private String mStrUserId;
	private TextView mTxtSurveyName, mTxtStoreName, mTxtVisitDate;
	private DynamicViews mDynaViews;
	private int mIntTotal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get full screen and no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.submittedresult);

		initialViews();
		initialConnectDB();
		getIntentValues();
		loadSubmittedResult();
	}

	private void initialViews() {
		mLiLaySubmittedResultQuestion = (LinearLayout) findViewById(R.id.llaySubmittedResultQues);
		mLiLaySubmittedResultAnswer = (LinearLayout) findViewById(R.id.llaySubmittedResultAnswer);
		mTxtSurveyName = (TextView) findViewById(R.id.txtSubmittedResultName);
		mTxtStoreName = (TextView) findViewById(R.id.txtSubmittedResultStore);
		mTxtVisitDate = (TextView) findViewById(R.id.txtSubmittedResultDate);
	}

	private void initialConnectDB() {
		mDB = new DBAdapter(SubmittedResult.this);
		mDB.open();
	}

	private void getIntentValues() {
		mStrUserId = getIntent().getStringExtra("userid");
		mStrChainId = getIntent().getStringExtra("chainid");
		mStrStoreId = getIntent().getStringExtra("storeid");
		mStrSurveyId = getIntent().getStringExtra("surveyid");
		mStrSurveyName = getIntent().getStringExtra("surveyname");
		mStrStoreName = getIntent().getStringExtra("storename");
		mIntTotal = getIntent().getIntExtra("total", 4);
		mTxtSurveyName.setText(Html.fromHtml("<b>Survey Name</b> - "
				+ mStrSurveyName));
		mTxtStoreName.setText(Html.fromHtml("<b>Store </b> - " + mStrStoreName));
		mTxtVisitDate.setText(new SimpleDateFormat("dd/MM/yyyy")
				.format(new Date()));
	}

	private void loadSubmittedResult() {
		TextView txt;
		View vi;
		mDynaViews = new DynamicViews(SubmittedResult.this, null);
		for (int i = 0; i < mIntTotal; i++) {
			ArrayList<ContentValues> arrlstResult = mDB.querySubmittedResult(
					mStrUserId, mStrChainId, mStrStoreId, mStrSurveyId,
					String.valueOf(i + 1));
			Log.i("Size", arrlstResult.size() + "");
			for (int j = 0; j < arrlstResult.size(); j++) {
				ContentValues content = arrlstResult.get(j);
				Log.i("Question", content.getAsString("question") + " - "
						+ content.getAsString("answer"));
				vi = mDynaViews.initialBorder();
				mLiLaySubmittedResultQuestion.addView(vi);
				txt = mDynaViews.initialTextViewByHei(
						content.getAsString("question"), 96);
				mLiLaySubmittedResultQuestion.addView(txt);
				vi = mDynaViews.initialBorder();
				mLiLaySubmittedResultAnswer.addView(vi);
				txt = mDynaViews.initialTextViewByHei(
						content.getAsString("answer"), 96);
				mLiLaySubmittedResultAnswer.addView(txt);
			}
		}
	}

	public void logoutMainMenu(View v) {
		if (mDB != null) {
			mDB.close();
		}
		startActivity(new Intent(this, SelectStore.class).putExtra("userid", mStrUserId));
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
