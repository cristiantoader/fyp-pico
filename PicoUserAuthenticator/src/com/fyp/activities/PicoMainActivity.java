package com.fyp.activities;

import com.fyp.activities.R;
import com.fyp.activities.util.SystemUiHider;
import com.fyp.services.UAService;
import com.fyp.services.UserAuthenticatorService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PicoMainActivity extends Activity {

	/**
	 * UI stuff.
	 */
	@SuppressWarnings("unused")
	private View controlsView;
	private TextView contentView;

	/**
	 * Intent to authenticator service
	 */
	private Intent picoService;
	private PicoBroadcastReceiver receiver;

	private final Messenger messageReceiver = new Messenger(new IncomingHandler());
	private Messenger messageService = null;
	private boolean boundService = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pico_main);

		this.controlsView = findViewById(R.id.fullscreen_content_controls);
		this.contentView = (TextView) findViewById(R.id.text);

		findViewById(R.id.Button01).setOnClickListener(buttonStartListener);
		findViewById(R.id.Button03).setOnClickListener(buttonStopListener);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system
	 * UI. This is to prevent the jarring behavior of controls going away while
	 * interacting with activity UI.
	 */
	View.OnClickListener buttonStartListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (picoService == null) {
				picoService = new Intent(PicoMainActivity.this,
						UserAuthenticatorService.class);
				picoService.putExtra("command", "start");
				startService(picoService);
			} else {
				contentView.append("Pico service already running.\n");
			}
		}
	};

	View.OnClickListener buttonStopListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (picoService != null) {
				// issue stop command
				picoService.putExtra("command", "stop");
				startService(picoService);

				// remove service
				picoService = null;
			} else {
				contentView.append("Pico service NULL\n");
			}

		}
	};

	private void registerReceiver() {
		receiver = new PicoBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter(
				PicoBroadcastReceiver.INTENT_RECEIVER);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(receiver, intentFilter);
	}

	private void doBindService() {
		bindService(new Intent(this, UAService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
		boundService = true;
	}

	private void doUnbindService() {
		if (boundService) {
			unbindService(serviceConnection);
			boundService = false;
		}
	}

	public class PicoBroadcastReceiver extends BroadcastReceiver {
		public static final String INTENT_RECEIVER = "com.fyp.activities.intentreceiver";
		private static final String RESULT = "result";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BroadcastReceiver", "received intent");

			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String result = String.valueOf(bundle.getBoolean(RESULT));
				contentView.append(result + "\n");
			}
		}
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UAService.MSG_GET_STATUS:
				contentView.append("Received from service: " + msg.arg1 + "\n");
				break;
			default:
				super.handleMessage(msg);
			}
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

}
