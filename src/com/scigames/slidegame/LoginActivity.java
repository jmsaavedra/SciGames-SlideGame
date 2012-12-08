// ServiceADKActivity.java
// ---------------------------
// RobotGrrl.com
// November 29, 2011

package com.scigames.slidegame;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.scigames.slidegame.ADKService;
import com.scigames.slidegame.ServiceADKApplication;
import com.scigames.serverutils.SciGamesHttpPoster;
import com.scigames.serverutils.SciGamesListener;
import com.scigames.slidegame.LoginActivity;
import com.scigames.slidegame.R;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements Runnable, SciGamesListener{
    /** Called when the activity is first created. */
    
	private static final String TAG = "LoginActivity";
	public String baseDbURL = "http://db.scigam.es";
	
    private boolean debug = false; //for debug info popups
    private boolean debugFakeBracelet = false; //to send fake bracelet + mat IDs
    
    private String debugBracelet1 = "5006affffffc3ffffffd9";
    private String debugBracelet2 = "5006affffffc3ffffffd9"; //500315518 (yellow) //500315affffffe5 (green) //5006affffffc3ffffffd9 (red) //500315c37 (blue)
    private String debugFabricID = "5006affffffc9ffffffcf";//"0056ffffff9affffff90";
    //private int debugSlideLevel = 2;
    private String slideSessionDataDebug = "";
    
    static int joulesPerDot = 120;
	
	/*** service stuff ***/
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	Thread mThread;
	TextView fileDescText, inputStreamText, outputStreamText, accessoryText;
	private Handler mHandler = new Handler();	
	/*** end service stuff ***/
	
    private String visitId = "VISITID";
    private String studentId = "STUDENTID";
    private String slideSessionIdIn = "null";
    private String currSlideLevel = "0";
    private String currMass = "null";
    //private String[] slideGoals = null;
    private String currPage = "login";
    private String currRfid = "null";
    /* the following protected so that value is stored even when we leave this class and return.
     * TODO: currPage and currRfid should be the same, things need to get moved around...
     */
    protected String currAttempt = "null"; 
	protected String chosenFabricValue = "null";
	protected static String thermalGoal = "null";
	protected static String kineticGoal = "null";
	protected String SlideLevel = "null";
	
    
    private boolean slideTimeGo = false;

    TextView greets;
    TextView fabricId;
    TextView noDevice;
    TextView gotIt;
    
    Button sendFakeSlideData; //for testing
    Button playBtn;
    Button pantsBtn;
    
    AlertDialog alertDialog;
    AlertDialog infoDialog;
    AlertDialog fabricDialog;
    
    Typeface ExistenceLightOtf;
    Typeface Museo300Regular;
    Typeface Museo500Regular;
    Typeface Museo700Regular;
    
    ImageView bg;
    
    SciGamesHttpPoster task = new SciGamesHttpPoster(LoginActivity.this, baseDbURL + "/pull/auth_rfid.php");
    
    SciMath calculator;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        task.setOnResultsListener(this);
        
		alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
	    alertDialog.setTitle("No Registration System Attached ");
	    alertDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        finish();
	        }
	    });
	    
	    infoDialog = new AlertDialog.Builder(LoginActivity.this).create();
	    infoDialog.setTitle("Debug Info");
	    infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        //moveOn = true;
	        }
	    });
	    
	    fabricDialog = new AlertDialog.Builder(LoginActivity.this).create();
	    fabricDialog.setTitle("Debug Info");
	    fabricDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        sendPress('Y');
	        }
	    });
        
        /******* service stuff ******/
        mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
		Log.e(TAG, "Hellohello!");	
		startService(new Intent(this, ADKService.class));
		/***** end service stuff *****/      		
		
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        
	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
	    Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");
	    
    	Resources res = getResources();
    	setContentView(R.layout.login_page);
        
        task.setOnResultsListener(this);
        //sendPress('X');
        
        currPage = "login";
        Log.d(TAG, "...end OnCreate");
        
        if(debugFakeBracelet){
        	sendFakeBraceletId(debugBracelet1);
        }
        sendPress('-');//set LED Meter to all potential (blue) on the first start up of app.
    }

	private void sendFakeBraceletId(String mBracelet){
		if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(LoginActivity.this,baseDbURL + "/pull/auth_rfid.php");
		    //set listener
	        task.setOnResultsListener(this);
	        //prepare key value pairs to send
			String thisBracelet = mBracelet;
			String[] keyVals = {"rfid", thisBracelet}; 
			//create AsyncTask, then execute
			AsyncTask<String, Void, JSONObject> serverResponse = null;
			serverResponse = task.execute(keyVals);
		} else {
			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
			alertDialog.show();	
	    }
	}
	
    @Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
    
    @Override
    protected void onNewIntent(Intent intent){
        Log.d(TAG,"onNewIntent");

        if(currPage.equals("login")){
  		  setContentView(R.layout.login_page);
  		  //sendPress('Y');
  		  currRfid = "";
        }
          if(intent.hasExtra("page")){
        	  if(intent.getExtras().getString("page").equals("login")){
        		  currPage = "login";
        		  slideTimeGo = false;
        		  setContentView(R.layout.login_page);
        		  if(debugFakeBracelet) sendFakeBraceletId(debugBracelet2);
        		  sendPress('Y');
        		  currRfid = "";
        	  } 
        	  
        	  /* from MenuActivity straight through to ObjectiveActivity */
        	  else if(intent.getExtras().getString("page").equals("objective")){
        		  currRfid = intent.getExtras().getString("rfid");
        		  studentId = intent.getExtras().getString("studentId");
        		  currSlideLevel = intent.getExtras().getString("slideLevel");
        		  currMass = intent.getExtras().getString("mass");
        		  
        		  //***Set LED GOAL Meter***//        		  
        		  calculator = new SciMath(Integer.parseInt(currMass), 0,0,0); //quick sciMath just to get potential, nothing else
        		  int thermalLeds = calculator.getThermalGoalDots();
        		  int kineticLeds = calculator.getKineticGoalDots();
        		  float thisPotential = calculator.getTotalPotential();
        		 
        		  if(debug || debugFakeBracelet){
		        	  infoDialog.setTitle("GOAL LED METER DEBUG");
		        	  infoDialog.setMessage(
		        			  "joulesPerDot: " + String.valueOf(joulesPerDot)+
		        			  "\nthis Potential: " +String.valueOf(thisPotential)+	        			
		        			  "\nthermalLEDs: "+ String.valueOf(thermalLeds)+
		        			  "\nkineticLEDs: "+ String.valueOf(kineticLeds)
		        			  );
		        	  infoDialog.show();
        		  }  
        		  sendLedMeterGoalValues(kineticLeds, thermalLeds);  //kinetic, thermal
        		  
	       		  Intent i = new Intent(LoginActivity.this, ObjectiveActivity.class);
	     		  Log.d(TAG,"new Intent");
	     		  i.putExtra("rfid",currRfid);
	     		  i.putExtra("studentId",studentId);
	     		  i.putExtra("slideLevel", currSlideLevel);
	     		  i.putExtra("mass", currMass);
	     		  Log.d(TAG,"startActivity...");
	     		  LoginActivity.this.startActivity(i);
	     		  Log.d(TAG,"...startActivity");
        	  } 
        	  
        	  /* from objectiveActivity to fabric page */
        	  else if(intent.getExtras().getString("page").equals("fabric")){
	        	  currPage = "fabric";
	        	  studentId = intent.getExtras().getString("studentId");
	        	  currSlideLevel = intent.getExtras().getString("slideLevel");
	        	  currRfid = intent.getExtras().getString("rfid");
	        			  
	        	  if(debug){
		        	  infoDialog.setTitle("objActivity to Fabric page:");
		        	  infoDialog.setMessage("studentID: " + studentId+ ", slideLevel: " + currSlideLevel);
		        	  infoDialog.show();
	        	  }
	        	  setContentView(R.layout.fabric_page);
	              fabricId = (TextView)findViewById(R.id.fabric_id);
				  setTextViewFont(Museo700Regular, fabricId);
        	      fabricId.setVisibility(View.INVISIBLE);
        	      
        	      pantsBtn = (Button)findViewById(R.id.btn_pants); //this is the right arrow
        	      pantsBtn.setOnClickListener(mChoosePants);
        	     // pantsBtn.setVisibility(View.VISIBLE);
        	      pantsBtn.setVisibility(View.INVISIBLE); //keep invisible for now (not using it)
        	      
	              playBtn = (Button)findViewById(R.id.btn_play); //this is the right arrow
	              playBtn.setOnClickListener(mSlidePage);
        	      playBtn.setVisibility(View.INVISIBLE); //set invisible until a fabric is swiped/chosen
	              
        	      Log.d(TAG,"...fabricId");
	              sendPress('Y');
	              if(debugFakeBracelet) sendFabricId(debugFabricID);
	              
	          /* from fabric page to take a slide turn page */    
        	  } else if (intent.getExtras().getString("page").equals("slideturn")){
        		  currPage = "sliding";
        		  setContentView(R.layout.slide_page);
        		  slideSessionIdIn = intent.getExtras().getString("slidesessionId");
        		  currSlideLevel = intent.getExtras().getString("slideLevel");
        		  //slideGoals = intent.getStringArrayExtra("slideGoals");
        	      sendFakeSlideData = (Button) findViewById(R.id.btn_sendfakedata);
        	      sendFakeSlideData.setOnClickListener(mSlideDataGo);
        	      
        	      //*****send press TELL ARDUINO SLIDE GO, WAIT FOR *******
        	      slideTimeGo = true;
        	      sendPress('S');
        	  } 
         }   		
    } 
    
	
	/********* server querying ***********/
	private void checkBraceletId(String braceletId){
		Log.d(TAG, "hit checkBraceletId()");
		Log.d(TAG, "braceletId to check: "+braceletId);
	  	if(debug){infoDialog.setTitle("hit checkBraceletId()");
		  	  infoDialog.setMessage(
		  			  "braceletId to check: "+braceletId
		  			  );
		  	  infoDialog.show();
	  	}
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(LoginActivity.this,baseDbURL + "/pull/auth_rfid.php");
		    //set listener
	        task.setOnResultsListener(this);
	        //prepare key value pairs to send
			String thisBracelet = braceletId.toLowerCase();
			String[] keyVals = {"rfid", thisBracelet}; 
			//create AsyncTask, then execute
			AsyncTask<String, Void, JSONObject> serverResponse = null;
			serverResponse = task.execute(keyVals);
			
	    } else {
			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
			alertDialog.show();	
	    }
	}
	
	private void sendFabricId(String braceletId){
		Log.d(TAG, "hit checkFabrictId()");
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(LoginActivity.this,baseDbURL + "/pull/slide_session.php");
		    //set listener
	        task.setOnResultsListener(LoginActivity.this);
	        //prepare key value pairs to send
			String thisBracelet = braceletId.toLowerCase();
			String[] keyVals = {"student_id", studentId, "fabric_rfid", thisBracelet}; 
			if(debug){
				infoDialog.setTitle("keyVals:");
				infoDialog.setMessage(keyVals[0]+keyVals[1]+keyVals[2]+keyVals[3]);
				infoDialog.show();
			}
			//create AsyncTask, then execute
			AsyncTask<String, Void, JSONObject> serverResponse = null;
			serverResponse = task.execute(keyVals);
	    } else {
			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
			alertDialog.show();
	    }
	}
	
	private void sendSlideData(String[] slideDataIn){ 
		Log.d(TAG, "hit sendSlideData()");
		
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(LoginActivity.this,baseDbURL + "/push/update_slide_data.php");
		    //set listener
	        task.setOnResultsListener(this);
	        
			//prepare key value pairs to send
			String[] keyVals = {"slide_length",slideDataIn[0] , "slide_angle",slideDataIn[1] ,"start_time",slideDataIn[2],
					"end_time",slideDataIn[3] ,"total_time",slideDataIn[4] ,"score",slideDataIn[5] ,"kinetic",slideDataIn[6],
					"potential",slideDataIn[7] ,"thermal",slideDataIn[8] ,"attempt",slideDataIn[9],"level_completed",slideDataIn[10],
					"slide_session_id", slideDataIn[12], "session_valid", slideDataIn[11]};
			if(debug){
				for(int i=0; i<keyVals.length-1; i+=2)
				slideSessionDataDebug += keyVals[i] + ": " + keyVals[i+1] + " \t ";
			}
	        
			//String[] keyVals = {"slide_length","5","slide_angle","30","start_time","255","end_time","160","total_time","931","score","1221","kinetic","55","potential","45","thermal","49","attempt","2","level_completed","false","slide_session_id","502c5e65c0c0ba831d000000"}; 
	        
//			infoDialog.setTitle("keyVals:");
//			infoDialog.setMessage(keyVals[0]+keyVals[1]+keyVals[2]+keyVals[3]);
//			infoDialog.show();
			//create AsyncTask, then execute
			AsyncTask<String, Void, JSONObject> serverResponse = null;
			serverResponse = task.execute(keyVals);
	    } else {
			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
			alertDialog.show();
	    }
	}

     OnClickListener mSlidePage = new OnClickListener(){
 	    public void onClick(View v) {
 			Log.d(TAG,"mSlidePage.onClick");
 			//startActivity(new Intent(ProfileActivity.this, Registration2RfidMass_AdkServiceActivity.class));
 			Intent i = new Intent(LoginActivity.this, LoginActivity.class);
 			Log.d(TAG,"new Intent");
 			i.putExtra("slidesessionId",slideSessionIdIn);
 			i.putExtra("slideLevel", currSlideLevel);
 			i.putExtra("page", "slideturn");
 			//i.putExtra("slideGoals", slideGoals);
 			Log.d(TAG,"startActivity...");
 			LoginActivity.this.startActivity(i);
 			Log.d(TAG,"...startActivity");
 			//sendPress('S');
 		}
     };
     
     OnClickListener mChoosePants = new OnClickListener(){ /* not using this anymore */
    	public void onClick(View v) {
    		Log.d(TAG, "mSendPants.onClick");
    		sendFabricId("0000000000"); //this fabricId is set to "Pants" in the database
    		//sendPress('Z');
    	}
     };
     
     /*** FAKE SLIDE DATA GO ***/
     OnClickListener mSlideDataGo = new OnClickListener(){ //gets called when FAKE SLIDE DATA is pressed
  	    public void onClick(View v) { 	    	
  	    	Log.d(TAG, "mSlideDataGo.onClick");
  	    	/* would be from Arduino! */
  	  	    int startGate = 95 + (int)(Math.random()*80);
  	  	    int endGate = startGate - (int)(Math.random()*50);
  	  	    int middle = 450+(int)(Math.random()*30);
  	    	prepareSlideData(startGate, middle, endGate);
  		}
      };
      
   /*** SLIDE DATA GO ***/
   public void prepareSlideData(int timer1, int timer2, int timer3){    //called when we receive slide timer durations from ARDUINO 
	    	Log.d(TAG, "mSlideDataGo.onClick");
	    	/* from Arduino! */
	    	int startGate = timer1;
	    	int endGate = timer3;
	    	int totalTime = timer1+timer2+timer3;
	    	
	    	/* from server! */
	    	int attempt = Integer.parseInt(currAttempt);
	    	//int fabricFrictionCoefficient = Integer.parseInt(chosenFabricValue);
	    	int tKineticGoal = Integer.parseInt(kineticGoal);
	    	int tThermalGoal = Integer.parseInt(thermalGoal);
	    	int level = Integer.parseInt(currSlideLevel);
	    	int mass = Integer.parseInt(currMass);
		    
		    
//	    	if (mass < 20) mass = 65; /* for debug for now! */
	    	
	    	Log.d(TAG, ">>>>>>> currMass: "+currMass);
	    	Log.d(TAG, ">>>>>>> currAttempt: "+currAttempt);
	    	Log.d(TAG, ">>>>>>> fabricValue: "+chosenFabricValue);
	    	Log.d(TAG, ">>>>>>> tKineticGoal: "+kineticGoal);
	    	Log.d(TAG, ">>>>>>> tThermalGoal: "+thermalGoal);
	    	Log.d(TAG, ">>>>>>> level: "+currSlideLevel);
	    	
	    	/* remains static for now: */
	    	float slideLength = 5.1f;
	    	float slideAngle = 30;
	    	
	    	//create the calculator object with incoming data from Arduino!
	    	calculator = new SciMath(mass, startGate, endGate, totalTime);

	    	
  	    	float thisPotential = calculator.getTotalPotential();
  		    float totalEnergy = calculator.getBottomKinetic() + calculator.getThermal(); 		  
  		    
  		    float kPct = (calculator.getBottomKinetic()/totalEnergy);
  		    float tPct = (calculator.getThermal()/totalEnergy);	  
  		    int kineticLeds = Math.round((thisPotential * kPct)/joulesPerDot);
  		    int thermalLeds = Math.round((thisPotential * tPct)/joulesPerDot);
  		    
  		    if(calculator.getSessionValid()){
  		    	sendLedMeterResultValues(kineticLeds, thermalLeds);
  		    } else sendLedMeterError();
  		    
  		    int thisScore = calculator.getScore(level, attempt, tKineticGoal, tThermalGoal);
	    	String mAchievedRatio = String.valueOf(calculator.getAchievedRatio());
	    	String mGoalRatio = String.valueOf(calculator.getGoalRatio());
	    	//String mGoalRatio = String.valueOf((float)tKineticGoal/tThermalGoal);
	    	 
  	    	String[] thisData = {
  	  	    		String.valueOf(slideLength),  //slide_length 	//0	
  	  	    		String.valueOf(slideAngle),	//slide_angle		//1
  	  	    		String.valueOf(startGate), 						//2
  	  	    		String.valueOf(endGate), 						//3
  	  	    		String.valueOf(totalTime),						//4
  	  	    		String.valueOf(thisScore), //REAL score 		//5
  	  	    		String.valueOf(calculator.getBottomKinetic()),	//6
  	  	    		String.valueOf(calculator.getTotalPotential()),	//7
  	  	    		String.valueOf(calculator.getThermal()),		//8
  	  	    		String.valueOf(attempt),	//attempt			//9
  	  	    		String.valueOf(calculator.getLevelPassed()), 	//10
  	  	    		String.valueOf(calculator.getSessionValid()),	//11
  	  	    		slideSessionIdIn 								//12
  	  	    	};
  	    	

	    	
		infoDialog.setTitle("Slide Session Data: ");
		infoDialog.setMessage( ">>> SLIDE SESSION VALUES <<<"+
				"\nstart_gate: "+thisData[2]+
				"\nmiddle_gate: "+timer2+
				"\nend_gate: "+thisData[3]+
				"\ntotal_time: "+thisData[4]+
				"\nslide length: "+thisData[0]+
				"\nslide_angle: "+thisData[1]+
				"\nscore: "+thisData[5]+
				"\ntotal_kinetic: "+thisData[6]+
    			"\ntotal_thermal: "+thisData[8]+
    			"\ntotal_potential: "+thisData[7]+
    			"\nattempt: "+thisData[9]+
    			"\nlevel_passed: "+thisData[10]+
    			"\nsession_valid: "+thisData[11]+
  	    		"\nslide_sessionID: "+thisData[12]+
  	    		"\nthermal_leds: "+thermalLeds+
  	    		"\nkinetic_leds: "+kineticLeds+
  	    		"\nthermal_PCT: "+tPct*100+
  	    		"\nkinetic_PCT: "+kPct*100+
    			"\nacheived_ratio: "+mAchievedRatio+
    			"\nagoal_ratio: "+mGoalRatio);
		infoDialog.show();
		
	    sendSlideData(thisData);/* BLAM - PASS THAT S***/
   }


	@Override
	public void onResultsSucceeded(String[] student, String[] slide_session,
			String[] slide_level, String[] objective_images, String[] fabric,
			String[] result_images, String[] score_images, String attempts,
			boolean no_session, JSONObject serverResponseJSON) throws JSONException {
		
		Log.d(TAG, "onResultsSucceded, ");
		Log.d(TAG, "currPage equals:" + currPage);
		
		if (currPage.equals("login")){
			
			Log.d(TAG, "onResultsSucceeded,login page: "+ serverResponseJSON.toString());
			//currAttempt = String.valueOf(serverResponseJSON.getInt("attempts"));
			currAttempt = attempts;
			thermalGoal = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getJSONObject("ratio").getInt("thermal"));
			kineticGoal = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getJSONObject("ratio").getInt("kinetic"));
			
			SciMath quickMath = new SciMath(Integer.valueOf(student[7]),0,0,0);
			Log.d(TAG, "mass: "+student[7]);
			int potentialLeds = quickMath.getPotentialDots();
			//LED METER AFTER SWIPE IN
			//sendLedMeterPotential(potentialLeds, 2);
			//sendLedMeterPotential(0,0); //clear all LEDs for now
			sendLedMeterAnimate();
			
			Log.d(TAG, "currAttempt received: "+ currAttempt);
	   		Intent i = new Intent(LoginActivity.this, MenuActivity.class);
			Log.d(TAG,"new Intent");
			i.putExtra("fName", student[2]);
			i.putExtra("lName", student[3]);
			i.putExtra("studentId",student[0]);
			i.putExtra("visitId",student[1]);
			i.putExtra("photo",student[4]);
			i.putExtra("slideLevel",student[5]);
			i.putExtra("rfid",student[6]);
			i.putExtra("mass", student[7]);
			Log.d(TAG,"startActivity...");
			LoginActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
		} 
		
		else if(currPage.equals("objective")){
	   		Intent i = new Intent(LoginActivity.this, LoginActivity.class);
			Log.d(TAG,"new Intent");
			//thermalGoal = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getJSONObject("ratio").getInt("thermal"));
			//kineticGoal = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getJSONObject("ratio").getInt("kinetic"));
			i.putExtra("page", "objective");
			//i.putExtra("studentId",studentId);
			Log.d(TAG,"startActivity...");
			LoginActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
			
		} else if(currPage.equals("fabric")){
			if(serverResponseJSON.isNull("fabric")){//if(serverResponseJSON.getString("fabric").equals(null)){ //
				//this is not a fabric! kid swiped his bracelet instead or something ridiculous.
				fabricDialog.setTitle("Not a Mat");
				fabricDialog.setMessage("Try again, this time with a Mat tag, located at the corner of your chosen materal.");
				fabricDialog.show();
			} else {
				fabricId.setVisibility(View.VISIBLE);
				playBtn.setVisibility(View.VISIBLE);
				String chosenFabric = serverResponseJSON.getJSONObject("fabric").getString("name");
				chosenFabricValue = String.valueOf(serverResponseJSON.getJSONObject("fabric").getInt("value"));
				thermalGoal = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getJSONObject("ratio").getInt("thermal"));
				kineticGoal = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getJSONObject("ratio").getInt("kinetic"));
				SlideLevel = String.valueOf(serverResponseJSON.getJSONObject("slide_level").getInt("level"));
							
