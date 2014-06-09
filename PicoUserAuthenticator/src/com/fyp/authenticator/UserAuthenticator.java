package com.fyp.authenticator;

import java.util.LinkedList;

import com.fyp.authenticator.dummy.DummyService;
import com.fyp.authenticator.face.FaceService;
import com.fyp.authenticator.location.LocationService;
import com.fyp.authenticator.voice.VoiceService;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * This only deals with authentication mechanisms. It communicates with them and
 * calculates confidence.
 * 
 * @author cristi
 * 
 */
@SuppressWarnings("unused")
public class UserAuthenticator {

	/** UAService service reference used for binding on other services. */
	private Service uaservice = null;

	/** List of available device services used for authentication. */
	private LinkedList<AuthMech> mechanisms;

	/** Overall confidence level. */
	private int confidence;

	private static final String TAG = "UserAuthenticator";

	/**
	 * Private constructor for the singleton object. Initialises the list of
	 * available devices and calculates an initial confidence level.
	 */
	public UserAuthenticator(Service service) {
		this.uaservice = service;
		this.mechanisms = new LinkedList<AuthMech>();

		this.initAvailableDevices();
	}

	/**
	 * Returns true if the user is authenticated.
	 * 
	 * @return true if user is authenticated.
	 */
	public int getConfidence() {
		Log.d(TAG, "getConfidence()");
		
		this.calculateConfidence();
		return this.confidence;
	}

	/**
	 * Calculates the confidence level of the authenticator based on existing
	 * authentication devices.
	 */
	private void calculateConfidence() {
		int confidence = 0;
		int weights = 0;
		
		Log.d(TAG, "calculateConfidence");
		
		for (AuthMech mech : this.mechanisms) {
			confidence += mech.getConfidence();
			weights += mech.getWeight();
		}

		weights = (weights == 0 ? 1 : weights);
		confidence /= weights;

		// cap the confidence level at 100
		if (confidence > 100) {
			confidence = 100;
		}

		this.confidence = confidence;
		Log.i(TAG, "Authentication confidence: " + this.confidence);
	}

	/**
	 * Checks the available devices that may be used for authentication and
	 * returns a list of devices.
	 * 
	 * @return list of authentication devices.
	 */
	private void initAvailableDevices() {
//		this.mechanisms.add(new AuthMech(uaservice, DummyService.class));
		this.mechanisms.add(new AuthMech(uaservice, VoiceService.class));
//		this.mechanisms.add(new AuthMech(uaservice, FaceService.class));
//		this.mechanisms.add(new AuthMech(uaservice, LocationService.class));
	}
	
	public void stopMechanisms() {
		Log.d(TAG, "stopMechanisms+");
		
		for (AuthMech mech : this.mechanisms) {
			Log.d(TAG, "stopping " + mech.getClass());
			
			Intent i = new Intent(this.uaservice, mech.getClass());
			this.uaservice.stopService(i);
		}
	}

}
