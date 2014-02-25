package com.fyp.services;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class UAService extends Service {

	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
	final Messenger messenger = new Messenger(new IncomingHandler()); // TODO: visibility 
	
	
	public static final int MSG_REGISTER_CLIENT = 0;
	public static final int MSG_UNREGISTER_CLIENT = 1;
	public static final int MSG_GET_STATUS = 2;

	@Override
    public void onCreate() {
		Log.i("UAService", "onCreate");
    }

    @Override
    public void onDestroy() {
    	Log.i("UAService", "onDestroy");
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
    	Log.i("UAService", "onBind");
        return messenger.getBinder();
    }

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				clients.add(msg.replyTo);
				break;

			case MSG_UNREGISTER_CLIENT:
				clients.remove(msg.replyTo);
				break;

			case MSG_GET_STATUS:
				for (Messenger client : clients) {
					try {
						client.send(Message.obtain(null, MSG_GET_STATUS,
								msg.arg1, 0));
					} catch (RemoteException e) {
						clients.remove(client);
					}
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
