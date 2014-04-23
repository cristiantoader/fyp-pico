package com.fyp.authenticator;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class AuthMechService extends Service {

	/** Latest authentication score. */
	protected int score = 0;
	
	/** Register to the authentication mechanism service. */
	protected static final int AUTH_MECH_REGISTER = 0;
	/** Unregister to the authentication mechanism service. */
	protected static final int AUTH_MECH_UNREGISTER = 1;
	/** Request status of authentication mechanism service. */
	protected static final int AUTH_MECH_GET_STATUS = 2;

	/** Messenger used for writing to the client. */
	protected Messenger clientWriter = null;
	/** Messenger used for reading from the client. */
	protected Messenger clientReader = new Messenger(new IncomingHandler(this));

	/**
	 * Method called when the service is created.
	 */
	public abstract void onCreate();

	/***
	 * Method called when the service is destroyed.
	 */
	public abstract void onDestroy();

	/**
	 * Used to return the messenger used for communication with the master
	 * service.
	 */
	public IBinder onBind(Intent arg0) {
		return clientReader.getBinder();
	}

	/**
	 * Used for communicating with the UAService UserAuthenticator class.
	 * 
	 * @author cristi
	 * 
	 */
	@SuppressLint("HandlerLeak") 
	class IncomingHandler extends Handler {

		private final WeakReference<AuthMechService> service;

		public IncomingHandler(AuthMechService service) {
			this.service = new WeakReference<AuthMechService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			AuthMechService am = service.get();

			switch (msg.what) {

			case AUTH_MECH_REGISTER:
				am.clientWriter = msg.replyTo;
				break;

			case AUTH_MECH_UNREGISTER:
				if (am.clientWriter == msg.replyTo) {
					am.clientWriter = null;
				}
				break;

			case AUTH_MECH_GET_STATUS:
				try {
					Log.d("AuthMechAbstract", "Score requested, sending " + score);
					msg.replyTo.send(Message.obtain(null, AUTH_MECH_GET_STATUS, score, 0));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

}
