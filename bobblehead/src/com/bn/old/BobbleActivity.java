package com.bn.old;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class BobbleActivity extends Activity implements SensorEventListener{

	
	public SensorManager mgr;
	private static float angle;
	private BobbleView bv;
	//private static Bitmap imgS;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        byte[] b = getIntent().getExtras().getByteArray("img");
        Bitmap imgS = BitmapFactory.decodeByteArray(b, 0, b.length);
        
        
        mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        bv = new BobbleView(this,mgr);
        
        bv.setBackG(imgS);
        
        
        
        
        
        setContentView(bv);
       
        /* 
        ImageView image = (ImageView) findViewById(R.id.imageView1);
        */
        //Canvas c = new Canvas(src);
        //Paint p = new Paint();
        //c.drawCircle(100, 100, 10, new Paint());
       // image.setImageBitmap(imgS);
        
        
    }
    
   /* protected void onDraw(Canvas can){
    	 can.drawCircle(100,100,10,new Paint());
    	  
    	
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       //getMenuInflater().inflate(R.menu.activity_bobble, menu);
        return true;
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        // Register this class as a listener for the accelerometer sensor
        mgr.registerListener((SensorEventListener) this, mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        // ...and the orientation sensor
        mgr.registerListener((SensorEventListener) this, mgr.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    protected void onStop()
    {
        // Unregister the listener
        mgr.unregisterListener((SensorListener) this);
        super.onStop();
    }
     
   /* public void onSensorChanged(SensorEvent event) {
       
        		
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            	//x isdown, y is right
            	int x=(int) event.values[0];//x
            	int y=(int) event.values[1];//y
            	
            	angle=(float) java.lang.Math.atan((double)x/y);
            	bv.angle=angle;
            }
            
        }
    
	//this is the interface method of "OnTouchListener"
	public boolean onTouch(View view,MotionEvent event)
	{
		//x1=(int)event.getX() ;     //some math logic to plot the circle  in exact touch place
		//y1=(int)event.getY();
		  //System.out.println("X,Y:"+"x"+","+y);      //see this output in "LogCat"
		
		angle+=.8;
		bv.angle=angle;
		//invalidate();      //calls onDraw methodle;
		return true;
		
	}*/

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	/*
    
    public class BobbleView1 extends View implements OnTouchListener,SensorEventListener{


    	Paint paint;     //using this ,we can draw on canvas
    	Bitmap backg;
    	Bitmap face;
    	
    	public float angle;
    	//private Sensor accelerometer;
    	
    	
    	public BobbleView1(Context context)
    	{
    		super(context);


    		//to make it focusable so that it will receive touch events properly
    		setFocusable(true);
    		
    		//adding touch listener to this view
    		this.setOnTouchListener(this);
    		mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
    		//mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
    		//accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	}
    	
    	
    	public void setBackG(Bitmap bm){
    		this.backg=bm;
    		this.face= BitmapFactory.decodeResource(getResources(), R.drawable.busey);
    	}

    	
    	
    	//overriding the View's onDraw(..) method
    	protected void onDraw(Canvas canvas)
    	{
    		//paint.setARGB(255, r, g, b);
    		Paint p = new Paint();
    		p.setFilterBitmap(true);
    		backg.setDensity(100 );
    		
    		int h=canvas.getHeight();
    		int w=canvas.getWidth();
    		
    		RectF bounds =new RectF(0,0,w,h);
    		//
    		
    		
    	//	canvas.drawText(Integer.toString(backg.getDensity()), 200, 200, paint);
    		canvas.drawBitmap(backg, null, bounds,p);
    		
    		int x1=100;
    		int y1=100;
    		bounds =new RectF(x1,y1,x1+face.getWidth(),y1+face.getHeight());
    		Matrix matrix = new Matrix();
    		matrix.setRotate(angle,face.getWidth()/2,face.getHeight()/2);
    		canvas.drawBitmap(face, matrix,null);
    		
    		
    		invalidate();
    		
    	}


		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}


		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			
		}


		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return false;
		}
    	
    	
    }*/
    
   
    
   

	

}
