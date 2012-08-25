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
    
    private boolean debug = false;
    
	static final private int QUIT_ID = Menu.FIRST;
    static final private int BACK_ID = Menu.FIRST + 1;

    private String firstNameIn = "FNAME";
    private String lastNameIn = "LNAME";
    private String passwordIn = "PWORD";
    private String massIn = "MASS";
    private String emailIn = "EMAIL";
    private String classIdIn = "CLASSID";
    private String studentIdIn = "STUDENTID";
    private String visitIdIn = "VISITID";
    private String rfidIn = "RFID";
    private String slideLevel = "SLIDELEVEL";
    private String cartLevel = "CARTLEVEL";
    private String photoUrl = "none";
    
    private String[] resultImg;
    private String[] scoreImg;
    
    private int resultImgNum = 0;
    private int scoreImgNum = 0;
    private boolean showingImgs = false;//no touchie
    
    TextView title;
    TextView mLevel;
    TextView mScore;
    TextView mFabric;
    TextView mAttempt;
    
    Typeface Museo300Regular;
    Typeface Museo500Regular;
    Typeface Museo700Regular;
    Typeface ExistenceLightOtf;
    
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
//        mAnimationView.setNextButton((Button)findViewById(R.id.btn_next));
//        mAnimationView.setBackButton((Button)findViewById(R.id.btn_back));
        
        Log.d(TAG,"...setContentView");
        resultImgNum = 0;
        scoreImgNum = 0;
        
        //resultImg[0] = "http://mysweetwebsite.com/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        //resultImg[1] = "http://mysweetwebsite.com/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        
		alertDialog = new AlertDialog.Builder(ReviewActivity.this).create();
	    alertDialog.setTitle("No Registration System Attached ");
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
        mFabric = (TextView)findViewById(R.id.fabric);
        mAttempt = (TextView)findViewById(R.id.attempt);
        //greets.setText(String.format(res.getString(R.string.profile_name), firstNameIn, lastNameIn));
        setTextViewFont(Museo700Regular, title);
        setTextViewFont(Museo500Regular, mLevel, mScore, mFabric);

        Log.d(TAG,"...Profile Info");
	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
	    Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");
	    
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
			if(debug){
				keyVals[0] = "rfid";
			    keyVals[1] = "500315c37"; //tester
			}
//			infoDialog.setTitle("rfidIn:");
//			infoDialog.setMessage(rfidIn);
//			infoDialog.show();
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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        Log.d(TAG,"...super.onResume()");
    }

    

	public void failedQuery(String failureReason) {

		Log.d(TAG, "failedQuery in Profile Activity");
	}        


	@Override
	public void onResultsSucceeded(String[] student, String[] slide_session,
			String[] slide_level, String[] objective_images, String[] fabric,
			String[] result_images, String[] score_images, String attempts,
			boolean no_session, JSONObject serverResponseJSON) throws JSONException {
		
		if(no_session == true){
			needSlideDataDialog.setTitle("No Slide Data Today! ");
			needSlideDataDialog.setMessage("Go play on the slide before checking your last score!");
			needSlideDataDialog.show();
		} else {
//			infoDialog.setTitle("onResults Succeded: ");
//			infoDialog.setMessage(serverResponseJSON.toString());
//			infoDialog.show();
			
	     	//update all text fields
	     	Resources res = getResources();
	     	
	        //TextView greets = (TextView)findViewById(R.id.greeting);
	     	mLevel.setText(String.format(res.getString(R.string.level), student[5]));
	        mScore.setText(String.format(res.getString(R.string.score), slide_session[4]));
	        mFabric.setText(String.format(res.getString(R.string.fabric), fabric[0]));
	        mAttempt.setText(String.format(res.getString(R.string.attempt), slide_session[1]));
	        
	        setTextViewFont(Museo700Regular, title);
	        setTextViewFont(Museo500Regular, mLevel, mScore, mFabric);
	        
	        resultImg = result_images;
	        scoreImg = score_images;
		}
	}
	

	
	private void setCurrImg(String imgURL){
		
		//View thisView = findViewById(R.id.review_page);
		//setContentView(thisView);
//		for(int i=0; i<40; i++){
//			drawForceView.updateX();
//			
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		
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
		
		int ePotential = 0;
		int eThermal = 0;
		int eKinetic = 0;

		//token 6 wins
		if(tokens[6].equals("Score")){
			bgStr = tokens[7];
		} else {
			level = Integer.parseInt(String.valueOf(tokens[6].charAt(1)));
			scene = Integer.parseInt(String.valueOf(tokens[6].charAt(12)));
			if (tokens[6].substring(tokens[6].lastIndexOf('_') + 1).equals("a")){
				bgStr = tokens[6];	    		
				resultImgNum++; 
		    	gunStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "b");
		    	Log.d(TAG, "gunStr: "+ gunStr);
		    	resultImgNum++;
		    	pieceStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "c");
		    	Log.d(TAG, "pieceStr: "+ pieceStr);
		    	
		    	if(resultImgNum > 0)reviewBtnBack.setVisibility(View.VISIBLE);
				
			} else if (tokens[6].substring(tokens[6].lastIndexOf('_') + 1).equals("d")){
				bgStr = tokens[6];
				resultImgNum++; 
				rockStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "e");
		    	Log.d(TAG, "rockStr: "+ rockStr);
		    	resultImgNum++;
		    	drillStr = tokens[6].replace(tokens[6].substring(tokens[6].lastIndexOf('_') + 1), "f");
		    	Log.d(TAG, "drillStr: "+ drillStr);
			} else if(tokens[6].substring(tokens[6].lastIndexOf('_') + 1).equals("02")){
				ePotential = 25; //these will get set by things coming in from DB
				eThermal = 12;
				eKinetic = 8;
				bgStr = tokens[6];
			}
			
			else {
				bgStr = tokens[6];
			}
			
		}
		


		//Log.d(TAG, String.valueOf(R.drawable._1_objective_01));
		//mAnimationThread.s
		int thisBg = getResources().getIdentifier(bgStr, "drawable", getPackageName());
		//Drawable dThisImg = findViewById(getResources().getIdentifier(imgStr, "drawable", getPackageName()));
		Log.d(TAG, "BG: "+bgStr);
		mAnimationThread.setBackgroundResource(thisBg);//+tokens[4]);
		
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
    		reviewBtnNext.setVisibility(View.INVISIBLE);
    		btnContinue.setText("Done");
    		btnContinue.setVisibility(View.VISIBLE);
    		//setCurrImg(scoreImg[scoreImgNum]);
    	}
		if(eThermal+eKinetic >= 20)	mAnimationThread.setSlideEnergy(10, 12, 8);
		
		mAnimationThread.doStart();
