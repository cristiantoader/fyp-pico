package com.fyp.authenticator.location;

import android.util.Log;

import com.fyp.authenticator.AuthMechService;
import com.fyp.authenticator.voice.VoiceDAO;
import com.fyp.authenticator.voice.VoiceRecord;
import com.fyp.authenticator.voice.VoiceService;

public class LocationService extends AuthMechService {

	/** Logging tag. */
	private static final String TAG = "AuthMechService";
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		/** Authentication period between consecutive samples. */
		private static final int AUTH_PERIOD = 7 * 1000;

		/** Recording time of the data. */
		private static final int RECORD_TIME = 3 * 1000;

		/**
		 * Threshold used in transforming Euclidean distance into a probability.
		 */
		private static final double THRESHOLD = 2;

		/** Logging tag. */
		private static final String TAG = "LocationServiceThread";

		/** Flag used to gently stop the thread. */
		private volatile boolean stop;

		/** DAO used to interface with the voice recognition library. */
		private VoiceDAO voiceDAO;

		public AuthenticatorThread() {
			this.stop = false;
		}

		@Override
		public void run() {
			// instantiating voice DAO when thread starts.

			// sampling loop.
			while (stop != true) {
				try {
					Log.d(TAG, "Start loop.");
					Thread.sleep(AUTH_PERIOD);


					Log.d(TAG, "Getting score.");

					// sending match score.
					score = 0;
					sendDecayedScore(true);

					// starts the decay process
					LocationService.this.startDecay();

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
