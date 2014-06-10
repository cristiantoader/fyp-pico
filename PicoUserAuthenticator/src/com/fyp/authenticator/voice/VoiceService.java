package com.fyp.authenticator.voice;

import com.fyp.authenticator.AuthMechService;

import android.util.Log;

/**
 * Observable service which needs to keep track of the user authentication
 * state. When requested, the UAService will require to know the current state
 * of this observable.
 * 
 * @author cristi
 * 
 */
public class VoiceService extends AuthMechService {

	/** Thread used to periodically authenticate the user and broadcast result. */
	private AuthenticatorThread voiceThread = null;

	/** Logging tag. */
	private static final String TAG = "AuthVoiceService";

	public void onCreate() {
		Log.i(TAG, "onCreate");

		this.initialWeight = 10000;

		if (voiceThread == null) {
			voiceThread = new AuthenticatorThread();
			voiceThread.start();
		}

	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");

		if (voiceThread != null) {
			try {
				voiceThread.stopThread();
				voiceThread.join();
				voiceThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (this.decayTimer != null) {
			this.decayTimer.stopTimer();
		}
	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		/** Authentication period between consecutive samples. */
		private static final int SAMPLING_RATE = 7 * 1000;

		/** Recording time of the data. */
		private static final int RECORD_TIME = 3 * 1000;

		/**
		 * Threshold used in transforming Euclidean distance into a probability.
		 */
		private static final double THRESHOLD = 2;

		/** Logging tag. */
		private static final String TAG = "VoiceServiceThread";

		/** Flag used to gently stop the thread. */
		private volatile boolean stop;

		/** DAO used to interface with the voice recognition library. */
		private VoiceAuthMediator mediator;

		public AuthenticatorThread() {
			this.stop = false;
		}

		@Override
		public void run() {
			// instantiating voice DAO when thread starts.
			this.mediator = new VoiceAuthMediator(VoiceService.this);

			// sampling loop.
			while (stop != true) {
				try {
					Log.d(TAG, "Start loop.");
					Thread.sleep(SAMPLING_RATE);

					VoiceDAO record = recordData();

					Log.d(TAG, "Getting score.");
					if (record == null || !record.hasRecording()) {
						Log.e(TAG, "record not created!");
						continue;
					}

					// sending match score.
					double dscore = this.mediator.getMatch(record);
					if (dscore == -1) {
						Log.w(TAG, "Noise detected, continuing decay.");
						continue;
					}
					
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}

					score = (int) Math.floor((1 - dscore / THRESHOLD) * 100);
					sendDecayedScore(true);

					// starts the decay process
					VoiceService.this.startDecay();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			this.stop = true;
		}

		/***
		 * Auxiliary method used to record a data sample.
		 * 
		 * @return
		 */
		private VoiceDAO recordData() {
			Log.d(TAG, "recordData+");

			VoiceDAO record = new VoiceDAO(VoiceService.this, "challenge.3gp");
			
			try {
				record.startRecord();
				Thread.sleep(RECORD_TIME);
				record.stopRecord();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "recordData- " + record);
			return record;
		}

	}

}
