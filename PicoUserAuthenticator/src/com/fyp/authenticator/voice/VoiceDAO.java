package com.fyp.authenticator.voice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.fyp.ctypto.KeyManager;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class VoiceDAO {

	/** Audio recorder object. */
	private AudioRecord mRecorder = null;

	/** Recording thread. */
	private RecordingThread rt = new RecordingThread();

	/** Audio record data. */
	private static final int SAMPLE_RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	private byte[] data = null;

	private static final String TAG = "VoideDAO";
	private int minBufferSize = 0;

	/** Audio file name. */
	private String name = null;
	/** Audio file path. */
	private String filePath = null;

	/** Android context used with the class. */
	private Context ctx = null;

	public static final String OWNER_FN = "owner.3gp";

	/**
	 * Public constructor of voice object.
	 * 
	 * This class can be used for recording, saving, and loading data from
	 * filesystem.
	 * 
	 * @param context
	 *            application context used for file management.
	 * @param fileName
	 *            name of the recording.
	 */
	public VoiceDAO(Context context, String fileName) {
		this.ctx = context.getApplicationContext();

		this.name = fileName;
		this.filePath = context.getFilesDir().toString();

	}

	public void startRecord() {

		if (this.data != null) {
			Log.w(TAG, "Existing recording will be overwritten!");
			this.data = null;
		}

		this.minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				CHANNEL_CONFIG, AUDIO_FORMAT);

		this.mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, this.minBufferSize);

		this.mRecorder.startRecording();
		this.rt.startThread();
	}

	public void stopRecord() {
		this.mRecorder.stop();
		this.mRecorder.release();

		this.rt.stopThread();
		this.data = this.rt.getData();
		this.rt = null;
	}

	public void saveRecording() {
		KeyManager km = null;

		FileOutputStream fos = null;
		CipherOutputStream cos = null;

		if (new File(getAbsoluteFilePath()).exists()) {
			Log.w(TAG, "File with the same name will get overwritten: "
					+ getAbsoluteFilePath());
		}

		try {
			km = KeyManager.getInstance(ctx);

			fos = new FileOutputStream(getAbsoluteFilePath());
			cos = new CipherOutputStream(fos, km.getEncryptionCipher());

			cos.write(this.data);
			cos.flush();
			cos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int getSampleRate() {
		return SAMPLE_RATE;
	}

	/**
	 * Retrieves the recording data from the record saved in internal memory.
	 * 
	 * @return recording data from the record saved in internal memory.
	 */
	public double[] getData() {
		double[] result = null;

		Log.d(TAG, "getOwnerData+");

		if (data == null) {
			Log.d(TAG, "Retrieving data from file.");
			loadDataFromFile();
		}

		result = getDataFromObject();

		Log.d(TAG, "getData- " + result);
		return result;
	}

	/**
	 * Gets data from object and converts it to the appropriate format.
	 * 
	 * @return recording data.
	 */
	private double[] getDataFromObject() {
		double[] result = new double[this.data.length];

		for (int i = 0; i < result.length; i++) {
			result[i] = (((double) this.data[i]) / 128) - 1;
		}

		return result;
	}

	/**
	 * Loads recording from file.
	 */
	private void loadDataFromFile() {
		KeyManager km = null;
		FileInputStream fis = null;
		CipherInputStream cis = null;

		if (!hasRecording()) {
			Log.d(TAG, "File does not exist: " + this.getAbsoluteFilePath());
			return;
		}

		try {
			km = KeyManager.getInstance(ctx);

			fis = new FileInputStream(getAbsoluteFilePath());
			cis = new CipherInputStream(fis, km.getDecryptionCipher());

			data = new byte[(int) fis.getChannel().size()];

			int i = 0, val = 0;
			while ((val = cis.read()) != -1) {
				data[i++] = (byte) val;
				i++;
			}

			cis.close();
		} catch (Exception e) {
			e.printStackTrace();
			data = null;
		}
	}

	/**
	 * Checks if the recording file exists in the file system.
	 * 
	 * @return true if the file exists, false otherwise.
	 */
	public boolean hasRecording() {
		return new File(getAbsoluteFilePath()).exists() || this.data != null;
	}

	private String getAbsoluteFilePath() {
		return this.filePath + "/" + this.name;
	}

	private class RecordingThread extends Thread {
		private volatile boolean recording = false;

		private ArrayList<Byte> threadData = new ArrayList<Byte>();

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
			int read = 0;
			byte buffer[] = new byte[minBufferSize];

			while (recording == true) {
				read = mRecorder.read(buffer, 0, buffer.length);

				if (read != AudioRecord.ERROR_INVALID_OPERATION) {
					addData(buffer);
				} else {
					Log.w(TAG, "AudioRecord error!");
				}
			}
		}

		private void addData(byte[] data) {
			for (byte b : data) {
				this.threadData.add(b);
			}
		}

		public byte[] getData() {
			byte[] result = new byte[this.threadData.size()];

			for (int i = 0; i < result.length; i++) {
				result[i] = this.threadData.get(i);
			}

			return result;
		}
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Generates an unique noise file name.
	 * 
	 * @param ctx
	 *            Context used for current path.
	 * @return The noise file name.
	 */
	public static String generateNoiseFileName(Context ctx) {
		String fn = null;

		do {
			fn = "noise" + new Random().nextInt() + ".3gp";

		} while (!new File(ctx.getFilesDir() + "/" + fn).exists());

		return fn;
	}

	public static LinkedList<VoiceDAO> getNoiseDAOs(Context ctx) {
		File dir = ctx.getFilesDir();
		LinkedList<VoiceDAO> noises = new LinkedList<VoiceDAO>();

		for (String fn : dir.list()) {
			if (fn.startsWith("noise") && fn.endsWith(".3gp")) {
				Log.d(TAG, "Added noise file: " + fn);
				noises.add(new VoiceDAO(ctx, fn));
			}
		}

		return noises;
	}
}
