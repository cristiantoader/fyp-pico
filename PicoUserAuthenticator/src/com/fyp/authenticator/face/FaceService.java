package com.fyp.authenticator.face;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

public class FaceService extends AuthMechService {

	private AuthenticatorThread faceThread = null;

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

	private boolean isSupported() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	private class AuthenticatorThread extends Thread {
		private volatile boolean running = false;
		private volatile AtomicBoolean faceReady = new AtomicBoolean(false);

		private FaceDAO dao = null;

		private static final int AUTH_PERIOD = 3 * 1000;

		private Camera camera = null;
		private volatile Bitmap picture = null;

		public void run() {
			this.dao = new FaceDAO(FaceService.this);

			Log.d(TAG, "initialise camera...");
			while(this.initialiseCamera() != true) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			while (this.running) {
				try {
					Log.d(TAG, "start...");
					int score = 0;
					double dscore = 0;

					Thread.sleep(AUTH_PERIOD);
					
					this.camera.setPreviewTexture(new SurfaceTexture(1));
					this.camera.startPreview();
					
					Log.d(TAG, "taking picture...");
					this.camera.takePicture(null, null, jpgCallpack);

					// get match based on image
					while (this.faceReady.compareAndSet(true, false)
							|| this.picture == null) {
						Log.d(TAG, "waiting for picture...");
						Thread.sleep(50);
					}

					dscore = this.dao.getMatch(this.picture);
					score = (int) (Math.floor(dscore) * 100);
					
					Log.d(TAG, "dscore: " + dscore);
					Log.d(TAG, "Sending score " + score + "...");
					
					clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS, score, 0));

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (IOException e) {
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

				this.camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

				if (FaceService.this.getResources().getConfiguration().orientation != 
						Configuration.ORIENTATION_LANDSCAPE) {
					Log.d(TAG, "initialiseCamera: orientation 90");
					camera.setDisplayOrientation(90);
					
				} else {
					Log.d(TAG, "initialiseCamera: orientation 0");
					camera.setDisplayOrientation(0);
				}

				
				success = true;
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Log.d(TAG, "initialiseCamera- " + success);
			return success;
		}
		
		// TODO: this code repeats in OwnerPictureCallback
		PictureCallback jpgCallpack = new PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.d("CAMERA", "onPictureTaken - raw");

//				camera.stopPreview();
//				camera.unlock();
//				camera.release();

				Bitmap bmp = null;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;	
				
				if (data == null) {
					Log.e("FaceService", "null data");
					return;
				}

				bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				if (bmp == null) {
					Log.e("FaceService", "Decoded bmp is null!");
					return;
				} else {
					Log.d("FaceService", "Decoded bmp is ok!");
				}
				
				Matrix rotMatrix = new Matrix();
				rotMatrix.postRotate(270);
				
				bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), rotMatrix, true);
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
