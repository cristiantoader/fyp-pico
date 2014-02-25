package com.fyp.logic;

import java.util.LinkedList;

import com.fyp.logic.authenticators.AuthDev;
import com.fyp.logic.authenticators.AuthDevDummy;

public class UserAuthenticator {

	private int confidence;
	private LinkedList<AuthDev> devices;

	private static final int authThreshold = 50;

	public UserAuthenticator() {
		// initialise list and get available devices.
		this.devices = new LinkedList<AuthDev>();
		this.getAvailableDevices();

		// calculate initial confidence level.
		this.calculateConfidence();
	}

	/**
	 * Returns true if the user is authenticated.
	 * 
	 * @return true if user is authenticated.
	 */
	public boolean isAuthenticated() {
		this.calculateConfidence();

		return (this.confidence >= UserAuthenticator.authThreshold);
	}

	/**
	 * Calculates the confidence level of the authenticator based on existing
	 * authentication devices.
	 */
	private void calculateConfidence() {
		int confidence = 0;

		for (AuthDev device : this.devices) {
			confidence += device.getConfidence();
		}

		// cap the confidence level at 100
		if (confidence > 100) {
			confidence = 100;
		}

		this.confidence = confidence;
	}

	/**
	 * Checks the available devices that may be used for authentication and
	 * returns a list of devices.
	 * 
	 * TODO: add devices.
	 * 
	 * @return list of authentication devices.
	 */
	private void getAvailableDevices() {
		// dummy device
		if (AuthDevDummy.checkExists()) {
			this.devices.add(AuthDevDummy.getDevice());
		}
	}

}
