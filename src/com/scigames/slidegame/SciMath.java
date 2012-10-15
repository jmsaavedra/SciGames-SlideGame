package com.scigames.slidegame;

import android.util.Log;

/* questions: 
 * 
 * - Should student get any points when they *do not pass level* ?  see my level pass check inside of getScore
 */
//@SuppressWarnings("unused")

public class SciMath {
	private static final String TAG = "SciMath";
	
	private static final float g = 9.81f; 	//gravity constant
	
	/* the following are constants for now (nysci slide), when deployed they will need to be variables for each school's slide */
	private static final float topHeight = 3.25f; 			//height of sensor1, meters
	private static final float bottomHeight = 1.14f; 		//height of sensor3, meters -- needs verifying
	private static final float lastSensorHeight = 0.889f;	//height of sensor4, meters (used for total potential calc)
	private static final float totalGateDistance = 5.1f; 	//distance btwn sensor1 and sensor4, meters -- needs verifying
	private static final float firstGateDistance = 0.355f; 	//distance of top gate, meters
	private static final float secondGateDistance = 0.381f; //distance of bottom gate, meters
	
	/* the following are variables that come from student's performance on the slide */
	private int mass;
	private float firstGateSeconds;
	private float secondGateSeconds;
	private float totalSeconds;
	
	public boolean bLevelPassed = false; //calculated in getScore()
	
	public SciMath(int m, int fMillis, int sMillis, int tMillis){ //when deployed at various schools, slide measurements will also get passed
		mass = m;
		firstGateSeconds = fMillis*0.001f; //turn milliseconds into seconds
		secondGateSeconds = sMillis*0.001f;
		totalSeconds = tMillis*0.001f;
	}
	
	public float getTotalKinetic(){
		//kinetic energy = (m*v^2)/2
		float thisKinetic = 0.0f;
		double totalVelocity = totalGateDistance/totalSeconds; //total velocity
		thisKinetic = (float) (mass * Math.pow(totalVelocity,2))/2;
		Log.d(TAG, "get Total Kinetic: " + String.valueOf(thisKinetic)); //for debugging
		return thisKinetic;
	}
	
	public float getBottomKinetic(){
		//kinetic energy = (m*v^2)/2
		float thisKinetic = 0.0f;
		double bottomVelocity = secondGateDistance/secondGateSeconds; //bottom gate velocity
		thisKinetic = (float) (mass * Math.pow(bottomVelocity,2))/2;
		Log.d(TAG, "get Bottom Kinetic: " + String.valueOf(thisKinetic)); 
		return thisKinetic;
	}
	
	public float getTotalPotential(){
		//potential energy = m*g*h
		float thisPotential = 0.0f;
		thisPotential = (mass*g*(topHeight-lastSensorHeight));
		Log.d(TAG, "get Total Potential: " + String.valueOf(thisPotential)); 
		return thisPotential;
	}
	
	public float getBottomPotential(){
		//potential energy = m*g*h
		float thisPotential = 0.0f;
		thisPotential = (mass*g*bottomHeight);
		Log.d(TAG, "get Bottom Potential: " + String.valueOf(thisPotential)); 
		return thisPotential;
	}
	
	public float getThermal(){
		
//		float thisThermal = 0.0f;
//		double endVelocity = secondGateDistance/secondGateSeconds; //velocity at bottom gates	
//		equation 1- NOT USED: thermal = m(g*h - v^2/2)
//		thisThermal = (float) (mass * (g*bottomHeight - Math.pow(endVelocity,2)/2));
//		Log.d(TAG, "get Thermal Calc 1: " + String.valueOf(thisThermal)); 
		
		//equation 2 - USING THIS: totalInitial - bottomKinetic - bottom potential
		float thisThermal2 = (float) totalInitialEnergy() - getBottomKinetic() - getBottomPotential();
		Log.d(TAG, "get Thermal Calc 2: " + String.valueOf(thisThermal2)); 
		return thisThermal2;	
	}
	
	public float totalInitialEnergy(){
		float totalInitEnergy = 0.0f;
		//totaInitial = m*v^2/2 + m*g*h
		double velocity = firstGateDistance/firstGateSeconds; //velocity at top gates
		totalInitEnergy = (float) ((mass * Math.pow(velocity,2) / 2) + mass*g*topHeight);
		Log.d(TAG, "get Total Inital: " + String.valueOf(totalInitEnergy)); 
		return totalInitEnergy;
	}
	
	public int getScore(int level, int attemptNum, int kineticGoal, int thermalGoal){
		int thisScore = 0;
		
		int predeterminedPts = (level+1)*1000; //levels start at 0 in the architecture so add 1
		
		float goalRatio = kineticGoal/thermalGoal; //kinetic to thermal ratio (goal)
		Log.d(TAG, "goalRatio: " + String.valueOf(goalRatio)); 
		
		float achievedRatio = getTotalKinetic()/getThermal(); //kinetic to thermal ratio (achieved)
		Log.d(TAG, "achievedRatio: " + String.valueOf(achievedRatio)); 
		
		float ratioDiff = Math.abs(goalRatio - achievedRatio); //absolute value of difference
		Log.d(TAG, "get Ratio Diff: " + String.valueOf(ratioDiff)); 
		
		if (ratioDiff <= 0.15){ //if they are within 10% of the goal ratio (this can be a difficulty variable!)
			bLevelPassed = true;
			//level has been passed, calculate bonus pts:
			int bonusPts = predeterminedPts/attemptNum; //bonus calculator
			thisScore = predeterminedPts + bonusPts;
		} else { //level failed
			thisScore = 0;  //no points for now...
		}
		return thisScore;
	}
	
	public boolean getLevelPassed(){
		return bLevelPassed;
	}
}


