package com.bn.bobblehead;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

public class FaceSelectActivity extends Activity {

		private SelectView mSelectView;
	    private SensorManager mSensorManager;
	    private PowerManager mPowerManager;
	    private WindowManager mWindowManager;
	    private Display mDisplay;
	    private WakeLock mWakeLock;

	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        byte[] b = getIntent().getExtras().getByteArray("img");
	        Bitmap imgS = BitmapFactory.decodeByteArray(b, 0, b.length);
	        
	        
	        
	        
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

	        // instantiate our simulation view and set it as the activity's content
	        mSelectView = new SelectView(this,imgS);
	        setContentView(mSelectView);
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
	        mSelectView.startSimulation();
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        /*
	         * When the activity is paused, we make sure to stop the simulation,
	         * release our sensor resources and wake locks
	         */

	        // Stop the simulation
	        mSelectView.stopSimulation();

	        // and release our wake-lock
	        mWakeLock.release();
	    }
    
    
    class SelectView extends View implements OnClickListener {
    	
    	private final Bitmap button;
    	
    	 private Sensor mAccelerometer;
    	 private Bitmap backg;
    	 
    	 public SelectView(Context context,Bitmap bm) {
             super(context);
             mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

             DisplayMetrics metrics = new DisplayMetrics();
             getWindowManager().getDefaultDisplay().getMetrics(metrics);
            
             
            
            // mMetersToPixelsX = mXDpi / 0.0254f;
             //mMetersToPixelsY = mYDpi / 0.0254f;*/

             
           Bitmap but = BitmapFactory.decodeResource(getResources(), R.drawable.go_button);
			 	
			button=Bitmap.createScaledBitmap(but, 100, 100, false);
            
			backg=Bitmap.createScaledBitmap(bm, metrics.widthPixels, metrics.heightPixels, false);
			
			buttonR=new Rect(metrics.widthPixels-150,metrics.heightPixels-200,metrics.widthPixels,metrics.heightPixels);
			
			
			
             Options opts = new Options();
             opts.inDither = true;
             opts.inPreferredConfig = Bitmap.Config.RGB_565;
             
             
             //backg = bm;
            
         }
    	 
    	 
    	 public void startSimulation() {
             /*
              * It is not necessary to get accelerometer events at a very high
              * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
              * automatic low-pass filter, which "extracts" the gravity component
              * of the acceleration. As an added benefit, we use less power and
              * CPU resources.
              */
            }

         public void stopSimulation() {
             
         }
         
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
		
		 protected void onDraw(Canvas canvas) {
			 	
			 	
			 	 
			 	System.out.println(canvas.getWidth());
	            
			 	canvas.drawBitmap(backg, 0, 0, null);
			 	if (selection != null){
	            	canvas.drawOval(selection, new Paint());
	            	System.out.println(selection.left+":"+selection.top+":"+selection.right+":"+selection.bottom);
	            	
	            }
			 	//buttonR=new Rect((int)(canvas.getWidth()-100f),(int)(canvas.getHeight()-200f),canvas.getHeight(),canvas.getWidth());
				canvas.drawBitmap(button, null,buttonR, null);
							 	
	           
	           
	            // and make sure to redraw asap
	            invalidate();
	        }
	        
		private Rect buttonR;
		private RectF selection;
		private boolean rectDrawn=false;
		int top=0;
		int left=0;
		int bottom=0;
		int right=0;
		public boolean onTouchEvent(MotionEvent e) {
	          // TODO Auto-generated method stub
			
			int x = (int) e.getX();
		    int y = (int) e.getY();
		   
		    
		    //System.out.println(x+ ":" + y);
		    if (buttonR.contains(x, y)){
					Intent i = new Intent(FaceSelectActivity.this,BobActivity.class);
					Bitmap bm=Bitmap.createBitmap(backg,(int)selection.left, (int)selection.top,(int)selection.width(),(int)selection.height());
					i.putExtra("img", bm);
					startActivity(i);
					return true;
					
				}
		    
			switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				 if (!rectDrawn){
					top=(int) y;
					left=(int) x;
				}
			case MotionEvent.ACTION_UP :
				right=x;
				bottom=y;
				rectDrawn=true;
			case MotionEvent.ACTION_MOVE:
				right=x;
				bottom=y;
				//rectDrawn=true;
			}
			
			if (top==0 && left==0 && right==0 && bottom==0){
				selection=null;
				
			}
			
			
			//selection=new Rect(100,0,100,0);
			selection=new RectF(left,top,right,bottom);
			return true;
			
			
	    }


		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		

    	
    }
}
