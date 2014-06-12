/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

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
 * Used for communication with an authentication mechanism service.
 * 
 * This class makes communication with authentication mechanism services opaque
 * to UAService. It is used for launching an authentication mechanism and
 * listening for status updates. The class only keeps track of the most recent
 * value provided by the authentication mechanism service.
 * 
 */
public class AuthMech {

	/** UAService reference. */
	private Service uaservice = null;

	/** Authentication mechanism service class. */
	private Class<?> mechServiceClass = null;
	/** Variable that tracks if the service is bound. */
	private boolean boundService = false;

	/** Messenger used for sending messages to the service. */
	private Messenger sender = null;
	/** Messenger used for receiving messages from the service. */
	private Messenger receiver = new Messenger(new IncomingHandler(this));

	/** Current confidence level registered by the authentication mechanism. */
	private int mechConfidence = 0;

	/** Original weight of the mechanism service without applying decay. */
	private int mechWeight = 0;

	/**
	 * Main constructor used for starting the service and initialising
	 * communication.
	 * 
	 * @param uaservice
	 *            UAService component to which the object belongs.
	 * @param serviceClass
	 *            Class of the authentication mechanism service.
	 */
	public AuthMech(Service uaservice, Class<?> serviceClass) {
		this.uaservice = uaservice;
		this.mechServiceClass = serviceClass;

		this.doBindService();
	}

	/**
	 * Retrieves the current confidence level of the mechanism multiplied by the
	 * decayed weight.
	 * 
	 * The current implementation does not use explicit confidence update
	 * requests from the mechanism service. This value is communicated
	 * periodically by the service to this AuthMech object.
	 * 
	 * @return the mechanism's confidence level multiplied by the decayed
	 *         weight.
	 */
	public int getConfidence() {
		return this.mechConfidence;
	}

	/**
	 * Retrieves the initial weight of the mechanism before the decay process.
	 * 
	 * @return initial weight of the mechanism.
	 */
	public int getWeight() {
		return this.mechWeight;
	}

	/**
	 * Method used to bind the corresponding mechanism service.
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
	 */
	public void doUnbindService() {
		if (boundService) {
			uaservice.unbindService(uaServiceAuthConn);
			boundService = false;
		}
	}

	/**
	 * Enables communication between UAService and an authentication mechanism
	 * service. May be used to send requests but that functionality is not
	 * used with this implementation.
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
	 * Used for receiving feedback from the mechanism service.
	 * 
	 * This class is used for listening for confidence updates from the
	 * authentication mechanism's service.
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
