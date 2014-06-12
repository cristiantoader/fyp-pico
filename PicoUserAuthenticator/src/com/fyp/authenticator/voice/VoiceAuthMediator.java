/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

package com.fyp.authenticator.voice;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;

import com.bitsinharmony.recognito.Recognito;

/**
 * Class used to mediate calls to the Recognito library.
 * 
 * @author cristi
 * 
 */
public class VoiceAuthMediator {
	
	/**Android context used for file IO*/
	private Context ctx = null;
	
	/** Reference to recognito library object */
	private Recognito<String> recognito = new Recognito<String>();

	/** Logging tag used for debugging. */
	private static final String TAG = "VoiceAuthMediator";

	/**
	 * Main constructor.
	 * 
	 * The constructor registers the application context and trains the
	 * recognito library object.
	 * 
	 * @param ctx
	 *            android context for application path.
	 */
	public VoiceAuthMediator(Context ctx) {
		this.ctx = ctx.getApplicationContext();
		
		trainRecognito();
	}

	/**
	 * Method used for training the recognito object using owner and noise data.
	 */
	private void trainRecognito() {
		Log.i(TAG, "trainRecognito+");

		VoiceDAO owner = new VoiceDAO(this.ctx, VoiceDAO.OWNER_FN);
		LinkedList<VoiceDAO> noises = VoiceDAO.getNoiseDAOs(this.ctx);
		
		// check for owner data.
		if (!owner.hasRecording()) {
			Log.e(TAG, "Null owner recording data!");
			return;
		
		} else {
			Log.d(TAG, "Owner rate:" + VoiceDAO.getSampleRate());
		}
		
		// adding owner data
		recognito.createVocalPrint("owner", owner.getData(),
				VoiceDAO.getSampleRate());

		// check for noise data
		if (noises.isEmpty()) {
			Log.d(TAG, "No noise data!");
			return;
		}
		
		for(VoiceDAO noise : noises) {
			Log.d(TAG, "Adding noise file: " + noise.getName());
			recognito.createVocalPrint(noise.getName(), 
					noise.getData(), 
					VoiceDAO.getSampleRate());
		}
		
		Log.i(TAG, "trainRecognito-");
	}
	
	/**
	 * Return's the match distance between the current recording and the owner.
	 * The result is returned as a Euclidean distance.
	 * 
	 * @param record
	 *            VoiceDAO challenge used for recognition.
	 * @return Euclidean distance indicating the confidence level that the
	 *         record represents the owner.
	 */
	public double getMatch(VoiceDAO record) {
		boolean owner = false;
		double result = Double.MAX_VALUE;
		
		Map<Double, String> matches = null;

		if (record == null || record.getData() == null) {
			Log.e(TAG, "Record file does not exist:" + record.getName());
			return 0;
		}

		matches = recognito.recognize(record.getData(),
				VoiceDAO.getSampleRate());

		for (Entry<Double, String> entry : matches.entrySet()) {			
			if (entry.getKey() < result) {
				result = entry.getKey();
				owner = entry.getValue().equals("owner");
			}
		}
		
		if (!owner) {
			Log.d(TAG, "Owner not best value!");
			result = - 1;
		}

		return result;
	}

}
