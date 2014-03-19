package com.fyp.authenticator.voice;

import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;

import com.bitsinharmony.recognito.Recognito;
import com.bitsinharmony.recognito.VocalPrint;

public class AuthDevVoiceDAO {
	/** Owner voice record manager. */
	private AuthDevVoiceRecord ownerRecord = null;

	/** Reference to recognito library object */
	private Recognito<String> recognito = new Recognito<String>();

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *          android context for application path.
	 */
	public AuthDevVoiceDAO(Context ctx) {
		this.ownerRecord = new AuthDevVoiceRecord(ctx, "owner.3gp");
		trainRecognito();
	}

	/**
	 * Trains recognito library for the owner's voice. Searches through internal
	 * data in order to train the recognito library.
	 */
	public void trainRecognito() {
		if (!this.ownerRecord.hasRecording()) {
			Log.e(this.getClass().toString(), "Null owner recording data..");
			return;
		}

		// TODO: remove after testing is over.
		@SuppressWarnings("unused")
		VocalPrint result = recognito.createVocalPrint("owner",
				this.ownerRecord.getRecordingData(), this.ownerRecord.getSampleRate());
	}


	/**
	 * Return's the match distance between the current recording and the owner.
	 * 
	 * @param filename
	 * @return recording match indicator.
	 */
	public double getMatch(AuthDevVoiceRecord record) {
		double result = 0;
		Map<Double, String> matches = null;

		if (record == null || !record.hasRecording()) {
			return 0;
		}

		matches = recognito.recognize(record.getRecordingData(),
				record.getSampleRate());

		for (Entry<Double, String> entry : matches.entrySet()) {

			if (entry.getValue().equals("owner")) {
				Log.i("recognito", "found owner at distance: " + entry.getKey());
				result = entry.getKey();
				break;
			}
		}

		return result;
	}

}
