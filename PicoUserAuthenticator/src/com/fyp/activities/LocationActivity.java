package com.fyp.activities;

import com.fyp.authenticator.location.LocationUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LocationActivity extends Activity {

	protected static final String TAG = "LocationActivity";
	
	private LocationUtil locUtil = null;

	private Button mStartButton = null;
	private Button mStopButton = null;

	OnClickListener mStartListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "StartListener+");
			
			if (locUtil != null) {
				Log.d(TAG, "Listener already started.");
				return;
			}
			
			locUtil = new LocationUtil(LocationActivity.this);
			locUtil.startCollectingLocations(2000);

			Log.d(TAG, "StartListener-");
		}
	};

	OnClickListener mStopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "StopListener+");

			if (locUtil != null) {
				locUtil.stopCollectingLocations();
				locUtil.saveCollectedLocations("owner-locations.dat");
			}
			
			Log.d(TAG, "StopListener-");
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_location);

		this.mStartButton = (Button) findViewById(R.id.ButtonLocationStart);
		this.mStartButton.setOnClickListener(mStartListener);

		this.mStopButton = (Button) findViewById(R.id.ButtonLocationStop);
		this.mStopButton.setOnClickListener(mStopListener);

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
