package com.fyp.activities;

import java.io.File;

import com.fyp.authenticator.voice.VoiceAuthMediator;
import com.fyp.authenticator.voice.VoiceDAO;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RecognitoActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "AudioRecordTest";
	private static final String FILE_NAME = "owner.3gp";

	private EditText timerDisplay = null;

	private Button mRecordButton = null;
	private Button mSaveButton = null;

	OnClickListener clickerRecord = new OnClickListener() {
		private boolean recording = false;
		private long mStartTime = 0;
		private VoiceDAO record = null;

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

		private void startTimer() {
			this.mStartTime = System.currentTimeMillis();
			timerDisplay.setText(String.format("%d:%02d", 0, 0));
			timerHandler.postDelayed(timerRunable, 1000);
		}

		private void stopTimer() {
			this.mStartTime = 0;
			timerHandler.removeCallbacks(timerRunable);
		}

		public void onClick(View v) {
			if (recording == false) {
				startTimer();
				startRecording();
				mRecordButton.setText("Stop");

			} else {
				stopTimer();
				stopRecording();
				mRecordButton.setText("Record");
			}
		}

		private void startRecording() {
			this.recording = true;
			this.record = new VoiceDAO(RecognitoActivity.this, FILE_NAME);
			this.record.startRecord();
		}

		private void stopRecording() {
			this.recording = false;
			this.record.stopRecord();
		}

	};

	OnClickListener clickerTest = new OnClickListener() {

		public void onClick(View v) {
			if (recordingExists()) {
				Log.i("RecognitoActivity", "recording exists!");

				@SuppressWarnings("unused")
				VoiceAuthMediator voiceDAO = new VoiceAuthMediator(RecognitoActivity.this);

			} else {
				Log.i("RecognitoActivity", "recording not present!");
			}
		}

		private boolean recordingExists() {
			return new File(getFilesDir() + "/" + FILE_NAME).exists();
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_recognito);

		this.mRecordButton = (Button) findViewById(R.id.ButtonRecognitoRecord);
		this.mRecordButton.setOnClickListener(clickerRecord);

		this.mSaveButton = (Button) findViewById(R.id.ButtonRecognitoTest);
		this.mSaveButton.setOnClickListener(clickerTest);

		this.timerDisplay = (EditText) findViewById(R.id.timer1);
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
