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
 * Main component of the token unlocking prototype.
 * 
 * This is implemented as a service that can be bound by other applications. It
 * gathers available data from the authentication mechanisms and calculates the
 * overall confidence of the scheme.
 * 
 * Authentication feedback can be gathered by external components either by
 * registering for broadcasts or through explicit requests.
 * 
 * @author cristi
 * 
 */
public class UAService extends Service {

	/** Bridge to user authenticator class which implements most functionality. */
	private static UserAuthenticator ua = null;

	/** Authenticator thread responsible of broadcasting messages. */
	private static AuthenticatorThread serviceThread = null;
	/** List of binded clients. */
	private HashMap<Messenger, Integer> clients = new HashMap<Messenger, Integer>();
	/** Message receiver sent to binded clients. */
	private final Messenger messenger = new Messenger(new IncomingHandler(this));

	/** Constant used to register a client for broadcast. */
	public static final int MSG_REGISTER_CLIENT = 0;
	/** Constant used to unregister a client for broadcast. */
	public static final int MSG_UNREGISTER_CLIENT = 1;
	/** Constant used to request user authentication status. */
	public static final int MSG_CONFIDENCE_UPDATE = 2;

	/**Logging tag used for debugging. */
	private static final String TAG = "UAService";

	/**
	 * When creating the service it creates a reference to the UserAuthenticator
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

		ua.stopMechanisms();
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

	/**
	 * Class used when PicoMainActivity binds on this service.
	 * 
	 * There is the possibility for explicit updates using
	 * MSG_CONFIDENCE_UPDATE, but this feature is never used.
	 * 
	 * @author cristi
	 * 
	 */
	static class IncomingHandler extends Handler {
		/** Weak reference to UAService component. */
		private final WeakReference<UAService> service;

		/** Basic constructor that registers reference to UAService object. */
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

			case MSG_CONFIDENCE_UPDATE:
				Messenger client = msg.replyTo;

				if (uas.clients.containsKey(client)) {
					int threshold = uas.clients.get(client);
					int confidence = ua.getConfidence();

					@SuppressWarnings("unused")
					int result = confidence >= threshold ? 1 : 0;

					try {
						client.send(Message.obtain(null, MSG_CONFIDENCE_UPDATE,
								confidence, 0));
						// client.send(Message.obtain(null, MSG_GET_STATUS,
						// result, 0));
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
	 * clients. This is how clients receive confidence updates.
	 * 
	 * @author cristi
	 * 
	 */
	private class AuthenticatorThread extends Thread {
		/** Flag used for controlling the thread. */
		private volatile boolean stop;

		/** Basic constructor.*/
		public AuthenticatorThread() {
			this.stop = false;
		}

		/**
		 * Thread's run method responsible of periodic broadcast of results to
		 * registered clients.
		 */
		@Override
		public void run() {
			while (stop != true) {
				try {
					Log.d(TAG, "Authenticator thread running.");
					Thread.sleep(1000);
					broadcastResult();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/** Method used by external objects to stop the thread. */
		public void stopThread() {
			this.stop = true;
		}

		/**
		 * Method used for acquiring the current overall confidence and
		 * broadcasting it to registered clients.
		 */
		private void broadcastResult() {
			int confidence = ua.getConfidence();

			for (Entry<Messenger, Integer> entry : clients.entrySet()) {
				Messenger client = entry.getKey();
				int threshold = entry.getValue();

				@SuppressWarnings("unused")
				int result = confidence >= threshold ? 1 : 0;

				try {
					// client.send(Message.obtain(null, MSG_GET_STATUS, result,
					// 0));
					client.send(Message.obtain(null, MSG_CONFIDENCE_UPDATE,
							confidence, 0));

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
