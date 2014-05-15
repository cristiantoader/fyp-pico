package com.fyp.authenticator;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Class used for communication from the UserAuthenticator to an
 * AuthMechService. It corresponds to one authentication mechanism service. It
 * is binded by UAService.
 * 
 * 
 * @author cristi
 * 
 */
public class AuthMech {

	/** UAService service. */
	private Service uaservice = null;

	/** Authentication mechanism service. */
	private Class<?> mechServiceClass = null;
	/** Variable that states if the service is bound. */
	private boolean boundService = false;

	/** Messenger used for sending messages to the service. */
	private Messenger sender = null;
	/** Messenger used for receiving messages from the service. */
	private Messenger receiver = new Messenger(new IncomingHandler(this));

	/** Current confidence level registered by the authentication mechanism. */
	private int mechConfidence = 0;

	/** Current decaying weight of the mechanism computed with the confidence. */
	private int mechWeight = 0;

	public AuthMech(Service uaservice, Class<?> serviceClass) {
		this.uaservice = uaservice;
		this.mechServiceClass = serviceClass;

		this.doBindService();
	}

	/**
	 * Retrieves the current confidence level of the mechanism.
	 * 
	 * The current implementation does not request the mechanism service for a
	 * confidence update. It is giving feedback based on the latest possible
	 * calculated confidence according to the mechanism's collection rules.
	 * 
	 * @return
	 */
	public int getConfidence() {
		return this.mechConfidence;
	}

	/**
	 * Retrieves the current decaying weight used for calculating the
	 * confidence.
	 * 
	 * @return
	 */
	public int getWeight() {
		return this.mechWeight;
	}

	/**
	 * Method used to bind on corresponding mechanism service.
	 * 
	 * This method registers the UAService as a client, and the messenger used
	 * for receiving data from the mechanism service.
	 */
	public void doBindService() {
		uaservice.bindService(new Intent(uaservice, mechServiceClass),
				uaServiceAuthConn, Context.BIND_AUTO_CREATE);
		boundService = true;
	}

	/**
	 * Method used to unBind service.
	 * 
	 * @param conn
	 *            service connection to unbind.
	 */
	public void doUnbindService(ServiceConnection conn) {
		if (boundService) {
			uaservice.unbindService(uaServiceAuthConn);
			boundService = false;
		}
	}

	/**
	 * Enables communication between UAService and authentication mechanism
	 * service. May be used to send requests but that functionality is not
	 * exploited in this implementation.
	 */
	private ServiceConnection uaServiceAuthConn = new ServiceConnection() {

		/**
		 * When connected to the service we need to reference the sender and
		 * register the receiver.
		 */
		public void onServiceConnected(ComponentName className, IBinder service) {
			sender = new Messenger(service);

			try {
				Message msg = Message.obtain(null,
						AuthMechService.AUTH_MECH_REGISTER);
				msg.replyTo = receiver;
				sender.send(msg);

			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		public void onServiceDisconnected(ComponentName className) {
			sender = null;
		}
	};

	/**
	 * Message handler in order to receive feedback from the mechanism service.
	 * 
	 * @author cristi
	 * 
	 */
	static class IncomingHandler extends Handler {
		private static final String TAG = "AuthMechInHandler";
		WeakReference<AuthMech> amwr;

		public IncomingHandler(AuthMech am) {
			this.amwr = new WeakReference<AuthMech>(am);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case AuthMechService.AUTH_MECH_GET_STATUS:
				Log.d(TAG, "Incoming messge from mechanism.");
				amwr.get().mechConfidence = msg.arg1;
				amwr.get().mechWeight = msg.arg2;
				break;

			default:
				Log.e(TAG, "Unknowin AuthMechService status");
				super.handleMessage(msg);
			}
		}
	}
}
