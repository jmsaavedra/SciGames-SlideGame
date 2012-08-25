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

//import java.io.ByteArrayInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.net.URI;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.scigames.serverutils.DownloadNarrativeImage;
import com.scigames.serverutils.DownloadProfilePhoto;
import com.scigames.serverutils.SciGamesHttpPoster;
import com.scigames.serverutils.SciGamesListener;
import com.scigames.slidegame.ObjectiveActivity;
import com.scigames.slidegame.R;

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
public class ObjectiveActivity extends Activity implements SciGamesListener{
    private String TAG = "ObjectiveActivity";
    
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
    
    private String[] objectiveImg;
    
    private int objectiveImgNum = 0;
    private boolean showingImgs = false;
    private boolean debug = false;
    
//    TextView title;
//    TextView mLevel;
//    TextView mScore;
//    TextView mFabric;
//    TextView mAttempt;
    
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
    
    SciGamesHttpPoster task = new SciGamesHttpPoster(ObjectiveActivity.this,"http://mysweetwebsite.com/pull/slide_results.php");
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
//    	photoUrl = i.getStringExtra("photo");
//    	photoUrl = "http://mysweetwebsite.com/" + photoUrl;
    	Log.d(TAG,"...getStringExtra");
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.objective_page);
        Log.d(TAG,"...setContentView");
        objectiveImgNum = 0;
        
        //resultImg[0] = "http://mysweetwebsite.com/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        //resultImg[1] = "http://mysweetwebsite.com/narrative_images/Level0/results/_0012_Layer-Comp-13.png";
        
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
	    
        //display name and profile info
        Resources res = getResources();
//        title = (TextView)findViewById(R.id.title);
//        mLevel = (TextView)findViewById(R.id.level);
//        mScore = (TextView)findViewById(R.id.score);
//        mFabric = (TextView)findViewById(R.id.fabric);
//        mAttempt = (TextView)findViewById(R.id.attempt);
//        //greets.setText(String.format(res.getString(R.string.profile_name), firstNameIn, lastNameIn));
//        setTextViewFont(Museo700Regular, title);
//        setTextViewFont(Museo500Regular, mLevel, mScore, mFabric);

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
		   	task = new SciGamesHttpPoster(ObjectiveActivity.this,"http://mysweetwebsite.com/pull/objective_images.php"); //objective_images.php
		    //set listener
	        task.setOnResultsListener(ObjectiveActivity.this);
	        //prepare key value pairs to send
			//String[] keyVals = {"rfid", rfidIn}; 
	        String[] keyVals = {"slide_game_level", "0"}; 
			if(debug){
				//keyVals[0] = "rfid";
			    //keyVals[1] = "500315c37"; //tester
			}
			infoDialog.setTitle("rfidIn:");
			infoDialog.setMessage(rfidIn);
			infoDialog.show();
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
			infoDialog.setTitle("onResults Succeded: ");
			infoDialog.setMessage(serverResponseJSON.toString());
			infoDialog.show();
			
	     	//update all text fields
	     	Resources res = getResources();
	     	
	        //TextView greets = (TextView)findViewById(R.id.greeting);
//	     	mLevel.setText(String.format(res.getString(R.string.level), student[5]));
//	        mScore.setText(String.format(res.getString(R.string.score), slide_session[4]));
//	        mFabric.setText(String.format(res.getString(R.string.fabric), fabric[0]));
//	        mAttempt.setText(String.format(res.getString(R.string.attempt), slide_session[1]));
//	        
//	        setTextViewFont(Museo700Regular, title);
//	        setTextViewFont(Museo500Regular, mLevel, mScore, mFabric);
	        
	        objectiveImg = objective_images;
	        //scoreImg = score_images;
	        btnContinue.setVisibility(View.INVISIBLE);
    		reviewBtnNext.setVisibility(View.VISIBLE);
    		setCurrImg(objectiveImg[0]);
		}
	}
	

	
	private void setCurrImg(String imgURL){
		
		View thisView = findViewById(R.id.objective_page);
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
	    		btnContinue.setText("Continue");
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
    
    OnClickListener mContinue = new OnClickListener(){
	    public void onClick(View v) {
//	    	if(!showingImgs){
//	    		btnContinue.setVisibility(View.INVISIBLE);
//	    		reviewBtnNext.setVisibility(View.VISIBLE);
//	    		setCurrImg(objectiveImg[0]);
//	    		showingImgs = true;
//	    	} else {
				Intent i = new Intent(ObjectiveActivity.this, LoginActivity.class);
				Log.d(TAG,"new Intent");
				i.putExtra("studentId",studentIdIn);
				i.putExtra("page", "fabric");
				Log.d(TAG,"startActivity...");
				ObjectiveActivity.this.startActivity(i);
				Log.d(TAG,"...startActivity");
	    	//}
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



