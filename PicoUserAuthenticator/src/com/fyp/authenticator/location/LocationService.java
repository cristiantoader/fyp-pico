/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission.
 * 
 */

package com.fyp.authenticator.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

/**
 * Location analysis service mechanism used in the proposed scheme.
 * 
 * @author cristi
 *
 */
public class LocationService extends AuthMechService {

	/** Thread used for periodic authentication. */
	private AuthenticatorThread locationThread = null;
	
	/** Logging tag. */
	private static final String TAG = "LocationService";
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		
		this.initialWeight = 8000;
		
		if (locationThread == null) {
			Log.d(TAG, "Starting location thread.");
			locationThread = new AuthenticatorThread();
			locationThread.start();
		}
		
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		
		if (locationThread != null) {
			Log.d(TAG, "Stopping location thread.");
			
			try {
				locationThread.stopThread();
				locationThread.join();
				locationThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (this.decayTimer != null) {
			this.decayTimer.stopTimer();
		}
	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		/** Authentication period between consecutive samples. */
		private static final int SAMPLING_RATE = 10 * 1000;

		/** Threshold for the number of acceptable meters.*/
		private static final double THRESHOLD = 2;

		/** Flag used to gently stop the thread. */
		private volatile boolean stop;

		/** Service context. */
		private Context ctx = null;
		
		/** Mediator object for location analysis algorithm. */
		private LocationAuthMediator mediator = null;
		/** Data access object used for collecting location samples. */
		private LocationDAO dao = null;
		
		/** Logging tag. */
		private static final String TAG = "LocationServiceThread";
		
		/**
		 * Basic constructor.
		 */
		public AuthenticatorThread() {
			this.stop = false;
			this.ctx = LocationService.this;
		}

		/**
		 * Main run() method for the thread.
		 * 
		 * This method is executed in order to provide periodic authentication
		 * data.
		 */
		@Override
		public void run() {
			Looper.prepare();

			int peh = 0;
			long start = System.currentTimeMillis();
			
			// instantiating voice DAO when thread starts.
			mediator = new LocationAuthMediator(ctx, "owner-locations.dat");
			mediator.loadOwnerData();
			
			dao = new LocationDAO(ctx);
			
			Log.d(TAG, "Initialisation time: " + (System.currentTimeMillis() - start));
			
			// sampling loop.
			while (stop != true) {
				try {
					start = System.currentTimeMillis();
					
					Log.d(TAG, "Getting current location.");
					Location current = dao.getCurrentLocation();
					if (current == null) {
						Log.w(TAG, "location not available, continue decay.");
						continue;
					}
					
					Log.d(TAG, "Getting score.");
					double dscore = mediator.getMinDistance(current);
					dscore = (dscore > THRESHOLD) ? THRESHOLD : dscore;
					
					Log.d(TAG, "Calculating percentage score.");
					peh = (int) ((1 - dscore / THRESHOLD) * 100);
					
					Log.d(TAG, "Starting decay process for score " + peh);
					sendDecayedScore(peh);
					startDecay();
					
					Log.d(TAG, "Authentication time: " + (System.currentTimeMillis() - start));
					Thread.sleep(SAMPLING_RATE);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/** Method used for other objects to stop this thread. */
		public void stopThread() {
			this.stop = true;
		}

	}
}
