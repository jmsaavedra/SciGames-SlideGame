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

import com.scigames.slidegame.DownloadProfilePhoto;
import com.scigames.slidegame.MenuActivity;
import com.scigames.slidegame.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.BitmapFactory.Options;
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
public class MenuActivity extends Activity implements SciGamesListener{
    private String TAG = "MenuActivity";
    
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
    
    private boolean debug = true;
    
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
    Button reviewBtn;
    
    DownloadProfilePhoto photoTask = new DownloadProfilePhoto(MenuActivity.this, "sUrl");
    SciGamesHttpPoster task = new SciGamesHttpPoster(MenuActivity.this,"mysweetwebsite.com/pull/objective_images.php");
    
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
        if(!debug){
	    	firstNameIn = i.getStringExtra("fName");
	    	lastNameIn = i.getStringExtra("lName");;
	    	studentIdIn = i.getStringExtra("studentId");
	    	visitIdIn = i.getStringExtra("visitId");
	    	rfidIn = i.getStringExtra("rfid");
	    	photoUrl = i.getStringExtra("photo");
	    	photoUrl = "http://mysweetwebsite.com/" + photoUrl;
	    	slideLevel = i.getStringExtra("slideLevel");
	    	Log.d(TAG,"...getStringExtra");
        } else {
	    	firstNameIn = "joe";
	    	lastNameIn = "saavedra";
	    	studentIdIn = "502d884fc0c0bad86e000001";
	    	visitIdIn = "502d8875c0c0bad86e000002";
	    	rfidIn = "500315c37";
	    	photoUrl = "student_images/502d882cc0c0bad76e000001/502d884fc0c0bad86e000001.jpg";
	    	photoUrl = "http://mysweetwebsite.com/" + photoUrl;
	    	slideLevel = "0";
        	
        }
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.menu_page);
        Log.d(TAG,"...setContentView");
       
        
        //display name and profile info
        Resources res = getResources();
        greets = (TextView)findViewById(R.id.student_name);
        greets.setText(String.format(res.getString(R.string.profile_name), firstNameIn, lastNameIn));
        setTextViewFont(Museo700Regular, greets); 

        Log.d(TAG,"...Profile Info");
	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
	    Typeface Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");
	    
        Log.d(TAG,"...instantiateButtons");
        playBtn = (Button) findViewById(R.id.btn_play);
        playBtn.setOnClickListener(mPlay);
        setButtonFont(ExistenceLightOtf, playBtn);
	    
        reviewBtn = (Button) findViewById(R.id.btn_review);
        reviewBtn.setOnClickListener(mReview);
        setButtonFont(ExistenceLightOtf, reviewBtn);
        
	    infoDialog = new AlertDialog.Builder(MenuActivity.this).create();
	    infoDialog.setTitle("Debug Info");
	    infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        // Write your code here to execute after dialog closed
	        }
	    });
        
		if(debug){
			infoDialog.setTitle("studentId");
			infoDialog.setMessage(studentIdIn);
			infoDialog.show();
		}
        
//      task.setOnResultsListener(this);
//		task.cancel(true);
//	    task = new SciGamesHttpPoster(MenuActivity.this,"http://mysweetwebsite.com/pull/return_profile.php");
//      task.setOnResultsListener(MenuActivity.this);
//        
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
        		
		//prepare key value pairs to send
		String[] keyVals = {"student_id", studentIdIn, "visit_id", visitIdIn}; 
		//create AsyncTask, then execute
		@SuppressWarnings("unused")
		AsyncTask<String, Void, JSONObject> serverResponse = null;
		serverResponse = task.execute(keyVals);
		Log.d(TAG,"...task.execute(keyVals)");
    }
        

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        Log.d(TAG,"...super.onResume()");
    }

    
    OnClickListener mPlay = new OnClickListener(){
	    public void onClick(View v) {
			Log.d(TAG,"mPlay.onClick");
			//startActivity(new Intent(ProfileActivity.this, Registration2RfidMass_AdkServiceActivity.class));
			Intent i = new Intent(MenuActivity.this, LoginActivity.class);
			Log.d(TAG,"new Intent");
			i.putExtra("studentId",studentIdIn);
			i.putExtra("page", "fabric");
			Log.d(TAG,"startActivity...");
			MenuActivity.this.startActivity(i);
			Log.d(TAG,"...startActivity");
		}
    };
    	
    OnClickListener mReview = new OnClickListener() {
        public void onClick(View v) {
			Log.d(TAG,"mReview.onClick");
			Intent i = new Intent(MenuActivity.this, ReviewActivity.class);
			Log.d(TAG,"new LoginActivity Intent");
			i.putExtra("rfid", rfidIn);
			i.putExtra("page", "slideReview");
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
		

     	//update all text fields
     	Resources res = getResources();
     	
        //TextView greets = (TextView)findViewById(R.id.greeting);
        greets.setText(String.format(res.getString(R.string.profile_name), student[2], student[3]));
  
        setTextViewFont(Museo500Regular, teachername, classid, mpass, classname);
        setTextViewFont(Museo700Regular, greets);
     	
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



