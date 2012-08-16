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
import com.scigames.slidegame.LoginActivity;
import com.scigames.slidegame.SciGamesHttpPoster;
import com.scigames.slidegame.R;

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
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements Runnable, SciGamesListener{
    /** Called when the activity is first created. */
    
	private static final String TAG = "LoginActivity";
	
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
    private String firstName = "FNAME";
    private String lastName = "LNAME";
    private String classId = "CLASSID";
    private String slideSessionIdIn = "null";
    
    private String currPage = "login";
    private String currRfid = "";
    
    private boolean debug = true;
    private boolean moveOn = false;

    TextView greets;
    TextView fabricId;
    TextView slideData;
    TextView slideInstructions;
    TextView debuggin;
    
    Button sendFakeSlideData; //for testing
    Button playBtn;
    
    AlertDialog alertDialog;
    AlertDialog infoDialog;
    
    Typeface ExistenceLightOtf;
    Typeface Museo300Regular;
    Typeface Museo500Regular;
    Typeface Museo700Regular;
    
    SciGamesHttpPoster task = new SciGamesHttpPoster(LoginActivity.this, "http://mysweetwebsite.com/pull/auth_rfid.php");
	
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
	        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        finish();
	        }
	    });
	    
	    infoDialog = new AlertDialog.Builder(LoginActivity.this).create();
	    infoDialog.setTitle("Debug Info");
	    infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        moveOn = true;
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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    	
	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
	    Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");
	    
    	Resources res = getResources();
    	setContentView(R.layout.login_page);

        greets = (TextView)findViewById(R.id.welcome);
        setTextViewFont(Museo700Regular, greets);
        Log.d(TAG,"...Greetings");
        	    
        task.setOnResultsListener(this);
        //sendPress('X');
        
        Log.d(TAG, "...end OnCreate");
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
        Log.d(TAG,"getIntent");
        Log.d(TAG,"values in:");
