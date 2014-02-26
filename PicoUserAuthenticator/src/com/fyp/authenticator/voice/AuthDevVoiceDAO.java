package com.fyp.authenticator.voice;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;
import com.bitsinharmony.recognito.Recognito;
import com.bitsinharmony.recognito.VocalPrint;

public class AuthDevVoiceDAO {

	private Recognito<String> recognito = new Recognito<String>();

	public AuthDevVoiceDAO() {
		trainRecognito();
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
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return matches.get(0).equals("owner");
	}
	
	private void trainRecognito() {
		File dir = new File("audio");
		
		for(File file : dir.listFiles()) {
			String fileName = file.getPath();
			
			if (fileName.endsWith(".wav")) {
				try {
					recognito.createVocalPrint("owner", file);
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}

// EOF
