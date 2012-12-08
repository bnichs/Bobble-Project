package com.bn.old;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class BobbleView extends View implements SensorEventListener, OnTouchListener{

	static int x,y,r=255,g=255,b=255;
	final static int radius=30;
	Paint paint;     //using this ,we can draw on canvas
	Bitmap backg;
	Bitmap face;
	//private Sensor accelerometer;
	private Sensor mAccelerometer;
	private SensorManager mgr;
	
	public BobbleView(Context context,SensorManager mggr)
	{
		super(context);
	    paint=new Paint();
		paint.setAntiAlias(true);       //for smooth rendering
		paint.setARGB(255, r, g, b);    //setting the paint color

		//to make it focusable so that it will receive touch events properly
		setFocusable(true);
		//mgr=(SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		//adding touch listener to this view
		this.setOnTouchListener(this);
		//sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		//mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		//accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	public void startBobble(){
		mgr.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        
		
	}
	
	public void stopBobble() {
        mgr.unregisterListener(this);
    }
	
	
	public void setBackG(Bitmap bm){
		this.backg=bm;
		//this.face= BitmapFactory.decodeResource(getResources(), R.drawable.busey);
	}
	
	
	private Bitmap rotate(Bitmap bm, float theta){
		int x=bm.getWidth();
		int y=bm.getHeight();
		Matrix matrix = new Matrix();
		matrix.setRotate(angle,x/2,y/2);
		return Bitmap.createBitmap(bm, 0, 0, x, y, matrix, true);
		
	}

	//overriding the View's onDraw(..) method
	public void onDraw(Canvas canvas)
	{
		paint.setARGB(255, r, g, b);
		Paint p = new Paint();
		p.setFilterBitmap(true);
		backg.setDensity(100 );
		
		int h=canvas.getHeight();
		int w=canvas.getWidth();
		
		RectF bounds =new RectF(0,0,w,h);
		canvas.drawText(Integer.toString(backg.getDensity()), 200, 200, paint);
		canvas.drawBitmap(backg, null, bounds,p);
		
		
		//bounds =new RectF(x1,y1,x1+face.getWidth(),y1+face.getHeight());
	//	Matrix matrix = new Matrix();
		//matrix.setRotate(angle,face.getWidth()/2,face.getHeight()/2);
		canvas.drawBitmap(rotate(face,angle),100,100,null);
		
		//Matrix m = new Matrix();
		//m
		//
		//drawing the circle 	
		canvas.drawCircle(x,y,radius,paint);
		invalidate();

	}
	
	
	
	
	public int x1,y1;
	public float angle=0;
	
	
	
	

	//this is the interface method of "OnTouchListener"
	public boolean onTouch(View view,MotionEvent event)
	{
		x1=(int)event.getX() ;     //some math logic to plot the circle  in exact touch place
		y1=(int)event.getY();
		  //System.out.println("X,Y:"+"x"+","+y);      //see this output in "LogCat"
		
		angle+=.8;
		
		invalidate();      //calls onDraw method
		return true;
		
	}
	
	
	
    public void onSensorChanged(SensorEvent event) {
       
        /*
         * record the accelerometer data, the event's timestamp as well as
         * the current time. The latter is needed so we can calculate the
         * "present" time during rendering. In this application, we need to
         * take into account how the screen is rotated with respect to the
         * sensors (which always return data in a coordinate space aligned
         * to with the screen in its native orientation).
         */
    		
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
        	//x isdown, y is right
        	int x=(int) event.values[0];//x
        	int y=(int) event.values[1];//y
        	
        	angle=(float) java.lang.Math.atan((double)x/y);
        			
        }
        invalidate();
        
    }
	

   

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	
}

