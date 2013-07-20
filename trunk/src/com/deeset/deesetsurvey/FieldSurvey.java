package com.deeset.deesetsurvey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.deeset.deesetsurvey.controller.DrawSomethingView;
import com.deeset.deesetsurvey.controller.DynamicViews;
import com.deeset.deesetsurvey.controller.SurveyQuestion;
import com.deeset.deesetsurvey.model.DBAdapter;
import com.deeset.deesetsurvey.controller.UploadFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
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
	private String mStrPathPhoto = "";
	private String mStrPathPhotoUpload = "";
	private String mStrPathSign = "";
	private String mStrPathSQL = "";
	private String mStrUsername = "";
	private String mStrTimestamp = "";
	private boolean mBlnCamera, mBlPhoto = false;
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
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
				mStrPathSign = drawView.getScreen(view, "signature");
				updateAnsSurvey(intIndex, mStrPathSign);
				File file = new File(mStrPathSign);
				if (file.exists()) {
					imgView.setImageBitmap(BitmapFactory
							.decodeFile(mStrPathSign));
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
		Log.i("Answer", mArrLstSurCont.get(intIndex).getmArrLstAns().get(0));
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
		Button btnCamera = mDynaViews.initialButton(160, 64, "Camera");
		btnCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				CAMERA_RESULT = intIndex;
				startActivityForResult(cameraIntent, CAMERA_RESULT);
			}
		});
		Button btnGallery = mDynaViews.initialButton(160, 64, "Gallery");
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
		ContentValues content = mDB.queryUserById(mStrUserId);
		if (content.getAsString("username") != null) {
			mStrUsername = content.getAsString("username");
			mStrTimestamp = String.valueOf(Calendar.getInstance()
					.getTimeInMillis());
			new SendSurvey(this).execute();
		}
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
		if (surveyQuestion.getmStrType().equals("9")) {
			return checkTakePhoto(surveyQuestion);
		}
		if (surveyQuestion.getmStrType().equals("11")) {
			return checkSignature(surveyQuestion);
		}
		return true;
	}

	private boolean checkSignature(SurveyQuestion surveyQuestion) {
		if (mStrPathSign.equals("")) {
			return false;
		} else {
			mArrlstAnswer.add(mStrPathSign);
			return true;
		}
	}

	private boolean checkTakePhoto(SurveyQuestion surveyQuestion) {
		if (mStrPathPhoto.equals("")) {
			return false;
		} else {
			mArrlstAnswer.add(mStrPathPhoto);
			return true;
		}
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
		// if (spin.getSelectedItemPosition() == 0) {
		// return false;
		// } else {
		mArrlstAnswer.add(spin.getSelectedItem().toString());
		return true;
		// }
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
		if (mStrPathPhoto.equals("") == false) {
			File file = new File(mStrPathPhoto);
			if (file.exists()) {
				ImageView img = (ImageView) findViewById(mIntCamareGalleryResult);
				img.setImageBitmap(BitmapFactory.decodeFile(mStrPathPhoto));
			}
		}
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_RESULT) {
			if (null != data && resultCode == RESULT_OK) {
				mBlnCamera = true;
				mBlPhoto = true;

				ImageView img = (ImageView) findViewById(mIntCamareGalleryResult);
				if (mStrPathPhoto.equals("") == false) {
					img.setImageBitmap(null);
				}

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				mStrPathPhoto = cursor.getString(columnIndex);
				cursor.close();
				updateAnsSurvey(CAMERA_RESULT, mStrPathPhoto);
				CAMERA_RESULT = 12345;
			} else {
				Toast.makeText(this, "Please choose an capture image",
						Toast.LENGTH_SHORT).show();
			}
		}
		if (requestCode == GALLERY_RESULT) {
			if (null != data && resultCode == RESULT_OK) {
				mBlnCamera = false;
				mBlPhoto = true;

				ImageView img = (ImageView) findViewById(mIntCamareGalleryResult);
				if (mStrPathPhoto.equals("") == false) {
					img.setImageBitmap(null);
				}

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				mStrPathPhoto = cursor.getString(columnIndex);
				cursor.close();
				updateAnsSurvey(GALLERY_RESULT, mStrPathPhoto);
				GALLERY_RESULT = 54321;
			} else {
				Toast.makeText(this, "Please choose an image",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private String resizePhoto(String strPath) {
		File file = new File(strPath);
		if (file.exists()) {
			Bitmap bitmap = new BitmapFactory().decodeFile(strPath);

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bytes);

			File path = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "DCMI");
			if (!path.exists()) {
				path.mkdir();
			}
			String strNewPath = path.getPath() + "/photo.jpg";

			File f = new File(strNewPath);
			try {
				f.createNewFile();
				FileOutputStream fo = new FileOutputStream(f);
				fo.write(bytes.toByteArray());

				fo.close();
				return strNewPath;
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}
		return "";
	}

	public class SendSurvey extends AsyncTask<String, Integer, String> {

		private Context mCtx;
		private ProgressDialog mProgress;
		private ArrayList<String> mAlstTitle, mAlstContent;

		public SendSurvey(Context ctx) {
			mCtx = ctx;
			mProgress = new ProgressDialog(mCtx);
		}

		@Override
		protected void onPreExecute() {
			mProgress.setIcon(android.R.drawable.stat_sys_upload);
			mProgress.setTitle("Upload data");
			mProgress.setMessage("Uploading...");
			mProgress.setCancelable(false);
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (mBlPhoto) {
				ImageView img = (ImageView) findViewById(mIntCamareGalleryResult);
				img.setImageBitmap(null);
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected String doInBackground(String... params) {
			mArrlstAnswer.clear();
			for (int i = 0; i < mArrLstSurCont.size(); i++) {
				if (checkValueView(mArrLstSurCont.get(i)) == false) {
					return "fill full answers";
				}
			}

			publishProgress(0);

			Log.i("Answers", mArrlstAnswer.toString());
			Log.i("Question Contain", mArrLstSurCont.size() + "");
			for (int i = 0; i < mArrLstSurCont.size(); i++) {
				insertAnswerDB((i + 1),
						mArrLstSurCont.get(i).getmStrQuesCont(),
						mArrlstAnswer.get(i));
			}

			exportData();

			String strResult;
			do {
				strResult = uploadFile(mStrPathSQL);
			} while (strResult.equals("fail") == true);

			do {
				strResult = uploadFile(mStrPathSign);
			} while (strResult.equals("fail") == true);

			resizePhotoUpload();

			do {
				strResult = uploadFile(mStrPathPhotoUpload);
			} while (strResult.equals("fail") == true);

			File file = new File(mStrPathPhotoUpload);
			if (file.exists()) {
				file.delete();
				mStrPathPhotoUpload = "";
			}

			return "error";
		}

		private void resizePhotoUpload() {
			if (mBlnCamera) {
				do {
					mStrPathPhotoUpload = resizePhoto(mStrPathPhoto);
				} while (mStrPathPhotoUpload.equals(""));
			} else {
				File file = new File(mStrPathPhoto);
				if (file.length() > 400000) {
					do {
						mStrPathPhotoUpload = resizePhoto(mStrPathPhoto);
					} while (mStrPathPhotoUpload.equals(""));
				}
			}
		}

		private String uploadFile(String strPath) {
			if (strPath.equals("") == false) {
				File fileUpload = new File(strPath);
				if (fileUpload.exists()) {
					UploadFile upload = new UploadFile(mCtx, mStrUsername,
							mStrTimestamp);
					upload.execute(strPath);
					try {
						if (upload.get(60, TimeUnit.SECONDS)) {
							Log.i("Upload", "Successful!");
							return "success";
						} else {
							return "fail";
						}
					} catch (InterruptedException e) {
						Log.i("InterruptedException Data", e.getMessage());
					} catch (ExecutionException e) {
						Log.i("ExecutionException Data", e.getMessage());
					} catch (TimeoutException e) {
						return "fail";
					}
				}
			}
			return "Not found";
		}

		private void exportData() {
			Log.i("Write file", "Start");
			StringBuffer strData = new StringBuffer();
			strData.append("INSERT INTO deesetsurvey('");
			mAlstTitle = new ArrayList<String>();
			addArrayTitle(mArrLstSurCont.size());
			for (int i = 0; i < mAlstTitle.size() - 1; i++) {
				strData.append(mAlstTitle.get(i) + "', '");
			}
			strData.append(mAlstTitle.get(mAlstTitle.size() - 1)
					+ "') VALUES ('");

			mAlstContent = new ArrayList<String>();
			addArrayContent(mArrLstSurCont.size());
			for (int i = 0; i < mAlstContent.size() - 1; i++) {
				strData.append(mAlstContent.get(i) + "', '");
			}
			strData.append(mAlstContent.get(mAlstContent.size() - 1) + "');");

			mStrPathSQL = Environment.getExternalStorageDirectory().toString()
					+ "/DCMI";
			File fileSQL = new File(mStrPathSQL);
			if (!fileSQL.exists()) {
				fileSQL.mkdir();
			}
			fileSQL = new File(mStrPathSQL, "survey.txt");
			mStrPathSQL += File.separator + "survey.txt";

			FileOutputStream fos;
			try {
				fos = new FileOutputStream(fileSQL);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				osw.write(strData.toString());
				osw.flush();
				osw.close();
				Log.i("Write file", "Done");
			} catch (FileNotFoundException e) {
				Log.i("FileNotFoundException", e.getMessage());
			} catch (IOException e) {
				Log.i("IOException", e.getMessage());
			}
		}

		private void addArrayContent(int intContSize) {
			mAlstContent.add(mStrChainId);
			mAlstContent.add(mStrStoreId);
			mAlstContent.add(mStrSurveyId);
			for (int i = 0; i < intContSize; i++) {
				mAlstContent.add(mArrLstSurCont.get(i).getmStrQuesCont());
				if (mArrLstSurCont.get(i).getmStrType().equals("9")) {
					String strArray[] = mStrPathPhoto.split("/");
					mAlstContent
							.add("ftp://deeset\\mobileorders@81.171.198.188/mobileorders/"
									+ mStrUsername
									+ "-"
									+ mStrTimestamp
									+ "-photo.jpg");
				} else {
					if (mArrLstSurCont.get(i).getmStrType().equals("11")) {
						mAlstContent
								.add("ftp://deeset\\mobileorders@81.171.198.188/mobileorders/"
										+ mStrUsername
										+ "-"
										+ mStrTimestamp
										+ "-signature.png");
					} else {
						mAlstContent.add(mArrlstAnswer.get(i));
					}
				}
			}
		}

		private void addArrayTitle(int intContSize) {
			mAlstTitle.add(0, "chainid");
			mAlstTitle.add(1, "storeid");
			mAlstTitle.add(2, "surveyid");

			for (int i = 3, j = 1; i < intContSize * 2 + 3; i += 2, j++) {
				mAlstTitle.add(i, "Ques" + j);
				mAlstTitle.add(i + 1, "Ans" + j);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			mProgress.dismiss();
			if (result.equals("fill full answers")) {
				Toast.makeText(mCtx, "Please answer the questions!",
						Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent(mCtx, SubmittedResult.class);
				putIntentValues(intent);
				mCtx.startActivity(intent);
			}
			super.onPostExecute(result);
		}

	}
}
