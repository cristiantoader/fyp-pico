package com.fyp.authenticator.voice;

import java.io.File;
import java.util.List;

import com.bitsinharmony.recognito.Recognito;

public class AuthDevVoiceDAO {

	private Recognito<String> recognito = new Recognito<String>();

	public AuthDevVoiceDAO() {
		// trainRecognito();
	}

	public void addVocalPrint(String fileName) {
		try {
			recognito.createVocalPrint("owner", new File(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getMatch(String filename) {
		List<String> matches = null;

		try {
			matches = recognito.recognize(new File(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return matches.get(0).equals("owner");
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
