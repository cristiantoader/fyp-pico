package com.fyp.unused;

// TODO: outdated

import java.util.Random;

import android.annotation.SuppressLint;
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

import com.fyp.authenticator.UAService;
import com.fyp.authenticator.voice.AuthVoiceService;

/**
 * TODO: tie this to a service context such that communication is possible with
 * the voice service.
 * 
 * @author cristi
 * 
 */
public class AuthDevVoice {

	private static AuthDevVoice dev = null;

	private Messenger messageService = null;
	private final Messenger messageReceiver = new Messenger(new IncomingHandler());
	private boolean boundService = false;

	private AuthDevVoice() {
//		this.confidence = 100;
	}

	public AuthDevVoice getDevice() {
		if (dev == null) {
			dev = new AuthDevVoice();
		}

		return dev;
	}

//	@Override
//	public int getConfidence() {
//		this.calculateConfidence();
//		return this.confidence;
//	}

	public static boolean checkExists() {
		return true;
	}

	private void calculateConfidence() {
//		this.confidence = new Random().nextInt(100);
	}

	private void doBindService(Context ctx) {
		ctx.bindService(new Intent(ctx, AuthVoiceService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
		boundService = true;
	}

	private void doUnbindService(Context ctx) {
		if (boundService) {
			ctx.unbindService(serviceConnection);
			boundService = false;
		}
	}

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			messageService = new Messenger(service);

			try {
				Message msg = Message.obtain(null, UAService.MSG_REGISTER_CLIENT);
				msg.replyTo = messageReceiver;
				messageService.send(msg);

			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		public void onServiceDisconnected(ComponentName className) {
			messageService = null;
		}
	};

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case UAService.MSG_GET_STATUS:
				boolean authenticated = (msg.arg1 == 1);
				Log.i("AuthDevVoice", "Received from service: " + authenticated + "\n");
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

}
