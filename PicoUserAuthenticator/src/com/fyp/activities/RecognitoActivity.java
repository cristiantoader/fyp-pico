package com.fyp.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fyp.authenticator.voice.AuthDevVoiceDAO;
import com.fyp.authenticator.voice.AuthDevVoiceRecord;

import android.app.Activity;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RecognitoActivity extends Activity {
	private static final String LOG_TAG = "AudioRecordTest";
	private static final String FILE_NAME = "/owner.3gp";

	private EditText timerDisplay = null;

	private Button mRecordButton = null;
	private Button mPlayButton = null;
	private Button mSaveButton = null;

	private AudioRecord mRecorder = null;
	private MediaPlayer mPlayer = null;

	private static final int SAMPLE_RATE = 44100;

	OnClickListener clickerRecord = new OnClickListener() {
		private boolean recording = false;
		private long mStartTime = 0;
		private AuthDevVoiceRecord record = null;

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
			this.record = new AuthDevVoiceRecord(RecognitoActivity.this, FILE_NAME);
			this.record.startRecord();
		}
		
		private void stopRecording() {
			this.recording = false;
			this.record.stopRecord();
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

	OnClickListener clickerSave = new OnClickListener() {

		public void onClick(View v) {
			if (recordingExists()) {
				Log.i("RecognitoActivity", "recording exists!");

				AuthDevVoiceDAO voiceDAO = new AuthDevVoiceDAO();
				voiceDAO.addVocalPrint(getRecordingData(), SAMPLE_RATE);

			} else {
				Log.i("RecognitoActivity", "recording not present!");
			}
		}

		private boolean recordingExists() {
			return new File(getFilesDir() + FILE_NAME).exists();
		}

		private double[] getRecordingData() {
			FileInputStream in = null;
			double[] result = null;
			int fileSize = 0;
			
			if (!recordingExists()) {
				return null;
			}

			try {
				in = new FileInputStream(getFilesDir() + FILE_NAME);
				
				fileSize = (int) in.getChannel().size();
				result = new double[fileSize];

				for (int i = 0; i < fileSize; i++) {
					int val = in.read();
					if (val == -1)
						break;
					
					result[i] = ((double) val / 128) - 1;
				}

				in.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return result;
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

		this.mSaveButton = (Button) findViewById(R.id.ButtonRecognitoSave);
		this.mSaveButton.setOnClickListener(clickerSave);

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
