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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.scigames.slidegame.LoginActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class SciGamesHttpPoster extends AsyncTask <String, Void, JSONObject> {

    private String TAG = "ScGamesHttpPoster";
    private String firstKey = "";
    private String secondKey = "";
    private String firstValue = "";
    private String secondValue = "";
    private String thirdKey = "";
    private String thirdValue = "";
    private String thisPostAddress = "";
    
    public JSONObject serverResponse = null;
    static InputStream is = null;
    static String json = "";
    public String failureReason = ""; //holds any explanations for failure
    public String[] parsedLoginInfo;
    
    
    static public Activity MyActivity;

    SciGamesListener listener;  
    
    SciGamesHttpPoster(Activity a, String addr){
    	thisPostAddress = addr;
    	MyActivity = a;
    }
    
	public void run() {
		// TODO Auto-generated method stub
	}
	
    public void setOnResultsListener(SciGamesListener listener) {
        this.listener = listener;
    }
	
    protected void onPreExecute (){
//    	ProgressDialog dialog ;
//    	dialog = ProgressDialog.show(Activity.activity ,"title","message");
    	
   }
	
	@Override
	protected JSONObject doInBackground (String... keyVals){
		Log.d(TAG, "keyVal.length: ");
		Log.d(TAG, String.valueOf(keyVals.length));
		Log.d(TAG, "keyVals: ");
		Log.d(TAG, "keyVals[0] "+ keyVals[0]);
		Log.d(TAG, "keyVals[1] "+ keyVals[1]);
		
	    String[] key = new String[keyVals.length/2];
	    String[] val = new String[keyVals.length/2];
	    int count = 0;
		for(int i=0; i < keyVals.length/2; i++){
			Log.d(TAG, "i ="+ String.valueOf(i));
			key[i] = keyVals[count];
			val[i] = keyVals[count+1];
			count+=2;
			Log.d(TAG, key[i] +":"+ val[i]);
		}    	
    	try{
        	JSONObject thisResponse=null;
        	Log.d(TAG, "...doInBackground (String... keyVals)");
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(thisPostAddress);//("http://requestb.in/xurt8kxu");
            Log.d(TAG, "...create POST");
            
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            for(int i=0; i < keyVals.length/2; i++){
            	nameValuePairs.add(new BasicNameValuePair(key[i], val[i]));
            }
            
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d(TAG, "...setEntity");
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "...executed");
            //Log.d(TAG, response.toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            Log.d(TAG, "...BufferedReader");
            String json = reader.readLine();
            String line = null;
            Log.d(TAG, "raw response: ");
            while ((line = reader.readLine()) != null) {
            	Log.d(TAG,line);
            }
            //Log.d(TAG, reader.toString());
            //String json = reader.readLine();
            Log.d(TAG, "incoming json:");
            Log.d(TAG, json);
            thisResponse = new JSONObject(json);
            //Log.d(TAG, thisResponse.toString());
            Log.d(TAG, "...jsonObject");
            //Log.d(TAG, "first return...");
            return thisResponse;
            
    	} catch (Exception e) {
    		Log.e(TAG, "--- failed at doInBackground");
    		e.printStackTrace();
            return null;
        }
	}
	
	protected void onPostExecute(JSONObject response) {
//    	this.serverResponse = response;  	
    	Log.d(TAG, "Called by Activity: ");
    	Log.d(TAG, MyActivity.toString());
    	String[] student = {"null"};
    	String[] slide_session = {"null"};
    	String[] slide_level = {"null"};
    	String[] fabric = {"null"};
    	String[] objective_images = {"null"};
    	String[] result_images = {"null"};
    	String[] score_images = {"null"};
    	String attempts = "null";
    	
    	if (MyActivity.toString().startsWith("com.scigames.slidegame.LoginActivity") || 
    			MyActivity.toString().startsWith("com.scigames.slidegame.ReviewActivity")){ 
		if(checkLoginFailed(response)){
			listener.failedQuery(failureReason);
		} else {
			if(response.has("student")){
				//parse student object
				try {
					student = parseStudent(response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("slide_session")){
				//parse slide session
				try {
					slide_session = parseSlideSession(response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("slide_level")){
				//parse slide_level
				try {
					slide_level = parseSlideLevel(response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("fabric")){
				//parse fabric
				try {
					fabric = parseFabric(response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("objective_images")){
				try {
					for(int i=0; i<response.getJSONArray("objective_images").length(); i++ ){
						objective_images[i] = response.getJSONArray("objective_images").getString(i);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("result_images")){
				try {
					for(int i=0; i<response.getJSONArray("result_images").length(); i++ ){
						result_images[i] = response.getJSONArray("result_images").getString(i);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("score_images")){
				try {
					for(int i=0; i<response.getJSONArray("score_images").length(); i++ ){
						score_images[i] = response.getJSONArray("score_images").getString(i);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(response.has("attempts")){
				try {
					attempts = response.getString("attempts");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				listener.onResultsSucceeded(student, slide_session, slide_level, objective_images, fabric, result_images, score_images, attempts, response);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    	}
	}
	
	private String[] parseStudent(JSONObject response) throws JSONException{
		Log.d(TAG, "parseThisStudent:");
		JSONObject student = null;
		String thisStudentId = "null";
		String thisStudentPhoto = "null";
		String thisFirstName = "null";
		String thisLastName = "null";
		String thisVisitId = "null";
		String slideLevel = "null";
		String rfid = "null";

		Log.d(TAG, "has student object:");
		student = response.getJSONObject("student");
		thisStudentId = student.getJSONObject("_id").getString("$id");
		thisFirstName = student.getString("first_name");
		thisLastName = student.getString("last_name");
		thisVisitId = student.getString("current_visit");
		slideLevel = student.getString("slide_game_level");
		rfid = student.getString("current_rfid");
		if(student.has("photo")){
			thisStudentPhoto = student.getString("photo");
		}
		 
		String[] parsedStudent = {thisStudentId, thisVisitId, thisFirstName, thisLastName, thisStudentPhoto, slideLevel, rfid };
		return parsedStudent;
	}
	
	private String[] parseSlideSession(JSONObject response) throws JSONException{
		JSONObject slideSessionObj = null;
		JSONObject energyObj = null;
		String slideSessionId = "null";
		String attempt = "null";
//		String startTime = "null"; //not using these for now...
//		String endTime = "null";
//		String totalTime = "null";
		String kinetic = "null";
		String thermal = "null";
		String potential = "null";
		String fabricId = "null";
		String levelCompleted = "null";
		String score = "null";
		String gameLevel = "null";
		
		slideSessionObj = response.getJSONObject("slide_session");
		slideSessionId = slideSessionObj.getJSONObject("_id").getString("$id");
		
		if(slideSessionObj.has("attempt")){ //this means we are recalling a slide_session, not creating one
			attempt = slideSessionObj.getString("attempt");
			gameLevel = slideSessionObj.getString("slide_game_level");
			levelCompleted = slideSessionObj.getString("level_completed");
			score = slideSessionObj.getString("score");
					
			energyObj = slideSessionObj.getJSONObject("energy");
			kinetic = energyObj.getString("kinetic");
			thermal = energyObj.getString("thermal");
			potential = energyObj.getString("potential");
			fabricId = slideSessionObj.getJSONObject("fabric").getString("$id");
		}
		
		String[] parsedSlideSession = {slideSessionId, attempt, gameLevel, levelCompleted, score, kinetic, thermal, potential, fabricId};
		return parsedSlideSession;
	}
	
	private String[] parseSlideLevel (JSONObject response) throws JSONException{
		JSONObject slideLevelObj = null;
		String level = "null";
		String kineticGoal = "null";
		String thermalGoal = "null";
		
		slideLevelObj = response.getJSONObject("slide_level");
		level = slideLevelObj.getString("level");
		kineticGoal = slideLevelObj.getJSONObject("ratio").getString("kinetic");
		thermalGoal = slideLevelObj.getJSONObject("ratio").getString("thermal");
		
		String[] parsedSlideLevel = {level, kineticGoal, thermalGoal};
		return parsedSlideLevel;
	}
	
	private String[] parseFabric (JSONObject response) throws JSONException{
		JSONObject fabric = null;
		String fabricName = "null";
		String fabricValue = "null";
		String fabricId = "null";
		
		fabric = response.getJSONObject("fabric");
		fabricName = fabric.getString("name");
		fabricValue = fabric.getString("value");
		fabricId = fabric.getJSONObject("_id").getString("$id");
		
		String[] parsedFabric = {fabricName, fabricValue, fabricId};
		return parsedFabric;
	}


    public boolean checkLoginFailed(JSONObject response){
		if((response).has("error")){
			Log.d(TAG, "BAD LOGIN");
			try {
				failureReason = response.get("error").toString();
				Log.d(TAG, failureReason);
			} catch (JSONException e) {
				Log.e(TAG, "failed at getting failedReason string");
				e.printStackTrace();
			}
			
			return true;
		} else return false;
    }
	
    /**** legacy ****/
	public String[] parseThisProfile(JSONObject response) throws JSONException{
		Log.d(TAG, "parseThisProfile:");
		JSONObject student = null;
		JSONObject studentId = null;
		JSONObject visits = null;
		JSONObject mClass = null;
		JSONObject teacher = null;
		
		String thisStudentId = "null";
		String thisFirstName = "null";
		String thisLastName = "null";
		String thisStudentPhoto = "null";
		String thisVisitId = "null";
		String cartLevel = "null";
		String slideLevel = "null";
		String mass = "null";
		String email = "null";
		String pw = "null";
		String classId = "null";
		String rfid = "null";
		String className = "null";
		String teacherName = "null";
		String schoolName = "null";
		String classid = "null";

		if(response.has("student")){
			Log.d(TAG, "has student object:");
			student = response.getJSONObject("student");
			studentId = student.getJSONObject("_id");
			thisStudentId = studentId.getString("$id");
			thisFirstName = student.getString("first_name");
			thisLastName = student.getString("last_name");
			thisVisitId = student.getString("current_visit");
			cartLevel = student.getString("cart_game_level");
			slideLevel = student.getString("slide_game_level");
			mass = student.getString("mass");
			email = student.getString("email");
			pw = student.getString("pw");
			classId = student.getString("class_id");
			rfid = student.getString("current_rfid");
			if(student.has("photo")){
				thisStudentPhoto = student.getString("photo");
			}
		} else {
			thisFirstName = response.getString("first_name");
			thisLastName = response.getString("last_name");
			if(response.has("_id")){
				Log.d(TAG, "has id object:");
				studentId = response.getJSONObject("_id");
				thisStudentId = studentId.getString("$id");
			}
		}
		
		if(response.has("class")){
			mClass = response.getJSONObject("class");
			className = mClass.getString("name");
			classid = mClass.getString("un");
		}
		
		if(response.has("teacher")){
			teacher = response.getJSONObject("teacher");
			teacherName = teacher.getString("first_name") + " " + teacher.getString("last_name");
		}
		
		String[] parsedProfile = {thisStudentId, thisStudentPhoto, thisFirstName, thisLastName, thisVisitId,
									mass, email, classId, pw, rfid, slideLevel, cartLevel, className, teacherName, classid, schoolName};
		return parsedProfile;
	}
}


// see http://androidsnippets.com/executing-a-http-post-request-with-httpclient    

