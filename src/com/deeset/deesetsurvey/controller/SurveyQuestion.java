package com.deeset.deesetsurvey.controller;

import java.util.ArrayList;

public class SurveyQuestion {

	private int mIntId;
	private ArrayList<Integer> mIntIdChild;
	private String mStrType;
	private String mStrQuesCont;
	private ArrayList<String> mArrLstAns;

	public String getmStrType() {
		return mStrType;
	}

	public void setmStrType(String mStrType) {
		this.mStrType = mStrType;
	}

	public String getmStrQuesCont() {
		return mStrQuesCont;
	}

	public void setmStrQuesCont(String mStrQuesCont) {
		this.mStrQuesCont = mStrQuesCont;
	}

	public ArrayList<String> getmArrLstAns() {
		return mArrLstAns;
	}

	public void setmArrLstAns(ArrayList<String> mArrLstAns) {
		this.mArrLstAns = mArrLstAns;
	}

	public int getmIntId() {
		return mIntId;
	}

	public void setmIntId(int mIntId) {
		this.mIntId = mIntId;
	}

	public ArrayList<Integer> getmIntIdChild() {
		return mIntIdChild;
	}

	public void setmIntIdChild(ArrayList<Integer> mIntIdChild) {
		this.mIntIdChild = mIntIdChild;
	}

	@Override
	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(mIntId + "#@#");
		strBuff.append(mIntIdChild.toString() + "#@#");
		strBuff.append(mStrQuesCont + "#@#");
		strBuff.append(mStrType + "#@#");
		strBuff.append(mArrLstAns + "@#@");
		return strBuff.toString();
	}

}
