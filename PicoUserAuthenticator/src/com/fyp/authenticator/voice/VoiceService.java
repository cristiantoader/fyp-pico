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
	
	public void onCreate() {
		Log.i("AuthVoiceService", "onCreate");

		if (voiceThread == null) {
			voiceThread = new AuthenticatorThread();
			voiceThread.start();
		}

	}

	public void onDestroy() {
		Log.i("AuthVoiceService", "onDestroy");

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

		private volatile boolean stop;
		private VoiceDAO voiceDAO;

		public AuthenticatorThread() {
			this.stop = false;
			this.voiceDAO = new VoiceDAO(VoiceService.this);
		}

		@Override
		public void run() {
			while (stop != true) {
				try {
					// TODO: can change this to having a separate method for recording a
					// challenge..
					VoiceRecord record = new VoiceRecord(
							VoiceService.this, "challenge.3gp");
					Log.d(this.getClass().toString(), "Starting loop...");

					Thread.sleep(AUTH_PERIOD);

					// gathering a recording sample.
					Log.d(this.getClass().toString(), "Gathering input...");
					record.startRecord();
					Thread.sleep(RECORD_TIME);
					record.stopRecord();

					// this would be an unexplained error.
					Log.d(this.getClass().toString(), "Getting score...");
					if (!record.hasRecording()) {
						Log.e(this.getClass().toString(), "record not created!");
						continue;
					}

					// sending match score.
					double dscore = this.voiceDAO.getMatch(record);
					if (dscore > THRESHOLD) {
						dscore = THRESHOLD;
					}
					
					score = (int) Math.floor((1 - dscore / THRESHOLD) * 100);

					Log.d(this.getClass().toString(), "Sending score " + score + "...");
					clientWriter.send(Message
							.obtain(null, AUTH_MECH_GET_STATUS, score, 0));

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

	}

}
