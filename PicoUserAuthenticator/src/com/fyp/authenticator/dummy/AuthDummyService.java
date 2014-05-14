package com.fyp.authenticator.dummy;

import android.util.Log;

import com.fyp.authenticator.AuthMechService;

public class AuthDummyService extends AuthMechService {

	/** Dummy data DAO. */
	private AuthDevDummyDAO dummyDAO = null;

	/** Thread used to periodically authenticate the user and broadcast result. */
	private AuthenticatorThread authThread = null;

	@Override
	public void onCreate() {
		Log.i("AuthDummyService", "onCreate");

		if (authThread == null) {
			authThread = new AuthenticatorThread();
			authThread.start();
		}

	}

	@Override
	public void onDestroy() {
		Log.i("AuthDummyService", "onDestroy");

		if (authThread != null) {
			try {
				authThread.stopThread();
				authThread.join();
				authThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (dummyDAO != null) {
			dummyDAO = null;
		}
	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		private static final int AUTH_PERIOD = 1000;
		private volatile boolean stop;

		public AuthenticatorThread() {
			this.stop = false;
		}

		@Override
		public void run() {
			// instantiating DAO when starting thread.
			if (dummyDAO == null) {
				dummyDAO = new AuthDevDummyDAO();
			}
			
			// authentication loop.
			while (stop != true) {
				try {
					Thread.sleep(AUTH_PERIOD);

					score = dummyDAO.getMatch();
					sendDecayedScore();
				
					// start decaying process after collecting data
					AuthDummyService.this.startDecay();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			this.stop = true;
		}

	}

}
