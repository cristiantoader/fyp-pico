package com.fyp.authenticator.voice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AuthDevVoiceRecord {

	/** Audio recorder object. */
	private AudioRecord mRecorder = null;

	/** Recording thread. */
	private RecordingThread rt = new RecordingThread();

	/** Variable stating if recording was saved. */
	private boolean recordingSaved = false;

	/** Audio record data. */
	private final int SAMPLE_RATE = 44100;
	private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private int minBufferSize = 0;

	/** Audio file data. */
	private String fileName = null;
	private String filePath = null;

	public AuthDevVoiceRecord(Context context, String fileName) {
		this.fileName = fileName;
		this.filePath = context.getFilesDir().toString();

		this.minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				CHANNEL_CONFIG, AUDIO_FORMAT);

		this.mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, this.minBufferSize);
	}

	public void startRecord() {
		this.mRecorder.startRecording();
		this.rt.startThread();
	}

	public void stopRecord() {
		this.mRecorder.stop();
		this.mRecorder.release();

		this.rt.stopThread();
		this.rt = null;

		this.recordingSaved = true;
	}

	public int getSampleRate() {
		return this.SAMPLE_RATE;
	}

	/**
	 * Retrieves the recording data from the record saved in internal memory.
	 * 
	 * @return recording data from the record saved in internal memory.
	 */
	public double[] getRecordingData() {
		FileInputStream in = null;
		double[] result = null;
		int fileSize = 0;

		if (doneRecoring() == false && !hasRecording()) {
			return null;
		}

		try {
			in = new FileInputStream(getAbsoluteFilePath());

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

	/**
	 * Checks if the recording process is over and the recording was marked as
	 * saved.
	 * 
	 * @return true if the recording is done, false otherwise.
	 */
	public boolean doneRecoring() {
		return this.recordingSaved;
	}

	/**
	 * Checks if the recording file exists in the file system.
	 * 
	 * @return true if the file exists, false otherwise.
	 */
	public boolean hasRecording() {
		return new File(getAbsoluteFilePath()).exists();
	}

	private String getAbsoluteFilePath() {
		return this.filePath + "/" + this.fileName;
	}

	private class RecordingThread extends Thread {
		private volatile boolean recording = false;

		public void startThread() {
			this.recording = true;
			this.start();
		}

		public void stopThread() {
			try {
				this.recording = false;
				this.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			FileOutputStream os = null;
			byte data[] = new byte[minBufferSize];

			try {
				os = new FileOutputStream(getAbsoluteFilePath());

				int read = 0;
				while (recording == true) {
					read = mRecorder.read(data, 0, data.length);

					if (read != AudioRecord.ERROR_INVALID_OPERATION) {
						os.write(data);
					}
				}

				os.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
