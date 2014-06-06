package com.fyp.authenticator.dummy;

import java.util.Random;

public class DummyAuthMediator {
	
	private Random rand = null;
	
	public DummyAuthMediator() {
		this.rand = new Random();
	}
	
	public int getMatch() {
		return this.rand.nextInt(100);
	}
}
