package com.fyp.authenticator.voice;

import java.io.File;
import java.util.Random;

import android.util.Log;

import com.fyp.authenticator.AuthDev;

import marf.*;
import marf.util.*;

public class AuthDevVoice extends AuthDev {

	private static AuthDevVoice dev = null;
	private AuthDevVoiceDAO db = null;

	private AuthDevVoice() {
		this.confidence = 100;
		this.db = new AuthDevVoiceDAO("speakers.txt");

		this.initMARF();
	}

	public AuthDevVoice getDevice() {
		if (dev == null) {
			dev = new AuthDevVoice();
		}

		return dev;
	}

	@Override
	public int getConfidence() {
		this.calculateConfidence();
		return this.confidence;
	}

	public static boolean checkExists() {
		return true;
	}

	private void calculateConfidence() {
		this.confidence = new Random().nextInt(100);
	}

	private void initMARF() {
		try {
			db.connect();
			db.query();

			MARF.setPreprocessingMethod(MARF.DUMMY);
			MARF.setFeatureExtractionMethod(MARF.FFT);
			MARF.setClassificationMethod(MARF.EUCLIDEAN_DISTANCE);
			MARF.setDumpSpectrogram(false);
			MARF.setSampleFormat(MARF.WAV);

			MARF.DEBUG = false;

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Training
		 */
		try {
			// TODO: training files..
			File[] dir = new File("training").listFiles();

			for (int i = 0; i < dir.length; i++) {
				String strFileName = dir[i].getPath();

				if (strFileName.endsWith(".wav")) {
					MARF.setSampleFile(strFileName);

					int id = db.getIDByFilename(strFileName, true);
					if (id != -1) {
						MARF.setCurrentSubject(id);
						MARF.train();
					} else {
						Log.e("MARF", "No speaker found for \"" + strFileName + "\".");
					}
					
				}
			}
		} catch (NullPointerException e) {
			Log.e("MARG", "Folder not found.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done training on folder.");
	}
}
