package com.fyp.authenticator.face;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
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
		private FaceDAO dao = null;

		/** Authentication period used between successful camera sampling. */
		private static final int AUTH_PERIOD = 3 * 1000;
		
		// TODO: maybe move this in DAO?	
		/** Euclidean distance threshold used in calculating confidence. */
		private static final double THRESHOLD = 1;

		// TODO: maybe all camera related stuff should be in a camera DAO.
		/** Camera objed used to capture images.*/
		private Camera camera = null;
		
		/** Bitmap data collected by the camera. */
		private volatile Bitmap picture = null;

		/** Flag used to mark that face image data is ready for processing. */
		private volatile AtomicBoolean faceReady = new AtomicBoolean(false);
		
		/** Object used to trick the camera into providing input without displaying it. */
		private final SurfaceTexture dummyTexture = new SurfaceTexture(1);

		public void run() {
			// instantiate face DAO when thread starts
			this.dao = new FaceDAO(FaceService.this);

			// data sampling loop
			while (this.running) {
				try {
					Log.d(TAG, "Loop start.");
					double dscore = 0;

					Thread.sleep(AUTH_PERIOD);

					Log.d(TAG, "initialise camera...");
					while (this.initialiseCamera() != true) {
						Thread.sleep(50);
					}

					Log.d(TAG, "taking picture...");
					this.camera.takePicture(null, null, jpgCallpack);

					// get match based on image
					while (this.faceReady.compareAndSet(true, false)
							|| this.picture == null) {
						Log.d(TAG, "waiting for picture...");
						Thread.sleep(50);
					}

					dscore = this.dao.getMatch(this.picture);
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}

					score = (int) (Math.floor((1 - dscore / THRESHOLD) * 100));
					sendDecayedScore();

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

		private boolean initialiseCamera() {
			boolean success = false;

			Log.d(TAG, "initialiseCamera+");

			try {
				Log.d(TAG, "initialiseCamera: open");

				this.camera = Camera
						.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

				if (FaceService.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					Log.d(TAG, "initialiseCamera: orientation 90");
					camera.setDisplayOrientation(90);

				} else {
					Log.d(TAG, "initialiseCamera: orientation 0");
					camera.setDisplayOrientation(0);
				}

				this.camera.setPreviewTexture(dummyTexture);
				this.camera.startPreview();

				success = true;

			} catch (Exception e) {
				e.printStackTrace();
				this.camera.stopPreview();
				this.camera.release();
			}

			Log.d(TAG, "initialiseCamera- " + success);
			return success;
		}

		// TODO: this code repeats in OwnerPictureCallback
		PictureCallback jpgCallpack = new PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.d("CAMERA", "onPictureTaken - raw");

				camera.stopPreview();
				camera.unlock();
				camera.release();

				Bitmap bmp = null;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;

				if (data == null) {
					Log.e("FaceService", "null data");
					return;
				}

				bmp = BitmapFactory.decodeByteArray(data, 0, data.length,
						options);
				if (bmp == null) {
					Log.e("FaceService", "Decoded bmp is null!");
					return;
				} else {
					Log.d("FaceService", "Decoded bmp is ok!");
				}

				Matrix rotMatrix = new Matrix();
				rotMatrix.postRotate(270);

				bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
						bmp.getHeight(), rotMatrix, true);
				bmp = Bitmap.createScaledBitmap(bmp,
						(int) (0.5 * bmp.getWidth()),
						(int) (0.5 * bmp.getHeight()), true);

				AuthenticatorThread.this.picture = bmp;
				AuthenticatorThread.this.faceReady.set(true);

				Log.d("FaceService", "normal exit-");
			}

		};

	}

}
