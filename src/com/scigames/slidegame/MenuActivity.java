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

import com.scigames.slidegame.LoginActivity;
import com.scigames.serverutils.DownloadProfilePhoto;
import com.scigames.serverutils.SciGamesHttpPoster;
import com.scigames.serverutils.SciGamesListener;
import com.scigames.slidegame.MenuActivity;
import com.scigames.slidegame.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuActivity extends Activity implements SciGamesListener{
    private String TAG = "MenuActivity";
    private String baseDbURL = "http://db.scigam.es";
    
    private boolean debug = false;

    private String firstNameIn = "FNAME";
    private String lastNameIn = "LNAME";
    private String massIn = "MASS";
    private String studentIdIn = "STUDENTID";
    private String visitIdIn = "VISITID";
    private String rfidIn = "RFID";
    private String slideLevelIn = "SLIDELEVEL";
    private String photoUrl = "none";
    
    AlertDialog infoDialog;
    
    TextView greets;
    TextView fname;
    TextView lname;
    TextView schoolname;
    TextView teachername;
    TextView mpass;
    TextView classid;
    TextView classname;
    
    Typeface Museo500Regular;
    Typeface Museo700Regular;
    Typeface ExistenceLightOtf;
    
    Button playBtn;
    
    DownloadProfilePhoto photoTask = new DownloadProfilePhoto(MenuActivity.this, "sUrl");
    SciGamesHttpPoster task = new SciGamesHttpPoster(MenuActivity.this, baseDbURL+"/pull/objective_images.php");
    
    AlertDialog alertDialog;
    public MenuActivity() {
    	

    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    	Log.d(TAG,"super.OnCreate");
        Intent i = getIntent();
        Log.d(TAG,"getIntent");
        //if(!debug){
	    	firstNameIn = i.getStringExtra("fName");
	    	lastNameIn = i.getStringExtra("lName");;
	    	studentIdIn = i.getStringExtra("studentId");
	    	visitIdIn = i.getStringExtra("visitId");
	    	rfidIn = i.getStringExtra("rfid");
	    	photoUrl = i.getStringExtra("photo");
	    	photoUrl = baseDbURL+"/"+photoUrl;
	    	slideLevelIn = i.getStringExtra("slideLevel");
	    	massIn = i.getStringExtra("mass");
	    	Log.d(TAG, "slideLevelIn: " + slideLevelIn);
	    	Log.d(TAG,"...getStringExtra");
        //}
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.menu_page);
        Log.d(TAG,"...setContentView");
       
	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
	    //Typeface Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");

        displayProfile();
    }
    
    @Override
    protected void onNewIntent(Intent i){ //for the 2nd, 3rd, 4th... time we arrive at Menu Activity.
    	Log.d(TAG, "onNewIntent");
    	rfidIn = i.getStringExtra("rfid");
    	studentIdIn = i.getStringExtra("studentId");
    	slideLevelIn = i.getStringExtra("slideLevel");
    	massIn = i.getStringExtra("mass");
    	firstNameIn = i.getStringExtra("fName");
    	lastNameIn = i.getStringExtra("lName");;
    	visitIdIn = i.getStringExtra("visitId");
    	photoUrl = i.getStringExtra("photo");
    	photoUrl = baseDbURL+"/"+photoUrl;
    	Log.d(TAG, "Menu Activity INs: ");
    	Log.d(TAG, rfidIn + studentIdIn + slideLevelIn + massIn);
    	displayProfile();
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        Log.d(TAG,"...super.onResume()");
    }

	private void displayProfile(){
        setContentView(R.layout.menu_page);
        Log.d(TAG,"...setContentView");
       
	    
	    //display name and profile info
	    Resources res = getResources();
	    greets = (TextView)findViewById(R.id.student_name);
	    greets.setText(String.format(res.getString(R.string.profile_name), firstNameIn, lastNameIn));
	    setTextViewFont(Museo700Regular, greets); 
	    Log.d(TAG,"...Profile Info");
	
	    playBtn = (Button) findViewById(R.id.btn_play);
	    playBtn.setOnClickListener(mPlay);
	    setButtonFont(ExistenceLightOtf, playBtn);
	    
	    infoDialog = new AlertDialog.Builder(MenuActivity.this).create();
	    infoDialog.setTitle("Debug Info");
	    infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        }
	    });
	    
		if(debug){
	//		infoDialog.setTitle("studentId");
	//		infoDialog.setMessage(studentIdIn);
	//		infoDialog.show();
		}
	    
		task.setOnResultsListener(this);
		task.cancel(true);
	    task = new SciGamesHttpPoster(MenuActivity.this,baseDbURL+"/pull/return_profile.php");
	    task.setOnResultsListener(MenuActivity.this);
	    
		//download photo
	    ImageView profilePhoto = (ImageView) findViewById(R.id.profile_image);
	    profilePhoto.setTag(photoUrl);
	    profilePhoto.setScaleX(1.4f);
	    profilePhoto.setScaleY(1.4f);
	    profilePhoto.setX(120f);
	    profilePhoto.setY(123f);
	    photoTask.cancel(true);
	    photoTask = new DownloadProfilePhoto(MenuActivity.this, photoUrl);
	    //AsyncTask<ImageView, Void, Bitmap> pPhoto = 
	 	photoTask.execute(profilePhoto);
	}
	

    
    OnClickListener mPlay = new OnClickListener(){
	    public void onClick(View v) {
			Log.d(TAG,"mPlay.onClick");
			//startActivity(new Intent(ProfileActivity.this, Registration2RfidMass_AdkServiceActivity.class));
			Intent i = new Intent(MenuActivity.this, LoginActivity.class);
			Log.d(TAG,"new Intent");
			i.putExtra("studentId",studentIdIn);
			i.putExtra("rfid", rfidIn);
			i.putExtra("page", "objective");
			i.putExtra("slideLevel", slideLevelIn);
			i.putExtra("mass", massIn);
			Log.d(TAG,"startActivity...");
			MenuActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
		}
    };
    	

	public void failedQuery(String failureReason) {

		Log.d(TAG, "failedQuery in Profile Activity");
	}        


	@Override
	public void onResultsSucceeded(String[] student, String[] slide_session,
			String[] slide_level, String[] objective_images, String[] fabric,
			String[] result_images, String[] score_images, String attempts,
			boolean no_session, JSONObject serverResponseJSON) throws JSONException {
		
		massIn = student[7];
     	//update all text fields
     	Resources res = getResources();
     	
        //TextView greets = (TextView)findViewById(R.id.greeting);
     	setTextViewFont(Museo500Regular, greets);
        greets.setText(String.format(res.getString(R.string.profile_name), student[2], student[3]));
        setTextViewFont(Museo500Regular, greets);
        //setTextViewFont(Museo500Regular, teachername, classid, mpass, classname);
        
     	
        Log.d(TAG,"...Profile Info");
		
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



