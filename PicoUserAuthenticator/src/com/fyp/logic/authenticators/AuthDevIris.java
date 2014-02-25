package com.fyp.logic.authenticators;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

public class AuthDevIris extends AuthDev implements PictureCallback {

	private static AuthDevIris device;
	
	private byte[] cameraData;
	
	private AuthDevIris() {
		this.confidence = 20;
		
		this.cameraData = null;
	}
	
	public static AuthDevIris getDevice() {
		// return null if there is no camera
		if(checkExists() == false) {
			return null;
		}
		
		// return singleton object
		if (AuthDevIris.device != null) {
			return device;
		} else {
			AuthDevIris.device = new AuthDevIris();
			return AuthDevIris.device;
		}
	}
	
	public static boolean checkExists() {
		if (Camera.getNumberOfCameras() > 0)
			return true;
		
		return false;
	}
	
	private boolean authenticateUser() {
		boolean auth = false;
		
		// compare and reach a result;
		
		return auth;
	}
	
	private void acquireCameraData() {
		byte[] result = null;
		getFrontCamera().takePicture(null, this, null);
		
		
		
	}
	
	private Camera getFrontCamera() {
		return Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
	}
	
	@Override
	public int getConfidence() {
		return this.confidence;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		this.cameraData = data;
		
	}

}
