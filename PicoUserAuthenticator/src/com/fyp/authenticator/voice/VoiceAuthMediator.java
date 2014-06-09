package com.fyp.authenticator.voice;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;

import com.bitsinharmony.recognito.Recognito;

public class VoiceAuthMediator {
	
	/**Android context used for file IO*/
	private Context ctx = null;
	
	/** Reference to recognito library object */
	private Recognito<String> recognito = new Recognito<String>();

	private static final String TAG = "VoiceDAO";

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *            android context for application path.
	 */
	public VoiceAuthMediator(Context ctx) {
		this.ctx = ctx.getApplicationContext();
		
		trainRecognito();
	}

	/**
	 * Trains recognito library for the owner's voice. Searches through internal
	 * data in order to train the recognito library.
	 */
	private void trainRecognito() {
		Log.i(TAG, "trainRecognito+");

		VoiceDAO owner = new VoiceDAO(this.ctx, "owner.3gp", false);
		LinkedList<VoiceDAO> noises = VoiceDAO.getNoiseDAOs(this.ctx);
		
		// check for owner data.
		if (!owner.hasRecording()) {
			Log.e(TAG, "Null owner recording data..");
			return;
		
		} else {
			Log.d(TAG, "Owner length:" + owner.getData().length);
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
			Log.d(TAG, "Adding noise file: " + noise.getFileName());
			recognito.createVocalPrint(noise.getFileName(), 
					noise.getData(), 
					VoiceDAO.getSampleRate());
		}
		
		Log.i(TAG, "trainRecognito-");
	}
	
	/**
	 * Return's the match distance between the current recording and the owner.
	 * 
	 * @param filename
	 * @return recording match indicator.
	 */
	public double getMatch(VoiceDAO record) {
		boolean owner = false;
		double result = Double.MAX_VALUE;
		
		Map<Double, String> matches = null;

		if (record == null || !record.hasRecording()) {
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
