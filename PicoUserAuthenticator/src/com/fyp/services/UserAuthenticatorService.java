package com.fyp.services;

import com.fyp.activities.PicoMainActivity.PicoBroadcastReceiver;
import com.fyp.logic.UserAuthenticator;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UserAuthenticatorService extends IntentService {

	private static UserAuthenticator ua = null;
	private static AuthenticatorThread serviceThread = null;

	public UserAuthenticatorService() {
		super("UserAuthenticatorService");
	}

	public UserAuthenticatorService(String name) {
		super(name);
	}

	public void onDestroy() {
		super.onDestroy();
	}
	
	protected void onHandleIntent(Intent intent) {
		Log.d("UAS-service", "onHandleIntent");

		String command = intent.getStringExtra("command");

		if (command.equals("start")) {
			startIntentCommand();
		} else if (command.equals("stop")) {
			stopIntentCommand();
		} else {
			Log.i("UAS intent", "unrecognised command: " + command);
		}

	}
	
	private void startIntentCommand() {
		// makes sure the UserAuthenticator bridge is active
		ua = this.getUserAuthSingleton();

		if (serviceThread == null) {
			serviceThread = new AuthenticatorThread();
			serviceThread.start();
		}
	}

	private void stopIntentCommand() {
		try {
			if (serviceThread != null) {
				serviceThread.stopThread();
				serviceThread.join();
				serviceThread = null;
			}

			this.stopSelf();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private UserAuthenticator getUserAuthSingleton() {
		if (UserAuthenticatorService.ua == null) {
			return new UserAuthenticator();
		} else {
			return UserAuthenticatorService.ua;
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
			Intent intent = new Intent();
			intent.setAction(PicoBroadcastReceiver.INTENT_RECEIVER);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("result", ua.isAuthenticated());

			sendBroadcast(intent);
		}

	}

}
