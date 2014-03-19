package com.fyp.authenticator.voice;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.bitsinharmony.recognito.Recognito;
import com.bitsinharmony.recognito.VocalPrint;

public class AuthDevVoiceDAO {

	private Recognito<String> recognito = new Recognito<String>();

	public AuthDevVoiceDAO() {
		// trainRecognito();
	}

	public VocalPrint addVocalPrint(String fileName) {
		VocalPrint result = null;
		
		try {
			result = recognito.createVocalPrint("owner", new File(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public VocalPrint addVocalPrint(double[] vocalSample, float sampleRate) {
		VocalPrint result = recognito.createVocalPrint("owner", vocalSample, sampleRate);
		return result;
	}

	public double getMatch(String filename) {
		double result = 0;
		Map<Double, String> matches = null;

		try {
			matches = recognito.recognize(new File(filename));
			for (Entry<Double, String> entry : matches.entrySet()) {
				if (entry.getValue().equals("owner")) {
					Log.i("recognito", "found owner at distance: " + entry.getKey());
					result = entry.getKey();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void trainRecognito() {
		File dir = new File("audio");

		for (File file : dir.listFiles()) {
			String fileName = file.getPath();

			if (fileName.endsWith(".wav")) {
				try {
					recognito.createVocalPrint("owner", file);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	}

}

// EOF
