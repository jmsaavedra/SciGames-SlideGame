/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.scigames.slidegame;

import org.json.JSONException;
import org.json.JSONObject;

import com.scigames.serverutils.DownloadNarrativeImage;
import com.scigames.serverutils.DownloadProfilePhoto;
import com.scigames.serverutils.SciGamesHttpPoster;
import com.scigames.serverutils.SciGamesListener;
import com.scigames.slidegame.ReviewActivity;
import com.scigames.slidegame.R;
import com.scigames.slidegame.ReviewAnimationView.ReviewAnimationThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
//import android.net.Uri;
import android.os.Bundle;
//import android.view.KeyEvent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class ReviewActivity extends Activity implements SciGamesListener{
    private String TAG = "ReviewActivity";
    
    private boolean debug = true;
    
	static final private int QUIT_ID = Menu.FIRST;
    static final private int BACK_ID = Menu.FIRST + 1;

    private String rfidIn = "RFID";
    
    private boolean tooMuchOomph = false;
    private boolean levelCompleted = false;
    private String score = "SCORE";
    private float kineticToThermalRatioGoal = 0.0f;
    private float kineticToThermalRatioMade = 0.0f;
    private float thermalGoal;
    private float kineticGoal;
    private float thermalMade;
    private float kineticMade;
    private float potentialMade;
    //private boolean checkCompletion = false; /* temporary! */
    
    private String[] resultImg;
    private String[] scoreImg;
    
    private int resultImgNum = 0;
    private int scoreImgNum = 0;
    private boolean showingImgs = false;//no touchie
    
    TextView title;
    TextView mLevel;
    TextView mScore;
    //TextView mScoreFinal;
    TextView mFabric;
    TextView mAttempt;
    
    //typefaces will hold fonts
    Typeface Museo300Regular, Museo500Regular,Museo700Regular, ExistenceLightOtf;
    
    Button reviewBtnNext;
    Button reviewBtnBack;
    Button btnContinue;
    
    AlertDialog alertDialog;
    AlertDialog infoDialog;
    AlertDialog needSlideDataDialog;
    
    SciGamesHttpPoster task = new SciGamesHttpPoster(ReviewActivity.this,"http://mysweetwebsite.com/pull/slide_results.php");
    DownloadNarrativeImage imgTask = new DownloadNarrativeImage(ReviewActivity.this, "url");
    
    /** A handle to the thread that's actually running the animation. */
    private ReviewAnimationThread mAnimationThread;

    /** A handle to the View in which the game is running. */
    private ReviewAnimationView mAnimationView;
    
    public ReviewActivity() {
    	
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    	Log.d(TAG,"super.OnCreate");
        Intent i = getIntent();
        Log.d(TAG,"getIntent");
    	rfidIn = i.getStringExtra("rfid");
//    	photoUrl = i.getStringExtra("photo");
//    	photoUrl = "http://mysweetwebsite.com/" + photoUrl;
    	Log.d(TAG,"...getStringExtra");
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.review_page);
        mAnimationView = (ReviewAnimationView) findViewById(R.id.review);
        mAnimationThread = mAnimationView.getThread();

        //load fonts
	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
	    Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");
        
        Log.d(TAG,"...setContentView");
        resultImgNum = 0;
        scoreImgNum = 0;
        
        //resultImg[0] = "http://mysweetwebsite.com/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        //resultImg[1] = "http://mysweetwebsite.com/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        
		alertDialog = new AlertDialog.Builder(ReviewActivity.this).create();
	    alertDialog.setTitle("alert title");
	    alertDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        finish();
	        }
	    });
	    
	    infoDialog = new AlertDialog.Builder(ReviewActivity.this).create();
	    infoDialog.setTitle("Debug Info");
	    infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        }
	    });
	    
	    needSlideDataDialog = new AlertDialog.Builder(ReviewActivity.this).create();
	    needSlideDataDialog.setTitle("Debug Info");
	    needSlideDataDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(ReviewActivity.this, LoginActivity.class);
				Log.d(TAG,"new LoginActivity Intent");
				i.putExtra("page", "login");
				Log.d(TAG,"startActivity...");
				ReviewActivity.this.startActivity(i);
				Log.d(TAG,"...startActivity");
	        }
	    });
	    
        //display name and profile info
        Resources res = getResources();
        title = (TextView)findViewById(R.id.title);
        mLevel = (TextView)findViewById(R.id.level);
        mScore = (TextView)findViewById(R.id.score);
        //mScoreFinal = (TextView)findViewById(R.id.score_final);
        mFabric = (TextView)findViewById(R.id.fabric);
        mAttempt = (TextView)findViewById(R.id.attempt);
        //set their font
        setTextViewFont(Museo700Regular, title);
        setTextViewFont(Museo500Regular, mLevel, mScore, mFabric);
        //make them invisible to start
        title.setVisibility(View.INVISIBLE);
        mLevel.setVisibility(View.INVISIBLE);
        mScore.setVisibility(View.INVISIBLE);
        //mScoreFinal.setVisibility(View.INVISIBLE);
        mFabric.setVisibility(View.INVISIBLE);
        mAttempt.setVisibility(View.INVISIBLE);
        Log.d(TAG,"...Profile Info");
        	    
	    reviewBtnNext = (Button) findViewById(R.id.btn_next);
        reviewBtnNext.setOnClickListener(mNext);
        reviewBtnNext.setVisibility(View.INVISIBLE);
        reviewBtnBack = (Button) findViewById(R.id.btn_back);
        reviewBtnBack.setOnClickListener(mBack);
        reviewBtnBack.setVisibility(View.INVISIBLE);
        btnContinue = (Button) findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(mContinue);
        
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(ReviewActivity.this,"http://mysweetwebsite.com/pull/slide_results.php");
		    //set listener
	        task.setOnResultsListener(ReviewActivity.this);
	        //prepare key value pairs to send
			String[] keyVals = {"rfid", rfidIn}; 
			//create AsyncTask, then execute
			AsyncTask<String, Void, JSONObject> serverResponse = null;
			serverResponse = task.execute(keyVals);
	    } else {
			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
			alertDialog.show();
	    }
    }
        
    @Override
    protected void onNewIntent(Intent i){
    	showingImgs = false;
    	//checkCompletion = false; //temporary
    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(R.layout.review_page);
        mAnimationView = (ReviewAnimationView) findViewById(R.id.review);
        mAnimationThread = mAnimationView.getThread();
	    reviewBtnNext = (Button) findViewById(R.id.btn_next);
        reviewBtnNext.setOnClickListener(mNext);
        reviewBtnNext.setVisibility(View.INVISIBLE);
        reviewBtnBack = (Button) findViewById(R.id.btn_back);
        reviewBtnBack.setOnClickListener(mBack);
        reviewBtnBack.setVisibility(View.INVISIBLE);
        btnContinue = (Button) findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(mContinue);
        
//        mAnimationView = (ReviewAnimationView) findViewById(R.id.review);
//        mAnimationThread = mAnimationView.getThread();
        
        Log.d(TAG,"...setContentView");
        resultImgNum = 0;
        scoreImgNum = 0;
        
    	rfidIn = i.getStringExtra("rfid");
    	Log.d(TAG, "onNewIntent rfidIn: "+rfidIn);
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(ReviewActivity.this,"http://mysweetwebsite.com/pull/slide_results.php");
		    //set listener
	        task.setOnResultsListener(ReviewActivity.this);
	        //prepare key value pairs to send
			String[] keyVals = {"rfid", rfidIn};
			if(debug){
				infoDialog.setTitle("rfidIn:");
				infoDialog.setMessage(rfidIn);
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
    
    @Override
    protected void onResume() {
        super.onResume();
        showingImgs = false;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        Log.d(TAG,"...super.onResume()");
    }

    @Override
    protected void onPause(){
    	super.onPause();
    	mAnimationThread.destroySurface(); //need to kill animation thread and view
    }

	public void failedQuery(String failureReason) {

		Log.d(TAG, "failedQuery in Profile Activity");
	}        


	@Override
	public void onResultsSucceeded(String[] student, String[] slide_session,
			String[] slide_level, String[] objective_images, String[] fabric,
			String[] result_images, String[] score_images, String attempts,
			boolean no_session, JSONObject serverResponseJSON) throws JSONException {
		
		Log.d(TAG, ">>> slide_level results: ");
        for(int i=0; i<slide_level.length; i++){
        	Log.d(TAG, slide_level[i].toString());
        }
				
		Log.d(TAG, ">>> result_images: " + result_images.toString());
        for(int i=0; i<result_images.length; i++){
        	Log.d(TAG, result_images[i].toString());
        }
		
		if(no_session == true){
			needSlideDataDialog.setTitle("No Slide Data Today! ");
			needSlideDataDialog.setMessage("Go play on the slide before checking your last score!");
			needSlideDataDialog.show();
		} else {
			if(debug){
				infoDialog.setTitle("onResults Succeded: ");
				infoDialog.setMessage(serverResponseJSON.toString());
				infoDialog.show();
			}
			
	        //Set local variables for deciding and displaying some stuff
	        Log.d(TAG, "thermalGoal: "+slide_level[2]);
	        Log.d(TAG, "kineticGoal: "+slide_level[1]);
	        Log.d(TAG, "thermalMade: "+slide_session[6]);
	        Log.d(TAG, "kineticMade: "+slide_session[5]);
	        
	        //get all the slide session data!
	        score = slide_session[4];
	        levelCompleted = Boolean.parseBoolean(slide_session[3]);
	        thermalGoal = (float)Integer.parseInt(slide_level[2]);
	        kineticGoal = (float)Integer.parseInt(slide_level[1]);
	        thermalMade = Float.parseFloat(slide_session[6]); //5 in kin
	        kineticMade = Float.parseFloat(slide_session[5]);
	        potentialMade = Float.parseFloat(slide_session[7]);
	        
//	        if(levelCompleted && !checkCompletion){ //this is temporary!!
//	    	    if (isNetworkAvailable()){
//	    		    task.cancel(true);
//	    		    //create a new async task for every time you hit login (each can only run once ever)
//	    		   	task = new SciGamesHttpPoster(ReviewActivity.this,"http://mysweetwebsite.com/pull/slide_results.php");
//	    		    //set listener
//	    	        task.setOnResultsListener(ReviewActivity.this);
//	    	        //prepare key value pairs to send
//	    			String[] keyVals = {"rfid", rfidIn}; 
//	    			//create AsyncTask, then execute
//	    			AsyncTask<String, Void, JSONObject> serverResponse = null;
//	    			serverResponse = task.execute(keyVals);
//	    			checkCompletion = true;
//	    	    } else {
//	    			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
//	    			alertDialog.show();
//	    	    }
//	        }
			
	     	/** update all text fields **/
			//locate textViews in this layout
	     	Resources res = getResources();
	        title = (TextView)findViewById(R.id.title);
	        mLevel = (TextView)findViewById(R.id.level);
	        mScore = (TextView)findViewById(R.id.score);
	        //mScoreFinal = (TextView)findViewById(R.id.score_final);
	        mFabric = (TextView)findViewById(R.id.fabric);
	        mAttempt = (TextView)findViewById(R.id.attempt);
	     	
	        //Set the TextView values for first review page
	        mScore.setText(String.format(res.getString(R.string.score), score));
	        //mScoreFinal.setText(String.format(res.getString(R.string.score), score));
	        mFabric.setText(String.format(res.getString(R.string.fabric), fabric[0]));
	        //for the level and attempt we add 1 here bc the database starts at level 0.
	        mAttempt.setText(String.format(res.getString(R.string.attempt), String.valueOf(Integer.parseInt(slide_session[1])+1)));
	        mLevel.setText(String.format(res.getString(R.string.level), String.valueOf(Integer.parseInt(slide_session[2])+1))); 
	        
	        setTextViewFont(Museo700Regular, title);
	        setTextViewFont(Museo500Regular, mLevel, mScore, mFabric, mAttempt);
	        
	        //set them to visible
	        title.setVisibility(View.VISIBLE);
	        mLevel.setVisibility(View.VISIBLE);
	        mScore.setVisibility(View.VISIBLE);
	        //mScoreFinal.setVisibility(View.INVISIBLE);
	        mFabric.setVisibility(View.VISIBLE);
	        mAttempt.setVisibility(View.VISIBLE);
	                
	        //calculate ratios
	        kineticToThermalRatioGoal = ((float)kineticGoal/(float)thermalGoal);
	        kineticToThermalRatioMade = ((float)kineticMade/(float)thermalMade);
	        
	        if(kineticToThermalRatioGoal > kineticToThermalRatioMade){
	        	tooMuchOomph = false;
	        } else if (kineticToThermalRatioGoal < kineticToThermalRatioMade){
	        	tooMuchOomph = true;
	        }
	        
	        //print out everything we got and calculated.
	        Log.d(TAG, "score: "+score);
	        Log.d(TAG, "levelCompleted: "+levelCompleted);
	        Log.d(TAG, "thermalGoal: "+String.valueOf(thermalGoal));
	        Log.d(TAG, "kineticGoal: "+String.valueOf(kineticGoal));
	        Log.d(TAG, "thermalMade: "+String.valueOf(thermalMade));
	        Log.d(TAG, "kineticMade: "+String.valueOf(kineticMade));
	        Log.d(TAG, "kineticToThermalRatioGoal: "+String.valueOf(kineticToThermalRatioGoal));
	        Log.d(TAG, "kineticToThermalRatioMade: "+String.valueOf(kineticToThermalRatioMade));
	        Log.d(TAG, "tooMuchOomph: "+String.valueOf(tooMuchOomph));
	       
	        resultImg = result_images;
	        Log.d(TAG, ">>> RESULT_IMGS RECEIVED:");
	        for(int i=0; i<resultImg.length; i++){
	        	Log.d(TAG, resultImg[i].toString());
	        }
	        scoreImg = score_images;
	        Log.d(TAG, ">>> SCORE_IMGS RECEIVED:");
	        for(int i=0; i<scoreImg.length; i++){
	        	Log.d(TAG, scoreImg[i].toString());
	        }
		}
	}
	
	private void setCurrImg(String imgURL){
				
		Log.d(TAG, "imgURL: ");
		Log.d(TAG, imgURL);
		//setContentView(R.layout.results_image_page);
		String delims = "[/.]";
		String[] tokens = imgURL.split(delims);
		
		String bgStr = "null";
		
		String gunStr = "null";
		String pieceStr = "null";
		
		String rockStr = "null";
		String drillStr = "null";
		
		int level = 0;
		int scene = 0;
		
		int thisPot = 10;
		int thisTherm = 10;
		int thisKinetic = 10;
		
		boolean slideAnimStart = false;

		//token 6 wins
		if(tokens[6].equals("Score")){ //score scene (last one)
			bgStr = tokens[7];

		}
		
		else {
			level = Integer.parseInt(String.valueOf(tokens[6].charAt(1)));
			scene = Integer.parseInt(String.valueOf(tokens[6].charAt(12)));
			Log.d(TAG, "CURR SCENE: " + String.valueOf(scene));
			
			//--- sizzle gun scene
			if (tokens[6].substring(tokens[6].lastIndexOf('_') + 1).equals("a")){ 
				bgStr = tokens[6];	    		
				resultImgNum++; 
		    	gunStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "b");
		    	Log.d(TAG, "gunStr: "+ gunStr);
		    	resultImgNum++;
		    	pieceStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "c");
		    	Log.d(TAG, "pieceStr: "+ pieceStr);
		    	//if(resultImgNum > 0)reviewBtnBack.setVisibility(View.VISIBLE);
		    
		    //--- oomph drill scene
			} else if (tokens[6].substring(tokens[6].lastIndexOf('_') + 1).equals("d")){ 
				bgStr = tokens[6];
				resultImgNum++; 
				rockStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "e");
		    	Log.d(TAG, "rockStr: "+ rockStr);
		    	resultImgNum++;
		    	drillStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "f");
		    	Log.d(TAG, "drillStr: "+ drillStr);
		    
	    	//--- slide energy scene
			} else if(tokens[6].substring(tokens[6].lastIndexOf('_') + 1).equals("02")){ 
				bgStr = tokens[6];
				 thisPot = (int)(potentialMade/100);
				 thisTherm = (int)(thermalMade/100);
				 thisKinetic = (int)(kineticMade/100);
				
				Log.d(TAG, "thisPot: "+String.valueOf(thisPot));
				Log.d(TAG, "thisTherm: "+String.valueOf(thisTherm));
				Log.d(TAG, "thisKinetic: "+String.valueOf(thisKinetic));
				slideAnimStart = true;
			}
			
			//--- all other images
			else {	
				bgStr = tokens[6];
			}
		}
		
		//select the BG resource from the .pngs in the 'drawable' folder
		int thisBg = getResources().getIdentifier(bgStr, "drawable", getPackageName());
		mAnimationThread.setBackgroundResource(thisBg);
		
		if (slideAnimStart){
			mAnimationThread.setSlideEnergy(thisPot, thisTherm, thisKinetic);
			mAnimationThread.doStart();
			slideAnimStart = false;
		}
		if(!gunStr.equals("null")){
			int thisGunStr = getResources().getIdentifier(gunStr, "drawable", getPackageName());
			int thisPieceStr = getResources().getIdentifier(pieceStr, "drawable", getPackageName());
			Log.d(TAG, String.valueOf(level) + "  " + String.valueOf(scene) + "  " + gunStr + "  " + pieceStr);
			Log.d(TAG, String.valueOf(level) + "  " + String.valueOf(scene) + "  " + String.valueOf(thisGunStr) + "  " + String.valueOf(thisPieceStr));
			mAnimationThread.setLevelScene(level, scene, thisGunStr, thisPieceStr);
		}
		else if(!rockStr.equals("null")){
			int thisRockStr = getResources().getIdentifier(rockStr, "drawable", getPackageName());
			int thisDrillStr = getResources().getIdentifier(drillStr, "drawable", getPackageName());
			Log.d(TAG, String.valueOf(level) + "  " + String.valueOf(scene) + "  " + rockStr + "  " + drillStr);
			Log.d(TAG, String.valueOf(level) + "  " + String.valueOf(scene) + "  " + String.valueOf(thisRockStr) + "  " + String.valueOf(thisDrillStr));
			mAnimationThread.setLevelScene(level, scene, thisRockStr, thisDrillStr);
		}
		
		if(scene == 4){
			
    		//reviewBtnNext.setVisibility(View.INVISIBLE);
    		//btnContinue.setVisibility(View.VISIBLE);
    	}
	}
	
    OnClickListener mNext = new OnClickListener(){
	    public void onClick(View v) {
	    	Log.d(TAG, "mNexted");
	    	resultImgNum++; 
	    	if(resultImgNum <= (resultImg.length-3)){ //-3 accounts for the 4 extra rock and gun images	
		    	Log.d(TAG, "curr resultImgNum" + String.valueOf(resultImgNum));
		    	Log.d(TAG, "setCurrImg: "+resultImg[resultImgNum]);
		    	Log.d(TAG, "result img #: "+resultImgNum + "out of: " + String.valueOf(resultImg.length));
		    	setCurrImg(resultImg[resultImgNum]);
		    	//if(resultImgNum > 0)reviewBtnBack.setVisibility(View.VISIBLE);
		    	
	    		
	    	} else { //show score!
	    		if(levelCompleted){
	    			setCurrImg(scoreImg[0]);
	    			mScore.setVisibility(View.VISIBLE);
	    		} else if (tooMuchOomph){
	    			setCurrImg(scoreImg[1]);
	    		} else if (!levelCompleted && !tooMuchOomph){
	    			setCurrImg(scoreImg[2]);
	    		}
	    		reviewBtnNext.setVisibility(View.INVISIBLE);
	    		//btnContinue.setText("Done");
	    		btnContinue.setX(35);
	    		btnContinue.setY(570);
	    		btnContinue.setVisibility(View.VISIBLE);
				mScore.setX(1060);
				mScore.setY(647);
				mScore.setHeight(50);
				mScore.setVisibility(View.VISIBLE);
	    		//setCurrImg(scoreImg[scoreImgNum]);
	    	}
	    	
		}
    };
    
    OnClickListener mBack = new OnClickListener(){
	    public void onClick(View v) {
	    	reviewBtnNext.setVisibility(View.VISIBLE);
	    	btnContinue.setVisibility(View.INVISIBLE);
	    	if(resultImgNum > 1){
	    		resultImgNum--;
	    		if(resultImgNum == 3 || resultImgNum == 4)
	    			resultImgNum = 2;
	    		else if(resultImgNum == 6 || resultImgNum == 7)
	    			resultImgNum = 5;
		    	setCurrImg(resultImg[resultImgNum]);
		    	Log.d(TAG, "setCurrImg: "+resultImg[resultImgNum]);
		    	if(resultImgNum == 1) reviewBtnBack.setVisibility(View.INVISIBLE);
		    	
	    	}
		}
    };
    
    OnClickListener mContinue = new OnClickListener(){
	    public void onClick(View v) {
	    	Log.d(TAG, "mContinue .onClick!");
	    	Log.d(TAG, "showingImgs: "+String.valueOf(showingImgs));
	    	if(debug){
		    	infoDialog.setTitle("mContinue .onClick!");
		    	infoDialog.setMessage("showingImgs: "+String.valueOf(showingImgs));
		    	infoDialog.show();
		    }
	    	if(!showingImgs){
	    		setCurrImg(resultImg[0]); // show he first result image
	    		reviewBtnNext.setVisibility(View.VISIBLE); //show review button
	    		btnContinue.setVisibility(View.INVISIBLE); //hide several buttons
	    		mLevel.setVisibility(View.INVISIBLE);
	    		mScore.setVisibility(View.INVISIBLE);
	    		mAttempt.setVisibility(View.INVISIBLE);
	    		mFabric.setVisibility(View.INVISIBLE);
	    		showingImgs = true;
	    	} else {
	    		//mAnimationView.surfaceDestroyed(null);
	    		mAnimationThread.destroySurface();
	    		Intent i = new Intent(ReviewActivity.this, LoginActivity.class);
	     		i.putExtra("page","login");
	     		Log.d(TAG,"startActivity...");
	     		ReviewActivity.this.startActivity(i);
	    	}
		}
    };
    	
//    OnClickListener mReview = new OnClickListener() {
//        public void onClick(View v) {
////			Log.d(TAG,"mReview.onClick");
////			Intent i = new Intent(ReviewActivity.this, LoginActivity.class);
////			Log.d(TAG,"new LoginActivity Intent");
////			i.putExtra("rfid", rfidIn);
////			i.putExtra("page", "slideReview");
////			Log.d(TAG,"startActivity...");
////			ReviewActivity.this.startActivity(i);
////			Log.d(TAG,"...startActivity");
//        }
//    };

	
    //----- check if tablet is connected to internet!
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
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
	@Override
	public void onBackPressed() {
		//do nothing
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		        
	}
}



