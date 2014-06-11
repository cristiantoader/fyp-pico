package com.fyp.authenticator.location;

import java.util.LinkedList;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Class used for managing location data.
 * 
 * @author cristi
 * 
 */
public class LocationDAO {

	/** Application context used for file operations. */
	private Context ctx = null;

	/** Object for accessing Android location services. */
	private LocationManager lm = null;

	/** Thraed used for recording multiple locations. */
	private LocationRecorder lr = null;

	/** Linked list used when collecting multiple locations. */
	private LinkedList<Location> locations = null;

	/** Tag used for debugging. */
	private static final String TAG = "LocationUtil";

	/**
	 * Main constructor.
	 * 
	 * Initialises the application context and Android location manager.
	 * 
	 * @param ctx
	 */
	public LocationDAO(Context ctx) {
		this.ctx = ctx;
		lm = (LocationManager) this.ctx
				.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Getter for the current location of the device.
	 * 
	 * The method works by initialising location updates at a fixed time
	 * interval. This forces the Android location manager to gather location
	 * data. One sample data is collected by this method, location updates are
	 * stopped, and the result is returned.
	 * 
	 * @return the current location of the device.
	 */
	public Location getCurrentLocation() {
		Location location = null;
		Log.d(TAG, "getCurrentLocation()+");

		initialiseLocationUpdates(500);

		// acquire location
		Criteria criteria = new Criteria();
		String provider = lm.getBestProvider(criteria, true);

		location = lm.getLastKnownLocation(provider);
		if (location != null) {
			Log.d(TAG, "getCurrentLocation- " + location.getLatitude() + " "
					+ location.getLongitude() + " " + location.getProvider());
		}

		this.lm.removeUpdates(locationListener);

		return location;
	}

	/**
	 * Method that starts periodic collection of location data.
	 * 
	 * @param interval
	 *            time interval between successful sampling.
	 */
	public void startCollectingLocations(int interval) {
		this.locations = new LinkedList<Location>();

		// starting location updates
		initialiseLocationUpdates(interval);

		this.lr = new LocationRecorder(2000);
		this.lr.startRecord();
	}

	/**
	 * Method that stops periodic collection of data.
	 */
	public void stopCollectingLocations() {
		if (this.lr == null) {
			Log.d(TAG, "Location recorder was not running.");
			return;
		}

		this.lr.stopRecord();
		this.lr = null;

		this.lm.removeUpdates(locationListener);
	}

	/**
	 * Used for saving multiple collected locations in internal storage.
	 * 
	 * @param name
	 *            name of the file where data is saved.
	 */
	public void saveCollectedLocations(String name) {
		LocationAuthMediator dao = new LocationAuthMediator(this.ctx, name);
		dao.saveOwnerData(locations);
		dao = null;

		this.locations.clear();
		this.locations = null;
	}

	/**
	 * Method that initialises android location manager for periodic location
	 * updates.
	 * 
	 * @param interval
	 *            time interval for periodic location updates.
	 */
	private void initialiseLocationUpdates(int interval) {
		// starting location updates
		this.lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				interval, 0, locationListener);
		this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval,
				0, locationListener);
	}

	/**
	 * Class used for creating a thread that periodically records locaiton data.
	 * 
	 * @author cristi
	 * 
	 */
	private class LocationRecorder implements Runnable {
		/** Sampling time interval. */
		private int interval = 0;

		/** Wrapped handler object used for task scheduling. */
		private Handler handler = null;

		/** Flag used for gently stopping the thread. */
		private volatile boolean running = false;

		/**
		 * Basic constructor that registers the sampling time interval.
		 * 
		 * @param interval
		 *            sampling time interval.
		 */
		public LocationRecorder(int interval) {
			this.interval = interval;
		}

		/**
		 * Main runnable method used for collecting periodic location updates.
		 * The best available provider is used for data sampling.
		 */
		@Override
		public void run() {
			Log.d(TAG, "timer run method.");

			// acquire location
			Criteria criteria = new Criteria();
			String provider = lm.getBestProvider(criteria, true);

			addLocation(lm.getLastKnownLocation(provider));

			// only reschedule if running is true
			if (this.running == true) {
				Log.d(TAG, "Reposting timer");
				this.handler.postDelayed(this, interval);
			}
		}

		/**
		 * Method used for starting the location recording thread.
		 * 
		 * This uses a Handler object in order to periodically schedule a
		 * runnable responsible of collecting the current location.
		 * 
		 * */
		public boolean startRecord() {
			Log.d(TAG, "startRecord+");

			if (handler != null || running == true) {
				return false;
			}

			this.running = true;

			HandlerThread hThread = new HandlerThread("hThread");
			hThread.start();

			handler = new Handler(hThread.getLooper());
			handler.postDelayed(this, interval);

			Log.d(TAG, "startRecord-");
			return true;
		}

		/** Method used for stopping the periodic location recorder. */
		public void stopRecord() {
			Log.d(TAG, "stopRecord+");

			// this is how we tell if the handler is stopped
			if (handler == null) {
				Log.d(TAG, "stopRecord: hadnler was null");
				return;
			}

			// stoping current task from rescheduling
			this.running = false;

			// removing pending tasks
			this.handler.removeCallbacks(this);
			this.handler = null;

			Log.d(TAG, "stopRecord-");
		}

		/**
		 * Method used for adding the current location to the list of samples.
		 * 
		 * @param location
		 *            location that is added to the list.
		 */
		public void addLocation(Location location) {

			if (location == null) {
				Log.w(TAG, "addLocation: location is null.");
				return;
			}

			Log.d(TAG,
					"addLocation: latitude=" + location.getLatitude()
							+ " longitude=" + location.getLongitude()
							+ " provider=" + location.getProvider()
							+ " accuracy=" + location.getAccuracy());

			locations.add(location);
		}

	}

	/**
	 * Anonymous dummy location listener used for collecting only one location.
	 * 
	 * This object offers no functionality, but forces the Android location
	 * manager to gather location data.
	 */
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			;
		}

		public void onProviderEnabled(String provider) {
			;
		}

		public void onProviderDisabled(String provider) {
			;
		}
	};
}
