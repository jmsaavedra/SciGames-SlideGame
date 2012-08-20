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

import com.scigames.slidegame.R.drawable;
import com.scigames.slidegame.R.string;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 *
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class ReviewAnimationView extends SurfaceView implements SurfaceHolder.Callback {
    class ReviewAnimationThread extends Thread {
        /*
         * Difficulty setting constants
         */
        public static final int DIFFICULTY_EASY = 0;
        public static final int DIFFICULTY_HARD = 1;
        public static final int DIFFICULTY_MEDIUM = 2;
        /*
         * Physics constants
         */
        public static final int PHYS_DOWN_ACCEL_SEC = 35;
        public static final int PHYS_FIRE_ACCEL_SEC = 80;
        public static final int PHYS_FUEL_INIT = 60;
        public static final int PHYS_FUEL_MAX = 100;
        public static final int PHYS_FUEL_SEC = 10;
        public static final int PHYS_SLEW_SEC = 120; // degrees/second rotate
        public static final int PHYS_SPEED_HYPERSPACE = 180;
        public static final int PHYS_SPEED_INIT = 30;
        public static final int PHYS_SPEED_MAX = 120;
        /*
         * State-tracking constants
         */
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;

        /*
         * Goal condition constants
         */
        public static final int TARGET_ANGLE = 18; // > this angle means crash
        public static final int TARGET_BOTTOM_PADDING = 17; // px below gear
        public static final int TARGET_PAD_HEIGHT = 8; // how high above ground
        public static final int TARGET_SPEED = 28; // > this speed means crash
        public static final double TARGET_WIDTH = 1.6; // width of target
        /*
         * UI constants (i.e. the speed & fuel bars)
         */
        public static final int UI_BAR = 100; // width of the bar(s)
        public static final int UI_BAR_HEIGHT = 10; // height of the bar(s)
        private static final String KEY_DIFFICULTY = "mDifficulty";
        private static final String KEY_DX = "mDX";

        private static final String KEY_DY = "mDY";
        private static final String KEY_FUEL = "mFuel";
        private static final String KEY_GOAL_ANGLE = "mGoalAngle";
        private static final String KEY_GOAL_SPEED = "mGoalSpeed";
        private static final String KEY_GOAL_WIDTH = "mGoalWidth";

        private static final String KEY_GOAL_X = "mGoalX";
        private static final String KEY_HEADING = "mHeading";
        private static final String KEY_LANDER_HEIGHT = "mLanderHeight";
        private static final String KEY_LANDER_WIDTH = "mLanderWidth";
        private static final String KEY_WINS = "mWinsInARow";

        private static final String KEY_X = "mX";
        private static final String KEY_Y = "mY";

        /*
         * Member (state) fields
         */
        /** The drawable to use as the background of the animation canvas */
        private Bitmap mBackgroundImage;
        private int bgDrawable;

        /**
         * Current height of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 1;

        /**
         * Current width of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;

        /** What to draw for the Lander when it has crashed */
        private Drawable mCrashedImage;

        /**
         * Current difficulty -- amount of fuel, allowed angle, etc. Default is
         * MEDIUM.
         */
        private int mDifficulty;

        /** Velocity dx. */
        private double mDX;

        /** Velocity dy. */
        private double mDY;

        /** Is the engine burning? */
        private boolean mEngineFiring;

        /** What to draw for the Lander when the engine is firing */
        private Drawable mFiringImage;

        /** Fuel remaining */
        private double mFuel;

        /** Allowed angle. */
        private int mGoalAngle;

        /** Allowed speed. */
        private int mGoalSpeed;

        /** Width of the landing pad. */
        private int mGoalWidth;

        /** X of the landing pad. */
        private int mGoalX;

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;

        /**
         * Lander heading in degrees, with 0 up, 90 right. Kept in the range
         * 0..360.
         */
        private double mHeading;

        /** Pixel height of lander image. */
        private int mLanderHeight;

        /** What to draw for the Lander in its normal state */
        private Drawable mLanderImage;

        /** Pixel width of lander image. */
        private int mLanderWidth;

        /** Used to figure out elapsed time between frames */
        private long mLastTime;

        /** Paint to draw the lines on screen. */
        private Paint mLinePaint;

        /** "Bad" speed-too-high variant of the line color. */
        private Paint mLinePaintBad;

        /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
        private int mMode;

        /** Currently rotating, -1 left, 0 none, 1 right. */
        private int mRotating;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;

        /** Scratch rect object. */
        private RectF mScratchRect;

        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;

        /** Number of wins in a row. */
        private int mWinsInARow;

        /** X of lander center. */
        private double mX;

        /** Y of lander center. */
        private double mY;
        
        
        private Paint mKineticPaint;
        private Paint mPotentialPaint;
        private Paint mThermalPaint;
        private Paint mGoldPaint;
        
        private Bitmap mGunImage;
        private Bitmap mPieceImage;
        private Bitmap mRockImage;
        private Bitmap mDrillImage;
        
        private Drawable mRockDrawable;
        private Drawable mDrillDrawable;
        private Drawable mLaserDrawable;
        private Drawable mPieceDrawable;
        
        private boolean isScene2 = false;
        private boolean isScene3 = false;
        private boolean isScene4 = false;
        
        private int potentialE = 0;
        private int thermalE = 0;
        private int tempTherm = 0;
        private int kineticE = 0;
        private long elapsedTimeCount = 0;
        
    	int groupX = 921;
    	int groupY = 260;

        public ReviewAnimationThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            Resources res = context.getResources();
            // cache handles to our key sprites & other drawables
            mLanderImage = context.getResources().getDrawable(
                    R.drawable.lander_plain);
            mFiringImage = context.getResources().getDrawable(
                    R.drawable.lander_firing);
            mCrashedImage = context.getResources().getDrawable(
                    R.drawable.lander_crashed);
        	mRockDrawable = res.getDrawable(2130837513);
        	mDrillDrawable = res.getDrawable(2130837514); 

            // load background image as a Bitmap instead of a Drawable b/c
            // we don't need to transform it and it's faster to draw this way
            mBackgroundImage = BitmapFactory.decodeResource(res,
                    R.drawable.bg_last_turn);
            mBackgroundImage = Bitmap.createScaledBitmap(
                    mBackgroundImage, 1280, 736, true);

            // Use the regular lander image as the model size for all sprites
            mLanderWidth = mLanderImage.getIntrinsicWidth();
            mLanderHeight = mLanderImage.getIntrinsicHeight();

            // Initialize paints for speedometer
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setARGB(255, 0, 255, 0);

            mLinePaintBad = new Paint();
            mLinePaintBad.setAntiAlias(true);
            mLinePaintBad.setARGB(255, 120, 180, 0);

            mThermalPaint = new Paint();
            mThermalPaint.setAntiAlias(true);
            mThermalPaint.setARGB(255, 230, 50, 100);
            
            mPotentialPaint = new Paint();
            mPotentialPaint.setAntiAlias(true);
            mPotentialPaint.setARGB(255, 50, 100, 230);
            
            mKineticPaint = new Paint();
            mKineticPaint.setAntiAlias(true);
            mKineticPaint.setARGB(255, 100, 230, 50);
            
            mGoldPaint = new Paint();
            mGoldPaint.setAntiAlias(true);
            mGoldPaint.setARGB(255, 255, 215, 0);

            mScratchRect = new RectF(0, 0, 0, 0);

            mWinsInARow = 0;
            mDifficulty = DIFFICULTY_MEDIUM;

            // initial show-up of lander (not yet playing)
            mX = mLanderWidth;
            mY = mLanderHeight * 2;
            mFuel = PHYS_FUEL_INIT;
            mDX = 0;
            mDY = 0;
            mHeading = 0;
            mEngineFiring = true;
            
            tempTherm = 0;
        	groupX = 921; //for the energy slide
        	groupY = 260;
        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        
        
        public void doStart() {
            synchronized (mSurfaceHolder) {
                // First set the game for Medium difficulty
                mFuel = PHYS_FUEL_INIT;
                mEngineFiring = false;
                mGoalWidth = (int) (mLanderWidth * TARGET_WIDTH);
                mGoalSpeed = TARGET_SPEED;
                mGoalAngle = TARGET_ANGLE;
                int speedInit = PHYS_SPEED_INIT;

                // Adjust difficulty params for EASY/HARD
                if (mDifficulty == DIFFICULTY_EASY) {
                    mFuel = mFuel * 3 / 2;
                    mGoalWidth = mGoalWidth * 4 / 3;
                    mGoalSpeed = mGoalSpeed * 3 / 2;
                    mGoalAngle = mGoalAngle * 4 / 3;
                    speedInit = speedInit * 3 / 4;
                } else if (mDifficulty == DIFFICULTY_HARD) {
                    mFuel = mFuel * 7 / 8;
                    mGoalWidth = mGoalWidth * 3 / 4;
                    mGoalSpeed = mGoalSpeed * 7 / 8;
                    speedInit = speedInit * 4 / 3;
                }

                // pick a convenient initial location for the lander sprite
                mX = mCanvasWidth / 2;
                mY = mCanvasHeight - mLanderHeight / 2;

                // start with a little random motion
                mDY = Math.random() * -speedInit;
                mDX = Math.random() * 2 * speedInit - speedInit;
                mHeading = 0;

                // Figure initial spot for landing, not too near center
                while (true) {
                    mGoalX = (int) (Math.random() * (mCanvasWidth - mGoalWidth));
                    if (Math.abs(mGoalX - (mX - mLanderWidth / 2)) > mCanvasHeight / 6)
                        break;
                }

                mLastTime = System.currentTimeMillis() + 100;
                setState(STATE_RUNNING);
            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         *
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);
                mRotating = 0;
                mEngineFiring = false;

                mDifficulty = savedState.getInt(KEY_DIFFICULTY);
                mX = savedState.getDouble(KEY_X);
                mY = savedState.getDouble(KEY_Y);
                mDX = savedState.getDouble(KEY_DX);
                mDY = savedState.getDouble(KEY_DY);
                mHeading = savedState.getDouble(KEY_HEADING);

                mLanderWidth = savedState.getInt(KEY_LANDER_WIDTH);
                mLanderHeight = savedState.getInt(KEY_LANDER_HEIGHT);
                mGoalX = savedState.getInt(KEY_GOAL_X);
                mGoalSpeed = savedState.getInt(KEY_GOAL_SPEED);
                mGoalAngle = savedState.getInt(KEY_GOAL_ANGLE);
                mGoalWidth = savedState.getInt(KEY_GOAL_WIDTH);
                mWinsInARow = savedState.getInt(KEY_WINS);
                mFuel = savedState.getDouble(KEY_FUEL);
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (mMode == STATE_RUNNING) updatePhysics();
                        doDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         *
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                if (map != null) {
                    map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
                    map.putDouble(KEY_X, Double.valueOf(mX));
                    map.putDouble(KEY_Y, Double.valueOf(mY));
                    map.putDouble(KEY_DX, Double.valueOf(mDX));
                    map.putDouble(KEY_DY, Double.valueOf(mDY));
                    map.putDouble(KEY_HEADING, Double.valueOf(mHeading));
                    map.putInt(KEY_LANDER_WIDTH, Integer.valueOf(mLanderWidth));
                    map.putInt(KEY_LANDER_HEIGHT, Integer
                            .valueOf(mLanderHeight));
                    map.putInt(KEY_GOAL_X, Integer.valueOf(mGoalX));
                    map.putInt(KEY_GOAL_SPEED, Integer.valueOf(mGoalSpeed));
                    map.putInt(KEY_GOAL_ANGLE, Integer.valueOf(mGoalAngle));
                    map.putInt(KEY_GOAL_WIDTH, Integer.valueOf(mGoalWidth));
                    map.putInt(KEY_WINS, Integer.valueOf(mWinsInARow));
                    map.putDouble(KEY_FUEL, Double.valueOf(mFuel));
                }
            }
            return map;
        }

        /**
         * Sets the current difficulty.
         *
         * @param difficulty
         */
        
        public void setBackgroundResource(int img){
        	Log.d("ReviewThread", "setBgResource");
        	Resources res = mContext.getResources();
        	
        	//bgDrawable = img;
        	Bitmap newBg;
        	newBg = BitmapFactory.decodeResource(res, img);        	
        	mBackgroundImage = Bitmap.createScaledBitmap(newBg, 1280, 736, true); 
        	isScene2 = false;
        	isScene3 = false; //assume not a scene
        	isScene4 = false; //assume not a scene
        	elapsedTimeCount = 0;
        }
        
        public void setLevelScene(int level, int scene, int foregroundImg, int middleGroundImg){
        	Resources res = mContext.getResources();
        	//Log.d(TAG, String.valueOf(level), String.valueOf(scene), int foregroundImg, int middleGroundImg)
        	Log.d("ReviewThread","setLevelScene: " + String.valueOf(level)+ " " + String.valueOf(scene));
        	Log.d("ReviewThread","Sceneimages: " + String.valueOf(foregroundImg)+ " " + String.valueOf(foregroundImg));

        	Log.d("ReviewThread","rockDrawable: " + mRockDrawable.toString());
        	Log.d("ReviewThread","drillDrawable: " + mDrillDrawable.toString());
        	if(scene == 3){
            	mLaserDrawable = res.getDrawable(foregroundImg);
            	mPieceDrawable = res.getDrawable(middleGroundImg);  
        		isScene3 = true;
        		isScene4 = false;
        	}
        	else {
            	mRockDrawable = res.getDrawable(foregroundImg);
            	mDrillDrawable = res.getDrawable(middleGroundImg);  
        		isScene3 = false;
        		isScene4 = true;
        	}
        }
        
        public void setSlideEnergy(int pot, int therm, int kin){
        	
        	//prepareGreen(pot);
        	//prepareRed(therm);
        	//prepareBlue(kin);
        	potentialE = pot;
        	thermalE = therm;
        	kineticE = kin;
        	isScene2 = true;
        }
        
        public void setDifficulty(int difficulty) {
            synchronized (mSurfaceHolder) {
                mDifficulty = difficulty;
            }
        }

        /**
         * Sets if the engine is currently firing.
         */
        public void setFiring(boolean firing) {
            synchronized (mSurfaceHolder) {
                mEngineFiring = firing;
            }
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {
                    mRotating = 0;
                    mEngineFiring = false;
                    Resources res = mContext.getResources();
                    CharSequence str = "";
//                    if (mMode == STATE_READY)
//                        str = res.getText(R.string.mode_ready);
//                    else if (mMode == STATE_PAUSE)
//                        str = res.getText(R.string.mode_pause);
//                    else if (mMode == STATE_LOSE)
//                        str = res.getText(R.string.mode_lose);
//                    else if (mMode == STATE_WIN)
//                        str = res.getString(R.string.mode_win_prefix)
//                                + mWinsInARow + " "
//                                + res.getString(R.string.mode_win_suffix);

                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    if (mMode == STATE_LOSE) mWinsInARow = 0;

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
                mBackgroundImage = Bitmap.createScaledBitmap(
                        mBackgroundImage, width, height, true);
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }


        /**
         * Draws the ship, fuel/speed bars, and background to the provided
         * Canvas.
         */
        private void doDraw(Canvas canvas) {
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            canvas.drawBitmap(mBackgroundImage, 0, 0, null);
            elapsedTimeCount++;
        	//Resources res = mContext.getResources();
        	//View thisView = (View) findViewById(R.id.review);
        	//thisView.setBackgroundResource(bgDrawable);
            int yTop = mCanvasHeight - ((int) mY + mLanderHeight / 2);
            int xLeft = (int) mX - mLanderWidth / 2;

            // Draw the fuel gauge
            int fuelWidth = (int) (UI_BAR * mFuel / PHYS_FUEL_MAX);
            mScratchRect.set(4, 4, 4 + fuelWidth, 4 + UI_BAR_HEIGHT);
            canvas.drawRect(mScratchRect, mLinePaint);

            // Draw the speed gauge, with a two-tone effect
            double speed = Math.sqrt(mDX * mDX + mDY * mDY);
            int speedWidth = (int) (UI_BAR * speed / PHYS_SPEED_MAX);

            if (speed <= mGoalSpeed) {
                mScratchRect.set(4 + UI_BAR + 4, 4,
                        4 + UI_BAR + 4 + speedWidth, 4 + UI_BAR_HEIGHT);
                canvas.drawRect(mScratchRect, mLinePaint);
            } else {
                // Draw the bad color in back, with the good color in front of
                // it
                mScratchRect.set(4 + UI_BAR + 4, 4,
                        4 + UI_BAR + 4 + speedWidth, 4 + UI_BAR_HEIGHT);
                canvas.drawRect(mScratchRect, mLinePaintBad);
                int goalWidth = (UI_BAR * mGoalSpeed / PHYS_SPEED_MAX);
                mScratchRect.set(4 + UI_BAR + 4, 4, 4 + UI_BAR + 4 + goalWidth,
                        4 + UI_BAR_HEIGHT);
                canvas.drawRect(mScratchRect, mLinePaint);
            }

            // Draw the landing pad
//            canvas.drawLine(mGoalX, 1 + mCanvasHeight - TARGET_PAD_HEIGHT,
//                    mGoalX + mGoalWidth, 1 + mCanvasHeight - TARGET_PAD_HEIGHT,
//                    mLinePaint);


            // Draw the ship with its current rotation
            canvas.save();
            canvas.rotate((float) mHeading, (float) mX, mCanvasHeight
                    - (float) mY);
//            if (mMode == STATE_LOSE) {
//                mCrashedImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
//                        + mLanderHeight);
//                mCrashedImage.draw(canvas);
//            } else if (mEngineFiring) {
//                mFiringImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
//                        + mLanderHeight);
//                mFiringImage.draw(canvas);
//            } else {
//                mLanderImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
//                        + mLanderHeight);
//                mLanderImage.draw(canvas);
//            }
            
            if(isScene2){
            	int energyRadius = 15;
            	if(groupX > 300){
            		groupX -= 1.1;//(int)(groupX*0.007);
            	}
            	if(groupY < 700){
            		groupY += 2;//(int)(groupY*0.009);
            	}
            	
            	if(elapsedTimeCount%15 == 0){
            		if(tempTherm<thermalE) tempTherm++;
            	}
            	for(int i=0; i<tempTherm; i++){
            		canvas.drawCircle(921-(int)(i*energyRadius*1.65), 260+(int)(i*energyRadius*1.8), energyRadius, mThermalPaint);
            	}
            	for(int i=0; i<kineticE; i++){
            		canvas.drawCircle((int)(groupX+Math.random()*(160)), (int)(groupY-Math.random()*(160)), energyRadius, mKineticPaint);
            	}//300 y700
            	for(int i=0; i<(int)(potentialE-(tempTherm/3+kineticE/2)); i++){
            		canvas.drawCircle((int)(groupX+Math.random()*(160)), (int)(groupY-Math.random()*(160)), energyRadius, mPotentialPaint);
            	}
            }
            
            /***** Laser melting gold into piece molde *****/
            if(isScene3){ 
            	int energyRadius = 10;
            	for(int i=0; i<thermalE-(int)elapsedTimeCount/25; i++){
            		//for(int j=0; j<thermalE/4;j++){
            			canvas.drawCircle(403, 530-(int)(i*energyRadius*2), energyRadius, mThermalPaint);
            		//}
            	}
            	canvas.save();
            	
            	int fillBarH = (int)(elapsedTimeCount/5);
            	if(fillBarH > thermalE*5) fillBarH = thermalE*5;

            	canvas.rotate(32, 402f, 572f);
            	canvas.drawRect(402, 572, 422, 568-fillBarH, mThermalPaint) ; //Laser
            	canvas.restore();
            	
            	int fillHeight = (int)elapsedTimeCount/5;
            	if(fillHeight > thermalE*10) fillHeight = thermalE*10;
            	canvas.drawRect(545, 690-fillHeight, 653, 690, mGoldPaint); //Piece
            	int mLaserX = 392;
            	int mLaserY = 438;
            	int mLaserW = 124;
            	int mLaserH = 154;
            	
            	int mPieceX = 540;
            	int mPieceY = 510;
            	int mPieceW = 127;
            	int mPieceH = 193;
            
            	//int mLaserW = mLaserDrawable.  getIntrinsicWidth();
            	//int mLaserH = mLaserDrawable.getIntrinsicHeight();
            	//Log.d("laserW", String.valueOf(mLaserW));
            	//Log.d("laserX", String.valueOf(laserX));
            	
            	mLaserDrawable.setBounds(mLaserX, mLaserY, mLaserX + mLaserW, mLaserY + mLaserH);
            	mLaserDrawable.draw(canvas);
            	mPieceDrawable.setBounds(mPieceX, mPieceY, mPieceX + mPieceW, mPieceY + mPieceH);
            	mPieceDrawable.draw(canvas);
            }
            
            /**** Drill pile of rocks *****/
            if(isScene4){
            	
            	int mRockX = 753;
            	int mRockY = 400;
            	int mRockW = 490;
            	int mRockH = 303;
            	
            	int mDrillX = 50;
            	int mDrillY = 310;
            	int mDrillW = 533;
            	int mDrillH = 300;
            	int energyRadius = 10;
            	for(int i=0; i<kineticE-(int)elapsedTimeCount/25; i++){
            		//for(int j=0; j<thermalE/4;j++){
            			canvas.drawCircle(92, 375-(int)(i*energyRadius*2), energyRadius, mKineticPaint);
            		//}
            	}
            	
            	int fillBarW = (int)(elapsedTimeCount/2);
            	if(fillBarW > kineticE*10) fillBarW = kineticE*10;
            	
            	
            	int DrillXMoved = mDrillX+ (int)elapsedTimeCount*3;
            	if(DrillXMoved >kineticE*80) DrillXMoved = kineticE*80;
            	
            	canvas.drawRect(DrillXMoved+50, mDrillY+135, DrillXMoved+50+fillBarW, mDrillY+135+60, mKineticPaint) ; //Drill Level
            	
            	//drill is in middleground
            	mDrillDrawable.setBounds(DrillXMoved, mDrillY, DrillXMoved + mDrillW, mDrillY + mDrillH); //Drill itself
            	mDrillDrawable.draw(canvas);
            	
            	//rock in foreground
            	mRockDrawable.setBounds(mRockX, mRockY, mRockX + mRockW, mRockY + mRockH); //Rock
            	mRockDrawable.draw(canvas);
            }
            canvas.restore();
        }

        /**
         * Figures the lander state (x, y, fuel, ...) based on the passage of
         * realtime. Does not invalidate(). Called at the start of draw().
         * Detects the end-of-game and sets the UI to the next state.
         */
        private void updatePhysics() {
            long now = System.currentTimeMillis();

            // Do nothing if mLastTime is in the future.
            // This allows the game-start to delay the start of the physics
            // by 100ms or whatever.
            if (mLastTime > now) return;

            double elapsed = (now - mLastTime) / 1000.0;

            // mRotating -- update heading
            if (mRotating != 0) {
                mHeading += mRotating * (PHYS_SLEW_SEC * elapsed);

                // Bring things back into the range 0..360
                if (mHeading < 0)
                    mHeading += 360;
                else if (mHeading >= 360) mHeading -= 360;
            }

            // Base accelerations -- 0 for x, gravity for y
            double ddx = 0.0;
            double ddy = -PHYS_DOWN_ACCEL_SEC * elapsed;

            if (mEngineFiring) {
                // taking 0 as up, 90 as to the right
                // cos(deg) is ddy component, sin(deg) is ddx component
                double elapsedFiring = elapsed;
                double fuelUsed = elapsedFiring * PHYS_FUEL_SEC;

                // tricky case where we run out of fuel partway through the
                // elapsed
                if (fuelUsed > mFuel) {
                    elapsedFiring = mFuel / fuelUsed * elapsed;
                    fuelUsed = mFuel;

                    // Oddball case where we adjust the "control" from here
                    mEngineFiring = false;
                }

                mFuel -= fuelUsed;

                // have this much acceleration from the engine
                double accel = PHYS_FIRE_ACCEL_SEC * elapsedFiring;

                double radians = 2 * Math.PI * mHeading / 360;
                ddx = Math.sin(radians) * accel;
                ddy += Math.cos(radians) * accel;
            }

            double dxOld = mDX;
            double dyOld = mDY;

            // figure speeds for the end of the period
            mDX += ddx;
            mDY += ddy;

            // figure position based on average speed during the period
            mX += elapsed * (mDX + dxOld) / 2;
            mY += elapsed * (mDY + dyOld) / 2;

            mLastTime = now;

            // Evaluate if we have landed ... stop the game
            double yLowerBound = TARGET_PAD_HEIGHT + mLanderHeight / 2
                    - TARGET_BOTTOM_PADDING;
            if (mY <= yLowerBound) {
                mY = yLowerBound;

                int result = STATE_LOSE;
                CharSequence message = "";
                Resources res = mContext.getResources();
                double speed = Math.sqrt(mDX * mDX + mDY * mDY);
                boolean onGoal = (mGoalX <= mX - mLanderWidth / 2 && mX
                        + mLanderWidth / 2 <= mGoalX + mGoalWidth);

                // "Hyperspace" win -- upside down, going fast,
                // puts you back at the top.
                if (onGoal && Math.abs(mHeading - 180) < mGoalAngle
                        && speed > PHYS_SPEED_HYPERSPACE) {
                    result = STATE_WIN;
                    mWinsInARow++;
                    doStart();

                    return;
                    // Oddball case: this case does a return, all other cases
                    // fall through to setMode() below.
                } else if (!onGoal) {
                    //message = res.getText(R.string.message_off_pad);
                } else if (!(mHeading <= mGoalAngle || mHeading >= 360 - mGoalAngle)) {
                    //message = res.getText(R.string.message_bad_angle);
                } else if (speed > mGoalSpeed) {
                    //message = res.getText(R.string.message_too_fast);
                } else {
                    result = STATE_WIN;
                    mWinsInARow++;
                }

                //setState(STATE_WIN, message);
                //setRunning(false);
            }
        }
    }
    

    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;
    private Button mNextBtn;
    private Button mBackBtn;

    /** The thread that actually draws the animation */
    private ReviewAnimationThread thread;

    public ReviewAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new ReviewAnimationThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                //mStatusText.setVisibility(m.getData().getInt("viz"));
                //mStatusText.setText(m.getData().getString("text"));

            }
        });

        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     * 
     */
    public ReviewAnimationThread getThread() {
        return thread;
    }

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
       // return thread.doKeyDown(keyCode, msg);
    	return (Boolean) null;
    }

    /**
     * Standard override for key-up. We actually care about these, so we can
     * turn off the engine or stop rotating.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
       // return thread.doKeyUp(keyCode, msg);
    	return (Boolean) null;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }
    
    public void setNextButton(Button button){
    	    	
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(1280, 736);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
