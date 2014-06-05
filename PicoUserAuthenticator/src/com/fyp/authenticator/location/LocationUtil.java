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

public class LocationUtil {

	private static final String TAG = "LocationUtil";

	private Context ctx = null;

	private LocationManager lm = null;

	private LocationRecorder lr = null;

	private LinkedList<Location> locations = null;

	public LocationUtil(Context ctx) {
		this.ctx = ctx;
		lm = (LocationManager) this.ctx
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public Location getCurrentLocation() {
		Location location = null;
		Log.d(TAG, "getCurrentLocation()+");
		
		initialiseLocationUpdates(500);
		
		// acquire location
		Criteria criteria = new Criteria();
		String provider = lm.getBestProvider(criteria, true);
		location = lm.getLastKnownLocation(provider);
		
		this.lm.removeUpdates(locationListener);

		Log.d(TAG, "getCurrentLocation- " + location.getLatitude() + " "
				+ location.getLongitude() + " " + location.getProvider());
		return location;
	}

	public void startCollectingLocations(int interval) {
		this.locations = new LinkedList<Location>();

		// starting location updates
		initialiseLocationUpdates(interval);

		this.lr = new LocationRecorder(2000);
		this.lr.startRecord();
	}

	public void stopCollectingLocations() {
		if (this.lr == null) {
			Log.d(TAG, "Location recorder was not running.");
			return;
		}

		this.lr.stopRecord();
		this.lr = null;

		this.lm.removeUpdates(locationListener);
	}

	public void saveCollectedLocations(String name) {
		LocationDAO dao = new LocationDAO(this.ctx, name);
		dao.saveOwnerData(locations);
		dao = null;

		this.locations.clear();
		this.locations = null;
	}

	private void initialiseLocationUpdates(int interval) {
		// starting location updates
		this.lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				interval, 0, locationListener);
		this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval,
				0, locationListener);
	}

	private class LocationRecorder implements Runnable {
		/** Time interval after which decay occurs. */
		private int interval = 0;

		/** Wrapped handler object used for task scheduling. */
		private Handler handler = null;

		private volatile boolean running = false;

		public LocationRecorder(int interval) {
			this.interval = interval;
		}

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
