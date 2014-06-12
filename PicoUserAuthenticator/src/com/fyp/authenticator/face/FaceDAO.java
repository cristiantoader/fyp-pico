/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission.
 * 
 */

package com.fyp.authenticator.face;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

/**
 * Class used for mediating the interface with the camera.
 * 
 * It provides access to camera objects and support for gathering pictures.
 * 
 * @author cristi
 * 
 */
public class FaceDAO {
	/** Camera object used for gathering pictures. */
	private Camera camera = null;
	
	/** Application context used for file access. */
	private Context context = null;
	
	/** Bitmap associated with the DAO. */
	private volatile Bitmap picture = null;

	/** Flag used to mark that face image data is ready for processing. */
	private volatile AtomicBoolean faceReady = new AtomicBoolean(false);

	/**
	 * Object used to trick the camera into providing input without displaying
	 * it.
	 */
	private final SurfaceTexture dummyTexture = new SurfaceTexture(1);

	/** Tag used for debugging. */
	private static final String TAG = "FaceDAO";
	
	/**
	 * Basic constructor that registers application context.
	 * 
	 * @param context
	 *            application context.
	 */
	public FaceDAO(Context context) {
		this.context = context;
	}

	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance() {
		Camera camera = null;
		
		try {
			camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

			if (this.context.getResources().getConfiguration().orientation != 
					Configuration.ORIENTATION_LANDSCAPE) {
				camera.setDisplayOrientation(90);
			} else {
				camera.setDisplayOrientation(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return camera;
	}

	/** Method used for initialising the camera used with the DAO. */
	public boolean initialiseCamera() {
		boolean success = false;

		Log.d(TAG, "initialiseCamera+");

		try {
			Log.d(TAG, "initialiseCamera: open");
			this.camera = getCameraInstance();

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

	/**
	 * Method used for taking a picture from the camera.
	 * 
	 * The camera needs to be initialised prior to calling this method.
	 * 
	 * @return Picture acquired from the Camera.
	 */
	public Bitmap takePicture() {
		this.camera.takePicture(null, null, jpgCallpack);

		// get match based on image
		while (this.faceReady.compareAndSet(true, false) || picture == null) {
			Log.d(TAG, "waiting for picture...");

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return this.picture;
	}

	/**
	 * Anonymous callback object for pictures received from the camera. Input
	 * data is scaled to 50% to save memory when performing authentication.
	 */
	PictureCallback jpgCallpack = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("CAMERA", "jpgCallpack");

			camera.stopPreview();
			camera.unlock();
			camera.release();

			Bitmap bmp = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.RGB_565;

			if (data == null) {
				Log.e("FaceService", "null data");
				FaceDAO.this.picture = null;
				return;
			}

			bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			if (bmp == null) {
				Log.e("FaceService", "Decoded bmp is null!");
				FaceDAO.this.picture = null;
				return;
			}

			Matrix rotMatrix = new Matrix();
			rotMatrix.postRotate(270);

			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), rotMatrix, true);
			bmp = Bitmap.createScaledBitmap(bmp, (int) (0.5 * bmp.getWidth()),
					(int) (0.5 * bmp.getHeight()), true);

			FaceDAO.this.picture = bmp;
			FaceDAO.this.faceReady.set(true);

			Log.d("FaceService", "normal exit-");
		}

	};

}
