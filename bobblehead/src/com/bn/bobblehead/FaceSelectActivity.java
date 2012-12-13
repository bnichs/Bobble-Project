package com.bn.bobblehead;

import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class FaceSelectActivity extends Activity {

	private SelectView mSelectView;
	private PowerManager mPowerManager;
	private WindowManager mWindowManager;
	private WakeLock mWakeLock;
	private String backPath;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get an instance of the PowerManager
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mWindowManager.getDefaultDisplay();

		// Create a bright wake lock
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());

		backPath = getIntent().getExtras().getString("backPath");

		// instantiate our simulation view and set it as the activity's content
		mSelectView = new SelectView(this);
		setContentView(mSelectView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
		setContentView(mSelectView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();
	}

	class SelectView extends View implements OnClickListener {

		public boolean flag;

		private final Bitmap button;
		private final BitmapFactory.Options options = new BitmapFactory.Options();;
		public Bitmap backg;
		private final Paint p = new Paint();
		private Rect buttonR;
		private RectF selection;
		private boolean rectDrawn = false;
		private int ooX;// ovals origin x
		private int ooY;// ovals origin y
		int top = 0;
		int left = 0;
		int bottom = 0;
		int right = 0;

		public SelectView(Context context) {
			super(context);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			final int width = metrics.widthPixels;
			final int height = metrics.heightPixels;

			// get go button and dimensions
			Bitmap tmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.go_button);
			button = Bitmap.createScaledBitmap(tmp, 200, 200, false);

			backg = Utils.decodeSampledBitmap(backPath, width, height);
			backg = Bitmap.createScaledBitmap(backg, metrics.widthPixels,
					metrics.heightPixels, false);

			buttonR = new Rect(width - 200, height - 200, width, height);

			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(4f);
			p.setARGB(255, 0, 200, 0);

			showDialog(DIALOG_ALERT);
		}

		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(backg, 0, 0, null);
			if (selection != null) {
				canvas.drawOval(selection, p);
			}
			canvas.drawBitmap(button, null, buttonR, null);
			invalidate();
		}

		public boolean onTouchEvent(MotionEvent e) {
			int x = (int) e.getX();// input coords
			int y = (int) e.getY();

			// Check for button press
			if (buttonR.contains(x, y)) {

				if (selection != null && selection.width() > 20
						&& selection.height() > 20) {
					Intent i = new Intent(FaceSelectActivity.this,
							BobActivity.class);
					Bitmap face;
					face = Utils.getCroppedBitmap(backg, selection);

					// Save the file for later use
					try {
						if (!HomeScreen.faceFil.exists()) {
							HomeScreen.faceFil.createNewFile();
						}
						FileOutputStream out = new FileOutputStream(
								HomeScreen.faceFil);

						face.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.flush();
						out.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					face.recycle();

					// save the face location
					RectF rec = new RectF(selection.left, selection.top,
							selection.left + selection.width(), selection.top
									+ selection.height());
					i.putExtra("backPath", backPath);
					i.putExtra("rec", rec);
					startActivity(i);
					return true;
				} else {
					if (!dialogOn) {
						showDialog(DIALOG_ALERT);
						dialogOn = true;
					}
				}

			}

			// Check for oval drawings
			switch (e.getAction()) {
			// get an origin for the oval
			case MotionEvent.ACTION_DOWN:
				if (!rectDrawn) {
					top = (int) y;
					left = (int) x;
					ooX = (int) x;
					ooY = (int) y;
				}

				// check x,y against the origin then save the rectangle
			case MotionEvent.ACTION_MOVE:
				if (x < ooX) {
					left = x;
					right = ooX;
				} else {
					left = ooX;
					right = x;
				}
				if (y < ooY) {
					top = y;
					bottom = ooY;
				} else {
					top = ooY;
					bottom = y;
				}

			}

			// if no oval is drawn
			if (top == 0 && left == 0 && right == 0 && bottom == 0) {
				selection = null;
			}
			selection = new RectF(left, top, right, bottom);
			return true;
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		public void onClick(View v) {
			// TODO Auto-generated method stub

		}

	}

	private static final int DIALOG_ALERT = 10;
	private static boolean dialogOn = false;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ALERT:
			// Create out AlterDialog
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please Select a face by drawing an oval around it.");
			builder.setCancelable(true);
			builder.setPositiveButton("Ok", new OkOnClickListener());
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return super.onCreateDialog(id);
	}

	private final class OkOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			dialogOn = false;
			dialog.dismiss();

		}
	}

}
