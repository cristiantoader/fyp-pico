/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission.
 * 
 */

package com.fyp.authenticator.dummy;

import java.util.Random;

/**
 * Class used for generating random confidence levels.
 * 
 * This class was purely developed to abide to the general design of the
 * application.
 * 
 * @author cristi
 * 
 */
public class DummyAuthMediator {
	/** Object used for random number generation. */
	private Random rand = null;

	/**
	 * Basic constructor that initialises the random number generator.
	 * */
	public DummyAuthMediator() {
		this.rand = new Random();
	}

	/**
	 * Getter for the current confidence level.
	 * 
	 * @return the current confidence level.
	 */
	public int getMatch() {
		return this.rand.nextInt(100);
	}
}
