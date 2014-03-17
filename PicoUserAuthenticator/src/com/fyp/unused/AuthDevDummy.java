package com.fyp.unused;

// TODO: outdated

//import java.util.Random;

/**
 * TODO: dummy class subject to change and only acts as a proof of
 * concept/testing.
 * 
 * @author cristi
 * 
 */
public class AuthDevDummy {

	private static AuthDevDummy device = null;

	private AuthDevDummy() {
		// this.calculateConfidence();
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

	// @Override
	// public int getConfidence() {
	// this.calculateConfidence();
	// return this.confidence;
	// }
	//
	// private void calculateConfidence() {
	// this.confidence = new Random().nextInt(100);
	// }
}
