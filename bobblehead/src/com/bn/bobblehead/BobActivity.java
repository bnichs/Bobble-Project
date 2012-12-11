/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bn.bobblehead;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 * 
 * @see SensorManager
 * @see SensorEvent
 * @see Sensor
 */

public class BobActivity extends Activity {

    private BobbleView mBobbleView;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private WakeLock mWakeLock;
    private String backPath;
    
    static {
        System.loadLibrary("imageprocessing");
      }

	public native void fisheye(Bitmap bmp);
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());

        Intent i = getIntent();     
        
        //get background
        Bitmap bg = BitmapFactory.decodeFile(i.getStringExtra("backPath"));
        
        //get face rectangle
        RectF rec = (RectF) i.getParcelableExtra("rec");
        
        
        
        mBobbleView = new BobbleView(this,bg,rec);
        setContentView(mBobbleView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
        mWakeLock.acquire();

        // Start the simulation
        mBobbleView.startBobble();
        
        setContentView(mBobbleView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop the simulation
        mBobbleView.stopBobble();

        // and release our wake-lock
        mWakeLock.release();
    }
    
    

    class BobbleView extends View implements SensorEventListener {
        private Sensor mAccelerometer;
        

        private float mXDpi;
        private float mYDpi;
        private float mMetersToPixelsX;
        private float mMetersToPixelsY;
        
        private Bitmap backg;

      
        private float mSensorX;
        private float mSensorY;
        private long mSensorTimeStamp;
        private long mCpuTimeStamp;
       
        private Face face;
    	
        class Face{
        	
        	private final Bitmap faceOrig;
        	private final RectF faceRectOrig;
        	private float rot;//rotation
        	private RectF rec;//current rectangle occupied
        	private RectF faceRectCurr;//current face rectangle. Accounts for rotaion

        	private Bitmap faceCurr;
        	
            public float posX,posY;

        	private double t;
            private float width, height;
            
            private final float maxRot=20;
            
            
            
        	public Face(RectF box){
        		
        		rec=box;
        		//final float ran = ((float) Math.random() - 0.5f) * 0.2f;
                //mOneMinusFriction = 1.0f - sFriction + ran;
        		        	
                float left=rec.left;
                float top=rec.top;
                float right=rec.right;
                float bottom=rec.bottom;
                 t= 0; 
                
                
                width=right-left;
                height=bottom-top;
                
                left=left-width/4f;
                right=right+width/4f;
                
                top=top-height/5f;
                bottom=bottom+height/5f;
                
                faceRectCurr=new RectF(left,top,right,bottom);
                faceRectOrig=new RectF(left,top,right,bottom);
                
               
                faceOrig=BitmapFactory.decodeFile(HomeScreen.faceFil.toString());
                fisheye(faceOrig); // native interface lines
                
        	}

        	
        	public void update(float sx, float sy, long timestamp){
        		
        		if (rot>maxRot){
        			rot=maxRot;
        		}if (rot<-maxRot){
        			rot=-maxRot;
        		}
        		
        		int x=faceOrig.getWidth();
        		int y=faceOrig.getHeight();
        		Matrix matrix = new Matrix();
        		matrix.setRotate(rot,x/2,y);
        		faceCurr= Bitmap.createBitmap(faceOrig, 0, 0, x, y, matrix, true);

        		float xOffset=((faceCurr.getWidth()-x) * .5f);
        		float yOffset=((faceCurr.getHeight()-y) * .5f);
        		
        		if (xOffset < x/2 || yOffset< y/2 ){
        		
	        		faceRectCurr.left=faceRectOrig.left-xOffset;
	        		faceRectCurr.right=faceRectOrig.right+xOffset;
	        		
	        		faceRectCurr.top=faceRectOrig.top-yOffset;
	        		faceRectCurr.bottom=faceRectOrig.bottom+yOffset;
        		}
        		computePhysics(sx, sy);
        		
        	}
        	
        	
        	 public void computePhysics(float sx, float sy) { // move around boxRec
        		t = t + 0.5;
    			faceRectCurr.left += (float) 8f*Math.sin(t);
             	faceRectCurr.right+= (float) 8f*Math.sin(t);
             	faceRectCurr.top += (float) 16f*Math.sin(t/5f);
    			faceRectCurr.bottom+= (float) 16f*Math.sin(t/5f);
        	 }
        
        }
       

        public void startBobble() {

            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        }

        public void stopBobble() {
            mSensorManager.unregisterListener(this);
        }

        public BobbleView(Context context,Bitmap bg,RectF box) {
            super(context);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;

           
           
            
            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
           
            
            backg=Bitmap.createScaledBitmap(bg, metrics.widthPixels, metrics.heightPixels, false);
			
            face=new Face(box);
            
        }
/*
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
           // mXOrigin = (w - backg.getWidth()) * 0.5f;
           // mYOrigin =(h - backg.getHeight()) * 0.5f;
           // mHorizontalBound = ((w / mMetersToPixelsX ) * 0.5f);
            //mVerticalBound = ((h / mMetersToPixelsY ) * 0.5f);
            
        }
*/
       
        private static final float maxRot=.1f;
        
        public void onSensorChanged(SensorEvent event) {
        	
        	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
               switch (mDisplay.getRotation()) {
	                case Surface.ROTATION_0:
	                    mSensorX = event.values[0];
	                    mSensorY = event.values[1];
	                    break;
	                case Surface.ROTATION_90:
	                    mSensorX = -event.values[1];
	                    mSensorY = event.values[0];
	                    break;
	                case Surface.ROTATION_180:
	                    mSensorX = -event.values[0];
	                    mSensorY = -event.values[1];
	                    break;
	                case Surface.ROTATION_270:
	                    mSensorX = event.values[1];
	                    mSensorY = -event.values[0];
	                    break;
	            }
	        
            }
            else if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            	face.rot=-((float) java.lang.Math.atan(event.values[1]/event.values[0]))*180f/(3.14f);
            	
            }	
            else{
            	return;
            }
            mSensorTimeStamp = event.timestamp;
            mCpuTimeStamp = System.nanoTime();
        }
        
        public boolean onTouchEvent(MotionEvent e) {
			// TODO Auto-generated method stub

			float x = (float) e.getX();
			float y = (float) e.getY();
			final long now = mSensorTimeStamp + (System.nanoTime() - mCpuTimeStamp);
			
			if (face.faceRectCurr.contains(x, y)) {
				face.update(x, y, now);
			}
			return true;
        }
       
        
        private Paint p=new Paint();

        @Override
        protected void onDraw(Canvas canvas) {

        	p.setStyle(Paint.Style.STROKE) ;
		 	p.setStrokeWidth(4f);
		 	p.setARGB(255, 0, 200, 0);

            canvas.drawBitmap(backg, 0, 0, null);
            
            final long now = mSensorTimeStamp + (System.nanoTime() - mCpuTimeStamp);
            
            
            final float sx = mSensorX;
            final float sy = mSensorY;

            
            face.update(sx, sy, now); // update the position
            canvas.drawBitmap(face.faceCurr,null,face.faceRectCurr, null);
            
            // and make sure to redraw asap
            invalidate();
        }
    
       
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}