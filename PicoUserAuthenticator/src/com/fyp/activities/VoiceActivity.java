/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

package com.fyp.activities;

import com.fyp.authenticator.voice.VoiceDAO;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Main activity used for setting up the voice recognition mechanism.
 * 
 * This component provides an interface for the user to register owner and noise
 * sample data. This information will later be used when training the voice
 * recognition mechanism.
 * 
 * @author cristi
 * 
 */
@SuppressWarnings("unused")
public class VoiceActivity extends Activity {
	/** Text box used for displaying current recording time. */
	private EditText timerDisplay = null;

	/** Owner record button. */
	private Button mRecordOwnerButton = null;
	/** Noise record button. */
	private Button mRecordBackgroundButton = null;

	/** Android logging tag.*/
	private static final String TAG = "VoiceActivity";

	/**
	 * Used for managing click events for the record button.
	 * 
	 * The class is used for starting and stopping a recording, both for owner
	 * and noise data.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthRecordListener implements OnClickListener {
		/** Button used with this action listener object.*/
		private Button button = null;
		
		/** Recording start time. */
		private long mStartTime = 0;

		/** VoiceDAO object used for managing recorded data. */
		private VoiceDAO record = null;

		/** File name of recorded data. */
		private String fileName = null;

		/** Handler used for reposting timer updates. */
		private Handler timerHandler = new Handler();
		/** Runnable that sets the current time in the timer display. */
		private Runnable timerRunable = new Runnable() {
			@Override
			public void run() {
				long millis = System.currentTimeMillis() - mStartTime;
				int seconds = (int) (millis / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;

				timerDisplay.setText(String.format("%d:%02d", minutes, 
						seconds));
				timerHandler.postDelayed(this, 500);
			}

		};

		/**
		 * Constructor for the listener class.
		 * 
		 * @param isOwner
		 *            determines if the recording will be for the owner or
		 *            noise.
		 */
		public AuthRecordListener(boolean isOwner) {
			if (isOwner) {
				this.fileName = VoiceDAO.OWNER_FN;
				this.button = VoiceActivity.this.mRecordOwnerButton;
			} else {
				this.fileName = VoiceDAO.generateNoiseFileName(
						VoiceActivity.this);
				this.button = VoiceActivity.this.mRecordBackgroundButton;
			}
		}

		/**
		 * On click method called when the button registered with this listener
		 * is pressed.
		 * 
		 * The method performs different tasks based on whether the recording
		 * has started or not.
		 */
		public void onClick(View v) {
			// start recording
			if (!isRecording()) {
				startTimer();
				startRecording();
				this.button.setText("Stop");

				// stop recording
			} else {
				stopTimer();
				stopRecording();
				this.button.setText("Record");
			}
		}

		/** Method responsible of starting the recording timer display. */
		private void startTimer() {
			this.mStartTime = System.currentTimeMillis();
			timerDisplay.setText(String.format("%d:%02d", 0, 0));
			timerHandler.postDelayed(timerRunable, 1000);
		}

		/** Method responsible of stopping the recording timer display. */
		private void stopTimer() {
			this.mStartTime = 0;
			timerHandler.removeCallbacks(timerRunable);
		}

		/** Method that starts the recording process using a VoiceDAO. */
		private void startRecording() {
			this.record = new VoiceDAO(VoiceActivity.this, this.fileName);
			this.record.startRecord();
		}

		/**
		 * Method that stops the recording process and saves data to internal
		 * storage.
		 */
		private void stopRecording() {
			this.record.stopRecord();
			this.record.saveRecording();
			this.record = null;
		}

		/**
		 * Determines whether a recording was currently started using this
		 * listener.
		 */
		private boolean isRecording() {
			return this.record != null;
		}

	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_voice);

		this.mRecordOwnerButton = (Button) findViewById(
				R.id.ButtonRecognitoRecordOwner);
		this.mRecordOwnerButton.setOnClickListener(
				new AuthRecordListener(true));

		this.mRecordBackgroundButton = (Button) findViewById(
				R.id.ButtonRecognitoRecordBackground);
		this.mRecordBackgroundButton.setOnClickListener(
				new AuthRecordListener(false));

		this.timerDisplay = (EditText) findViewById(R.id.timer1);
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
