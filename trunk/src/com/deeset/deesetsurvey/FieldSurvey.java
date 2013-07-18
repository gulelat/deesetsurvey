package com.deeset.deesetsurvey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import com.deeset.deesetsurvey.controller.DrawSomethingView;
import com.deeset.deesetsurvey.controller.DynamicViews;
import com.deeset.deesetsurvey.controller.SurveyQuestion;
import com.deeset.deesetsurvey.model.DBAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FieldSurvey extends Activity {

	private DBAdapter mDB;
	private LinearLayout mLiLayFieldSurvey;
	private TextView mTxtSurveyName, mTxtStoreName, mTxtVisitDate;
	private DynamicViews mDynaViews;
	private ArrayList<SurveyQuestion> mArrLstSurCont;
	private ArrayList<String> mArrlstAnswer;
	private ArrayList<String> mArrLstAns;
	private ArrayList<Integer> mArrLstIdChild;
	private String mStrChainId;
	private String mStrStoreId;
	private String mStrStoreName;
	private String mStrSurveyId;
	private String mStrSurveyName;
	private String mStrUserId;
	private AtomicInteger mAtomicInt;
	private int mIntCamareGalleryResult;
	private int CAMERA_RESULT = 12345;
	private int GALLERY_RESULT = 54321;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get full screen and no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.fieldsurvey);

		initialViews();
		initialConnectDB();
		getIntentValues();
		loadStaticSurveysQuestion();
	}

	private void initialViews() {
		mLiLayFieldSurvey = (LinearLayout) findViewById(R.id.llayFieldSurveyQuestions);
		mTxtSurveyName = (TextView) findViewById(R.id.txtFieldSurveyName);
		mTxtStoreName = (TextView) findViewById(R.id.txtFieldSurveyStore);
		mTxtVisitDate = (TextView) findViewById(R.id.txtFieldSurveyDate);
	}

	private void initialConnectDB() {
		mDB = new DBAdapter(FieldSurvey.this);
		mDB.open();
	}

	private void getIntentValues() {
		mStrUserId = getIntent().getStringExtra("userid");
		mStrChainId = getIntent().getStringExtra("chainid");
		mStrStoreId = getIntent().getStringExtra("storeid");
		mStrStoreName = getIntent().getStringExtra("storename");
		mStrSurveyId = getIntent().getStringExtra("surveyid");
		mStrSurveyName = getIntent().getStringExtra("surveyname");
		mTxtSurveyName.setText(Html.fromHtml("<b>Survey Name</b> - "
				+ mStrSurveyName));
		mTxtStoreName
				.setText(Html.fromHtml("<b>Store </b> - " + mStrStoreName));
		mTxtVisitDate.setText(new SimpleDateFormat("dd/MM/yyyy")
				.format(new Date()));
		Log.i("Intent", mStrUserId + " " + mStrChainId + " " + mStrStoreId
				+ " " + mStrSurveyId);
	}

	private void loadStaticSurveysQuestion() {
		mArrLstSurCont = new ArrayList<SurveyQuestion>();
		mArrlstAnswer = new ArrayList<String>();
		mAtomicInt = new AtomicInteger(0);
		String strQuestion;
		mIntCamareGalleryResult = mAtomicInt.getAndIncrement();

		ArrayList<ContentValues> arrlstSurCont = mDB
				.querySurveyContains(mStrSurveyId);
		for (int i = 0; i < arrlstSurCont.size(); i++) {
			mArrLstAns = new ArrayList<String>();
			mArrLstIdChild = new ArrayList<Integer>();
			ContentValues cont = arrlstSurCont.get(i);
			if (cont.size() != 0) {
				loadSurveyAnswer(cont.getAsString("Question_ID"));
			} else {
				arrlstSurCont.remove(i);
			}
			strQuestion = cont.getAsString("Question_Order") + "). "
					+ cont.getAsString("Question_Description");
			Log.i("Question Contain",
					strQuestion + " - " + cont.getAsString("Question_Type"));
			createQuestions(mAtomicInt.getAndIncrement(), mArrLstIdChild,
					strQuestion, cont.getAsString("Question_Type"), mArrLstAns);
		}
		Log.i("Question Contain", mArrLstSurCont.size() + "");
		loadQuestionViews();
	}

	private void loadSurveyAnswer(String strQuesId) {
		ArrayList<ContentValues> arrlst = mDB.querySurveyAnwers(strQuesId);
		for (int i = 0; i < arrlst.size(); i++) {
			mArrLstAns.add(arrlst.get(i).getAsString("Question_Answer_Value"));
			mArrLstIdChild.add(mAtomicInt.getAndIncrement());
		}
	}

	private void loadQuestionViews() {
		mDynaViews = new DynamicViews(FieldSurvey.this, null);
		SurveyQuestion surQues;
		for (int i = 0; i < mArrLstSurCont.size(); i++) {
			surQues = mArrLstSurCont.get(i);
			if (surQues.getmStrType().equals("1")) {
				loadSpinnerQuestion(surQues);
			}
			if (surQues.getmStrType().equals("2")) {
				loadRadioGroupQuestion(surQues);
			}
			if (surQues.getmStrType().equals("3")) {
				loadCheckBoxQuestion(surQues);
			}
			if (surQues.getmStrType().equals("4")) {
				loadEditTextQuestion(surQues, 4);
			}
			if (surQues.getmStrType().equals("5")) {
				loadEditTextQuestion(surQues, 5);
			}
			if (surQues.getmStrType().equals("6")) {
				loadEditTextQuestion(surQues, 6);
			}
			if (surQues.getmStrType().equals("7")) {
				loadEditTextQuestion(surQues, 7);
			}
			if (surQues.getmStrType().equals("8")) {
				loadTextViewSection(surQues);
			}
			if (surQues.getmStrType().equals("9")) {
				loadButton(surQues, i);
			}
			if (surQues.getmStrType().equals("11")) {
				loadImageView(surQues, i);
			}
			if (surQues.getmStrType().equals("12")) {

			}
		}
	}

	private void loadImageView(SurveyQuestion surQues, final int intIndex) {
		TextView txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(),
				false);
		mLiLayFieldSurvey.addView(txt);
		ImageView imgView = mDynaViews.initialImageView(420, 140);
		imgView.setId(mAtomicInt.getAndIncrement());
		imgView.setBackgroundResource(R.drawable.border);
		int intPadding = convertDIPtoPixel(2);
		imgView.setPadding(intPadding, intPadding, intPadding, intPadding);
		imgView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogSign((ImageView) v, intIndex);
			}
		});
		mLiLayFieldSurvey.addView(imgView);
	}

	protected void dialogSign(ImageView imgView, int intIndex) {
		LinearLayout llayout = new LinearLayout(this);
		LinearLayout llayoutSign = initialDrawView();
		llayout.addView(llayoutSign);
		llayout.setBackgroundColor(Color.GRAY);
		llayout.setGravity(Gravity.CENTER);
		initialDialogSign(llayout, llayoutSign, imgView, intIndex);
	}

	public LinearLayout initialDrawView() {
		LinearLayout llaySign = new LinearLayout(this);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.WRAP_CONTENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		llaySign.setLayoutParams(layParams);

		DrawSomethingView drawView = new DrawSomethingView(this);
		layParams = new LayoutParams();
		layParams.width = convertDIPtoPixel(270);
		layParams.height = convertDIPtoPixel(90);
		drawView.setLayoutParams(layParams);

		llaySign.addView(drawView);
		llaySign.setBackgroundColor(Color.WHITE);
		llaySign.setGravity(Gravity.CENTER);
		return llaySign;
	}

	public int convertDIPtoPixel(float intDIP) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				intDIP, getResources().getDisplayMetrics());
	}

	private void initialDialogSign(LinearLayout llayout, final View view,
			final ImageView imgView, final int intIndex) {
		final DrawSomethingView drawView = new DrawSomethingView(this);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Please give signature");
		dialog.setView(llayout);
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			// do something when the button is clicked
			public void onClick(DialogInterface dialog, int arg1) {
				String strPath = drawView.getScreen(view, "signature");
				updateAnsSurvey(intIndex, strPath);
				File file = new File(strPath);
				if (file.exists()) {
					imgView.setImageBitmap(BitmapFactory.decodeFile(strPath));
				}
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}

	protected void updateAnsSurvey(int intIndex, String strPath) {
		Log.i("Answer", mArrLstSurCont.get(intIndex).getmArrLstAns().size()
				+ "");
		SurveyQuestion sur = mArrLstSurCont.get(intIndex);
		ArrayList<String> arrlstAns = new ArrayList<String>();
		arrlstAns.add(strPath);
		sur.setmArrLstAns(arrlstAns);
		mArrLstSurCont.remove(intIndex);
		mArrLstSurCont.add(intIndex, sur);
		Log.i("Answer",
				mArrLstSurCont.get(intIndex).getmArrLstAns().get(0));
	}

	private void loadButton(SurveyQuestion surQues, final int intIndex) {
		TextView txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(),
				false);
		mLiLayFieldSurvey.addView(txt);
		ImageView img = mDynaViews.initialImageView(420, 420);
		img.setId(mIntCamareGalleryResult);
		img.setBackgroundResource(R.drawable.border);
		img.setPadding(2, 2, 2, 2);
		mLiLayFieldSurvey.addView(img);
		LinearLayout llayout = new LinearLayout(this);
		LinearLayout llayoutCamera = new LinearLayout(this);
		Button btnCamera = mDynaViews.initialButton(128, 64, "Camera");
		btnCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				CAMERA_RESULT = intIndex;
				startActivityForResult(cameraIntent, CAMERA_RESULT);
			}
		});
		Button btnGallery = mDynaViews.initialButton(128, 64, "Gallery");
		btnGallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				GALLERY_RESULT = intIndex;
				startActivityForResult(intent, GALLERY_RESULT);
			}
		});
		llayout.setOrientation(LinearLayout.HORIZONTAL);
		llayoutCamera.addView(btnCamera);
		llayoutCamera.setPadding(0, 0, 64, 0);
		llayout.addView(llayoutCamera);
		llayout.addView(btnGallery);
		llayout.setPadding(0, 16, 0, 0);
		mLiLayFieldSurvey.addView(llayout);
	}

	private void loadTextViewSection(SurveyQuestion surQues) {
		TextView txt;
		if (surQues.getmArrLstAns().size() != 0) {
			txt = mDynaViews.initialTextView(surQues.getmStrQuesCont() + " "
					+ surQues.getmArrLstAns().get(0), false);
		} else {
			txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(), false);
		}
		mLiLayFieldSurvey.addView(txt);
	}

	private void loadRadioGroupQuestion(SurveyQuestion surQues) {
		TextView txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(),
				false);
		mLiLayFieldSurvey.addView(txt);
		RadioGroup radGroup = mDynaViews.initialRadioGroup(
				surQues.getmArrLstAns(), surQues.getmIntIdChild());
		radGroup.setId(surQues.getmIntId());
		mLiLayFieldSurvey.addView(radGroup);
	}

	private void loadEditTextQuestion(SurveyQuestion surQues, int intTypeEdt) {
		TextView txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(),
				false);
		mLiLayFieldSurvey.addView(txt);
		EditText edtText = mDynaViews.initialEditText(intTypeEdt);
		edtText.setId(surQues.getmIntId());
		mLiLayFieldSurvey.addView(edtText);
	}

	private void loadCheckBoxQuestion(SurveyQuestion surQues) {
		TextView txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(),
				false);
		mLiLayFieldSurvey.addView(txt);
		CheckBox ckb;
		for (int i = 0; i < surQues.getmArrLstAns().size(); i++) {
			ckb = mDynaViews.initialCheckBox(surQues.getmArrLstAns().get(i));
			ckb.setId(surQues.getmIntIdChild().get(i));
			mLiLayFieldSurvey.addView(ckb);
		}
	}

	private void loadSpinnerQuestion(SurveyQuestion surQues) {
		TextView txt = mDynaViews.initialTextView(surQues.getmStrQuesCont(),
				false);
		mLiLayFieldSurvey.addView(txt);
		Spinner spin = mDynaViews.initialSpinner(surQues.getmArrLstAns());
		spin.setId(surQues.getmIntId());
		mLiLayFieldSurvey.addView(spin);
	}

	private void createQuestions(int intId, ArrayList<Integer> arrlstIdChild,
			String strQuesCont, String strType, ArrayList<String> arrlstAns) {
		SurveyQuestion surQues = new SurveyQuestion();
		surQues.setmIntId(intId);
		surQues.setmIntIdChild(arrlstIdChild);
		surQues.setmStrQuesCont(strQuesCont);
		surQues.setmStrType(strType);
		surQues.setmArrLstAns(arrlstAns);
		mArrLstSurCont.add(surQues);
	}

	public void submitSurveyQuestions(View v) {
		mArrlstAnswer.clear();
		for (int i = 0; i < mArrLstSurCont.size(); i++) {
			if (checkValueView(mArrLstSurCont.get(i)) == false) {
				Toast.makeText(FieldSurvey.this,
						"Please answer the questions!", Toast.LENGTH_SHORT)
						.show();
				return;
			}
		}

		Log.i("Answers", mArrlstAnswer.toString());
		Log.i("Question Contain", mArrLstSurCont.size() + "");
		for (int i = 0; i < mArrLstSurCont.size(); i++) {
			insertAnswerDB((i + 1), mArrLstSurCont.get(i).getmStrQuesCont(),
					mArrlstAnswer.get(i));
		}

		Intent intent = new Intent(FieldSurvey.this, SubmittedResult.class);
		putIntentValues(intent);
		startActivity(intent);
	}

	private void insertAnswerDB(int intSeqNum, String strQuesCont,
			String strAnswer) {
		ContentValues content = new ContentValues();
		content.put("userid", mStrUserId);
		content.put("chain", mStrChainId);
		content.put("store", mStrStoreId);
		content.put("surveyname", mStrSurveyId);
		content.put("seqnum", intSeqNum);
		content.put("question", strQuesCont);
		content.put("answer", strAnswer);
		mDB.insertAnswerDB("TblSubmitted", content);
	}

	private void putIntentValues(Intent intent) {
		intent.putExtra("userid", mStrUserId);
		intent.putExtra("chainid", mStrChainId);
		intent.putExtra("storeid", mStrStoreId);
		intent.putExtra("surveyid", mStrSurveyId);
		intent.putExtra("storename", mStrStoreName);
		intent.putExtra("surveyname", mStrSurveyName);
		intent.putExtra("total", mArrLstSurCont.size());
	}

	private boolean checkValueView(SurveyQuestion surveyQuestion) {
		if (surveyQuestion.getmStrType().equals("1")) {
			return checkSpinner(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("2")) {
			return checkRadioGroup(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("3")) {
			return checkCheckBox(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("4")) {
			return checkEditText(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("5")) {
			return checkEditText(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("6")) {
			return checkEditText(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("7")) {
			return checkEditText(surveyQuestion);
		}
		return true;
	}

	private boolean checkCheckBox(SurveyQuestion surveyQuestion) {
		CheckBox ckb;
		StringBuffer strBuff = new StringBuffer();
		int i = 0;
		for (; i < surveyQuestion.getmIntIdChild().size(); i++) {
			ckb = (CheckBox) findViewById(surveyQuestion.getmIntIdChild()
					.get(i));
			if (ckb.isChecked()) {
				strBuff.append(ckb.getText().toString() + "\n");
			}
		}
		if (i == surveyQuestion.getmIntIdChild().size()) {
			mArrlstAnswer.add(strBuff.toString());
			return true;
		} else {
			return false;
		}
	}

	private boolean checkEditText(SurveyQuestion surveyQuestion) {
		EditText edtText = (EditText) findViewById(surveyQuestion.getmIntId());
		if (edtText.getText().toString().equals("")) {
			return false;
		} else {
			mArrlstAnswer.add(edtText.getText().toString());
			return true;
		}
	}

	private boolean checkRadioGroup(SurveyQuestion surveyQuestion) {
		RadioGroup radGroup = (RadioGroup) findViewById(surveyQuestion
				.getmIntId());
		if (radGroup.getCheckedRadioButtonId() == -1) {
			return false;
		} else {
			Log.i("radId", radGroup.getCheckedRadioButtonId() + "");
			RadioButton rad = (RadioButton) findViewById(radGroup
					.getCheckedRadioButtonId());
			if (rad.getId() != -1) {
				mArrlstAnswer.add(rad.getText().toString());
			}
			return true;
		}
	}

	private boolean checkSpinner(SurveyQuestion surveyQuestion) {
		Spinner spin = (Spinner) findViewById(surveyQuestion.getmIntId());
		if (spin.getSelectedItemPosition() == 0) {
			return false;
		} else {
			mArrlstAnswer.add(spin.getSelectedItem().toString());
			return true;
		}
	}

	public void logoutMainMenu(View v) {
		if (mDB != null) {
			mDB.close();
		}
		startActivity(new Intent(this, SelectStore.class).putExtra("userid",
				mStrUserId));
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_RESULT) {
			if (null != data && resultCode == RESULT_OK) {
				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bytes);
				String strPath = Environment.getExternalStorageDirectory()
						+ File.separator + "DCIM/Camera/"
						+ Calendar.getInstance().getTimeInMillis() + ".png";
				File f = new File(strPath);
				try {
					f.createNewFile();
					FileOutputStream fo = new FileOutputStream(f);
					fo.write(bytes.toByteArray());
					fo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ImageView img = (ImageView) findViewById(mIntCamareGalleryResult);
				img.setImageBitmap(BitmapFactory.decodeFile(strPath));
				updateAnsSurvey(CAMERA_RESULT, strPath);
				CAMERA_RESULT = 12345;
			} else {
				Toast.makeText(this, "Please choose an capture image",
						Toast.LENGTH_SHORT).show();
			}
		}
		if (requestCode == GALLERY_RESULT) {
			if (null != data && resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String strPath = cursor.getString(columnIndex);
				cursor.close();
				ImageView img = (ImageView) findViewById(mIntCamareGalleryResult);
				img.setImageBitmap(BitmapFactory.decodeFile(strPath));
				updateAnsSurvey(GALLERY_RESULT, strPath);
				GALLERY_RESULT = 54321;
			} else {
				Toast.makeText(this, "Please choose an image",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
