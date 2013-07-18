package com.deeset.deesetsurvey.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawSomethingView extends View {
	private static final float STROKE_WIDTH = 5f;

	/** Need to track this so the dirty region can accommodate the stroke. **/
	private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
	private Context ctx;
	private Paint paint = new Paint();
	private Path path = new Path();
	private float lastTouchX;
	private float lastTouchY;
	private final RectF dirtyRect = new RectF();

	public DrawSomethingView(Context context) {
		super(context);
		this.ctx = context;
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(5f);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPath(path, paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			path.moveTo(eventX, eventY);
			lastTouchX = eventX;
			lastTouchY = eventY;
			// There is no end point yet, so don't waste cycles invalidating.
			return true;

		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			// Start tracking the dirty region.
			resetDirtyRect(eventX, eventY);

			// When the hardware tracks events faster than they are delivered,
			// the
			// event will contain a history of those skipped points.
			int historySize = event.getHistorySize();
			for (int i = 0; i < historySize; i++) {
				float historicalX = event.getHistoricalX(i);
				float historicalY = event.getHistoricalY(i);
				expandDirtyRect(historicalX, historicalY);
				path.lineTo(historicalX, historicalY);
			}

			// After replaying history, connect the line to the touch point.
			path.lineTo(eventX, eventY);
			break;

		default:
			return false;
		}

		// Include half the stroke width to avoid clipping.
		invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
				(int) (dirtyRect.top - HALF_STROKE_WIDTH),
				(int) (dirtyRect.right + HALF_STROKE_WIDTH),
				(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

		lastTouchX = eventX;
		lastTouchY = eventY;

		return true;
	}

	/**
	 * Called when replaying history to ensure the dirty region includes all
	 * points.
	 */
	private void expandDirtyRect(float historicalX, float historicalY) {
		if (historicalX < dirtyRect.left) {
			dirtyRect.left = historicalX;
		} else if (historicalX > dirtyRect.right) {
			dirtyRect.right = historicalX;
		}
		if (historicalY < dirtyRect.top) {
			dirtyRect.top = historicalY;
		} else if (historicalY > dirtyRect.bottom) {
			dirtyRect.bottom = historicalY;
		}
	}

	/**
	 * Resets the dirty region when the motion event occurs.
	 */
	private void resetDirtyRect(float eventX, float eventY) {

		// The lastTouchX and lastTouchY were set when the ACTION_DOWN
		// motion event occurred.
		dirtyRect.left = Math.min(lastTouchX, eventX);
		dirtyRect.right = Math.max(lastTouchX, eventX);
		dirtyRect.top = Math.min(lastTouchY, eventY);
		dirtyRect.bottom = Math.max(lastTouchY, eventY);
	}

	public void onMove(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {

	}

	public String getScreen(View layout, String strSignName) {
		String strPath = "";
		File myPath;

		View ve = layout;
		ve.setDrawingCacheEnabled(true);
		Bitmap b = ve.getDrawingCache();

		String extr = Environment.getExternalStorageDirectory().toString()
				+ "/DCMI";
		myPath = new File(extr);
		if (!myPath.exists()) {
			myPath.mkdir();
		}

		myPath = new File(extr, strSignName + ".png");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(myPath);
			b.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
			MediaStore.Images.Media.insertImage(ctx.getContentResolver(), b,
					"Screen", "screen");
			strPath = myPath.getPath();
			Log.i("path", strPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strPath;
	}

}
