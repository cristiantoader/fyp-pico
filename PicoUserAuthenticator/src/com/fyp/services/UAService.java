package com.fyp.services;

import java.util.ArrayList;

import com.fyp.authenticator.UserAuthenticator;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class UAService extends Service {

	/** Bridge to user authenticator class which provides functionality. */
	private static UserAuthenticator ua = null;

	/** Authenticator thread responsible of broadcasting messages. */
	private static AuthenticatorThread serviceThread = null;
	/** List of binded clients. */
	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
	/** Message receiver from binded clients. */
	private final Messenger messenger = new Messenger(new IncomingHandler());

	/* Constants used with client communication. */
	public static final int MSG_REGISTER_CLIENT = 0;
	public static final int MSG_UNREGISTER_CLIENT = 1;
	public static final int MSG_GET_STATUS = 2;

	/**
	 * When creating the service it gets a reference to the UserAuthenticator and
	 * starts the authenticator thread which is responsible for broadcasting
	 * results.
	 */
	@Override
	public void onCreate() {
		Log.i("UAService", "onCreate");

		if (ua == null) {
			ua = UserAuthenticator.getUserAuthenticator();
		}

		if (serviceThread == null) {
			serviceThread = new AuthenticatorThread();
			serviceThread.start();
		}
	}

	/**
	 * Destroy method cleans up creation.
	 */
	@Override
	public void onDestroy() {
		Log.i("UAService", "onDestroy");

		if (serviceThread != null) {
			try {
				serviceThread.stopThread();
				serviceThread.join();
				serviceThread = null;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (ua != null) {
			ua = null;
		}
	}

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * this service to receive messages.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.i("UAService", "onBind");
		return messenger.getBinder();
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				clients.add(msg.replyTo);
				break;

			case MSG_UNREGISTER_CLIENT:
				clients.remove(msg.replyTo);
				break;

			case MSG_GET_STATUS:
				Messenger req = msg.replyTo;

				if (clients.contains(req)) {
					try {
						int value = (ua.isAuthenticated() == true) ? 1 : 0;
						req.send(Message.obtain(null, MSG_GET_STATUS, value, 0));

					} catch (RemoteException e) {
						e.printStackTrace();
						clients.remove(req);
					}
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Authenticator thread responsible of broadcasting the result to registered
	 * clients.
	 * 
	 * @author cristi
	 * 
	 */
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
			for (Messenger client : clients) {
				try {
					int value = (ua.isAuthenticated() == true) ? 1 : 0;
					client.send(Message.obtain(null, MSG_GET_STATUS, value, 0));

				} catch (RemoteException e) {
					e.printStackTrace();
					clients.remove(client);
				}
			}
		}
	}
}
