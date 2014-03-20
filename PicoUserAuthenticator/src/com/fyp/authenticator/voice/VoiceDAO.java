package com.fyp.authenticator.voice;

import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;

import com.bitsinharmony.recognito.Recognito;
import com.bitsinharmony.recognito.VocalPrint;

public class VoiceDAO {
	/** Owner voice record manager. */
	private VoiceRecord ownerRecord = null;

	/** Reference to recognito library object */
	private Recognito<String> recognito = new Recognito<String>();

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *          android context for application path.
	 */
	public VoiceDAO(Context ctx) {
		this.ownerRecord = new VoiceRecord(ctx, "owner.3gp");
		trainRecognito();
	}

	/**
	 * Trains recognito library for the owner's voice. Searches through internal
	 * data in order to train the recognito library.
	 */
	public void trainRecognito() {
		Log.i("trainRecognito", "entering");

		if (!this.ownerRecord.hasRecording()) {
			Log.e(this.getClass().toString(), "Null owner recording data..");
			return;
		}
		
		Log.i("trainRecognito", "recording data" + this.ownerRecord.getRecordingData());
		Log.i("trainRecognito", "recording data len" + this.ownerRecord.getRecordingData().length);
		Log.i("trainRecognito", "recording sample rate" + this.ownerRecord.getSampleRate());


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
	public double getMatch(VoiceRecord record) {
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
