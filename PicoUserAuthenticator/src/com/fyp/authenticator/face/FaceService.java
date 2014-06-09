package com.fyp.authenticator.face;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

/**
 * Face recognition mechanism used in the proposed scheme.
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
			this.faceThread.stopThread();
			this.faceThread = null;
		}

		this.decayTimer.stopTimer();
		Log.d(TAG, "onDestroy-");
	}

	/**
	 * Returns true if the mechanism is supported by hand held hardware.
	 * 
	 * @return
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

		/** DAO used to interface with the face recognition library. */
		private FaceAuthMediator mediator = null;

		/** Authentication period used between successful camera sampling. */
		private static final int AUTH_PERIOD = 3 * 1000;

		/** Euclidean distance threshold used in calculating confidence. */
		private static final double THRESHOLD = 1;

		private FaceDAO cameraUtil = null;

		public void run() {
			// instantiate face DAO when thread starts
			this.mediator = new FaceAuthMediator(FaceService.this);

			this.cameraUtil = new FaceDAO(FaceService.this);

			// data sampling loop
			while (this.running) {
				try {
					Log.d(TAG, "Loop start.");
					double dscore = 0;

					Thread.sleep(AUTH_PERIOD);

					Log.d(TAG, "initialise camera...");
					while (this.cameraUtil.initialiseCamera() != true) {
						Thread.sleep(50);
					}

					Log.d(TAG, "taking picture...");
					Bitmap picture = this.cameraUtil.takePicture();

					dscore = this.mediator.getMatch(picture);
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}

					score = (int) (Math.floor((1 - dscore / THRESHOLD) * 100));
					sendDecayedScore(true);

					// start decaying process after collecting data
					FaceService.this.startDecay();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

		public void startThread() {
			this.running = true;
			this.start();
		}

		public void stopThread() {
			try {
				this.running = false;
				this.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
