package com.fyp.authenticator.dummy;

import android.util.Log;

import com.fyp.authenticator.AuthMechService;

/**
 * Class that implements dummy authentication mechanism.
 * 
 * The dummy authentication mechanism is created as a service that extends the
 * AuthMechService class.
 * 
 * @author cristi
 * 
 */
public class DummyService extends AuthMechService {

	/** Thread used to periodically authenticate the user and broadcast result. */
	private AuthenticatorThread authThread = null;

	@Override
	public void onCreate() {
		Log.i("AuthDummyService", "onCreate");

		this.initialWeight = 10000;

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

		this.decayTimer.stopTimer();
	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		private static final int AUTH_PERIOD = 5000;
		private volatile boolean stop;

		/** Dummy data DAO. */
		private DummyAuthMediator dummyDAO = null;
		
		/** Basic constructor. */
		public AuthenticatorThread() {
			this.stop = false;
		}

		/**
		 * Main run() method for the thread.
		 * 
		 * This method is executed in order to provide periodic authentication
		 * data.
		 */
		@Override
		public void run() {
			// instantiating DAO when starting thread.
			if (dummyDAO == null) {
				dummyDAO = new DummyAuthMediator();
			}

			// authentication loop.
			while (stop != true) {
				try {
					Thread.sleep(AUTH_PERIOD);

					score = dummyDAO.getMatch();
					sendDecayedScore(true);

					// start decaying process after collecting data
					DummyService.this.startDecay();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/** Method used for other objects to stop this thread. */
		public void stopThread() {
			this.stop = true;
		}

	}

}
