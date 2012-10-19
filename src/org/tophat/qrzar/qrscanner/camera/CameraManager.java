package org.tophat.qrzar.qrscanner.camera;

import java.io.IOException;
import java.util.List;

import org.tophat.qrzar.qrscanner.QRScannerInterface;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraManager {
	
	private Camera mCamera;
	private static final String TAG = CameraManager.class.getSimpleName();
	private SurfaceHolder surfaceHolder;
	
	@SuppressLint("NewApi")
	public CameraManager(SurfaceHolder _surfaceHolder, QRScannerInterface _activity) throws IOException{
		this.surfaceHolder = _surfaceHolder;
		
		if(Build.VERSION.SDK_INT>8){
			//Assumes the user has a camera.
			mCamera = Camera.open(0);
			Log.i(TAG, "Only devices Gingerbread up should call this.");
		}else{
			mCamera = Camera.open();
		}
	
		int degreeOrientation = _activity.getScreenRotation()*90;

		Camera.Parameters parameters = mCamera.getParameters();
		
		try{
			parameters.setPictureSize(_surfaceHolder.getSurfaceFrame().width(),_surfaceHolder.getSurfaceFrame().height());
			mCamera.setParameters(parameters);
		}catch(RuntimeException e){
			e.printStackTrace();
		}
		
		List<String> focusModes = parameters.getSupportedFocusModes();
		
		//Some phones don't support continuous smooth focus.
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
		{
		    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}
		else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
		{
		    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}
		
		try{
			mCamera.setParameters(parameters);
		}catch(RuntimeException e){
			e.printStackTrace();
		}
		
		mCamera.setDisplayOrientation((450-degreeOrientation)%360);
		
		mCamera.setPreviewDisplay(_surfaceHolder);
	}
	
	public void startPreview(){
		mCamera.startPreview();
	}
	
	public void close(){

		mCamera.stopPreview();
		mCamera.release();
	}
	
	public void requestPreviewCallback(PreviewCallback _callback){
		mCamera.setPreviewCallback(_callback);
	}
	
	/**
	 * Stops the active preview from running - encountering some issues with ensuring that the preview hasn't stopped already, which is currently casuing the application to crash on most active Android devices
	 */
	public void cancelPreviewCallback()
	{
		try
		{
			mCamera.setPreviewCallback(null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
