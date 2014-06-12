/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

package com.fyp.activities;

import com.fyp.authenticator.location.LocationAuthMediator;
import com.fyp.authenticator.location.LocationDAO;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Main activity used for setting up the location analysis mechanism.
 * 
 * This component is responsible for providing the user with an interface for
 * collecting location data. This data will be later used for the location
 * analysis algorithm.
 * 
 * @author cristi
 * 
 */
public class LocationActivity extends Activity {
	/** Location DAO object used for collecting location data. */
	private LocationDAO locUtil = null;

	/** Button used for starting location sampling. */
	private Button mStartButton = null;
	/** Button used for stopping location sampling. */
	private Button mStopButton = null;

	/** Logging tag used by the class. */
	private static final String TAG = "LocationActivity";

	/**
	 * Anonymous event listener for mStartButton.
	 * 
	 * It initialises the location DAO object and starts location data
	 * collection.
	 * 
	 */
	OnClickListener mStartListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "StartListener+");

			if (locUtil != null) {
				Log.d(TAG, "Listener already started.");
				return;
			}

			locUtil = new LocationDAO(LocationActivity.this);
			locUtil.startCollectingLocations(2000);

			Log.d(TAG, "StartListener-");
		}
	};

	/**
	 * Anonymous event listener for mStopButton.
	 * 
	 * The listener stops data collected by the DAO, and issues a save command
	 * such that samples are saved in internal memory.
	 * 
	 */
	OnClickListener mStopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "StopListener+");

			if (locUtil != null) {
				locUtil.stopCollectingLocations();
				locUtil.saveCollectedLocations("owner-locations.dat");

				new LocationAuthMediator(LocationActivity.this,
						"owner-locations.dat").loadOwnerData();

				locUtil = null;
			}

			Log.d(TAG, "StopListener-");
		}
	};

	
	/**
	 * Method called when the Activity is created.
	 * 
	 * This method is used for initialising the user interface and registering
	 * listeners.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_location);

		this.mStartButton = (Button) findViewById(R.id.ButtonLocationStart);
		this.mStartButton.setOnClickListener(mStartListener);

		this.mStopButton = (Button) findViewById(R.id.ButtonLocationStop);
		this.mStopButton.setOnClickListener(mStopListener);

	}

	/**
	 * Method called when the Activity is sent to background.
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}

}
