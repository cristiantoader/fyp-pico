/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

package com.fyp.authenticator.face;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

/**
 * Face recognition service mechanism used in the proposed scheme.
 * 
 * @author cristi
 * 
 */
public class FaceService extends AuthMechService {

	/** Thread used for periodic authentication. */
	private AuthenticatorThread faceThread = null;

	/** Logging tag. */
	private static final String TAG = "FaceService";

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate+");

		this.initialWeight = 8000;

		if (!isSupported()) {
			throw new RuntimeException("Camera not supported!");
		}

		if (this.faceThread == null) {
			this.faceThread = new AuthenticatorThread();
			this.faceThread.startThread();
		}

		Log.d(TAG, "onCreate-");
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy+");

		if (this.faceThread != null) {
			try {
				this.faceThread.stopThread();
				this.faceThread.join();
				this.faceThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (this.decayTimer != null) {
			this.decayTimer.stopTimer();
		}
		
		Log.d(TAG, "onDestroy-");
	}

	/**
	 * Returns true if the mechanism is supported by hand held hardware.
	 * 
	 * @return true if the mechanism is supported by hand held hardware.
	 */
	private boolean isSupported() {
		return getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA);
	}

	/**
	 * Thread used for periodic face authentication.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		/** Flag used for a gentle stop of the thread. */
		private volatile boolean running = false;

		/** Mediator used to interface with the face recognition library. */
		private FaceAuthMediator mediator = null;

		/** DAO for interfacing with the Android Camera. */
		private FaceDAO cameraUtil = null;
		
		/** Authentication sampling rate. */
		private static final int SAMPLING_RATE = 10 * 1000;

		/** Euclidean distance threshold used in calculating confidence. */
		private static final double THRESHOLD = 1;

		/**
		 * Main run() method for the thread.
		 * 
		 * This method is executed in order to provide periodic authentication
		 * data.
		 */
		public void run() {
			int peh = 0;
			long start = System.currentTimeMillis();
			
			// instantiate face DAO when thread starts
			this.mediator = new FaceAuthMediator(FaceService.this);

			this.cameraUtil = new FaceDAO(FaceService.this);

			Log.d(TAG, "Initialisation: " + (System.currentTimeMillis() - start));
			
			// data sampling loop
			while (this.running) {
				try {
					start = System.currentTimeMillis();
					
					Log.d(TAG, "Loop start.");
					double dscore = 0;

					Log.d(TAG, "initialise camera...");
					while (this.cameraUtil.initialiseCamera() != true) {
						Thread.sleep(50);
					}

					Log.d(TAG, "taking picture...");
					Bitmap picture = this.cameraUtil.takePicture();
					
					if (!FaceAuthMediator.hasFace(picture)) {
						Log.w(TAG, "No faces in picture. continue decay.");
						Thread.sleep(SAMPLING_RATE);
						continue;
					}
					
					Log.d(TAG, "Calculating score.");
					dscore = this.mediator.getMatch(picture);
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}

					peh = (int) (Math.floor((1 - dscore / THRESHOLD) * 100));
					sendDecayedScore(peh);

					// start decaying process after collecting data
					FaceService.this.startDecay();
					Log.d(TAG, "Authentication time: " + (System.currentTimeMillis() - start));
					
					Log.d(TAG, "sleeping " + SAMPLING_RATE);
					Thread.sleep(SAMPLING_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

		/** Method used for other objects to start this thread. */
		public void startThread() {
			this.running = true;
			this.start();
		}
		
		/** Method used for other objects to stop this thread. */
		public void stopThread() {
			this.running = false;
		}

	}

}
