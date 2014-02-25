package com.fyp.services;

import java.util.ArrayList;

import com.fyp.logic.UserAuthenticator;

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

	private static UserAuthenticator ua = null;
	private static AuthenticatorThread serviceThread = null;

	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
	private final Messenger messenger = new Messenger(new IncomingHandler());

	public static final int MSG_REGISTER_CLIENT = 0;
	public static final int MSG_UNREGISTER_CLIENT = 1;
	public static final int MSG_GET_STATUS = 2;

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
	 * sending messages to the service.
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