//          if(intent.hasExtra("studentId")){
//	          studentId = intent.getExtras().getString("studentId");
//	          infoDialog.setTitle("data in:");
//	          infoDialog.setMessage(studentId);
//	          infoDialog.show();
//          }
          if(intent.hasExtra("page")){
        	  if(intent.getExtras().getString("page").equals("login")){
        		  currPage = "login";
        		  setContentView(R.layout.login_page);
        		  currRfid = "";
        		  
        	  } else if(intent.getExtras().getString("page").equals("fabric")){
	        	  currPage = "fabric";
	        	  setContentView(R.layout.fabric_page);
	              fabricId = (TextView)findViewById(R.id.fabric_id);
	              playBtn = (Button)findViewById(R.id.btn_play);
	              playBtn.setOnClickListener(mSlidePage);
        	      setTextViewFont(ExistenceLightOtf, playBtn);
	              Log.d(TAG,"...fabricId");
        	  } else if (intent.getExtras().getString("page").equals("slideturn")){
        		  currPage = "sliding";
        		  setContentView(R.layout.slide_page);
        		  slideData = (TextView)findViewById(R.id.slide_data);
        		  slideInstructions = (TextView)findViewById(R.id.slide_instructions);
        		  //setTextViewFont(Museo700Regular, slideData, slideInstructions);  
        		  
        		  slideSessionIdIn = intent.getExtras().getString("slidesessionId");
        		  infoDialog.setTitle("slideSessionIdIn: ");
    	          infoDialog.setMessage(slideSessionIdIn);
    	          infoDialog.show();
    	          
        		  debuggin = (TextView)findViewById(R.id.debug);
        		  debuggin.setText("created new LoginActivity");
        	      sendFakeSlideData = (Button) findViewById(R.id.btn_sendfakedata);
        	      sendFakeSlideData.setOnClickListener(mSlideDataGo);
        	      //setButtonViewFont(ExistenceLightOtf, sendFakeSlideData);
        	  } else if (intent.getExtras().getString("page").equals("slideReview")){
        		  currPage = "slideReview";
        		  currRfid = intent.getExtras().getString("rfid");
	       		  Intent i = new Intent(LoginActivity.this, ReviewActivity.class);
	     		  Log.d(TAG,"new Intent");
	     		  i.putExtra("rfid",currRfid);;
	     		  Log.d(TAG,"startActivity...");
	     		  LoginActivity.this.startActivity(i);
	     		  Log.d(TAG,"...startActivity");

        	  }
         } 
    } 
    
    @Override
	public void onResume() {
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
			setContentView(R.layout.no_device);
		}
		// Let's update the textviews for easy debugging here...
		updateTextViews();
	}
    
    @Override
	public void onPause() {
    	Log.v(TAG, "onPause");
    	//closeAccessory();
    	try {
    		ADKService.self.startUpdater();
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
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;
		int iValue = 0;
		String thisBracelet = "";
		int thisMassR = 0;
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
			while (i < ret) {
				int len = ret - i;
				Log.v(TAG, "Read: " + buffer[i]);	
				final int val = (int)buffer[i];
				byte[] value = new byte [ret];
				value[i] = (byte)buffer[i];
				iValue = (int)buffer[i];
				if(i == 0 && iValue == 111){
					thisMassR = buffer[1] & 0xFF;
				}
				Log.d(TAG, "buffer: "+String.valueOf(i));
				Log.d(TAG, Integer.toHexString(iValue));
				thisBracelet = thisBracelet.concat(Integer.toHexString(iValue));
				Log.d(TAG, "thisBracelet: "+thisBracelet);
				
				i++;
			}	
			final String fThisBracelet = thisBracelet;
			mHandler.post(new Runnable() {
				
				public void run() {
	            	// This gets executed on the UI thread so it can safely modify Views
					if(currPage.equals("fabric")){
						currRfid = fThisBracelet;
						fabricId.setText(fThisBracelet);
						sendFabricId(fThisBracelet);
					} else if(currPage.equals("sliding")){
						//this is where slide sensor data is received!
						
					} else {
						greets.setText(fThisBracelet);
						checkBraceletId(fThisBracelet);
					}
				}
			});
//			switch (buffer[i]) {
//				default:
//					Log.d(TAG, "unknown msg: " + buffer[i]);
//					i = len;
//					break;
//				}
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
		
	// --------------
	// User interface
	// --------------
	
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
		sendPress('X');
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
	
	/********* server querying ***********/
	
	private void checkBraceletId(String braceletId){
		Log.d(TAG, "hit checkBraceletId()");
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(LoginActivity.this,"http://mysweetwebsite.com/pull/auth_rfid.php");
		    //set listener
	        task.setOnResultsListener(LoginActivity.this);
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
		Log.d(TAG, "hit checkBraceletId()");
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(LoginActivity.this,"http://mysweetwebsite.com/pull/slide_session.php");
		    //set listener
	        task.setOnResultsListener(LoginActivity.this);
	        //prepare key value pairs to send
			String thisBracelet = braceletId.toLowerCase();
			String[] keyVals = {"student_id", studentId, "fabric_rfid", thisBracelet}; 
			infoDialog.setTitle("keyVals:");
			infoDialog.setMessage(keyVals[0]+keyVals[1]+keyVals[2]+keyVals[3]);
			infoDialog.show();
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
		   	task = new SciGamesHttpPoster(LoginActivity.this,"http://mysweetwebsite.com/push/update_slide_data.php");
		    //set listener
	        task.setOnResultsListener(LoginActivity.this);
	        
			//prepare key value pairs to send
			String[] keyVals = {"slide_length",slideDataIn[0] , "slide_angle",slideDataIn[1] ,"start_time",slideDataIn[2],
					"end_time",slideDataIn[3] ,"total_time",slideDataIn[4] ,"score",slideDataIn[5] ,"kinetic",slideDataIn[6],
					"potential",slideDataIn[7] ,"thermal",slideDataIn[8] ,"attempt",slideDataIn[9],"level_completed",slideDataIn[10],
					"slide_session_id", slideDataIn[11]}; 
	        
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
 			Log.d(TAG,"mDone.onClick");
 			//startActivity(new Intent(ProfileActivity.this, Registration2RfidMass_AdkServiceActivity.class));
 			Intent i = new Intent(LoginActivity.this, LoginActivity.class);
 			Log.d(TAG,"new Intent");
 			i.putExtra("slidesessionId",slideSessionIdIn);
 			i.putExtra("page", "slideturn");
 			Log.d(TAG,"startActivity...");
 			LoginActivity.this.startActivity(i);
 			Log.d(TAG,"...startActivity");
 		}
     };
     
     OnClickListener mSlideDataGo = new OnClickListener(){
  	    public void onClick(View v) {
  	    	
  	    	
  	    	String[] thisData = {"5","30","250", "150", "955","1415","55","101","45","1","true",slideSessionIdIn};

//  	    	thisData[0] = "5"; 		//'slide_length'
//  	    	thisData[1] = "30"; 	//'slide_angle'
//  	    	thisData[2] = "250"; 	//'start_time'
//  	    	thisData[3] = "150"; 	//'end_time'
//  	    	thisData[4] = "955"; 	//'total_time'
//  	    	thisData[5] = "1205"; 	//'score'
//  	    	thisData[6] = "55"; 	//'kinetic'
//  	    	thisData[7] = "101";	//'potential'
//  	    	thisData[8] = "45"; 	//'thermal'
//  	    	thisData[9] = "1"; 		//'attempt'
//  	    	thisData[10] = "false"; //'level_completed'
//  	    	thisData[11] = slideSessionIdIn;
  	    	debuggin.setText("...setThisData");
//  	    	String thisDataFull = null;
//  	    	for(int i=0; i<thisData.length; i++){
//  	    		thisDataFull = thisDataFull + thisData[i-1];
//  	    	}
//			infoDialog.setTitle("got through slideDataGo");
//			infoDialog.setMessage("holy crap");
//			infoDialog.show();
  	    	slideData.setText("sending: "+thisData[0]+thisData[1]+thisData[2]+thisData[3]+thisData[4]+thisData[5]+thisData[6]
  	    			+thisData[7]+thisData[8]+thisData[9]+thisData[10]+thisData[11]);
  	    	sendSlideData(thisData);
  		}
      };


	@Override
	public void onResultsSucceeded(String[] student, String[] slide_session,
			String[] slide_level, String[] objective_images, String[] fabric,
			String[] result_images, String[] score_images, String attempts,
			JSONObject serverResponseJSON) throws JSONException {
		if (currPage.equals("login")){
	   		Intent i = new Intent(LoginActivity.this, MenuActivity.class);
			Log.d(TAG,"new Intent");
			i.putExtra("fName", student[2]);
			i.putExtra("lName", student[3]);
			i.putExtra("studentId",student[0]);
			i.putExtra("visitId",student[1]);
			i.putExtra("photo",student[4]);
			i.putExtra("slideLevel",student[5]);
			i.putExtra("rfid",student[6]);
			Log.d(TAG,"startActivity...");
			LoginActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
		}
		else if(currPage.equals("fabric")){			
			fabricId.setText(serverResponseJSON.toString());
			slideSessionIdIn = slide_session[0];
			infoDialog.setTitle("onResults from fabric update slidesessionid: ");
			infoDialog.setMessage(slideSessionIdIn);
			infoDialog.show();
		} 
		else if(currPage.equals("sliding")) {
			slideInstructions.setText(serverResponseJSON.toString());
			infoDialog.setTitle("SlideData ResultsSucceeded");
			infoDialog.setMessage(serverResponseJSON.toString());
			infoDialog.show();
			
	   		Intent i = new Intent(LoginActivity.this, LoginActivity.class);
			Log.d(TAG,"new Intent");
			i.putExtra("page", "login");
			Log.d(TAG,"startActivity...");
			LoginActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
		}
		else if(currPage.equals("slideReview")){
			infoDialog.setTitle("slideReview ResultsSucceeded");
			infoDialog.setMessage(serverResponseJSON.toString());
			infoDialog.show();
		}
	}
	
	public void failedQuery(String failureReason) {

		Log.d(TAG, "LOGIN FAILED, REASON: " + failureReason);
		infoDialog.setTitle("failed query");
		infoDialog.setMessage(failureReason);
		infoDialog.show();
		onResume();
	}
	
	
    //---- methods for setting fonts!!
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
        
	}
}
