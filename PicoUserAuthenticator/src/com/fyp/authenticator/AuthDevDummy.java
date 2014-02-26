package com.fyp.authenticator;

import java.util.Random;

public class AuthDevDummy extends AuthDev {

	private static AuthDevDummy device = null;

	private AuthDevDummy() {
		this.calculateConfidence();
	}

	public static AuthDevDummy getDevice() {
		// return null if there is no camera
		if (checkExists() == false) {
			return null;
		}

		// return singleton object
		if (AuthDevDummy.device != null) {
			return device;
		} else {
			AuthDevDummy.device = new AuthDevDummy();
			return AuthDevDummy.device;
		}
	}

	public static boolean checkExists() {
		return true;
	}

	@Override
	public int getConfidence() {
		this.calculateConfidence();
		return this.confidence;
	}

	private void calculateConfidence() {
		this.confidence = new Random().nextInt(100);
	}
}
