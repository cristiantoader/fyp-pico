package com.fyp.authenticator;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
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

	public int getInitialWeight() {
		return this.initialWeight;
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
		Log.d(TAG, "startDecay+");
		
		this.decayedWeight = this.initialWeight;

		if (this.decayTimer == null) {
			this.decayTimer = new DecayTimer();
		}

		this.decayTimer.stopTimer();
		this.decayTimer.startTimer();
		
		Log.d(TAG, "startDecay-");
	}

	/**
	 * Class used to periodically decay the weight of the mechanism.
	 * 
	 * @author cristi
	 * 
	 */
	public class DecayTimer implements Runnable {
		/** Time interval after which decay occurs. */
		private static final int INTERVAL = 1000;

		/** Wrapped handler object used for task scheduling. */
		private Handler handler = null;
		
		private volatile boolean running = false;
		
		/** Exponential decay rate. */
		protected double rate = 0.8;

		/**
		 * Method called once every INTERVAL in order to update the
		 * decayedWeight. Once the weight was decayed, the updated confidence is
		 * sent to UAService.
		 */
		@Override
		public void run() {
			Log.d(TAG, "timer run method.");
			
			decay();
			sendDecayedScore(false);
			
			// only reschedule if running is true
			if (this.running == true) {
				Log.d(TAG, "Reposting timer");
				this.handler.postDelayed(this, INTERVAL);
			}
		}

		/**
		 * Starts the decay process of the initialWeight. The updated result may
		 * be found in decayedWeight.
		 * 
		 * @return
		 */
		public boolean startTimer() {
			Log.d(TAG, "startTimer+");
			
			if (handler != null || running == true) {
				return false;
			}
			
			this.running = true;
			
			HandlerThread hThread = new HandlerThread("hThread");
			hThread.start();
			
			handler = new Handler(hThread.getLooper());
			handler.postDelayed(this, INTERVAL);
			

			Log.d(TAG, "startTimer-");
			return true;
		}

		/**
		 * Stops the decaying process of the initialWeight.
		 * 
		 * The method does nothing if the Timer was not yet started.
		 */
		public void stopTimer() {
			Log.d(TAG, "stopTimer+");
			
			// this is how we tell if the handler is stopped
			if (handler == null) {
				Log.d(TAG, "stopTimer: hadnler was null");
				return;
			}

			// stoping current task from rescheduling
			this.running = false;
						
			// removing pending tasks
			this.handler.removeCallbacks(this);
			this.handler = null;
			
			Log.d(TAG, "stopTimer-");
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
			Log.d(TAG, "decay+");
//			decayedWeight = (int) (decayedWeight * Math.pow(Math.E, -rate));
			decayedWeight = (int) (decayedWeight * rate);
		}
	}

	public void sendDecayedScore(boolean fresh) {
		Log.d(TAG, "sendDecayedScore+");
		
		if (fresh) {
			this.decayedWeight = this.initialWeight;
		}
		
		int decayedScore = this.decayedWeight * this.score;

		Log.e(TAG, "Service score: " + score);
		Log.e(TAG, "Decayed weight: " + decayedWeight);
		Log.e(TAG, "Decayed score: " + decayedScore);

		try {
			clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS,
					decayedScore, this.initialWeight));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		Log.d(TAG, "sendDecayedScore-");
	}
}
