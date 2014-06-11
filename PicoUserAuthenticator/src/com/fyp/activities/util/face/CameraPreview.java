package com.fyp.activities.util.face;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class was originally developed by Google Android authors.
 * 
 * This class is based on code downloaded from [1], with small changes. The link
 * was last visited on 06.11.2014.
 * 
 * [1] http://developer.android.com/guide/topics/media/camera.html
 */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	/** Surface holder for displaying current data from camera. */
	private SurfaceHolder mHolder;

	/** Camera object for gathering data. */
	private Camera mCamera;

	/** Logging tag used for debugging. */
	private static final String TAG = "CameraPreview";

	/**
	 * Constructor for the camera preview.
	 * 
	 * The class registers the camera and context used for displaying the
	 * preview.
	 * 
	 * @param context
	 *            Component context used with the preview.
	 * @param camera
	 *            Camera object for which the preview is displayed.
	 */
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);

		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}
}
