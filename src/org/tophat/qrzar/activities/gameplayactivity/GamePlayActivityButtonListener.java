package org.tophat.qrzar.activities.gameplayactivity;

import android.R;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GamePlayActivityButtonListener implements OnTouchListener {

	GamePlayActivity mActivity;
	
	public GamePlayActivityButtonListener(GamePlayActivity _activity) {
		mActivity = _activity;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(v.getId()){
			case GamePlayActivityConstants.SHOOT_BUTTON:
				
				switch(event.getAction()){
				case MotionEvent.ACTION_UP:
					mActivity.stopScan();
					break;
				case MotionEvent.ACTION_DOWN:
					mActivity.startScan();
					break;
				}
				break;
		}
		
		return false;
	}

}
