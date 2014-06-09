package com.fyp.authenticator.dummy;

import android.util.Log;

import com.fyp.authenticator.AuthMechService;

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
			authThread.stopThread();
			authThread = null;
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
		
		public AuthenticatorThread() {
			this.stop = false;
		}

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

		public void stopThread() {
			try {
				this.stop = true;
				this.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
