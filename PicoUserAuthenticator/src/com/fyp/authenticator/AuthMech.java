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
 * AuthMechService.
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

	public AuthMech(Service uaservice, Class<?> serviceClass) {
		this.uaservice = uaservice;
		this.mechServiceClass = serviceClass;

		this.doBindService();
	}

	public int getConfidence() {
		return this.mechConfidence;
	}

	/**
	 * Method used to bind on corresponding mechanism service.
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
	 *          service connection to unbind.
	 */
	public void doUnbindService(ServiceConnection conn) {
		if (boundService) {
			uaservice.unbindService(uaServiceAuthConn);
			boundService = false;
		}
	}

	/**
	 * Enables communication between this service and observable objects.
	 */
	private ServiceConnection uaServiceAuthConn = new ServiceConnection() {

		/**
		 * When connected to the service we need to reference the sender and
		 * register the receiver.
		 */
		public void onServiceConnected(ComponentName className, IBinder service) {
			sender = new Messenger(service);

			try {
				Message msg = Message.obtain(null, AuthMechService.AUTH_MECH_REGISTER);
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

	static class IncomingHandler extends Handler {
		WeakReference<AuthMech> amwr;

		public IncomingHandler(AuthMech am) {
			this.amwr = new WeakReference<AuthMech>(am);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case AuthMechService.AUTH_MECH_GET_STATUS:
				amwr.get().mechConfidence = msg.arg1;
				break;

			default:
				Log.e("AuthMech", "Unknowin AuthMechService status: " + msg.what);
				super.handleMessage(msg);
			}
		}
	}
}
