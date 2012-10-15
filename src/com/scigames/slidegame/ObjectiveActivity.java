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
import com.scigames.serverutils.SciGamesHttpPoster;
import com.scigames.serverutils.SciGamesListener;
import com.scigames.slidegame.ObjectiveActivity;
import com.scigames.slidegame.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ObjectiveActivity extends Activity implements SciGamesListener{
    private String TAG = "ObjectiveActivity";
    
    private boolean debug = false;
    

    private String studentIdIn = "STUDENTID";
    private String rfidIn = "RFID";
    private String slideLevelIn = "SLIDELEVEL";
    
    private String[] objectiveImg;
    
    protected int objectiveImgNum = 0;
    
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
    
    SciGamesHttpPoster task = new SciGamesHttpPoster(ObjectiveActivity.this,"http://db.scigam.es/pull/slide_results.php");
    DownloadNarrativeImage imgTask = new DownloadNarrativeImage(ObjectiveActivity.this, "url");
    
    public ObjectiveActivity() {
    	
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
    	studentIdIn = i.getStringExtra("studentId");
    	slideLevelIn = i.getStringExtra("slideLevel");
//    	photoUrl = i.getStringExtra("photo");
//    	photoUrl = "http://db.scigam.es/" + photoUrl;
    	Log.d(TAG,"...getStringExtra");
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.objective_page);
        Log.d(TAG,"...setContentView");
        objectiveImgNum = 0;
        
        //resultImg[0] = "http://db.scigam.es/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        //resultImg[1] = "http://db.scigam.es/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        
		alertDialog = new AlertDialog.Builder(ObjectiveActivity.this).create();
	    alertDialog.setTitle("No Registration System Attached ");
	    alertDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        finish();
	        }
	    });
	    
	    infoDialog = new AlertDialog.Builder(ObjectiveActivity.this).create();
	    infoDialog.setTitle("Debug Info");
	    infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
	        }
	    });
	    
	    needSlideDataDialog = new AlertDialog.Builder(ObjectiveActivity.this).create();
	    needSlideDataDialog.setTitle("Debug Info");
	    needSlideDataDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(ObjectiveActivity.this, LoginActivity.class);
				Log.d(TAG,"new LoginActivity Intent");
				i.putExtra("page", "login");
				Log.d(TAG,"startActivity...");
				ObjectiveActivity.this.startActivity(i);
				Log.d(TAG,"...startActivity");
	        }
	    });

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
        
        getObjectiveImages(slideLevelIn);
    }
    
    private void getObjectiveImages(String mSlideLevelIn){
	    if (isNetworkAvailable()){
		    task.cancel(true);
		    //create a new async task for every time you hit login (each can only run once ever)
		   	task = new SciGamesHttpPoster(ObjectiveActivity.this,"http://db.scigam.es/pull/objective_images.php"); //objective_images.php
		    //set listener
	        task.setOnResultsListener(ObjectiveActivity.this);
	        //prepare key value pairs to send
			//String[] keyVals = {"rfid", rfidIn}; 
	        String[] keyVals = {"slide_game_level", mSlideLevelIn}; 
			if(debug){
				infoDialog.setTitle("rfidIn:");
				infoDialog.setMessage(rfidIn);
				infoDialog.show();
			}

			//create AsyncTask, then execute
			@SuppressWarnings("unused")
			AsyncTask<String, Void, JSONObject> serverResponse = null;
			serverResponse = task.execute(keyVals);
	    } else {
			alertDialog.setMessage("You're not connected to the internet. Make sure this tablet is logged into a working Wifi Network.");
			alertDialog.show();
	    }
    }
    
    @Override
    protected void onNewIntent(Intent i){
    	View thisView = findViewById(R.id.objective_page); //find view
		setContentView(thisView);
    	thisView.setBackgroundResource(R.drawable.bg_blank);
    	objectiveImgNum = 0;
    	objectiveImg = null;
    	if(i.getExtras().getString("slideLevel") != null){
    		Log.d(TAG, "onNewIntent!");
        	rfidIn = i.getStringExtra("rfid");
        	studentIdIn = i.getStringExtra("studentId");
        	slideLevelIn = i.getStringExtra("slideLevel");
        	
    		String thisSlideLevel = i.getExtras().getString("slideLevel");
    		getObjectiveImages(slideLevelIn);
    	} 
    
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        objectiveImgNum = 0;
//        objectiveImg = null;
//        Log.d(TAG, "objectiveImgNum: "+ String.valueOf(objectiveImgNum));
//        Log.d(TAG,"Objective Activity..super.onResume()");
//        Log.d(TAG,"currSlideLevel: "+String.valueOf(slideLevelIn));
//        
//    }

    

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
			if(debug){
				infoDialog.setTitle("onResults Succeded -ObjActivity: ");
				infoDialog.setMessage(serverResponseJSON.toString());
				infoDialog.show();
			}
			     	
	        objectiveImg = objective_images;
	        //scoreImg = score_images;
	        if(objectiveImg.length>1){
		        btnContinue.setVisibility(View.INVISIBLE);
	    		reviewBtnNext.setVisibility(View.VISIBLE);
	        } else { //if there is just 1 objective image!
	        	btnContinue.setVisibility(View.VISIBLE);
	    		reviewBtnNext.setVisibility(View.INVISIBLE);
	        }
	        Log.d(TAG, "about to set: objectiveImg");
	        Log.d(TAG, "objectiveImgNum: "+ String.valueOf(objectiveImgNum));
    		setCurrImg(objectiveImg[objectiveImgNum]);
		}
	}
	

	
	private void setCurrImg(String imgURL){
		
		View thisView = findViewById(R.id.objective_page); //find view
		setContentView(thisView);
		Log.d(TAG, "imgURL: ");
		Log.d(TAG, imgURL);
		//setContentView(R.layout.results_image_page);
		String delims = "[/.]";
		String[] tokens = imgURL.split(delims);
		String imgStr;
		if(tokens[6].equals("Score")){
			imgStr = tokens[7];
		} else imgStr = tokens[6];
		//token 6 wins
		Log.d(TAG, tokens[6]);
		int thisImg = getResources().getIdentifier(imgStr, "drawable", getPackageName());
		Log.d(TAG, String.valueOf(thisImg));
		//Log.d(TAG, String.valueOf(R.drawable._1_objective_01));
		thisView.setBackgroundResource(thisImg);//+tokens[4]);
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

	    	if(objectiveImgNum < objectiveImg.length-2){
	    		objectiveImgNum++; 
		    	setCurrImg(objectiveImg[objectiveImgNum]);
		    	Log.d(TAG, String.valueOf(objectiveImgNum));
		    	Log.d(TAG, String.valueOf(objectiveImg.length));
		    	if(objectiveImgNum > 0)reviewBtnBack.setVisibility(View.VISIBLE);
	    	} else {
	    		objectiveImgNum++; 
	    		reviewBtnNext.setVisibility(View.INVISIBLE);
	    		btnContinue.setVisibility(View.VISIBLE);
	    		setCurrImg(objectiveImg[objectiveImgNum]);
	    	}
		}
    };
    
    OnClickListener mBack = new OnClickListener(){
	    public void onClick(View v) {
	    	reviewBtnNext.setVisibility(View.VISIBLE);
	    	if(objectiveImgNum > 0){
	    		objectiveImgNum--; 
		    	setCurrImg(objectiveImg[objectiveImgNum]);
		    	if(objectiveImgNum == 0) reviewBtnBack.setVisibility(View.INVISIBLE);
	    	}
		}
    };
    
    OnClickListener mContinue = new OnClickListener(){ //same img as mNext, but different intent
	    public void onClick(View v) {
//	    	View thisView = findViewById(R.id.objective_page); //find view
//			setContentView(thisView);
//	    	thisView.setBackgroundResource(R.drawable.bg_blank);
	    	Log.d(TAG, "leaving ObjectiveActivity, to fabric through login:");
	    	Log.d(TAG, "this studentId: "+studentIdIn);
	    	Log.d(TAG, "this slideLevel: "+ slideLevelIn);
	    	Log.d(TAG,  "this rfid: "+ rfidIn);
	    	
				Intent i = new Intent(ObjectiveActivity.this, LoginActivity.class);
				Log.d(TAG,"new Intent");
				i.putExtra("studentId",studentIdIn);
				i.putExtra("page", "fabric");
				i.putExtra("slideLevel", slideLevelIn);
				i.putExtra("rfid", rfidIn);
				Log.d(TAG,"startActivity...");
				ObjectiveActivity.this.startActivity(i);
				Log.d(TAG,"...startActivity");
		}
    };
	
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



