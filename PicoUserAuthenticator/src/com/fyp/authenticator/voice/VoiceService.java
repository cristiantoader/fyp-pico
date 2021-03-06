/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

package com.fyp.authenticator.voice;

import com.fyp.authenticator.AuthMechService;

import android.util.Log;

/**
 * Voice recognition service mechanism used in the proposed scheme.
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
		private static final int SAMPLING_RATE = 10 * 1000;

		/** Recording time of sample data. */
		private static final int RECORD_TIME = 3 * 1000;

		/** Threshold used in transforming Euclidean distance into a probability.*/
		private static final double THRESHOLD = 2;

		/** Flag used to gently stop the thread. */
		private volatile boolean stop;

		/** Mediator for the voice recognition library. */
		private VoiceAuthMediator mediator = null;

		/** Data access object for recording data. */
		private VoiceDAO record = null;
		
		/** Logging tag. */
		private static final String TAG = "VoiceServiceThread";
		
		/**
		 * Basic constructor.
		 */
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
			long start = System.currentTimeMillis();
			int peh = 0;
			
			// instantiating voice DAO when thread starts.
			this.mediator = new VoiceAuthMediator(VoiceService.this);

			Log.d(TAG, "Initialisation time: " + (System.currentTimeMillis() - start));
			
			// sampling loop.
			while (stop != true) {
				try {
					start = System.currentTimeMillis();
					
					this.record = recordData();

					Log.d(TAG, "Getting score.");
					if (record == null || !record.hasRecording()) {
						Log.e(TAG, "record not created!");
						continue;
					}

					// sending match score.
					double dscore = this.mediator.getMatch(record);
					if (dscore == -1) {
						Log.w(TAG, "Noise detected, continuing decay.");
						Thread.sleep(SAMPLING_RATE);
						continue;
					}
					
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}

					peh = (int) Math.floor((1 - dscore / THRESHOLD) * 100);
					sendDecayedScore(peh);

					// starts the decay process
					VoiceService.this.startDecay();
					Log.d(TAG, "Authentication time: " + 
							(System.currentTimeMillis() - start - RECORD_TIME));

					Thread.sleep(SAMPLING_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/** Method used for stopping the thread. */
		public void stopThread() {
			this.stop = true;
		}

		/**
		 * Auxiliary method used to record a data sample using a VoiceDAO.
		 * 
		 * @return recording data sample as a VoiceDAO.
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
