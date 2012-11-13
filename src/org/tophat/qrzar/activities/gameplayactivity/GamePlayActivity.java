package org.tophat.qrzar.activities.gameplayactivity;

import java.util.HashMap;

import org.tophat.android.exceptions.HttpException;
import org.tophat.qrzar.R;
import org.tophat.qrzar.activities.mainactivity.MainActivity;
import org.tophat.qrzar.qrscanner.QRScanner;
import org.tophat.qrzar.qrscanner.QRScannerInterface;
import org.tophat.qrzar.sdkinterface.SdkInterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GamePlayActivity extends Activity implements QRScannerInterface {

	private static final String TAG = GamePlayActivity.class.getSimpleName();
	private QRScanner mQRScanner;
	private GamePlayActivityMessageHandler mHandler;
	private SdkInterface sdk;
	private TextView mTeam1Score, mTeam2Score, mTimer;
	private boolean mAlive;
	private boolean interfaceAliveState;
	private Button shootButton;
	
	/**
	 * Async task Lock
	 */
	private boolean actionLock;
	
	private GamePlayActivityCountDownTimer mCountdownTimer;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);
        
        mHandler = new GamePlayActivityMessageHandler(this);
        
        //Bundle b = getIntent().getExtras();
        //org.tophat.QRzar.models.Player p = (org.tophat.QRzar.models.Player)b.getParcelable("player");
        sdk = MainActivity.sdk;
        
        mTeam1Score = (TextView)findViewById(R.id.team1Score);
        mTeam2Score = (TextView)findViewById(R.id.team2Score);
        mTimer = (TextView)findViewById(R.id.timer);
        
        mAlive = true;
        interfaceAliveState = true;
        actionLock = false;
        
        updateScoresAndTimer();
        
        addListenerToButtons();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	mQRScanner = new QRScanner(findViewById(R.id.cameraSurface));
    }

    @Override
    public void onPause(){
    	super.onPause();
    	//mQRScanner.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game_play, menu);
        return true;
    }
    
    
    /**
     * Game control methods
     */
    
    public void startScan(){
    	mQRScanner.requestPreviewCallback();
    }
    
    public void stopScan(){
    	mQRScanner.cancelPreviewCallback();
    }
    
    public void hasScannedResult(String result)
    {
    	if (!this.actionLock)
    	{
	    	if (this.mAlive)
	    	{
		    	if (sdk.isValidPlayerCode(result))
		    	{
		    		new HasScannedResult(result).execute();
		    	}
	    	}
	    	else
	    	{
	    		if (sdk.isValidRespawnCode(result))
	    		{
	    			new RespawnTask(result).execute();
	    		}
	    	}
    	}
    }
    
    private class RespawnTask extends AsyncTask<Void, Boolean, Boolean> 
 	{	
 		private ProgressDialog dialog;
 		private String result;
 		private HttpException error;
 		
 		public RespawnTask(String result)
 		{
 			super();
 			
 			this.result = result;
 			this.error = null;
 		}
 		
 		@Override    
 		protected void onPreExecute() 
 		{       
 		    super.onPreExecute();
 		    
 		    actionLock = true;
 		    
 		    dialog = ProgressDialog.show(GamePlayActivity.this, "", 
 	                "Respawing...", true);
 		}
 		    
 		protected Boolean doInBackground(Void... details) 
 		{
 	    	try 
 	    	{
 				sdk.respawn(sdk.getPlayer(), result);
 				
 				return true;
 			} 
 	    	catch (HttpException e) 
 	    	{
 				e.printStackTrace();
 				
 				error = e;
 				return false;
 			}
 		}

 	     protected void onPostExecute(Boolean data)
 	     {
 	    	this.dialog.cancel();
 	    	actionLock = false;
 	    	
 	    	if (!data)
 	    	{
 	    		(Toast.makeText(GamePlayActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG)).show();
 	    	}
 	    	else
 	    	{
 	    		setUserMessage("Respawn successful!");
 	    	}
 	     }
 	 }
    
    
    private class HasScannedResult extends AsyncTask<Void, Boolean, Boolean> 
 	{	
 		private ProgressDialog dialog;
 		private String result;
 		private HttpException error;
 		
 		public HasScannedResult(String result)
 		{
 			super();
 			
 			this.result = result;
 		}
 		
 		@Override    
 		protected void onPreExecute() 
 		{       
 		    super.onPreExecute();
 		    
 		    actionLock = true;
 		    error = null;
 		    dialog = ProgressDialog.show(GamePlayActivity.this, "", 
 	                "Termination in progress...", true);
 		}
 		    
 		protected Boolean doInBackground(Void... details) 
 		{
 	    	try 
 	    	{
 				sdk.kill(sdk.getPlayer(), this.result);
 				
 				return true;
 			} 
 	    	catch (HttpException e) 
 	    	{
 				e.printStackTrace();
 				error = e;
 				return false;
 			}
 		}

 	     protected void onPostExecute(Boolean data)
 	     {
 	    	this.dialog.cancel();
 	    	
 	    	actionLock = false;
 	    	
 	    	if (data)
 	    	{
 	    		Log.i(TAG, "Terminated.");
 	    		((TextView) findViewById(R.id.userMessage)).setText("Tagged +1");
 	    	}
 	    	else
 	    	{
 	    		if (error.getStatusCode() == 409)
 	    		{
 	    			setUserMessage("Player is dead or invalid code.");
 	    			(Toast.makeText(GamePlayActivity.this.getApplicationContext(), "This player is already dead.", Toast.LENGTH_LONG)).show();
 	    		}
 	    		else if (error.getStatusCode() == 404)
 	    		{
 	    			(Toast.makeText(GamePlayActivity.this.getApplicationContext(), "This player does not exist / QR is invalid.", Toast.LENGTH_LONG)).show();
 	    		}
 	    		else
 	    		{
 	    			(Toast.makeText(GamePlayActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG)).show();
 	    		}
 	    	}
 	     }
 	 }
    
    
    public void setTimer(String timeToSet){
    	mTimer.setText(timeToSet);
    }
    
    public void checkAlive()
    {
    	mAlive = sdk.playerIsAlive();
    }
    
    public void checkAliveUI()
    {
    	if(!mAlive && this.interfaceAliveState){
			mTimer.setTextColor(0xFFFF0000);
			
			((View)findViewById(R.id.deadbox)).setBackgroundColor(Color.RED);
			((View)findViewById(R.id.deadbox)).setAlpha((float) 0.5);
			shootButton.setText("Respawn");
			interfaceAliveState = mAlive;
			setUserMessage("Head to your base to respawn!");
    	}
    	else if (mAlive && !this.interfaceAliveState)
    	{
			mTimer.setTextColor(0xFFFFFFFF);
			
			((View)findViewById(R.id.deadbox)).setBackgroundColor(Color.TRANSPARENT);
			((View)findViewById(R.id.deadbox)).setAlpha((float) 1);
			shootButton.setText(getString(R.string.shoot));
			interfaceAliveState = mAlive;
    	}
    	
    	updateScores();
    }
    
    public void setUserMessage(String message)
    {
    	((TextView) findViewById(R.id.userMessage)).setText(message);
    	((View) findViewById(R.id.messageLayout)).setBackgroundColor(Color.BLACK);
    }
    
    /**
     * Private methods
     */
    
    private void addListenerToButtons(){
    	GamePlayActivityButtonListener listener = new GamePlayActivityButtonListener(this);
    	
    	this.shootButton = (Button)findViewById(R.id.shoot_button);
    	shootButton.setId(GamePlayActivityConstants.SHOOT_BUTTON);
    	shootButton.setOnTouchListener(listener);
    }
    
    private void updateScoresAndTimer(){
    	
    	HashMap<String,Integer>map = sdk.getTeamScoresAndRemainingTime();
        
        mTeam1Score.setText(Integer.toString(sdk.team1));
        mTeam2Score.setText(Integer.toString(sdk.team2));
        
        if(mCountdownTimer!=null)
        	mCountdownTimer.cancel();
        
        mCountdownTimer = new GamePlayActivityCountDownTimer(this, map.get("timer")*1000, 1000);
        
        mCountdownTimer.start();
    }
    
    private void updateScores(){
    	
    	HashMap<String,Integer>map = sdk.getTeamScoresAndRemainingTime();
        
        mTeam1Score.setText(Integer.toString(sdk.team1));
        mTeam2Score.setText(Integer.toString(sdk.team2));
    }
    
    /**
     * Interface required methods.
     */

	@Override
	public Handler getHandler() {
		
		return mHandler;
	}

	@Override
	public Rect getPreviewScreenRect() {
		View preview = findViewById(R.id.previewView);
    	return new Rect(preview.getLeft(),preview.getTop(),preview.getWidth()+preview.getLeft(),preview.getHeight()+preview.getTop());
	}

	@Override
	public int getScreenRotation() {
		WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		return windowManager.getDefaultDisplay().getRotation();
	}
}
