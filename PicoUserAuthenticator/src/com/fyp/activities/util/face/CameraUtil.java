package com.fyp.activities.util.face;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.fyp.authenticator.face.FaceService;

public class CameraUtil {

	private Camera camera = null;
	private static final String TAG = "CameraUtil";
	private Context context = null;
	
	/** Object used to trick the camera into providing input without displaying it. */
	private final SurfaceTexture dummyTexture = new SurfaceTexture(1);
	
	public CameraUtil(Context context) {
		this.context = context;
	}
	
	

	public boolean initialiseCamera() {
		boolean success = false;

		Log.d(TAG, "initialiseCamera+");

		try {
			Log.d(TAG, "initialiseCamera: open");

			this.camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

			if (this.context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
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
}
