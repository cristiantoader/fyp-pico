package com.fyp.services;

import java.util.ArrayList;

import com.fyp.authenticator.voice.AuthDevVoiceDAO;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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
public class AuthVoiceService extends Service {

	private static final int AUTH_VOICE_REGISTER = 0;
	private static final int AUTH_VOICE_UNREGISTER = 1;
	private static final int AUTH_VOICE_GET_STATUS = 2;

	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
	private Messenger messenger = new Messenger(new IncomingHandler());

	/** DAO bridge for communicating with the underlying voice recognition API. */
	private AuthDevVoiceDAO voiceDAO = null;

	private AuthenticatorThread voiceThread = null;

	/**
	 * When creating the service a voice DAO is created in order to interact with
	 * the voice recognition library. An authenticator thread is started which
	 * authenticates the user periodically.
	 * 
	 * TODO: create a way to intercept calls and authenticate the user during the
	 * call.
	 * 
	 */
	@Override
	public void onCreate() {
		Log.i("AuthVoiceService", "onCreate");

		if (voiceDAO == null) {
			voiceDAO = new AuthDevVoiceDAO();
		}

		if (voiceThread == null) {
			voiceThread = new AuthenticatorThread();
			voiceThread.start();
		}

	}

	/**
	 * When service is destroyed the authenticator thread is closed and the voice
	 * DAO is unreferenced.
	 */
	@Override
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

	@Override
	public IBinder onBind(Intent arg0) {
		return messenger.getBinder();
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case AUTH_VOICE_REGISTER:
				clients.add(msg.replyTo);
				break;

			case AUTH_VOICE_UNREGISTER:
				clients.remove(msg.replyTo);
				break;

			case AUTH_VOICE_GET_STATUS:
				try {
					// TODO: change value
					msg.replyTo.send(Message.obtain(null, AUTH_VOICE_GET_STATUS, 1, 0));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private class AuthenticatorThread extends Thread {
		private volatile boolean stop;

		public AuthenticatorThread() {
			this.stop = false;
		}

		@Override
		public void run() {
			while (stop != true) {
				try {
					Thread.sleep(1000);
					// TODO:
					broadcastResult();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			this.stop = true;
		}

		private void broadcastResult() {
			// for (Messenger client : clients) {
			// try {
			// int value = (ua.isAuthenticated() == true) ? 1 : 0;
			// client.send(Message.obtain(null, MSG_GET_STATUS, value, 0));
			//
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// clients.remove(client);
			// }
			// }
		}
	}
}
