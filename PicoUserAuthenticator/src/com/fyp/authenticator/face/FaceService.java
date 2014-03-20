package com.fyp.authenticator.face;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fyp.authenticator.AuthMechService;

public class FaceService extends AuthMechService {

	private AuthenticatorThread faceThread = null;

	@Override
	public void onCreate() {
		if (this.faceThread == null) {
			this.faceThread = new AuthenticatorThread();
			this.faceThread.startThread();
		}
	}

	@Override
	public void onDestroy() {
		if (this.faceThread != null) {
			this.faceThread.stopThread();
			this.faceThread = null;
		}
	}

	private class AuthenticatorThread extends Thread {
		private volatile boolean running = false;
		private FaceDAO dao = new FaceDAO();

		private static final int AUTH_PERIOD = 1 * 1000;

		public void run() {

			while (this.running) {
				try {
					int score = 0;
					
					Thread.sleep(AUTH_PERIOD);
					// TODO: get image data

					// TODO: get match based on image
					score = (int) Math.floor(this.dao.getMatch(null));

					// sending the score
					Log.d(this.getClass().toString(), "Sending score " + score + "...");
					clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS, score, 0));

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

		}

		public void startThread() {
			this.running = true;
			this.start();
		}

		public void stopThread() {
			try {
				this.running = false;
				this.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
