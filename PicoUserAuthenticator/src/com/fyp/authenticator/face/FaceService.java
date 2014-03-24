package com.fyp.authenticator.face;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

public class FaceService extends AuthMechService {

	private AuthenticatorThread faceThread = null;

	@Override
	public void onCreate() {
		// TODO: figure out how to better support this.
		if (!isSupported()) {
			throw new RuntimeException("Camera not supported!");
		}

		if (this.faceThread == null) {
			this.faceThread = new AuthenticatorThread();
			this.faceThread.startThread();
		}
	}

	@Override
	public void onDestroy() {
		if (this.faceThread != null) {
			this.faceThread.stopThread();
			this.faceThread = null;
		}
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
		
		private FaceDAO dao = new FaceDAO();

		private static final int AUTH_PERIOD = 3 * 1000;

		private Camera camera = null;
		private Bitmap picture = null;
		
		public void run() {

			while (this.running) {
				try {
					Log.d("FaceService", "starting loop");
					int score = 0;

					// taking picture
					Thread.sleep(AUTH_PERIOD);
					Log.d("FaceService", "taking picture");
					this.initialiseCamera();
					this.camera.takePicture(shutterCallback, null, rawCallback);

					// get match based on image
					while(this.faceReady.compareAndSet(true, false))
						Log.d("FaceService", "Not ready!");
					
					double dscore = this.dao.getMatch(this.picture);
					Log.d("FaceService", "dscore: " + dscore);
					
					score = (int) Math.floor(dscore);

					// sending the score
					Log.d(this.getClass().toString(), "Sending score " + score + "...");
					clientWriter.send(Message
							.obtain(null, AUTH_MECH_GET_STATUS, score, 0));

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
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
		
		private void initialiseCamera() {
			try {
				this.camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
				this.camera.setPreviewTexture(new SurfaceTexture(1));
				this.camera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		PictureCallback rawCallback = new PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.d("CAMERA", "onPictureTaken - raw");
				
				camera.stopPreview();
				camera.release();
				
				Bitmap bmpRes = null;
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 8;
				
				if (data == null) {
					Log.e("FaceService", "null data");
					return;
				}
				
				bmpRes = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				if (bmpRes == null) {
					Log.e("FaceService", "Decoded bmp is null!");
					return;
				} else {
					Log.d("FaceService", "Decoded bmp is ok!");
				}
				
				AuthenticatorThread.this.picture = bmpRes;
				AuthenticatorThread.this.faceReady.set(true);

				Log.d("FaceService", "normal exit-");
			}
			
		};
		
		ShutterCallback shutterCallback = new ShutterCallback() {
			public void onShutter() {
				Log.i("CAMERA", "onShutter'd");
			}
		};
		
	}

}
