/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission.
 * 
 */

package com.fyp.activities;

import com.fyp.activities.util.face.CameraPreview;
import com.fyp.activities.util.face.OwnerPictureCallback;
import com.fyp.authenticator.face.FaceDAO;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Main activity use for configuring the face recognition mechanism.
 * 
 * This class is used for gathering a face sample from the owner of the token.
 * This will be later used as training data for the face recognition algorithm.
 * 
 * @author cristi
 * 
 */
public class FaceActivity extends Activity {
	/** Camera object used for taking pictures. */
	private Camera camera = null;

	/** Standard Camera preview for the user. */
	private CameraPreview preview = null;

	/** Button used for acquiring a picture from the Camera. */
	private Button pictureButton = null;

	/**
	 * Anonymous listener used with the picture button. When pressed it launches
	 * a Camera.takePicture() event.
	 * 
	 */
	OnClickListener clickerPicture = new OnClickListener() {
		public void onClick(View v) {
			camera.takePicture(null, null, new OwnerPictureCallback(
					FaceActivity.this));
		}
	};

	/**
	 * Method called when the Activity is created.
	 * 
	 * This method is used for initialising the user interface and registering
	 * listeners.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_face);

		camera = new FaceDAO(this).getCameraInstance();
		preview = new CameraPreview(this, camera);

		FrameLayout previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
		previewLayout.addView(preview);

		this.pictureButton = (Button) findViewById(R.id.ButtonFaceCapture);
		this.pictureButton.setOnClickListener(clickerPicture);
	}

	/**
	 * Method called when the Activity is sent to background.
	 */
	@Override
	protected void onPause() {
		camera = null;
		super.onPause();
	}

}
