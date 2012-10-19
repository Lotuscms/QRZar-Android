package org.tophat.qrzar.activities.gameplayactivity;

import org.tophat.android.exceptions.HttpException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

public class GamePlayActivityCountDownTimer extends CountDownTimer {

	private GamePlayActivity mGamePlayActivity;

	public GamePlayActivityCountDownTimer(GamePlayActivity activity, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		mGamePlayActivity = activity;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTick(long millisUntilFinished) {
		int secondsToGo = (int)(millisUntilFinished / 1000);
        mGamePlayActivity.setTimer(String.format("%02d:%02d", secondsToGo/60,secondsToGo%60));
        
        new CheckIfDead().execute();

	}
	
    private class CheckIfDead extends AsyncTask<Void, Void, Void> 
 	{	
 		@Override    
 		protected void onPreExecute() 
 		{       
 		    super.onPreExecute();
 		}
 		    
 		protected Void doInBackground(Void... details) 
 		{
 			mGamePlayActivity.checkAlive();
			return null;
 		}

 	     protected void onPostExecute(Void data)
 	     {
 	    	mGamePlayActivity.checkAliveUI();
 	     }
 	 }

}
