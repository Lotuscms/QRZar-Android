package org.tophat.qrzar.activities.mainactivity;

import java.io.IOException;

import org.tophat.QRzar.models.Player;
import org.tophat.android.exceptions.HttpException;
import org.tophat.android.exceptions.NoInternetConnection;
import org.tophat.qrzar.R;
import org.tophat.qrzar.activities.gameplayactivity.GamePlayActivity;
import org.tophat.qrzar.qrscanner.QRScanner;
import org.tophat.qrzar.qrscanner.QRScannerInterface;
import org.tophat.qrzar.sdkinterface.SdkInterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements QRScannerInterface{

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private QRScanner mQRScanner;
	private Handler mHandler;
	public static SdkInterface sdk;
	public static Context context;
	
	private boolean mJoining;
	
	public static Player p;
	public MediaPlayer mp;
	
	/**
	 * Activity life cycle methods
	 */
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrzar_main);
        
        this.context = this.getApplicationContext();
        
        mHandler = new MainActivityMessageHandler(this);
        sdk = new SdkInterface();
        
        this.mJoining = false;
        
        addListenerToButtons();
        
        mp = new MediaPlayer().create(MainActivity.this, R.raw.intro);
        AudioManager audioManager = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE); audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        mp.setLooping(true);
        mp.start();
        
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    	// Define a listener that responds to location updates
    	LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	      // Called when a new location is found by the network location provider.
    	    	
    	      sdk.updateLocation(location);
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

    	    public void onProviderEnabled(String provider) {}

    	    public void onProviderDisabled(String provider) {}
    	  };

    	// Register the listener with the Location Manager to receive location updates
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
	
	@Override
	public void onResume(){
		super.onResume();
		mQRScanner = new QRScanner(findViewById(R.id.cameraSurface));
		mp.start();
	}
    
    @Override
    public void onPause(){
    	super.onPause();
    	//mQRScanner.close();
    	mp.pause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_qrzar_main, menu);
        return true;
    }
    
    /**
     * Game control methods
     */
    
    public void startScanForGame(){
    	mQRScanner.requestPreviewCallback();
    	findViewById(R.id.logo).setVisibility(View.INVISIBLE);
    }
    
    public void stopScanForGame(){
    	mQRScanner.cancelPreviewCallback();
    	findViewById(R.id.logo).setVisibility(View.VISIBLE);
    }
    
    public void hasScannedResult(String result){
    	
    	if(sdk.validToProcessTShirt(result)){
    		
	    	this.stopScanForGame();
	    	
	    	sdk.setTShirtCode(result);
	    	
	    	new AnonymousUserTask().execute();
	    	
	    	((TextView) findViewById(R.id.mainMessage)).setText("Please scan the game code");
	    	
    	}else if(sdk.validToProcessGameCode(result) && !mJoining){
    		
    		mJoining = true;
       		try
    		{
       			this.stopScanForGame();
    			this.mQRScanner.stop();
    		}
    		catch( RuntimeException e)
    		{
    			e.printStackTrace();
    		}
    		
    		sdk.setGameCode(SdkInterface.decodeGameCode(result));
    		
    		mp.stop();
    		new JoinGameTask().execute();
    	}
    }
    
    private class AnonymousUserTask extends AsyncTask<Void, Boolean, Boolean> 
	{	
		private ProgressDialog dialog;
		
		@Override    
		protected void onPreExecute() 
		{       
		    super.onPreExecute();
		    
		    dialog = ProgressDialog.show(MainActivity.this, "", 
	                "Loading Server Details. Please wait...", true);
		}
		    
		protected Boolean doInBackground(Void... details) 
		{
			return MainActivity.this.setAnonymousToken();
		}

	     protected void onPostExecute(Boolean data)
	     {
	    	this.dialog.cancel();
	    	
	    	if (data)
	    	{
	 			Log.i(TAG, "T-Shirt code loaded");
	    	}
	     }
	}
    
    private class JoinGameTask extends AsyncTask<Void, Boolean, Boolean> 
	{	
		private ProgressDialog dialog;
		
		@Override    
		protected void onPreExecute() 
		{       
		    super.onPreExecute();
		    
		    dialog = ProgressDialog.show(MainActivity.this, "", 
	                "Join Game. Please wait...", true);
		}
		    
		protected Boolean doInBackground(Void... details) 
		{
			return sdk.joinGame();
		}

	     protected void onPostExecute(Boolean data)
	     {
	    	this.dialog.cancel();
	    	
	    	if (data)
	    	{
	    		mJoining = false;
	 			Intent intent = new Intent(MainActivity.this, GamePlayActivity.class);
				
	 			MainActivity.p = sdk.getPlayer();
	 			startActivity(intent);
	 			Log.i(TAG, "Game Joined");
	    	}
	     }
	 }
    
    
    /**
     * Private methods
     */
    
    private void addListenerToButtons(){
    	MainActivityButtonListener listener = new MainActivityButtonListener(this);
    	
    	Button playButton = (Button)findViewById(R.id.join_button);
    	playButton.setId(MainActivityConstants.PLAY_BUTTON);
    	playButton.setOnTouchListener(listener);
    	
    	Button infoButton = (Button)findViewById(R.id.info_button);
    	infoButton.setId(MainActivityConstants.INFO_BUTTON);
    	infoButton.setOnTouchListener(listener);
    	
    	Button rankButton = (Button)findViewById(R.id.rank_button);
    	rankButton.setId(MainActivityConstants.RANK_BUTTON);
    	rankButton.setOnTouchListener(listener);
    }
    
    
	    /**
	     *  Private interface helper methods
	     */
    
    
    private boolean setAnonymousToken(){

    	boolean didSucceed = false;
    	try 
    	{
			sdk.anonymous_connect();
			Log.i(TAG, "Successfully attained api token.");
			didSucceed = true;
    	}
    	catch (NoInternetConnection e)
    	{
    		// Do something special when there is no internet connection / server is unreachable ??
    		Log.i(TAG,e.getMessage());
    	}
    	catch (HttpException e) 
    	{
    		//This will show whatever error the user encounters via toast on the users screen.
    		Log.i(TAG,e.getMessage());
		}
    	

    	return didSucceed;
    }
    
    
    /**
     * QRScanner Interface Methods
     */
    
    @Override
    public Rect getPreviewScreenRect(){
    	View preview = findViewById(R.id.previewView);
    	return new Rect(preview.getLeft(),preview.getTop(),preview.getWidth()+preview.getLeft(),preview.getHeight()+preview.getTop());
    }
    
    
    @Override
    public Handler getHandler(){
    	return mHandler;
    }



	@Override
	public int getScreenRotation() {
		WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		return windowManager.getDefaultDisplay().getRotation();
	}
    
}