//				slideGoals[0] = chosenFabricValue;
//				slideGoals[1] = thermalGoal;
//				slideGoals[2] =  kineticGoal;
//				slideGoals[3] =  SlideLevel;
				
				//show fabric name
				Resources res = getResources();
				fabricId.setText(String.format(res.getString(R.string.fabric_choice), chosenFabric));
				//get sessionID
				slideSessionIdIn = slide_session[0];
				if(debug){
					infoDialog.setTitle("onResults currPage.equals fabric: ");
					infoDialog.setMessage(serverResponseJSON.toString());
					infoDialog.show();
				}
				
			//	fabricId.setText(serverResponseJSON.toString());
			//	slideSessionIdIn = slide_session[0];
			}
		} 
		else if(currPage.equals("sliding")) {
			if(debug){
//				slideInstructions.setText(serverResponseJSON.toString());
				infoDialog.setTitle("SlideData ResultsSucceeded");
				infoDialog.setMessage(slideSessionDataDebug);
				infoDialog.show();
			}
			
	   		Intent i = new Intent(LoginActivity.this, LoginActivity.class); //reset the login page for next swipe in 
			Log.d(TAG,"new Intent");
			i.putExtra("page", "login");
			Log.d(TAG,"startActivity...");
			LoginActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
		}
		else if(currPage.equals("slideReview")){
			if(debug){
				infoDialog.setTitle("slideReview ResultsSucceeded");
				infoDialog.setMessage(serverResponseJSON.toString());
				infoDialog.show();
			}
		}
	}
	
	public void failedQuery(String failureReason) {

		Log.d(TAG, "LOGIN FAILED, REASON: " + failureReason);

		if(currPage.equals("login")){
			fabricDialog.setTitle("Bracelet Problem");
			fabricDialog.setMessage("Make sure you've already checked in at the Registration Station.");
			fabricDialog.show();
		} else {
			infoDialog.setTitle("Failed Query:");
			infoDialog.setMessage(failureReason);
			infoDialog.show();
		}
		onResume();
	}
	
	
	/****** ADK stuff!! *****/
	  	@Override
		public void onResume() {
		  Log.d(TAG, "onResume() hit!!");
	    	Resources res = getResources();
			super.onResume();
			//setContentView(R.layout.login_page);
			try {
				ADKService.self.stopUpdater();
			} catch(Exception e) {
				Log.d(TAG, "Stopping the updater failed");
			}
			Intent intent = getIntent();
			if (mInputStream != null && mOutputStream != null) {
				Log.v(TAG, "input and output stream weren't null!");
				enableControls(true);
				return;
			}
			UsbAccessory[] accessories = mUsbManager.getAccessoryList();
			Log.v(TAG, "all the accessories: " + accessories);
			UsbAccessory accessory = (accessories == null ? null : accessories[0]);
			if (accessory != null) {
				if (mUsbManager.hasPermission(accessory)) {
					Log.v(TAG, "mUsbManager does have permission");
					openAccessory(accessory);
				} else {
					Log.v(TAG, "mUsbManager did not have permission");
					synchronized (mUsbReceiver) {
						if (!mPermissionRequestPending) {
							mUsbManager.requestPermission(accessory,
									mPermissionIntent);
							mPermissionRequestPending = true;
						}
					}
				}
			} else {
				Log.d(TAG, "mAccessory is null");
				Log.d(TAG, "NO ACCESSORY ATTACHED");
				//alertDialog.setMessage("Please Attach the Registration System to this Tablet and Login");
				//alertDialog.show();
				if(!debug && !debugFakeBracelet){
					setContentView(R.layout.no_device);
				}
		        
			}
			// Let's update the textviews for easy debugging here...
			updateTextViews();
		}
	    
	    @Override
		public void onPause() {
	    	Log.v(TAG, "onPause");
	    	//closeAccessory();
	    	try {
	    		if(!currPage.equals("sliding")){
	    			ADKService.self.startUpdater();
	    		}
			} catch(Exception e) {		
			} 	
	        Log.v(TAG, "done, now pause");
			super.onPause();
		}

		@Override
		public void onDestroy() {
			Log.v(TAG, "onDestroy");
			unregisterReceiver(mUsbReceiver);
			super.onDestroy();
		}
		
		@Override
		protected void onStop() {
		      super.onStop();
		}

		//@Override
		public void run() {
			int ret = 0; //number of bytes returned
			byte[] buffer = new byte[16384]; //holds all bytes returned
			int i;
			int iValue = 0;
			String thisBracelet = "";
			int slideTime1 = 0;
			int slideTime1b = 0;
			int slideTime1c = 0;
			int slideTime2 = 0;
			int slideTime3 = 0;
			int slideCtr = 0;
			//while (ret >= 0) {
			while(true){
				try {
					ret = mInputStream.read(buffer);
					Log.d(TAG, "ret =" + String.valueOf(ret));
				} catch (IOException e) {
					break;
				}
				thisBracelet = "";
				i = 0;
				byte[] sValue1 = new byte [8]; //will hold slide values from buffer
				byte[] sValue2 = new byte [8]; //will hold slide values from buffer
				byte[] sValue3 = new byte [8]; //will hold slide values from buffer
				
				int one = 0;
				int two = 0;
				int three = 0;
				byte crc = 0;
				
				if(!slideTimeGo){ /* for when we are reading any RFID tag */
					while (i < ret) {
						int len = ret - i;
						Log.v(TAG, "Read: " + buffer[i]);	
						final int val = (int)buffer[i];
						byte[] bValue = new byte [ret];
						bValue[i] = (byte)buffer[i];
						iValue = (int)buffer[i];
						Log.d(TAG, "currBuffer: "+String.valueOf((int)i));
						Log.d(TAG, Integer.toHexString(iValue));
						thisBracelet = thisBracelet.concat(Integer.toHexString(iValue));
						Log.d(TAG, "thisBracelet: "+thisBracelet);
						i++;
					}
				} else { /* for when we are receiving slide data */
					while (i < ret){
						
						crc = (byte)((0x00FF & buffer[0]) ^ (0x00FF & buffer[1]) ^ (0x00FF &
								buffer[2]) ^ (0x00FF & buffer[3]) ^ (0x00FF & buffer[4]) ^ (0x00FF & buffer[5]));
								// calculate the "CRC"
						
						if (buffer[6] == crc) {  // compare to the received "CRC" - if it matches we'll trust that the received data is good
							
							one = ((int)(buffer[0] & 0x00FF) * 256) + (int)(buffer[1] & 0x00FF);
							two = ((int)(buffer[2] & 0x00FF) * 256) + (int)(buffer[3] & 0x00FF);
							three = ((int)(buffer[4] & 0x00FF) * 256) + (int)(buffer[5] & 0x00FF);
						}
						i++;	
					}
				}
				
				/* values need to be final before passing to handler */
				final int fRet = ret;
				//final byte[] finalVals1 = sValue1;	//copy array into a final byte[]
				//final byte[] finalVals2 = sValue2; //copy array into a final byte[]
				//final byte[] finalVals3 = sValue3; //copy array into a final byte[]
				
				final int tOne = one;
				final int tTwo = two;
				final int tThree = three;
				final byte fCrc = crc;
				final String fThisBracelet = thisBracelet;
				mHandler.post(new Runnable() {
					
					public void run() {
		            	// This gets executed on the UI thread so it can safely modify Views
						if(currPage.equals("fabric")){
							Log.d(TAG, "checkFabricID: "+ fThisBracelet);
							currRfid = fThisBracelet; //not a bracelet at all in this case
							sendFabricId(fThisBracelet);
						} else if(currPage.equals("sliding")){
							//this is where slide sensor data is received!
							infoDialog.setTitle("slide times returned:");
//							String mFinalVals1 = "mfv1: ";
//							for(int i=0; i<finalVals1.length; i++){
//								mFinalVals1 = mFinalVals1+ " " + String.valueOf((finalVals1[i]&0xff));
//							}							
							infoDialog.setMessage(
								"numBytesReceived= "+String.valueOf(fRet) +
								"    slideTime1= "+String.valueOf(tOne) +
								"    slideTime2= "+String.valueOf(tTwo)  +
								"    slideTime3= "+String.valueOf(tThree) +
								"    CRC= "+String.valueOf(fCrc)
							);
							infoDialog.show();
							prepareSlideData(tOne, tTwo, tThree);
							
						} else { //we are doing initial login=
							Log.d(TAG, "from arduino fThisBraceletId: "+ fThisBracelet);
							checkBraceletId(fThisBracelet);
						}
					}
				});
//				switch (buffer[i]) {
//					default:
//						Log.d(TAG, "unknown msg: " + buffer[i]);
//						i = len;
//						break;
//					}
			}
		}

	    // ------------
	    // ADK Handling
		// ------------
		
		private void openAccessory(UsbAccessory accessory) {
			
			Log.e(TAG, "openAccessory: " + accessory);
			Log.d(TAG, "this is mUsbManager: " + mUsbManager);
			mFileDescriptor = mUsbManager.openAccessory(accessory);
			
			Log.d(TAG, "Tried to open");
			
			if (mFileDescriptor != null) {
				mAccessory = accessory;
				FileDescriptor fd = mFileDescriptor.getFileDescriptor();
				mInputStream = new FileInputStream(fd);
				mOutputStream = new FileOutputStream(fd);
				mThread = new Thread(null, this, "DemoKit"); // meep
				mThread.start(); // meep
				Log.d(TAG, "accessory opened");
				setContentView(R.layout.login_page);
				if(currPage.equals("login"))sendPress('Y');
				enableControls(true);
			} else {
				Log.d(TAG, "accessory open fail");
				
					Log.d(TAG, "NO ACCESSORY ATTACHED");
					alertDialog.setMessage("Please Attach the Registration System to this Tablet and Login");
					alertDialog.show();
				
				enableControls(false);
			}
		}
		
		private void closeAccessory() {
			Log.e(TAG, "closing accessory");
			try {
				if (mFileDescriptor != null) {
					mFileDescriptor.close();
				}
			} catch (IOException e) {
			} finally {
				mFileDescriptor = null;
				mAccessory = null;
			}
			enableControls(false);
		}
		
		//*********** send LED meter data ************//
		public void sendLedMeterPotential(int potentialLeds, int pushLeds){
			Log.d(TAG, "sendLedPotential:");
			Log.d(TAG, Integer.toString(potentialLeds));
			Log.d(TAG, "sendLedPush:");
			Log.d(TAG, Integer.toString(pushLeds));
			
			byte[] buffer = new byte[3];
			buffer[0] = '-';
			buffer[1] = (byte)potentialLeds;
			buffer[2] = (byte)pushLeds;
			
			if (mOutputStream != null) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}
		}
		
		public void sendLedMeterGoalValues(int kLeds, int tLeds){
			Log.d(TAG, "sendLedMeterGoalValues kinetic:");
			Log.d(TAG, Integer.toString(kLeds));
			Log.d(TAG, "thermal:");
			Log.d(TAG, Integer.toString(tLeds));
			byte[] buffer = new byte[3];
			buffer[0] = '+';
			buffer[1] = (byte)kLeds;
			buffer[2] = (byte)tLeds;
			
			if (mOutputStream != null) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}
		}
		
		public void sendLedMeterAnimate(){
			Log.d(TAG, "sendLedMeterAnimate");
			byte[] buffer = new byte[3];
			buffer[0] = '*';
			buffer[1] = (byte)1;
			buffer[1] = (byte)1;
			if (mOutputStream != null) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}
		}
		
		public void sendLedMeterError(){
			Log.d(TAG, "sendLedMeterError");
			byte[] buffer = new byte[3];
			buffer[0] = 'x';
			buffer[1] = (byte)1;
			buffer[1] = (byte)1;
			if (mOutputStream != null) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}	
		}
		
		public void sendLedMeterResultValues(int kLeds, int tLeds){
			Log.d(TAG, "sendLedMeterResultValues kinetic:");
			Log.d(TAG, Integer.toString(kLeds));
			Log.d(TAG, "thermal:");
			Log.d(TAG, Integer.toString(tLeds));
			byte[] buffer = new byte[3];
			buffer[0] = '_';
			buffer[1] = (byte)kLeds;
			buffer[2] = (byte)tLeds;
			
			if (mOutputStream != null) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}
		}

		public void sendCommand(byte command, byte target, int value) {
			Log.e(TAG,"sendCommand hit");
			byte[] buffer = new byte[3];
			if (value > 255)
				value = 255;

			buffer[0] = command;
			buffer[1] = target;
			buffer[2] = (byte) value;
			if (mOutputStream != null && buffer[1] != -1) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}
		}
		
	    public void sendPress(char c) {
			Log.d(TAG, "sendPress hit: ");
			Log.d(TAG, String.valueOf(c));
	    	byte[] buffer = new byte[2];
			buffer[0] = (byte)'!';
			buffer[1] = (byte)c;
				
			if (mOutputStream != null) {
				try {
					mOutputStream.write(buffer);
				} catch (IOException e) {
					Log.e(TAG, "write failed", e);
				}
			}
		}
	    
		public boolean adkConnected() {
	    	//if(mInputStream != null && mOutputStream != null) return true;
	    	if(mFileDescriptor != null) return true;
	    	return false;
	    }
			
		private void enableControls(boolean b) {
			((ServiceADKApplication) getApplication()).setInputStream(mInputStream);
			((ServiceADKApplication) getApplication()).setOutputStream(mOutputStream);
			((ServiceADKApplication) getApplication()).setFileDescriptor(mFileDescriptor);
			((ServiceADKApplication) getApplication()).setUsbAccessory(mAccessory);
			updateTextViews();
			
			if(!b) {
				try {
		    		ADKService.self.stopUpdater();
				} catch(Exception e) {
					
				}
			}
			//sendPress('X');
		}
	    
	    private void updateTextViews() {

	    	Log.v(TAG, "updated text views");    	
	    }

		private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (ACTION_USB_PERMISSION.equals(action)) {
					synchronized (this) {
						UsbAccessory accessory = UsbManager.getAccessory(intent);
						if (intent.getBooleanExtra(
								UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
							openAccessory(accessory);
						} else {
							Log.d(TAG, "permission denied for accessory "
									+ accessory);
						}
						mPermissionRequestPending = false;
					}
				} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (accessory != null && accessory.equals(mAccessory)) {
						closeAccessory();
					}
				}
			}
		};
		
    //---- methods for setting fonts
    public static void setTextViewFont(Typeface tf, TextView...params) {
        for (TextView tv : params) {
            tv.setTypeface(tf);
        }
    } 
    public static void setEditTextFont(Typeface tf, EditText...params) {
        for (EditText tv : params) {
            tv.setTypeface(tf);
        }
    }  
    public static void setButtonFont(Typeface tf, Button...params) {
        for (Button tv : params) {
            tv.setTypeface(tf);
        }
    }
    
    //----- check if tablet is connected to internet!
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    //----- disable back button
	@Override
	public void onBackPressed() {
		//do nothing
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		//getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}
	
//	@Override
//	  public boolean onTouchEvent(MotionEvent event){
//		try {
//			
//			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//			Thread.sleep(250);
//			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return debug;
//	}
}
