package com.fyp.authenticator.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

public class LocationService extends AuthMechService {

	/** Logging tag. */
	private static final String TAG = "LocationService";
	
	private AuthenticatorThread locationThread = null;
	
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
		
		this.decayTimer.stopTimer();
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

		/**
		 * Number of acceptable meters threshold
		 */
		private static final double THRESHOLD = 2;

		/** Logging tag. */
		private static final String TAG = "LocationServiceThread";

		/** Flag used to gently stop the thread. */
		private volatile boolean stop;

		/** Service context. */
		private Context ctx = null;
		
		private LocationAuthMediator mediator = null;
		private LocationDAO dao = null;
		
		public AuthenticatorThread() {
			this.stop = false;
			this.ctx = LocationService.this;
		}

		@Override
		public void run() {
			Looper.prepare();

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
					score = (int) ((1 - dscore / THRESHOLD) * 100);
					
					Log.d(TAG, "Starting decay process for score " + score);
					sendDecayedScore(true);
					startDecay();
					
					Log.d(TAG, "Authentication time: " + (System.currentTimeMillis() - start));
					Thread.sleep(SAMPLING_RATE);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			this.stop = true;
		}

	}
}
