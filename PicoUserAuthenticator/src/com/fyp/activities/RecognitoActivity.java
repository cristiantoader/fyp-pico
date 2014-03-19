package com.fyp.activities;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

// TODO: http://stackoverflow.com/questions/8499042/android-audiorecord-example
public class RecognitoActivity extends Activity {
	private static final String LOG_TAG = "AudioRecordTest";
	private static final String FILE_NAME = "/owner.3gp";

	private EditText timerDisplay = null;

	private Button mRecordButton = null;
	private Button mPlayButton = null;

	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;


	OnClickListener clickerRecord = new OnClickListener() {
		private boolean mStartRecording = true;
		private long mStartTime = 0;

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

		public void onClick(View v) {
			onRecord(mStartRecording);
			mStartRecording = !mStartRecording;
		}

		private void onRecord(boolean start) {
			if (start) {
				startTimer();
				startRecording();
				mRecordButton.setText("Save");

			} else {
				stopTimer();
				stopRecording();
				mRecordButton.setText("Record");
			}
		}

		private void startTimer() {
			this.mStartTime = System.currentTimeMillis();
			timerDisplay.setText(String.format("%d:%02d", 0, 0));
			timerHandler.postDelayed(timerRunable, 1000);
		}

		private void startRecording() {
			try {
				mRecorder = new MediaRecorder();
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mRecorder.setOutputFile(getFilesDir() + FILE_NAME);
				mRecorder.prepare();
				mRecorder.start();
			} catch (IOException e) {
				Log.e(LOG_TAG, "prepare() failed");
			}
		}

		private void stopTimer() {
			this.mStartTime = 0;
			timerHandler.removeCallbacks(timerRunable);
		}

		private void stopRecording() {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	};

	OnClickListener clickerPlay = new OnClickListener() {
		private boolean mStartPlaying = true;

		public void onClick(View v) {
			onPlay(mStartPlaying);
			mStartPlaying = !mStartPlaying;
		}

		private void onPlay(boolean start) {
			if (start) {
				startPlaying();
			} else {
				stopPlaying();
			}
		}

		private void startPlaying() {
			try {
				mPlayer = new MediaPlayer();
				mPlayer.setDataSource(getFilesDir() + FILE_NAME);
				mPlayer.prepare();
				mPlayer.start();
			} catch (IOException e) {
				Log.e(LOG_TAG, "prepare() failed");
			}
		}

		private void stopPlaying() {
			mPlayer.release();
			mPlayer = null;
		}
	};


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_recognito);

		this.mRecordButton = (Button) findViewById(R.id.ButtonRecognitoRecord);
		this.mRecordButton.setOnClickListener(clickerRecord);

		this.mPlayButton = (Button) findViewById(R.id.ButtonRecognitoPlay);
		this.mPlayButton.setOnClickListener(clickerPlay);

		this.timerDisplay = (EditText) findViewById(R.id.timer1);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
