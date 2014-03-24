package com.fyp.activities;

import java.lang.ref.WeakReference;

import com.fyp.activities.R;
import com.fyp.activities.util.SystemUiHider;
import com.fyp.authenticator.UAService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

	private final Messenger messageReceiver = new Messenger(new IncomingHandler(
			this));
	private Messenger messageService = null;
	private boolean boundService = false;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pico_main);

		this.controlsView = findViewById(R.id.fullscreen_content_controls);
		this.contentView = (TextView) findViewById(R.id.text);

		findViewById(R.id.Button01).setOnClickListener(buttonStartListener);
		findViewById(R.id.Button03).setOnClickListener(buttonStopListener);

		findViewById(R.id.ButtonVoice).setOnClickListener(buttonAudioListener);
		findViewById(R.id.ButtonFace).setOnClickListener(buttonFaceListener);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
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

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system
	 * UI. This is to prevent the jarring behavior of controls going away while
	 * interacting with activity UI.
	 */
	View.OnClickListener buttonStartListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!boundService) {
				doBindService();
			} else {
				contentView.append("Pico service already bound. \n");
			}
		}
	};

	View.OnClickListener buttonStopListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (boundService) {
				doUnbindService();
			} else {
				contentView.append("Pico service already not bound. \n");
			}

		}
	};

	View.OnClickListener buttonAudioListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(PicoMainActivity.this, RecognitoActivity.class));
		}
	};
	
	View.OnClickListener buttonFaceListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(PicoMainActivity.this, FaceActivity.class));
		}
	};

	static class IncomingHandler extends Handler {
		WeakReference<PicoMainActivity> amwr;

		public IncomingHandler(PicoMainActivity am) {
			this.amwr = new WeakReference<PicoMainActivity>(am);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case UAService.MSG_GET_STATUS:
				boolean authenticated = (msg.arg1 == 1);
				amwr.get().contentView.append("Received from service: " + authenticated
						+ "\n");
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
