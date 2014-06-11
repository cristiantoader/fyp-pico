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

/**
 * Class used for managing recording data.
 * 
 * The class provides features for recording and saving sample record data.
 * 
 * @author cristi
 * 
 */
public class VoiceDAO {
	/** Android context used with the class. */
	private Context ctx = null;
	
	/** Anrdoid object for recording data. */
	private AudioRecord mRecorder = null;

	/** Recording thread. */
	private RecordingThread rt = new RecordingThread();

	/** Predefined AudioRecord data sample rate. */
	private static final int SAMPLE_RATE = 44100;
	/** Predefined AudioRecord data channel configuration. */
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	/** Predefined AudioRecord data format. */
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	/** Minimum buffer size used when recording data. */
	private int minBufferSize = 0;
	
	/** Recording data in byte format. */
	private byte[] data = null;

	/** Logging tag used for debugging. */
	private static final String TAG = "VoideDAO";

	/** Audio file name. */
	private String name = null;
	/** Audio file path. */
	private String filePath = null;

	/** Predefined variable for the owner file name. */
	public static final String OWNER_FN = "owner.3gp";

	/**
	 * Public constructor of voice object.
	 * 
	 * The constructor registers the application context and file path data.
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

	/**
	 * Method used for starting to record data from the microphone.
	 */
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

	/**
	 * Method used for stopping to record data from the microphone.
	 */
	public void stopRecord() {
		if (this.rt == null) {
			Log.w(TAG, "DAO is not currently recording!");
			return;
		}
		
		this.mRecorder.stop();
		this.mRecorder.release();
		this.mRecorder = null;

		this.rt.stopThread();
		this.data = this.rt.getData();
		this.rt = null;
	}

	/**
	 * Method used for saving recorded data in internal storage. Data is saved
	 * in encrypted format.
	 */
	public void saveRecording() {
		KeyManager km = null;

		FileOutputStream fos = null;
		CipherOutputStream cos = null;

		Log.d(TAG, "saveRecording+");
		
		if (new File(getAbsoluteFilePath()).exists()) {
			Log.w(TAG, "File with the same name will get overwritten: "
					+ getAbsoluteFilePath());
		}
		
		if (this.data == null) {
			Log.w(TAG, "No data was recorded for saving.");
			return;
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

		Log.d(TAG, "saveRecording-");
	}

	/**
	 * Getter for recording sample rate.
	 * 
	 * @return recording sample rate.
	 */
	public static int getSampleRate() {
		return SAMPLE_RATE;
	}

	/**
	 * Retrieves recording data in double[] format either from within the object
	 * or from internal memory.
	 * 
	 * If data is retrieved from internal memory, it is first decrypted.
	 * 
	 * @return recording data from the record saved in internal memory.
	 */
	public double[] getData() {
		double[] result = null;

		Log.d(TAG, "getData+");

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

		Log.d(TAG, "getDataFromObject+");
		
		for (int i = 0; i < result.length; i++) {
			result[i] = ((double) this.data[i]) / 128;
		}

		Log.d(TAG, "getDataFromObject-");
		return result;
	}

	/**
	 * Loads decrypted recording data from file.
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

			// read data from stream
			int b;
			ArrayList<Byte> decByteList = new ArrayList<Byte>();
			while((b = cis.read()) != -1) {
				decByteList.add((byte) b);
			}
			
			// saving data
			data = new byte[decByteList.size()];
			for (int i = 0; i < data.length; i++) {
				data[i] = decByteList.get(i);
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

	/**
	 * Getter for the recording file absolute file path.
	 * 
	 * @return the recording file absolute file path.
	 */
	private String getAbsoluteFilePath() {
		return this.filePath + "/" + this.name;
	}

	/**
	 * Class used for recording data from the microphone.
	 * 
	 * @author cristi
	 * 
	 */
	private class RecordingThread extends Thread {
		/** Flag used for gently stopping the thread.*/
		private volatile boolean recording = false;

		/** Byte list used when gathering data from the microphone input stream. */
		private ArrayList<Byte> threadData = new ArrayList<Byte>();

		/** Method used for starting the recording thread. */
		public void startThread() {
			this.recording = true;
			this.start();
		}

		/** Method used for stopping the recording thread. */
		public void stopThread() {
			try {
				this.recording = false;
				this.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Main thread method used for recording and processing microphone data.
		 */
		public void run() {
			int read = 0;
			byte buffer[] = new byte[minBufferSize];

			while (recording == true) {
				read = mRecorder.read(buffer, 0, buffer.length);

				if (read != AudioRecord.ERROR_INVALID_OPERATION) {
					addData(buffer, read);
				} else {
					Log.w(TAG, "AudioRecord error!");
				}
			}
		}

		/**
		 * Adds a byte[] of data to the overall data gathered by the thread.
		 * 
		 * @param data
		 *            buffer containing recording data.
		 * @param read
		 *            number of bytes from the buffer that contain recording
		 *            data.
		 */
		private void addData(byte[] data, int read) {
			for (int i = 0; i < read; i++) {
				this.threadData.add(data[i]);
			}
		}

		/**
		 * Returns all the data collected by the recording thread.
		 * 
		 * @return data collected by the recording thread.
		 */
		public byte[] getData() {
			byte[] result = new byte[this.threadData.size()];

			for (int i = 0; i < result.length; i++) {
				result[i] = this.threadData.get(i);
			}

			return result;
		}
	}

	/**
	 * Getter for the name of the recording.
	 * 
	 * @return the name of the recording.
	 */
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

		} while (new File(ctx.getFilesDir() + "/" + fn).exists());

		return fn;
	}

	/**
	 * Method used for loading noise DAOs.
	 * 
	 * The method searches the application for recorded noise files and creates
	 * a DAO linked list all recorded noises. This method is used during the
	 * training process of the voice recognition library.
	 * 
	 * @param ctx
	 *            application context used for file access.
	 * @return VoiceDAO list of noises saved in internal storage.
	 */
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
