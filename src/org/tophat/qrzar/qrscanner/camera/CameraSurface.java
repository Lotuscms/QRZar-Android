package org.tophat.qrzar.qrscanner.camera;

import java.io.IOException;

import org.tophat.qrzar.qrscanner.QRScannerInterface;
import org.tophat.qrzar.qrscanner.qrdecoder.DecoderCallback;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraSurface extends SurfaceView implements Callback {
	
	QRScannerInterface mActivity;
	CameraManager mCameraManager;
	
	@SuppressWarnings("deprecation")
	public CameraSurface(Context _context, AttributeSet _attrs) {
		super(_context, _attrs);
		mActivity = (QRScannerInterface)_context;
		getHolder().addCallback(this);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void close(){
		mCameraManager.cancelPreviewCallback();
		
		//Let's just leave it open for now
		mCameraManager.close();
	}
	
	public void stop()
	{
		this.mCameraManager.close();
	}
	
	public void start(){
		mCameraManager.startPreview();
	}
	
	public void requestPreviewCallback(){
		mCameraManager.requestPreviewCallback(new DecoderCallback(mActivity,this));
	}

	public void cancelPreviewCallback(){
		mCameraManager.cancelPreviewCallback();
	}
	
	public Rect getCameraRect(){
    	
    	return new Rect(this.getLeft(),this.getTop(),this.getWidth()+this.getLeft(),this.getHeight()+this.getTop());
    }
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		try {
			mCameraManager = new CameraManager(holder, mActivity);
			
			mCameraManager.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (RuntimeException e)
		{
			Toast t = Toast.makeText(CameraSurface.this.getContext(), "Failed to connect to camera service.", Toast.LENGTH_LONG);
			t.show();
			System.exit(0);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

}
