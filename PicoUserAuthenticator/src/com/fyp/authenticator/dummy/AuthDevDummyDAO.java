package com.fyp.authenticator.dummy;

import java.util.Random;

public class AuthDevDummyDAO {
	
	private Random rand = null;
	
	public AuthDevDummyDAO() {
		this.rand = new Random();
	}
	
	public int getMatch() {
		return this.rand.nextInt(100);
	}
}
