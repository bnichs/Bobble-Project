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

import java.io.File;

import com.bn.bobblehead.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Display;
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

    private SimulationView mSimulationView;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private WakeLock mWakeLock;

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
        //byte[] b = i.getExtras().getByteArray("face");
        //Bitmap face = BitmapFactory.decodeByteArray(b, 0, b.length);
        
        //System.out.println(face.getHeight());
       // instantiate our simulation view and set it as the activity's content
        //b = getIntent().getExtras().getByteArray("backg");
        
        
        Bitmap bg = BitmapFactory.decodeFile(HomeScreen.backFil.toString());
        
        
        RectF rec = (RectF) i.getParcelableExtra("rec");
        
        
        mSimulationView = new SimulationView(this,bg,rec);
        setContentView(mSimulationView);
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
        mSimulationView.startSimulation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */

        // Stop the simulation
        mSimulationView.stopSimulation();

        // and release our wake-lock
        mWakeLock.release();
    }
    
    

    class SimulationView extends View implements SensorEventListener {
        // diameter of the balls in meters
        private static final float sBallDiameter = 0.035f;
        private static final float sBallDiameter2 = sBallDiameter * sBallDiameter;

        // friction of the virtual table and air
        private static final float sFriction = 1f;

        private Sensor mAccelerometer;
        private long mLastT;
        private float mLastDeltaT;

        private float mXDpi;
        private float mYDpi;
        private float mMetersToPixelsX;
        private float mMetersToPixelsY;
        private Bitmap mBitmap;
        private Bitmap backg;
        private float mXOrigin;
        private float mYOrigin;
        private float mSensorX;
        private float mSensorY;
        private long mSensorTimeStamp;
        private long mCpuTimeStamp;
        private float mHorizontalBound;
        private float mVerticalBound;
        private float angle=0;
        
        private Face face;

        
        class Face{
        	
        	private Bitmap face;
        	private float rot;//rotation
        	private RectF rec;//current rectangle occupied
        	private RectF boxRec;
        	private RectF moveBox;
        	
        	private float mPosX;
            private float mPosY;
            private float mAccelX;
            private float mAccelY;
            private float mLastPosX;
            private float mLastPosY;
            private float mOneMinusFriction;
            public float posX,posY;
            private double springK = 0.000003;
            private double dampingK = 0.000035;
        	private double t;
            private float width, height;
            
        	public Face(RectF box){
        		
        		rec=box;
        		final float ran = ((float) Math.random() - 0.5f) * 0.2f;
                mOneMinusFriction = 1.0f - sFriction + ran;
        		        	
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
                
                boxRec=new RectF(left,top,right,bottom);
                
                int moveL = (int) (left - (left/6f));
                int moveT = (int) (top - (top/6f));
                int moveR = (int) (right + (right/6f));
                int moveB = (int) (bottom + (bottom/6f));
                
                moveBox = new RectF(moveL, moveT, moveR, moveB);

               
                face=BitmapFactory.decodeFile(HomeScreen.faceFil.toString());
                
                
                System.out.println(face.getWidth()+":"+face.getHeight());
        	}
        	
        	
        	public void update(float sx, float sy, long timestamp){
        		computePhysics(sx, sy);
        	}
        	
        	
        	 public void computePhysics(float sx, float sy) { // move around boxRec
        		t= t + 0.5;
             	boxRec.left += (float) Math.sin(t);
             	boxRec.right+= (float) Math.sin(t);
             	boxRec.top += (float) Math.sin(2*t);
             	boxRec.bottom+= (float) Math.sin(2*t);
 	     }
        	
        	public void rotate(float theta){
        		int x=face.getWidth();
        		int y=face.getHeight();
        		Matrix matrix = new Matrix();
        		matrix.setRotate(theta,x/2,y/2);
        		face= Bitmap.createBitmap(face, 0, 0, x, y, matrix, true);
        		
        	}

        }
       

        public void startSimulation() {

            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        }

        public void stopSimulation() {
            mSensorManager.unregisterListener(this);
        }

        public SimulationView(Context context,Bitmap bg,RectF box) {
            super(context);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;

           
            //mBitmap = bm;
            
            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inScaled = false;
            
            backg=Bitmap.createScaledBitmap(bg, metrics.widthPixels, metrics.heightPixels, false);
			
            face=new Face(box);
            
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
            mXOrigin = (w - backg.getWidth()) * 0.5f;
            mYOrigin =(h - backg.getHeight()) * 0.5f;
            mHorizontalBound = ((w / mMetersToPixelsX ) * 0.5f);
            mVerticalBound = ((h / mMetersToPixelsY ) * 0.5f);
            
        }

       
        public void onSensorChanged(SensorEvent event) {
        	
        	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
               
            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

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
	           
	           System.out.println(mSensorX+":"+mSensorY);
            }
            else if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            	//face.rotate((float) java.lang.Math.atan(event.values[1]/event.values[0]));
            }	
            else{
            	return;
            }
            mSensorTimeStamp = event.timestamp;
            mCpuTimeStamp = System.nanoTime();
        }
        
        private Paint p=new Paint();


        @Override
        protected void onDraw(Canvas canvas) {

        	p.setStyle(Paint.Style.STROKE) ;
		 	p.setStrokeWidth(4f);
		 	p.setARGB(255, 0, 200, 0);

            canvas.drawBitmap(backg, 0, 0, null);
            
            /*
             * compute the new position of our object, based on accelerometer
             * data and present time.
             */


            final long now = mSensorTimeStamp + (System.nanoTime() - mCpuTimeStamp);
            
            
            final float sx = mSensorX;
            final float sy = mSensorY;

            
           // particleSystem.update(sx, sy, now);

       
            final float xc = mXOrigin;
            final float yc = mYOrigin;
            final float xs = mMetersToPixelsX;
            final float ys = mMetersToPixelsY;
            final Bitmap bitmap = mBitmap;
            
            //face.face.setDensity(300);
            
            face.update(sx, sy, now); // update the position
            canvas.drawBitmap(face.face,null,face.boxRec, null);
            
            //System.out.println(face.face.getWidth()+":"+face.face.getHeight());
            
//            canvas.drawRect(face.boxRec, p);
//            canvas.drawRect(face.rec, p);
//            canvas.drawRect(face.moveBox, p);
            
            // and make sure to redraw asap
            invalidate();
        }
        
       private Rect rectFtoRect(RectF r){
        	return new Rect((int)r.left,(int)r.top,(int)r.right,(int)r.bottom);
        }
       
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}