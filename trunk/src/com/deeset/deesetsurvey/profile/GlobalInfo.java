package com.deeset.deesetsurvey.profile;

import android.content.Context;
import android.widget.Toast;

public class GlobalInfo {

	private static String userId;
	private static String storeId;
	private static String storeName;
	private static String surveyId;
	private static String surveyName;

	public static String getSurveyId() {
		return surveyId;
	}

	public static void setSurveyId(String surveyId) {
		GlobalInfo.surveyId = surveyId;
	}

	public static String getSurveyName() {
		return surveyName;
	}

	public static void setSurveyName(String surveyName) {
		GlobalInfo.surveyName = surveyName;
	}

	public static String getStoreId() {
		return storeId;
	}

	public static void setStoreId(String storeId) {
		GlobalInfo.storeId = storeId;
	}

	public static String getStoreName() {
		return storeName;
	}

	public static void setStoreName(String storeName) {
		GlobalInfo.storeName = storeName;
	}

	public static String getUserId() {
		return userId;
	}

	public static void setUserId(String userId) {
		GlobalInfo.userId = userId;
	}

	public static void showToast(Context ctx, String message) {
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
	}

}
