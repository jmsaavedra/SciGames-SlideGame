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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import java.io.FileInputStream;

public class DownloadNarrativeImage extends AsyncTask<ImageView, Void, Bitmap> {    
	ImageView imageView = null;
	private static Context context;
	String TAG = "DLNarrativeImage";
	
	DownloadNarrativeImage(Activity a, String url){
		DownloadNarrativeImage.context = getAppContext();
	}
	
    public static Context getAppContext() {
        return DownloadNarrativeImage.context;
    }
	
	@Override
    protected void onProgressUpdate(Void... values) {
    }

	@Override
	protected Bitmap doInBackground(ImageView... imageViews) {
		this.imageView = imageViews[0];
		String fileName = (String)imageView.getTag();
		Log.d(TAG, "filName:");
		Log.d(TAG, fileName);
		String fullImgPath = getContextPath() + "/" + fileName;
		Log.d(TAG, "fullImgPath to check: ");
		Log.d(TAG, fullImgPath);
		Bitmap bMap = BitmapFactory.decodeFile(fullImgPath);
	    if (bMap != null) {
	    	Log.d(TAG, "bMap is not null, using bMap");
	        return bMap;
	    } else {
	    	bMap = download_image(fileName);
	    	return bMap;
	    } 
	}
	
	public String getContextPath(){
//		byte[] saveData = JNIGetSaveData();
//		int saveDataSize = JNIGetSaveDataSize();
//		
//	    String FILENAME = "saved_data";
//	    FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
//	    outputStream.write(saveData, 0, saveDataSize);
//	    outputStream.close();
//	    
//		Log.d(TAG, DownloadNarrativeImage.getAppContext().getFilesDir().toString());
//		File file = new File(getAppContext().getFilesDir(), "hello");
//        String path = getFilesDir().toString();
//        Log.d("DL_NarrativeImg_Path:", path);
		String path = "null";
       return path;
	}
	
    @Override
    protected void onPostExecute(Bitmap result) {
    	 imageView.setImageBitmap(result);
    }
    
    private Bitmap download_image(String url){
    	Log.d(TAG, "download_image");
    	Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            //The openfileOutput() method creates a file on the phone/internal storage in the context of your application ]
            
            final FileOutputStream fos = getAppContext().openFileOutput(url, Context.MODE_PRIVATE);
            
            // Use the compress method on the BitMap object to write image to the OutputStream
            bm.compress(CompressFormat.JPEG, 90, fos);
            
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("Hub","Error getting the image from server : " + e.getMessage().toString());
        } 
    	return bm;
    }
}