package org.tophat.qrzar.qrscanner;

import org.tophat.qrzar.qrscanner.camera.CameraSurface;

import android.view.View;

public class QRScanner {
	
	CameraSurface mCameraSurface;
	
	public QRScanner(View _cameraSurface) {
		mCameraSurface = (CameraSurface) _cameraSurface;
	}

	public void close() {
		mCameraSurface.close();
		
	}

	public void requestPreviewCallback() {
		mCameraSurface.requestPreviewCallback();
		
	}

	public void cancelPreviewCallback() {
		mCameraSurface.cancelPreviewCallback();
		
	}

}
