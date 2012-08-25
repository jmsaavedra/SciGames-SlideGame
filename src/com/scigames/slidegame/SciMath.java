package com.scigames.slidegame;

import android.util.Log;

/* questions: 
 * 
 * - where do i account for the angle of the slide?
 * - for thermal [ m(g*h - v^2/2) ], i should be using velocity and height from BOTTOM of the slide?
 * - I actually have 2 formulas for thermal based on our docs, see below --
 * - Should student get any points when they *do not pass level* ?  see my level pass check inside of getScore
 */
public class SciMath {
	
	private static final String TAG = "SciMath";
	
	private static final float g = 9.81f; 	//gravity constant
	
	/* the following are constants for now (nysci slide), when deployed they will need to be variables */
	private static final float topHeight = 3.505f; 			//height of sensor1, meters
	private static final float bottomHeight = 1.5f; 		//height of sensor3, meters -- needs verifying
	private static final float totalGateDistance = 4.8f; 	//distance btwn sensor1 and sensor4, meters -- needs verifying
	private static final float firstGateDistance = 0.368f; 	//distance of top gate, meters
	private static final float secondGateDistance = 0.343f; //distance of bottom gate, meters
	
	private int mass;
	private int firstGateMillis;
	private int secondGateMillis;
	private int totalMillis;
	
	public SciMath(int m, int fMillis, int sMillis, int tMillis){ //when deployed at various schools, slide measurements will also get passed
		mass = m;
		firstGateMillis = fMillis;
		secondGateMillis = sMillis;
		totalMillis = tMillis;
	}
	
	public float getTotalKinetic(){
		//kinetic energy = (m*v^2)/2
		float thisKinetic = 0.0f;
		double totalVelocity = totalGateDistance/totalMillis; //total velocity
		thisKinetic = (float) (mass * Math.pow(totalVelocity,2))/2;
		Log.d(TAG, "Total Kinetic: " + String.valueOf(thisKinetic)); //for debugging
		return thisKinetic;
	}
	
	public float getBottomKinetic(){
		//kinetic energy = (m*v^2)/2
		float thisKinetic = 0.0f;
		double bottomVelocity = secondGateDistance/secondGateMillis; //bottom gate velocity
		thisKinetic = (float) (mass * Math.pow(bottomVelocity,2))/2;
		Log.d(TAG, "Bottom Kinetic: " + String.valueOf(thisKinetic)); 
		return thisKinetic;
	}
	
	public float getTotalPotential(){
		//potential energy = m*g*h
		float thisPotential = 0.0f;
		thisPotential = (mass*g*topHeight);
		Log.d(TAG, "Total Potential: " + String.valueOf(thisPotential)); 
		return thisPotential;
	}
	
	public float getBottomPotential(){
		//potential energy = m*g*h
		float thisPotential = 0.0f;
		thisPotential = (mass*g*bottomHeight);
		Log.d(TAG, "Bottom Potential: " + String.valueOf(thisPotential)); 
		return thisPotential;
	}
	
	public float getThermal(){
		//thermal = m(g*h - v^2/2)
		float thisThermal = 0.0f;
		double endVelocity = secondGateDistance/secondGateMillis; //velocity at bottom gates
		
		//equation 1:
		thisThermal = (float) (mass * (g*bottomHeight - Math.pow(endVelocity,2)/2));
		Log.d(TAG, "Thermal Calc 1: " + String.valueOf(thisThermal)); 
		
		//OR equation 2:
		float thisThermal2 = (float) totalInitialEnergy() - getBottomKinetic() - getBottomPotential();
		Log.d(TAG, "Thermal Calc 2: " + String.valueOf(thisThermal2)); 
		return thisThermal;	
	}
	
	public float getThermal2(){
		//thermal = m(g*h - v^2/2)
		float thisThermal = 0.0f;
		double endVelocity = secondGateDistance/secondGateMillis; //velocity at bottom gates
		
		//equation 1:
		thisThermal = (float) (mass * (g*bottomHeight - Math.pow(endVelocity,2)/2));
		Log.d(TAG, "Thermal Calc 1: " + String.valueOf(thisThermal)); 
		
		//OR equation 2:
		float thisThermal2 = (float) totalInitialEnergy() - getBottomKinetic() - getBottomPotential();
		Log.d(TAG, "Thermal Calc 2: " + String.valueOf(thisThermal2)); 
		return thisThermal2;	
	}
	
	public float totalInitialEnergy(){
		float totalInitEnergy = 0.0f;
		//totaInitial = m*v^2/2 + m*g*h
		double velocity = firstGateDistance/firstGateMillis; //velocity at top gates
		totalInitEnergy = (float) ((mass * Math.pow(velocity,2) / 2) + mass*g*topHeight);
		Log.d(TAG, "Total Inital: " + String.valueOf(totalInitEnergy)); 
		return totalInitEnergy;
	}
	
	public int getScore(int level, int attemptNum, int kineticGoal, int thermalGoal){
		int thisScore = 0;
		
		int predeterminedPts = (level+1)*1000; //levels start at 0 in the architecture so add 1
		float goalRatio = kineticGoal/thermalGoal; //kinetic to thermal ratio (goal)
		float achievedRatio = getTotalKinetic()/getThermal(); //kinetic to thermal ratio (achieved)
		
		float ratioDiff = Math.abs(goalRatio - achievedRatio); //absolute value of difference
		Log.d(TAG, "Ratio Diff: " + String.valueOf(ratioDiff)); 
		
		if (ratioDiff <= 0.1){ //if they are within 10% of the goal ratio (this can be a difficulty variable!)
			//level has been passed, calculate bonus pts:
			int bonusPts = predeterminedPts/attemptNum; //bonus calculator
			thisScore = predeterminedPts + bonusPts;
		} else { //level failed
			thisScore = 0;  //no points for now...
		}
		return thisScore;
	}
}


