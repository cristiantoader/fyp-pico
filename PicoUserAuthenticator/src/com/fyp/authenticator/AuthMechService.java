package com.fyp.authenticator;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

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
 * Abstract class used for predefining the communication channel for all
 * authentication mechanism services.
 * 
 * All authentication mechanisms that are or will be implemented need to extend
 * this class.
 * 
 * @author cristi
 * 
 */
public abstract class AuthMechService extends Service {

	/**
	 * Latest authentication score. This needs to be multiplied by decaying
	 * weight factor.
	 */
	protected int score = 0;

	/**
	 * Decayed score. This score needs to be returned to UAService for
	 * processing.
	 */
	protected int decayedWeight = 0;

	/** Initial weight of the mechanism. */
	protected int initialWeight = 0;

	/** Timer process used in decaying the weight. */
	protected DecayTimer decayTimer = null;

	/** Register to the authentication mechanism service. */
	protected static final int AUTH_MECH_REGISTER = 0;
	/** Unregister to the authentication mechanism service. */
	protected static final int AUTH_MECH_UNREGISTER = 1;
	/** Request status of authentication mechanism service. */
	protected static final int AUTH_MECH_GET_STATUS = 2;

	private static final String TAG = "AuthMechService";

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
	 * Used for receiving requests from the UAService UserAuthenticator class.
	 * 
	 * This class is currently used only for registering the UAService as a
	 * client. Although explicit confidence requests are supported, such
	 * requests are never made in this implementation.
	 * 
	 * The updates are made using the clientWriter object instantiated in this
	 * class.
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
					Log.d("AuthMechAbstract", "Score requested, sending "
							+ score);
					msg.replyTo.send(Message.obtain(null, AUTH_MECH_GET_STATUS,
							score, 0));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Function used to start the decay of the initial weight.
	 * 
	 * This restarts the current weight and starts a timer which periodically
	 * decays the weight. Upon each successful decay a broadcast is sent back to
	 * UAService in order to update on the current confidence level.
	 */
	public void startDecay() {
		this.decayedWeight = this.initialWeight;

		if (this.decayTimer == null) {
			this.decayTimer = new DecayTimer();
		}

		this.decayTimer.stopTimer();
		this.decayTimer.startTimer();
	}

	/**
	 * Class used to periodically decay the weight of the mechanism.
	 * 
	 * @author cristi
	 * 
	 */
	private class DecayTimer extends TimerTask {
		/** Time interval after which decay occurs. */
		private static final int INTERVAL = 1000;

		/** Wrapped timer object used for task scheduling. */
		private Timer timer = null;

		/** Exponential decay rate. */
		protected int rate = 2;

		/**
		 * Method called once every INTERVAL in order to update the
		 * decayedWeight. Once the weight was decayed, the updated confidence is
		 * sent to UAService.
		 */
		@Override
		public void run() {
			decay();
			sendDecayedScore();
		}

		/**
		 * Starts the decay process of the initialWeight. The updated result may
		 * be found in decayedWeight.
		 * 
		 * @return
		 */
		public boolean startTimer() {
			if (timer != null) {
				return false;
			}

			timer = new Timer();
			timer.scheduleAtFixedRate(this, 0, INTERVAL);

			return true;
		}

		/**
		 * Stops the decaying process of the initialWeight.
		 * 
		 * The method does nothing if the Timer was not yet started.
		 */
		public void stopTimer() {
			if (timer == null) {
				return;
			}

			timer.cancel();
			timer = null;
		}

		/**
		 * Exponential decay function for the current confidence level.
		 * 
		 * This function is called periodically in order to decay the confidence
		 * level of the mechanism. The decay is exponential is a function of
		 * time between successful authentications. The term
		 * "successful authentication" means that data collection was possible
		 * and the mechanism was able to successfully produce a result,
		 * regardless of what this result may be.
		 */
		private void decay() {
			decayedWeight = (int) (initialWeight * Math.pow(Math.E, -rate));
		}
	}

	public void sendDecayedScore() {
		int decayedScore = this.decayedWeight * this.score;

		Log.d(TAG, "Service score: " + score);
		Log.d(TAG, "Decayed score: " + decayedScore);

		try {
			clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS,
					decayedScore, 0));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
