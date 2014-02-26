package com.fyp.authenticator;

public abstract class AuthDev {
	
	/** Variable which stores the confidence level in the device. */
	protected int confidence;

	/**
	 * Communicates with device in order to calculate the confidence level.
	 * 
	 * @return device confidence level that the user is correct.
	 */
	public abstract int getConfidence();
	
	// TODO: make it as an abstract factory returning different types of objects
	

}
