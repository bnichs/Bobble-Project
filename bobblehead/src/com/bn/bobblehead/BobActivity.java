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
import android.graphics.BitmapFactory.Options;
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
        byte[] b = i.getExtras().getByteArray("face");
        Bitmap face = BitmapFactory.decodeByteArray(b, 0, b.length);
        
        //System.out.println(face.getHeight());
       // instantiate our simulation view and set it as the activity's content
        //b = getIntent().getExtras().getByteArray("backg");
        
        
        Bitmap bg = BitmapFactory.decodeFile(HomeScreen.fil.toString());
        
        
        RectF rec = (RectF) i.getParcelableExtra("rec");
        
        
        mSimulationView = new SimulationView(this,bg,face,rec);
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
        private final ParticleSystem mParticleSystem = new ParticleSystem();
        private float angle=0;
        
        private Face face;

        
        class Face{
        	
        	private Bitmap face;
        	private float rot;//rotation
        	private RectF rec;//current rectangle occupied
        	private RectF boxRec;
        	
        	private float mPosX;
            private float mPosY;
            private float mAccelX;
            private float mAccelY;
            private float mLastPosX;
            private float mLastPosY;
            private float mOneMinusFriction;
        	
        	public Face(Bitmap f, RectF r){
        		
        		rec=r;
        		final float ran = ((float) Math.random() - 0.5f) * 0.2f;
                mOneMinusFriction = 1.0f - sFriction + ran;
        		        	
                float left=rec.left;
                float top=rec.top;
                float right=rec.right;
                float bottom=rec.bottom;
                
                float width=right-left;
                float height=bottom-top;
                
                left=left-width/4f;
                right=right+width/4f;
                
                top=top-height/5f;
                bottom=bottom+height/5f;
                
                boxRec=new RectF(left,top,right,bottom);
                
                face=Bitmap.createScaledBitmap(f, (int)width, (int)height, false);
                
                System.out.println(face.getWidth()+":"+face.getHeight());
        	}
        	
        	public void update(float sx, float sy, long timestamp){
        		final long t = timestamp;
                if (mLastT != 0) {
                    final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
                    if (mLastDeltaT != 0) {
                        final float dTC = dT / mLastDeltaT;
                        this.computePhysics(sx, sy, dT, dTC);
                        
                    }
                    mLastDeltaT = dT;
                }
                mLastT = t;
                
                this.resolveCollisionsWithBounds();
        	}
        	
        	public void resolveCollisionsWithBounds(){
        		float bLeft=boxRec.left;
        		float bRight=boxRec.right;
        		float bTop=boxRec.top;
        		float bBottom=boxRec.bottom;
        		
        		float left=rec.left;
        		float right=rec.right;
        		float top=rec.top;
        		float bottom=rec.bottom;
        		
        		if (left<bLeft){left=bLeft;}
        		else if (right>bRight){right=bRight;}
        		else if (top<bTop){top=bTop;}
        		else if (bottom>bBottom){bottom=bBottom;}
        		
        		rec=new RectF(left,top,right,bottom);
        		
        	}
        	
        	 public void computePhysics(float sx, float sy, float dT, float dTC) {
                 // Force of gravity applied to our virtual object
                 final float m = 1000.0f; // mass of our virtual object
                 final float gx = -sx * m;
                 final float gy = -sy * m;

                 /*
                  * �F = mA <=> A = �F / m We could simplify the code by
                  * completely eliminating "m" (the mass) from all the equations,
                  * but it would hide the concepts from this sample code.
                  */
                 final float invm = 1.0f / m;
                 final float ax = gx * invm;
                 final float ay = gy * invm;

                 /*
                  * Time-corrected Verlet integration The position Verlet
                  * integrator is defined as x(t+�t) = x(t) + x(t) - x(t-�t) +
                  * a(t)�t�2 However, the above equation doesn't handle variable
                  * �t very well, a time-corrected version is needed: x(t+�t) =
                  * x(t) + (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2 We also add
                  * a simple friction term (f) to the equation: x(t+�t) = x(t) +
                  * (1-f) * (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2
                  */
                 final float dTdT = dT * dT;
                 final float x = mPosX + mOneMinusFriction * dTC * (mPosX - mLastPosX) + mAccelX
                         * dTdT;
                 final float y = mPosY + mOneMinusFriction * dTC * (mPosY - mLastPosY) + mAccelY
                         * dTdT;
                 mLastPosX = mPosX;
                 mLastPosY = mPosY;
                 mPosX = x;
                 mPosY = y;
                 mAccelX = ax;
                 mAccelY = ay;
             }
            /*
             *  public void computePhysics(float sx, float sy, float dT, float dTC) {
             	float vx = (float) (sx * (-springK - mAccelX * dampingK));
             	float vy = (float) (sy * (-springK - mAccelY * dampingK));
             	mAccelX += vx * dT;
             	mAccelY += vy * dT;
             	
             	mLastPosX = mPosX;
             	mLastPosY = mPosY;
             	mPosX = mAccelX * dT;
             	mPosY = mAccelY * dT;
 	     }*/
        	
        	
        	
        	
        	public void rotate(float theta){
        		int x=face.getWidth();
        		int y=face.getHeight();
        		Matrix matrix = new Matrix();
        		matrix.setRotate(theta,x/2,y/2);
        		face= Bitmap.createBitmap(face, 0, 0, x, y, matrix, true);
        		
        	}

        	
        	
        }
        /*
         * Each of our particle holds its previous and current position, its
         * acceleration. for added realism each particle has its own friction
         * coefficient.
         */
        class Particle {
            private float mPosX;
            private float mPosY;
            private float mAccelX;
            private float mAccelY;
            private float mLastPosX;
            private float mLastPosY;
            private float mOneMinusFriction;
 private double springK = 0.000003;
            private double dampingK = 0.000035;
            
            private Bitmap face;
            

            Particle() {
                // make each particle a bit different by randomizing its
                // coefficient of friction
                final float r = ((float) Math.random() - 0.5f) * 0.2f;
                mOneMinusFriction = 1.0f - sFriction + r;
                //face=bm;
                
                
            }

            
            
            public void computePhysics(float sx, float sy, float dT, float dTC) {
                // Force of gravity applied to our virtual object
                final float m = 1000.0f; // mass of our virtual object
                final float gx = -sx * m;
                final float gy = -sy * m;

                /*
                 * �F = mA <=> A = �F / m We could simplify the code by
                 * completely eliminating "m" (the mass) from all the equations,
                 * but it would hide the concepts from this sample code.
                 */
                final float invm = 1.0f / m;
                final float ax = gx * invm;
                final float ay = gy * invm;

                /*
                 * Time-corrected Verlet integration The position Verlet
                 * integrator is defined as x(t+�t) = x(t) + x(t) - x(t-�t) +
                 * a(t)�t�2 However, the above equation doesn't handle variable
                 * �t very well, a time-corrected version is needed: x(t+�t) =
                 * x(t) + (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2 We also add
                 * a simple friction term (f) to the equation: x(t+�t) = x(t) +
                 * (1-f) * (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2
                 */
                final float dTdT = dT * dT;
                final float x = mPosX + mOneMinusFriction * dTC * (mPosX - mLastPosX) + mAccelX
                        * dTdT;
                final float y = mPosY + mOneMinusFriction * dTC * (mPosY - mLastPosY) + mAccelY
                        * dTdT;
                mLastPosX = mPosX;
                mLastPosY = mPosY;
                mPosX = x;
                mPosY = y;
                mAccelX = ax;
                mAccelY = ay;
            }
           /*
            *  public void computePhysics(float sx, float sy, float dT, float dTC) {
            	float vx = (float) (sx * (-springK - mAccelX * dampingK));
            	float vy = (float) (sy * (-springK - mAccelY * dampingK));
            	mAccelX += vx * dT;
            	mAccelY += vy * dT;
            	
            	mLastPosX = mPosX;
            	mLastPosY = mPosY;
            	mPosX = mAccelX * dT;
            	mPosY = mAccelY * dT;
	     }*/

            /*
             * Resolving constraints and collisions with the Verlet integrator
             * can be very simple, we simply need to move a colliding or
             * constrained particle in such way that the constraint is
             * satisfied.
             */
            public void resolveCollisionWithBounds() {
                final float xmax = mHorizontalBound;
                //System.out.println(xmax);
                //System.out.println(mHorizontalBound*mMetersToPixelsX+":"+mVerticalBound*mMetersToPixelsY);
                
                final float ymax = mVerticalBound;
                final float x = mPosX;
                final float y = mPosY;
                if (x > xmax) {
                    mPosX = xmax;
                } else if (x < -xmax) {
                    mPosX = -xmax;
                }
                if (y > ymax) {
                    mPosY = ymax;
                } else if (y < -ymax) {
                    mPosY = -ymax;
                }
            }
        }

        /*
         * A particle system is just a collection of particles
         */
        class ParticleSystem {
            static final int NUM_PARTICLES = 1;
            private Particle mBalls[] = new Particle[NUM_PARTICLES];

            ParticleSystem() {
                /*
                 * Initially our particles have no speed or acceleration
                 */
                for (int i = 0; i < mBalls.length; i++) {
                    mBalls[i] = new Particle();
                }
            }

            /*
             * Update the position of each particle in the system using the
             * Verlet integrator.
             */
            private void updatePositions(float sx, float sy, long timestamp) {
                final long t = timestamp;
                if (mLastT != 0) {
                    final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
                    if (mLastDeltaT != 0) {
                        final float dTC = dT / mLastDeltaT;
                        final int count = mBalls.length;
                        for (int i = 0; i < count; i++) {
                            Particle ball = mBalls[i];
                            ball.computePhysics(sx, sy, dT, dTC);
                        }
                    }
                    mLastDeltaT = dT;
                }
                mLastT = t;
            }

            /*
             * Performs one iteration of the simulation. First updating the
             * position of all the particles and resolving the constraints and
             * collisions.
             */
            public void update(float sx, float sy, long now) {
                // update the system's positions
                updatePositions(sx, sy, now);

                // We do no more than a limited number of iterations
                final int NUM_MAX_ITERATIONS = 10;

                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
                boolean more = true;
                final int count = mBalls.length;
                for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
                    more = false;
                    for (int i = 0; i < count; i++) {
                        Particle curr = mBalls[i];
                        for (int j = i + 1; j < count; j++) {
                            Particle ball = mBalls[j];
                            float dx = ball.mPosX - curr.mPosX;
                            float dy = ball.mPosY - curr.mPosY;
                            float dd = dx * dx + dy * dy;
                            // Check for collisions
                            if (dd <= sBallDiameter2) {
                                /*
                                 * add a little bit of entropy, after nothing is
                                 * perfect in the universe.
                                 */
                                dx += ((float) Math.random() - 0.5f) * 0.0001f;
                                dy += ((float) Math.random() - 0.5f) * 0.0001f;
                                dd = dx * dx + dy * dy;
                                // simulate the spring
                                final float d = (float) Math.sqrt(dd);
                                final float c = (0.5f * (sBallDiameter - d)) / d;
                                curr.mPosX -= dx * c;
                                curr.mPosY -= dy * c;
                                ball.mPosX += dx * c;
                                ball.mPosY += dy * c;
                                more = true;
                            }
                        }
                        /*
                         * Finally make sure the particle doesn't intersects
                         * with the walls.
                         */
                        curr.resolveCollisionWithBounds();
                    }
                }
            }

            public int getParticleCount() {
                return mBalls.length;
            }

            public float getPosX(int i) {
                return mBalls[i].mPosX;
            }

            public float getPosY(int i) {
                return mBalls[i].mPosY;
            }
        }

        public void startSimulation() {
            /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        }

        public void stopSimulation() {
            mSensorManager.unregisterListener(this);
        }

        public SimulationView(Context context,Bitmap bg,Bitmap bm,RectF box) {
            super(context);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;

           
            mBitmap = bm;
            
            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inScaled = false;
            
            backg=Bitmap.createScaledBitmap(bg, metrics.widthPixels, metrics.heightPixels, false);
			
            face=new Face(bm,box);
            
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

            final ParticleSystem particleSystem = mParticleSystem;
            final long now = mSensorTimeStamp + (System.nanoTime() - mCpuTimeStamp);
            
            
            final float sx = mSensorX;
            final float sy = mSensorY;

            
            particleSystem.update(sx, sy, now);
            face.update(sx, sy, now);
       
            final float xc = mXOrigin;
            final float yc = mYOrigin;
            final float xs = mMetersToPixelsX;
            final float ys = mMetersToPixelsY;
            final Bitmap bitmap = mBitmap;
            final int count = particleSystem.getParticleCount();
            for (int i = 0; i < count; i++) {
                /*
                 * We transform the canvas so that the coordinate system matches
                 * the sensors coordinate system with the origin in the center
                 * of the screen and the unit is the meter.
                 */

                final float x = xc + particleSystem.getPosX(i) * xs;
                final float y = yc - particleSystem.getPosY(i) * ys;
                
                
                //System.out.println(angle);
                //canvas.drawBitmap(bitmap, x, y, null);
            }
            
            //face.face.setDensity(300);
            
            
            
            canvas.drawBitmap(bitmap, 0,50, null);
            
            //System.out.println(face.face.getWidth()+":"+face.face.getHeight());
            
            canvas.drawRect(face.boxRec, p);
            canvas.drawRect(face.rec, p);
            
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