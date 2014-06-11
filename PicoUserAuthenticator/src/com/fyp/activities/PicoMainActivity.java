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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressWarnings("unused")
public class PicoMainActivity extends Activity {

	/** Main view reference used by the Activity. */
	private View controlsView;
	/** Text view used for displaying authentication confidence. */
	private TextView contentView;

	/** Messenger used for receiving data from UAService. */
	private Messenger messageReceiver = new Messenger(new IncomingHandler(this));
	/** Messenger used for sending commands to UAService. */
	private Messenger messageService = null;

	/** State variable that determines if UAService is currently bound. */
	private boolean boundService = false;

	/** Android logging tag used by the class. */
	private static final String TAG = "PicoMainActivity";

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
		findViewById(R.id.ButtonLocation).setOnClickListener(
				buttonLocationListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * Method used for starting and binding the activity to UAService.
	 */
	private void doBindService() {
		Intent i = new Intent(this, UAService.class);

		startService(i);
		bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

		boundService = true;
	}

	/**
	 * Method used for unbinding the activity from UAService. This does not stop
	 * the service.
	 */
	private void doUnbindService() {
		if (boundService) {
			unbindService(serviceConnection);
			boundService = false;
		}
	}

	/**
	 * Method used for stopping UAService. This method only works if no other
	 * clients are currently connected to the service.
	 */
	private void doStopService() {
		Intent i = new Intent(this, UAService.class);
		stopService(i);
	}

	/**
	 * Listener for the start button of the Activity.
	 * 
	 * This listener is responsible for starting and binding the UAService.
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

	/**
	 * Listener for the stop button of the Activity.
	 * 
	 * The listener is responsible from unbinding and stopping UAService. It is
	 * important to stop UAService while testing. Otherwise it will remain
	 * running in the background.
	 */
	View.OnClickListener buttonStopListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (boundService) {
				doUnbindService();
				doStopService();

				contentView.append("Pico service stopped. \n");
			}

		}
	};

	/**
	 * Listener used for starting the activity responsible of configuring voice
	 * recognition owner data.
	 */
	View.OnClickListener buttonAudioListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(PicoMainActivity.this, VoiceActivity.class));
		}
	};

	/**
	 * Listener used for starting the activity responsible of configuring face
	 * recognition owner data.
	 */
	View.OnClickListener buttonFaceListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(PicoMainActivity.this, FaceActivity.class));
		}
	};

	/**
	 * Listener used for starting the activity responsible of configuring
	 * location analysis owner data.
	 */
	View.OnClickListener buttonLocationListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(PicoMainActivity.this,
					LocationActivity.class));
		}
	};

	/**
	 * The class is responsible of processing messages received from UAService.
	 * 
	 * @author cristi
	 * 
	 */
	static class IncomingHandler extends Handler {
		WeakReference<PicoMainActivity> amwr;

		/**
		 * Weak reference to the main activity in order to access the text view
		 * component such that data received from UAService can be displayed by
		 * the Activity.
		 * 
		 * @param am
		 *            PicoMainActivity reference.
		 */
		public IncomingHandler(PicoMainActivity am) {
			this.amwr = new WeakReference<PicoMainActivity>(am);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case UAService.MSG_CONFIDENCE_UPDATE:
				int confidence = msg.arg1;
				amwr.get().contentView.append("Confidence level: " + confidence
						+ "\n");
				break;

			default:
				Log.w(TAG, "Unknown message received from UAService.");
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Class used for binding to the UAService.
	 * 
	 * This only issues a MSG_REGISTER_CLIENT request. All subsequent
	 * communication is done via messages from UAService. The PicoMainActivity
	 * may choose to send explicit requests using the messageService object
	 * instantiated when binding to UAService.
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			messageService = new Messenger(service);

			try {
				// arg1 is the threshold for the client authentication.
				Message msg = Message.obtain(null,
						UAService.MSG_REGISTER_CLIENT);
				msg.replyTo = messageReceiver;
				msg.arg1 = 50;

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