//		thisView.setBackgroundResource(thisImg);
		//drawForceView.
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//---- if image does not exist, download it	
		//download photo
        //ImageView narrativeImg = (ImageView) findViewById(R.id.narrative_image);
        //narrativeImg.setTag(imgURL);
        //narrativeImg.setScaleX(1.4f);
        //narrativeImg.setScaleY(1.4f);
        //narrativeImg.setX(120f);
        //narrativeImg.setY(123f);
        //imgTask.cancel(true);
        //imgTask = new DownloadNarrativeImage(ReviewActivity.this, imgURL);
        //AsyncTask<ImageView, Void, Bitmap> pPhoto = 
        //imgTask.execute(narrativeImg);
//---- else if image does exist, set background.
		//thisView.setBackgroundResource(R.id.results_image_page);
	}
	
    OnClickListener mNext = new OnClickListener(){
	    public void onClick(View v) {
	    	Log.d(TAG, "mNexted");
	    	if(resultImgNum < resultImg.length){
	    		resultImgNum++; 
		    	Log.d(TAG, "curr resultImgNum" + String.valueOf(resultImgNum));
		    	Log.d(TAG, "setCurrImg: "+resultImg[resultImgNum]);
		    	setCurrImg(resultImg[resultImgNum]);
		    	if(resultImgNum > 0)reviewBtnBack.setVisibility(View.VISIBLE);
		    	
	    	} else if (resultImgNum >=resultImg.length && scoreImgNum<scoreImg.length-1 ){
	    		
	    		scoreImgNum++;
	    		setCurrImg(scoreImg[scoreImgNum]);
	    		Log.d(TAG, "setCurrImg: "+resultImg[resultImgNum]);
	    		
	    	} else { //last slide
	    		reviewBtnNext.setVisibility(View.INVISIBLE);
	    		btnContinue.setText("Done");
	    		btnContinue.setVisibility(View.VISIBLE);
	    		setCurrImg(scoreImg[scoreImgNum]);
	    	}
	    	
		}
    };
    
    OnClickListener mBack = new OnClickListener(){
	    public void onClick(View v) {
	    	reviewBtnNext.setVisibility(View.VISIBLE);
	    	if(resultImgNum > 0 && scoreImgNum <= 0){
	    		resultImgNum--; 
		    	setCurrImg(resultImg[resultImgNum]);
		    	Log.d(TAG, "setCurrImg: "+resultImg[resultImgNum]);
		    	if(resultImgNum == 0) reviewBtnBack.setVisibility(View.INVISIBLE);
		    	
	    	} else if (resultImgNum >= resultImg.length && scoreImgNum > 0){
	    		scoreImgNum--;
	    		setCurrImg(scoreImg[scoreImgNum]);
	    		Log.d(TAG, "setCurrImg: "+resultImg[resultImgNum-1]);
	    	} else {
//	    		reviewBtnNext.setText("Done");
//	    		setCurrImg(scoreImg[scoreImgNum]);
	    	}
	    	//mAnimationThread.doStart();
		}
    };
    
    OnClickListener mContinue = new OnClickListener(){
	    public void onClick(View v) {
	    	if(!showingImgs){
	    		btnContinue.setVisibility(View.INVISIBLE);
	    		reviewBtnNext.setVisibility(View.VISIBLE);
	    		setCurrImg(resultImg[0]);
	    		showingImgs = true;
	    		mLevel.setVisibility(View.INVISIBLE);
	    		mScore.setVisibility(View.INVISIBLE);
	    		mAttempt.setVisibility(View.INVISIBLE);
	    		mFabric.setVisibility(View.INVISIBLE);
	    	} else {
	    		Intent i = new Intent(ReviewActivity.this, LoginActivity.class);
	     		//i.putExtra("rfid",currRfid);;
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



