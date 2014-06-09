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

public class FaceActivity extends Activity {
	private Camera mCamera = null;
	private CameraPreview mPreview = null;

	private Button mPictureButton = null;

	OnClickListener clickerPicture = new OnClickListener() {
		public void onClick(View v) {
			mCamera.takePicture(null, null, new OwnerPictureCallback(FaceActivity.this));
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_face);

		mCamera = new FaceDAO(this).getCameraInstance();
		mPreview = new CameraPreview(this, mCamera);
		
		FrameLayout previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
		previewLayout.addView(mPreview);

		this.mPictureButton = (Button) findViewById(R.id.ButtonFaceCapture);
		this.mPictureButton.setOnClickListener(clickerPicture);
	}

	@Override
	protected void onPause() {
		mCamera = null;
		super.onPause();
	}

}
