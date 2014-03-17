package com.fyp.authenticator.dummy;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

public class AuthDummyService extends AuthMechService {

	/** Dummy data DAO. */
	private AuthDevDummyDAO dummyDAO = null;

	/** Thread used to periodically authenticate the user and broadcast result. */
	private AuthenticatorThread voiceThread = null;

	@Override
	public void onCreate() {
		Log.i("AuthDummyService", "onCreate");

		if (dummyDAO == null) {
			dummyDAO = new AuthDevDummyDAO();
		}

		if (voiceThread == null) {
			voiceThread = new AuthenticatorThread();
			voiceThread.start();
		}

	}

	@Override
	public void onDestroy() {
		Log.i("AuthDummyService", "onDestroy");

		if (voiceThread != null) {
			try {
				voiceThread.stopThread();
				voiceThread.join();
				voiceThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (dummyDAO != null) {
			dummyDAO = null;
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
					
					clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS,
							dummyDAO.getMatch(), 0));

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
