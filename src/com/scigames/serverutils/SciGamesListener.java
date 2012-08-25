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

package com.scigames.serverutils;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;


public interface SciGamesListener {
	
	public void onResultsSucceeded(String[] student, String[] slide_session, String[] slide_level, String[] objective_images, 
			String[] fabric, String[] result_images, String[] score_images, String attempts, boolean no_session, JSONObject serverResponseJSON) throws JSONException;
	public void failedQuery(String failureReason);
	
}