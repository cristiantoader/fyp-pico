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
public class AuthVoiceService extends AuthMechService {

	/** DAO bridge for communicating with the underlying voice recognition API. */
	private AuthDevVoiceDAO voiceDAO = null;

	/** Thread used to periodically authenticate the user and broadcast result. */
	private AuthenticatorThread voiceThread = null;

	public void onCreate() {
		Log.i("AuthVoiceService", "onCreate");

		if (voiceDAO == null) {
			voiceDAO = new AuthDevVoiceDAO(this);
		}

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

		if (voiceDAO != null) {
			voiceDAO = null;
		}
	}

	/**
	 * Thread used to periodically authenticate the user.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		private static final int AUTH_PERIOD = 1000;
		private volatile boolean stop;

		public AuthenticatorThread() {
			this.stop = false;
		}

		@Override
		public void run() {
			while (stop != true) {
				try {
					Thread.sleep(AUTH_PERIOD);

					// TODO: use DAO to actually send good results based on periodic input
					// validation.
					clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS, 50, 0));

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
