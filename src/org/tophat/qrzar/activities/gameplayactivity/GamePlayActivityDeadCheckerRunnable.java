package org.tophat.qrzar.activities.gameplayactivity;

public class GamePlayActivityDeadCheckerRunnable implements Runnable {
	
	private GamePlayActivity mGamePlayActivity;

	public GamePlayActivityDeadCheckerRunnable(GamePlayActivity activity) {
		mGamePlayActivity = activity;
		
	}

	@Override
	public void run() {
		mGamePlayActivity.checkAlive();
	}

}
