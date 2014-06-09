package com.fyp.activities;

import com.fyp.authenticator.voice.VoiceDAO;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class VoiceActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "AudioRecordTest";
	

	/** Text box used for displaying current recording time. */
	private EditText timerDisplay = null;

	/** Owner record button. */
	private Button mRecordOwnerButton = null;
	/** Noise record button.*/
	private Button mRecordBackgroundButton = null;
	
	private class AuthRecordListener implements OnClickListener {
		/** Recording start time. */
		private long mStartTime = 0;
		
		/** VoiceDAO object used for managing recorded data.*/
		private VoiceDAO record = null;
		
		/** File name of recorded data.*/
		private String fileName = null;
		
		private Handler timerHandler = new Handler();
		private Runnable timerRunable = new Runnable() {
			@Override
			public void run() {
				long millis = System.currentTimeMillis() - mStartTime;
				int seconds = (int) (millis / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;

				timerDisplay.setText(String.format("%d:%02d", minutes, seconds));
				timerHandler.postDelayed(this, 500);
			}

		};
		
		public AuthRecordListener(boolean isOwner) {
			if (isOwner) {
				this.fileName = VoiceDAO.OWNER_FN;
			} else {
				this.fileName = VoiceDAO.getNoiseFileName(VoiceActivity.this);
			}
		}

		public void onClick(View v) {
			// start recording
			if (!isRecording()) {
				startTimer();
				startRecording();
				mRecordOwnerButton.setText("Stop");
				
			// stop recording
			} else {
				stopTimer();
				stopRecording();
				mRecordOwnerButton.setText("Record");
			}
		}

		private void startTimer() {
			this.mStartTime = System.currentTimeMillis();
			timerDisplay.setText(String.format("%d:%02d", 0, 0));
			timerHandler.postDelayed(timerRunable, 1000);
		}

		private void stopTimer() {
			this.mStartTime = 0;
			timerHandler.removeCallbacks(timerRunable);
		}
		
		private void startRecording() {
			this.record = new VoiceDAO(VoiceActivity.this, this.fileName);
			this.record.startRecord();
		}

		private void stopRecording() {
			this.record.stopRecord();
			this.record = null;
		}
		
		private boolean isRecording() {
			return this.record == null;
		}

	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_voice);

		this.mRecordOwnerButton = (Button) findViewById(R.id.ButtonRecognitoRecordOwner);
		this.mRecordOwnerButton.setOnClickListener(new  AuthRecordListener(true));

		this.mRecordBackgroundButton = (Button) findViewById(R.id.ButtonRecognitoRecordBackground);
		this.mRecordBackgroundButton.setOnClickListener(new  AuthRecordListener(false));

		this.timerDisplay = (EditText) findViewById(R.id.timer1);
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
