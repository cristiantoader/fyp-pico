package com.fyp.authenticator;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * @author cristi
 * 
 */
public class UAService extends Service {

	/** Bridge to user authenticator class which provides functionality. */
	private static UserAuthenticator ua = null;

	/** Authenticator thread responsible of broadcasting messages. */
	private static AuthenticatorThread serviceThread = null;
	/** List of binded clients. */
	private HashMap<Messenger, Integer> clients = new HashMap<Messenger, Integer>();
	/** Message receiver from binded clients. */
	private final Messenger messenger = new Messenger(new IncomingHandler(this));

	/** Constant used to register a client for broadcast. */
	public static final int MSG_REGISTER_CLIENT = 0;
	/** Constant used to unregister a client for broadcast. */
	public static final int MSG_UNREGISTER_CLIENT = 1;
	/** Constant used to request user authentication status. */
	public static final int MSG_GET_STATUS = 2;

	private static final String TAG = "UAService";

	/**
	 * When creating the service it gets a reference to the UserAuthenticator
	 * and starts the authenticator thread which is responsible for broadcasting
	 * results.
	 */
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");

		if (ua == null) {
			ua = new UserAuthenticator(this);
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
		Log.i(TAG, "onDestroy");

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
		Log.i(TAG, "onBind");
		return messenger.getBinder();
	}

	static class IncomingHandler extends Handler {
		private final WeakReference<UAService> service;

		public IncomingHandler(UAService service) {
			this.service = new WeakReference<UAService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			UAService uas = service.get();
			if (uas == null) {
				return;
			}

			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				uas.clients.put(msg.replyTo, msg.arg1);
				break;

			case MSG_UNREGISTER_CLIENT:
				uas.clients.remove(msg.replyTo);
				break;

			case MSG_GET_STATUS:
				Messenger client = msg.replyTo;

				if (uas.clients.containsKey(client)) {
					int threshold = uas.clients.get(client);
					int confidence = ua.getConfidence();

					int result = confidence >= threshold ? 1 : 0;
					
					try {
						client.send(Message.obtain(null, MSG_GET_STATUS, result, 0));
					} catch (RemoteException e) {
						e.printStackTrace();
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
			for (Entry<Messenger, Integer> entry : clients.entrySet()) {
				Messenger client = entry.getKey();
				
				int threshold = entry.getValue();
				int confidence = ua.getConfidence();
				
				int result = confidence >= threshold ? 1 : 0;
				
				try {
					client.send(Message.obtain(null, MSG_GET_STATUS, result, 0));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
