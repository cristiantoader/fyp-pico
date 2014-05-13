package com.fyp.authenticator.voice;

import com.fyp.authenticator.AuthMechService;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

/**
 * Observable service which needs to keep track of the user authentication
 * state. When requested, the UAService will require to know the current state
 * of this observable.
 * 
 * @author cristi
 * 
 */
public class VoiceService extends AuthMechService {

	/** Thread used to periodically authenticate the user and broadcast result. */
	private AuthenticatorThread voiceThread = null;

	private static final double THRESHOLD = 2;

	private static final String TAG = "AuthVoiceService";
	
	public void onCreate() {
		Log.i(TAG, "onCreate");

		if (voiceThread == null) {
			voiceThread = new AuthenticatorThread();
			voiceThread.start();
		}

	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");

		if (voiceThread != null) {
			try {
				voiceThread.stopThread();
				voiceThread.join();
				voiceThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		private static final int AUTH_PERIOD = 1 * 1000;
		private static final int RECORD_TIME = 3 * 1000;
		private static final String TAG = "VoiceServiceThread";

		private volatile boolean stop;
		private VoiceDAO voiceDAO;

		public AuthenticatorThread() {
			this.stop = false;
		}

		@Override
		public void run() {
			
			this.voiceDAO = new VoiceDAO(VoiceService.this);
			
			while (stop != true) {
				try {
					Log.d(TAG, "Start loop.");
					Thread.sleep(AUTH_PERIOD);
					
					VoiceRecord record = recordData();


					Log.d(TAG, "Getting score.");
					if (record == null || !record.hasRecording()) {
						Log.e(TAG, "record not created!");
						continue;
					}

					// sending match score.
					double dscore = this.voiceDAO.getMatch(record);
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}
					
					score = (int) Math.floor((1 - dscore / THRESHOLD) * 100);

					Log.d(TAG, "Voice score: " + score);
					clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS, score, 0));

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			this.stop = true;
		}
		
		private VoiceRecord recordData() {
			Log.d(TAG, "recordData+");

			VoiceRecord record = new VoiceRecord(VoiceService.this, "challenge.3gp");

			try {
				record.startRecord();
				Thread.sleep(RECORD_TIME);
				record.stopRecord();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Log.d(TAG, "recordData- " + record);
			return record;
		}
		
	}

}
