package com.deeset.deesetsurvey.controller;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.Html;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class DynamicViews extends View {

	private Context mCtx;

	public DynamicViews(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCtx = context;
	}

	public View initialBorder() {
		View vi = new View(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = convertDIPtoPixel(1);
		vi.setLayoutParams(layParams);
		vi.setBackgroundColor(Color.BLACK);
		return vi;
	}

	public TextView initialTextView(String strText, boolean blnUnderline) {
		TextView txt = new TextView(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		txt.setLayoutParams(layParams);
		if (blnUnderline) {
			txt.setText(Html.fromHtml("<u>" + strText + "</u>"));
			txt.setTextColor(Color.BLUE);
		} else {
			txt.setText(strText);
		}
		txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		int intPad = convertDIPtoPixel(8);
		txt.setPadding(intPad, intPad, intPad, intPad);
		return txt;
	}

	public TextView initialTextViewByHei(String strText, int intHei) {
		TextView txt = new TextView(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = convertDIPtoPixel(intHei);
		txt.setLayoutParams(layParams);
		txt.setText(strText);
		txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		int intPad = convertDIPtoPixel(8);
		txt.setPadding(intPad, intPad, intPad, intPad);
		return txt;
	}

	public Spinner initialSpinner(ArrayList<String> mArrLstAns) {
		Spinner spin = new Spinner(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		spin.setLayoutParams(layParams);
		ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(mCtx,
				android.R.layout.simple_spinner_item, mArrLstAns);
		arrAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(arrAdapter);
		return spin;
	}

	public CheckBox initialCheckBox(String strAnsCont) {
		CheckBox ckb = new CheckBox(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		ckb.setLayoutParams(layParams);
		ckb.setText(strAnsCont);
		return ckb;
	}

	public EditText initialEditText(int intTypeEdt) {
		EditText edt = new EditText(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		edt.setLayoutParams(layParams);
		edt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		if (intTypeEdt == 4) {
			edt.setMaxLines(1);
		}
		if (intTypeEdt == 5) {
			edt.setMinLines(3);
		}
		if (intTypeEdt == 6) {
			edt.setHint("dd/mm/yyyy");
			edt.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
			edt.setMaxLines(1);
		}
		if (intTypeEdt == 7) {
			edt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			edt.setMaxLines(1);
		}
		return edt;
	}

	public ImageView initialImageView(int intWidth, int intHeight) {
		ImageView img = new ImageView(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = intWidth;
		layParams.height = intHeight;
		img.setLayoutParams(layParams);
		return img;
	}

	public RadioGroup initialRadioGroup(ArrayList<String> mArrLstAns,
			ArrayList<Integer> mArrLstIdChild) {
		RadioGroup radGroup = new RadioGroup(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		radGroup.setLayoutParams(layParams);
		for (int i = 0; i < mArrLstAns.size(); i++) {
			radGroup.addView(initialRadioButton(mArrLstAns.get(i),
					mArrLstIdChild.get(i)));
		}
		return radGroup;
	}

	public RadioButton initialRadioButton(String strAnsCont, int intId) {
		RadioButton rad = new RadioButton(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = LayoutParams.MATCH_PARENT;
		layParams.height = LayoutParams.WRAP_CONTENT;
		rad.setLayoutParams(layParams);
		rad.setText(strAnsCont);
		rad.setId(intId);
		return rad;
	}

	public Button initialButton(int intWidth, int intHeight, String strText) {
		Button btn = new Button(mCtx);
		LayoutParams layParams = new LayoutParams();
		layParams.width = intWidth;
		layParams.height = intHeight;
		btn.setLayoutParams(layParams);
		btn.setText(strText);
		return btn;
	}

	public int convertDIPtoPixel(float intDIP) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				intDIP, mCtx.getResources().getDisplayMetrics());
	}

}
