package com.fyp.authenticator.voice;

import java.util.Random;

import com.fyp.authenticator.AuthDev;

import marf.*;
import marf.util.*;

public class AuthDevVoice extends AuthDev {

	private static AuthDevVoice dev = null;
	
	private AuthDevVoice() {
		this.confidence = 100;
	}
	
	public AuthDevVoice getDevice() {
		if(dev == null) {
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
	
}
