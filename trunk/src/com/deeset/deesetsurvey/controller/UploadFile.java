package com.deeset.deesetsurvey.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UploadFile extends AsyncTask<String, Integer, Boolean> {

	private Context mCtx;
	private String mStrMerId, mStrUsername;

	public UploadFile(Context ctx, String strUsername, String strMerId) {
		this.mCtx = ctx;
		this.mStrMerId = strMerId;
		this.mStrUsername = strUsername;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		Log.i("Upload", "Start");
		FTPClient mFTP = new FTPClient();
        try {
            mFTP.connect("81.171.198.188");
            mFTP.login("deeset\\mobileorders", "Kk9.cl417");
            mFTP.setFileType(FTP.BINARY_FILE_TYPE);
            mFTP.enterLocalPassiveMode();
            
            File file = new File(params[0]);
            FileInputStream ifile = new FileInputStream(file);
            
            String strArray[] = params[0].split("/");
            String strFileName = mStrUsername + "-" + mStrMerId + "-" + strArray[strArray.length-1];
            Log.i("File path", params[0]);
            Log.i("File name", strFileName);
            
            mFTP.storeFile(strFileName, ifile);
            
            File fileTest = new File("/mnt/sdcard/DCMI/test.txt");
            FileOutputStream desFileStream = new FileOutputStream(fileTest);
            boolean status = mFTP.retrieveFile(strFileName, desFileStream);
            desFileStream.close();
            
            if (status) {
            	if (fileTest.length() != file.length()) {
            		Log.i("Upload", "Error");
            		mFTP.disconnect();
            		return false;
            	} else {
            		fileTest.delete();
            		Log.i("Upload", "Done");
            	}
            }
            
            mFTP.disconnect();
            return true;
        } catch (SocketException e) {
            Log.i("SocketException", e.getMessage());
        } catch (IOException e) {
        	Log.i("IOException", e.getMessage());
        }
		return false;
	}

}
