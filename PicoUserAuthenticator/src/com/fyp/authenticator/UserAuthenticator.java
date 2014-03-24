package com.fyp.authenticator;

import java.util.LinkedList;

import com.fyp.authenticator.dummy.AuthDummyService;
import com.fyp.authenticator.face.FaceService;
import com.fyp.authenticator.voice.VoiceService;

import android.app.Service;

public class UserAuthenticator {

	/** UAService service reference used for binding on other services. */
	private Service uaservice = null;

	/** List of available device services used for authentication. */
	private LinkedList<AuthMech> mechanism;

	/** Default authentication threshold. */
	private static final int authThreshold = 50;
	/** Overall confidence level. */
	private int confidence;

	/**
	 * Private constructor for the singleton object. Initialises the list of
	 * available devices and calculates an initial confidence level.
	 */
	public UserAuthenticator(Service service) {
		this.uaservice = service;
		this.mechanism = new LinkedList<AuthMech>();

		this.initAvailableDevices();
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

		for (AuthMech mech : this.mechanism) {
			confidence += mech.getConfidence();
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
	 * TODO: may automate this using XML list in resources/
	 * 
	 * @return list of authentication devices.
	 */
	private void initAvailableDevices() {
		this.mechanism.add(new AuthMech(uaservice, AuthDummyService.class));
		this.mechanism.add(new AuthMech(uaservice, VoiceService.class));
//		this.mechanism.add(new AuthMech(uaservice, FaceService.class));
	}

}
