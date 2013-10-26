package com.deeset.deesetsurvey.model;

import java.io.IOException;
import java.util.ArrayList;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class JDBCAdapter {

	private final static String NAMESPACE = "http://codewaretechnologies.com/";
	private final static String URL = "http://m.deeset.co.uk/questionaire/survey_api.asmx";

	public final static String METHOD_CHECKUSERLOGIN = "CheckUserLogin";
	public final static String METHOD_GETCHAINDATA = "GetChainData";
	public final static String METHOD_GETSTOREDATA = "GetStoreData";
	public final static String METHOD_GETSTATICSURVEYDATA = "GetSaticSurveyData";
	public final static String METHOD_GETCURRENTSURVEYDATA = "GetCurrentSurveyData";
	public final static String METHOD_GETQUESTIONSDATA = "GetQuestionsData";
	public final static String METHOD_GETQUESTIONOPTIONDATA = "GetQuestionOptionData";
	public final static String METHOD_INSERTSURVEYANSWER = "InsertSurveyAnswer";
	
	public final static int RESULT_OK = 0;
	public final static int RESULT_NOTCONNECT = 1;
	public final static int RESULT_NOTDATA = -1;
	public final static int RESULT_EMPTYDATA = 2;
	
	public final static String STR_NOCONNECT = "No connect to server!";
	public final static String STR_NODATAOFFLINE = "Unavailable data! Please connect internet!";
	public final static String STR_NOTLOADDATA = "Can't load data!";
	public final static String STR_EMPTYDATA = "Can't found data for ";

	public final static String TYPE_INTEGER = "int";
	public final static String TYPE_STRING = "string";
	
	public final static long TIME_UPDATE = 15 * 60 * 1000;
	public final static long TIME_OUT = 1 * 60 * 60 * 1000;

	private SoapObject request;
	private SoapSerializationEnvelope envelope;
	private HttpTransportSE androidHttpTransport;

	public void connectToWS(String strMethod) {
		request = new SoapObject(JDBCAdapter.NAMESPACE, strMethod);
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		androidHttpTransport = new HttpTransportSE(JDBCAdapter.URL);
	}

	public void insertValuePropWS(String strNameFieldDB, String strValue) {
		PropertyInfo strVarProp = new PropertyInfo();
		strVarProp.setName(strNameFieldDB);
		strVarProp.setValue(strValue);
		strVarProp.setType(String.class);
		request.addProperty(strVarProp);
	}

	public void insertValuePropWS(String strNameFieldDB, int intValue) {
		PropertyInfo strVarProp = new PropertyInfo();
		strVarProp.setName(strNameFieldDB);
		strVarProp.setValue(intValue);
		strVarProp.setType(Integer.class);
		request.addProperty(strVarProp);
	}

	public SoapObject interactServer(ArrayList<String> alstWS, String strMethod) {
		SoapObject soapOject = null;
		connectToWS(strMethod);
		for (int i = 0; i < alstWS.size(); i++) {
			addTypeParams(alstWS.get(i), alstWS.get(++i), alstWS.get(++i));
		}
		envelope.setOutputSoapObject(request);
		envelope.dotNet = true;
		String SOAP_ACTION = JDBCAdapter.NAMESPACE + strMethod;
		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);
			soapOject = (SoapObject) envelope.getResponse();
		} catch (IOException e) {
			Log.i("IOException", e.getMessage());
		} catch (XmlPullParserException e) {
			Log.i("XmlPullParserException", e.getMessage());
		}
		return soapOject;
	}

	public String interactServerString(ArrayList<String> alstWS, String strMethod) {
		String strResult = "";
		connectToWS(strMethod);
		for (int i = 0; i < alstWS.size(); i++) {
			addTypeParams(alstWS.get(i), alstWS.get(++i), alstWS.get(++i));
		}
		envelope.setOutputSoapObject(request);
		envelope.dotNet = true;
		String SOAP_ACTION = JDBCAdapter.NAMESPACE + strMethod;
		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);
			strResult = envelope.getResponse().toString();
		} catch (IOException e) {
			Log.i("IOException", e.getMessage());
		} catch (XmlPullParserException e) {
			Log.i("XmlPullParserException", e.getMessage());
		}
		Log.i("insert", strResult);
		return strResult;
	}
	
	private void addTypeParams(String strType, String strKey, String strValue) {
		if (strType.equals(JDBCAdapter.TYPE_INTEGER)) {
			insertValuePropWS(strKey, Integer.valueOf(strValue));
		}
		if (strType.equals(JDBCAdapter.TYPE_STRING)) {
			insertValuePropWS(strKey, strValue);
		}
	}

}
