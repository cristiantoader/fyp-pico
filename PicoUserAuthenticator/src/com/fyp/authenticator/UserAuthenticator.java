/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission.
 * 
 */

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
 * Class that implements most of the functionality used by UAService.
 * 
 * @author cristi
 * 
 */
@SuppressWarnings("unused")
public class UserAuthenticator {

	/** UAService service reference used for binding on other services. */
	private Service uaservice = null;

	/** List of available authentication mechanism services. */
	private LinkedList<AuthMech> mechanisms;

	/** Overall confidence level. */
	private int confidence;

	/** Tag used for debugging. */
	private static final String TAG = "UserAuthenticator";

	/**
	 * Private constructor for the singleton object. Initialises the list of
	 * available devices and calculates an initial confidence level.
	 */
	public UserAuthenticator(Service service) {
		this.uaservice = service;
		this.mechanisms = new LinkedList<AuthMech>();

		this.initAvailableMechianisms();
	}

	/**
	 * Getter for the overall confidence level.
	 * 
	 * The method is responsible of calculating the current overall confidence
	 * and returning the result as an integer ranging from 0 to 100.
	 * 
	 * @return the overall confidence level.
	 */
	public int getConfidence() {
		Log.d(TAG, "getConfidence()");
		
		this.calculateConfidence();
		return this.confidence;
	}

	/**
	 * Calculates the confidence level of the authenticator based on existing
	 * authentication mechanisms.
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
			Log.w(TAG, "Truncating confidence from " + confidence);
			confidence = 100;
		}

		this.confidence = confidence;
		Log.i(TAG, "Authentication confidence: " + this.confidence);
	}

	/**
	 * Initialises a list of registered authentication mechanisms.
	 * 
	 * A list of registered AuthMech objects is initialised. This indirectly
	 * results in the launch of the authentication mechanism services.
	 * 
	 */
	private void initAvailableMechianisms() {
//		this.mechanisms.add(new AuthMech(uaservice, DummyService.class));
		this.mechanisms.add(new AuthMech(uaservice, VoiceService.class));
		this.mechanisms.add(new AuthMech(uaservice, FaceService.class));
		this.mechanisms.add(new AuthMech(uaservice, LocationService.class));
	}
	
	/**
	 * Method used for stopping authentication mechanisms.
	 */
	public void stopMechanisms() {
		Log.d(TAG, "stopMechanisms+");
		
		for (AuthMech mech : this.mechanisms) {
			Log.d(TAG, "stopping " + mech.getClass());
			
			mech.doUnbindService();
			
			Intent i = new Intent(this.uaservice, mech.getClass());
			this.uaservice.stopService(i);
		}
	}

}
