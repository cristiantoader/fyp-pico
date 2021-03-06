/*
 * Copyright (c) 2014 Cristian Toader
 *
 * See the file license.txt for copying permission (MIT licence).
 * 
 */

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
 * Abstract class used for standardising all authentication mechanism services.
 * 
 * All authentication mechanisms that are or will be implemented need to extend
 * this class.
 * 
 * The class defines a communication interface with AuthMech objects belonging
 * to UAServce. It also implements a decay process used for periodically
 * updating the weight of the mechanism.
 * 
 * All authentication mechanisms extending this class need to declare an initial
 * confidence level in the onCreate() method.
 * 
 * @author cristi
 * 
 */
public abstract class AuthMechService extends Service {

	/**
	 * Latest authentication score. This needs to be multiplied by decaying
	 * weight factor.
	 */
	private int score = 0;
	
	/** Prior probability used for Bayesian update. */
	private double prior = -1;

	/**
	 * Current decayed weight of the mechanism.
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

	/** Messenger used for writing to the client. */
	protected Messenger clientWriter = null;
	/** Messenger used for reading from the client. */
	protected Messenger clientReader = new Messenger(new IncomingHandler(this));

	/** Logging tag used for debugging.*/
	private static final String TAG = "AuthMechService";
	
	/**
	 * Standard onCreate method for the service.
	 * 
	 * Classes that implement this method need to initialise the initialWeight
	 * field.
	 * */
	public abstract void onCreate();

	/**
	 * Standard onDestroy method for the service.
	 * 
	 * Classes that implement this method need to call decayTimer.stopTimer() in
	 * orde to stop the decay process.
	 */
	public abstract void onDestroy();

	/**
	 * Used for returning the messenger used for communication with this
	 * service.
	 */
	public IBinder onBind(Intent arg0) {
		return clientReader.getBinder();
	}

	/**
	 * Getter for initial weight of the mechanism.
	 * 
	 * @return the initial weight of the mechanism.
	 */
	public int getInitialWeight() {
		return this.initialWeight;
	}
	
	/**
	 * Used for receiving requests from the AuthMech objects belonging to
	 * UAService.
	 * 
	 * This class is currently used only for registering UAService as a client.
	 * Although explicit confidence requests are supported, such requests are
	 * never made with this implementation.
	 * 
	 * Confidence updates are made using the clientWriter object instantiated in
	 * this class.
	 * 
	 * @author cristi
	 * 
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		/** Weak reference to the authentication mechanism service.*/
		private final WeakReference<AuthMechService> service;

		/** Basic constructor that sets the AuthMechSerivce reference. */
		public IncomingHandler(AuthMechService service) {
			this.service = new WeakReference<AuthMechService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			AuthMechService am = service.get();

			switch (msg.what) {

			case AUTH_MECH_REGISTER:
				am.clientWriter = msg.replyTo;
				
				// we send an initial score of 0 basically
				sendDecayedScore();
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
	 * Function used for starting the decay of the initial weight.
	 * 
	 * This restarts the current weight and starts a timer which periodically
	 * decays the weight. Upon each successful decay a broadcast is sent back to
	 * the registered AuthMech in order to update its confidence level.
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
		
		/** Flag used for managing the state of the timer. */
		private volatile boolean running = false;
		
		/** Linear decay rate. */
		protected double rate = 0.8;

		/**
		 * Method called every INTERVAL in order to update the decayedWeight.
		 * Once the weight was decayed, the updated confidence is sent to
		 * UAService.
		 */
		@Override
		public void run() {
			Log.d(TAG, "timer run method.");
			
			decay();
			sendDecayedScore();
			
			// only reschedule if running is true
			if (this.running == true) {
				Log.d(TAG, "Reposting timer");
				this.handler.postDelayed(this, INTERVAL);
			}
		}

		/**
		 * Starts the decay process of the initialWeight. The updated result is
		 * stored in decayedWeight.
		 * 
		 * @return true if the timer is started, false otherwise.
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
		 * Liner decay function for the current confidence level.
		 * 
		 * This function is called periodically in order to decay the confidence
		 * level of the mechanism. The decay implementation is currently a
		 * linear decrease function based on time between successful
		 * authentications. The term "successful authentication" means that data
		 * collection was possible and the mechanism was able to successfully
		 * produce a result, regardless of what this result may be.
		 */
		private void decay() {
			Log.d(TAG, "decay+");
//			decayedWeight = (int) (decayedWeight * Math.pow(Math.E, -rate));
			decayedWeight = (int) (decayedWeight * rate);
		}
	}

	/**
	 * Used for sending computing the new score and sending it to the
	 * corresponding AuthMech.
	 * 
	 * The function uses the supplied evidence probability from parameter
	 * <code>newScore</code> in order to compute the new posterior probability
	 * that will be multiplied by the mechanism's initial weight and the result
	 * is sent to the corresponding <code>AuthMech</code> object.
	 * 
	 * The posterior probability is calculated using Bayesian update. The
	 * equation is summarised as follows:
	 * 
	 * P(H|E) = (P(H) * P(E|H)) / 
	 * 		(P(H) * P(E|H) + P(~H) * P(E|~H));
	 * 
	 * In this equation: 
	 * 		P(H) = <code>this.prior</code> 
	 * 		P(E|H) = <code>newScore</code>
	 * 
	 * @param newScore
	 *            this is the current probability recorded by the mechanism. It
	 *            is the probability that the sample E supports the hypothesis H
	 *            that the owner is present, mathematically noted as P(E|H).
	 */
	public void sendDecayedScore(int newScore) {
		Log.d(TAG, "sendDecayedScore(newscore)+");

		if (newScore > 100) {
			Log.w(TAG, "New score value too high, needs truncating: "
					+ newScore);
			newScore = 100;
		}

		if (newScore < 0) {
			Log.w(TAG, "New score value too low, needs truncating: " + newScore);
			newScore = 0;
		}

		if (this.prior == -1) {
			Log.d(TAG, "initialisinst prior");
			this.prior = ((double)newScore) / 100;
		}
		
		// P(H) = prior probability.
		double ph = this.prior;
		// P(E|H) = probability of evidence belonging to hypothesis.
		double peh = ((double) newScore) / 100;
		// P(H|E) = posterior probability.
		double phe = 0;

		Log.d(TAG, "P(H) = " + ph);
		Log.d(TAG, "P(E|H) = " + peh);

		// calculating posterior probability.
		phe = (ph * peh) / (ph * peh + (1 - ph) * (1 - peh));
		Log.d(TAG, "P(H|E) = " + phe);

		// saving posterior as prior for next iteration.
		if (phe != 0) {
			this.prior = phe;
		} else {
			// we don't let it be 0, otherwise the score will always remain 0
			this.prior = 0.01;
		}

		this.score = (int) (phe * 100);
		this.decayedWeight = this.initialWeight;

		sendDecayedScore();
		Log.d(TAG, "sendDecayedScore(newscore)-");
	}

	/**
	 * Function used for calculating and sending the current decayed score to
	 * the registered <code>AuthMech</code>.
	 */
	public void sendDecayedScore() {
		Log.d(TAG, "sendDecayedScore()+");

		int decayedScore = this.decayedWeight * this.score;

		try {
			clientWriter.send(Message.obtain(null, AUTH_MECH_GET_STATUS,
					decayedScore, this.initialWeight));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "sendDecayedScore-");
	}
}
