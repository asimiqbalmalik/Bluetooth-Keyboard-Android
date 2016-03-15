package com.damas.blejoystickapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.damas.bluetoothkeyboard.R;

public class JoystickIME extends InputMethodService {

	private boolean isBluetoothConnected = false;

	private Button mbtnLeft;
	private Button mbtnRight;
	private Button mbtnUp;
	private Button mbtnDown;
	private EditText mtxtBleData;
	private String mFirstCharacter;
	private String mSecondCharacter;
	private long mKeyboardTimeout;
	private String mKeyboardTime = "";
	private boolean isKeyboardCountend;
	private BroadcastReceiver mReceiver = null;
	private ImageView mImgViewBluetooth;

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		public void run() {
			// afficher();
			changeImageBluetooth();
		}
	};

	@Override
	public View onCreateInputView() {
		Log.d("JoystickIME", "onCreateInputView CALLED!");
		View mInputView = (View) getLayoutInflater().inflate(R.layout.view,
				null);
		mtxtBleData = (EditText) mInputView.findViewById(R.id.txtBleData);
		mbtnLeft = (Button) mInputView.findViewById(R.id.btnLeft);
		mbtnRight = (Button) mInputView.findViewById(R.id.btnRight);
		mbtnUp = (Button) mInputView.findViewById(R.id.btnUp);
		mbtnDown = (Button) mInputView.findViewById(R.id.btnDown);
		mImgViewBluetooth = (ImageView) mInputView
				.findViewById(R.id.imageviewBluetooth);

		changeImageBluetooth();
		// handler.postDelayed(runnable, 1000);

		if (mReceiver == null) {
			IntentFilter filter = new IntentFilter("speedExceeded");
			IntentFilter filter11 = new IntentFilter("Ext");

			mReceiver = new AdbReceiver();
			registerReceiver(mReceiver, filter11);
			LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
					filter);

			IntentFilter filter1 = new IntentFilter(
					BluetoothDevice.ACTION_ACL_CONNECTED);
			IntentFilter filter2 = new IntentFilter(
					BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
			IntentFilter filter3 = new IntentFilter(
					BluetoothDevice.ACTION_ACL_DISCONNECTED);
			this.registerReceiver(mReceiver, filter1);
			this.registerReceiver(mReceiver, filter2);
			this.registerReceiver(mReceiver, filter3);
		}

		return mInputView;
	}

	public void changeImageBluetooth() {
		Log.d("changeImageBluetooth", "" + isBluetoothConnected);
		if (isBluetoothConnected) {
			mImgViewBluetooth.setBackgroundDrawable(this.getResources()
					.getDrawable(R.drawable.bluetoothoff)); // on
		} else {
			mImgViewBluetooth.setBackgroundDrawable(this.getResources()
					.getDrawable(R.drawable.bluetoothon)); // off

		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("JoystickIME", "onCreate CALLED!");
	}

	@Override
	public void onDestroy() {
		if (mReceiver != null)
			unregisterReceiver(mReceiver);
		super.onDestroy();
		Log.d("JoystickIME", "onDestroy CALLED!");
	}

	@Override
	public void onFinishInput() {
		// TODO Auto-generated method stub
		super.onFinishInput();
		Log.d("JoystickIME", "onFinishInput CALLED!");
	}

	@Override
	public void onInitializeInterface() {
		// TODO Auto-generated method stub
		super.onInitializeInterface();
		Log.d("JoystickIME", "onInitializeInterface CALLED!");

	}

	public void takeAction(String message) {
		if (message.length() == 2) {
			mFirstCharacter = message.substring(0, 1);
			mSecondCharacter = message.substring(1, 2);
			// mtxtBleData.setText(intent.getStringExtra("Message"));
			InputConnection ic = getCurrentInputConnection();

			if (mSecondCharacter.equalsIgnoreCase("T")) {
				// TOUCHED BLUE
				if (mFirstCharacter.equalsIgnoreCase("L")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_blue);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(d);
					mbtnRight.setBackgroundDrawable(r);
					mbtnUp.setBackgroundDrawable(r);
					mbtnDown.setBackgroundDrawable(r);
					mtxtBleData.setText("Left Touched");
					// send text to edit text

				} else if (mFirstCharacter.equalsIgnoreCase("R")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_blue);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(r);
					mbtnRight.setBackgroundDrawable(d);
					mbtnUp.setBackgroundDrawable(r);
					mbtnDown.setBackgroundDrawable(r);
					mtxtBleData.setText("Right Touched");
				} else if (mFirstCharacter.equalsIgnoreCase("U")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_blue);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(r);
					mbtnRight.setBackgroundDrawable(r);
					mbtnUp.setBackgroundDrawable(d);
					mbtnDown.setBackgroundDrawable(r);
					mtxtBleData.setText("Up Touched");
				} else if (mFirstCharacter.equalsIgnoreCase("D")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_blue);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(r);
					mbtnRight.setBackgroundDrawable(r);
					mbtnUp.setBackgroundDrawable(r);
					mbtnDown.setBackgroundDrawable(d);
					// mtxtBleData.setText("Down Touched");
				}

			} else if (mSecondCharacter.equalsIgnoreCase("U")) {
				// UnPressed ORIGINAL
				if (mFirstCharacter.equalsIgnoreCase("L")) {
					Drawable d = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(d);

					// mtxtBleData.setText("Left UnPressed");
				} else if (mFirstCharacter.equalsIgnoreCase("R")) {
					Drawable d = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnRight.setBackgroundDrawable(d);
					// mtxtBleData.setText("Right UnPressed");
				} else if (mFirstCharacter.equalsIgnoreCase("U")) {
					Drawable d = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnUp.setBackgroundDrawable(d);
					// mtxtBleData.setText("Up UnPressed");
				} else if (mFirstCharacter.equalsIgnoreCase("D")) {
					Drawable d = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnDown.setBackgroundDrawable(d);
					// mtxtBleData.setText("Down UnPressed");
				}

			} else if (mSecondCharacter.equalsIgnoreCase("P")) {
				// PRESSED RED
				if (mFirstCharacter.equalsIgnoreCase("L")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_red);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(d);
					mbtnRight.setBackgroundDrawable(r);
					mbtnUp.setBackgroundDrawable(r);
					mbtnDown.setBackgroundDrawable(r);
					if (mtxtBleData != null)
						mtxtBleData.setText("Left Pressed");
					// SEND VALUE TO THAT EDIT TEXT
					if (ic != null)
						ic.commitText("L", 1);
				} else if (mFirstCharacter.equalsIgnoreCase("R")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_red);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(r);
					mbtnRight.setBackgroundDrawable(d);
					mbtnUp.setBackgroundDrawable(r);
					mbtnDown.setBackgroundDrawable(r);
					if (mtxtBleData != null)
						mtxtBleData.setText("Right Pressed");
					// SEND VALUE TO THAT EDIT TEXT
					if (ic != null)
						ic.commitText("R", 1);
				} else if (mFirstCharacter.equalsIgnoreCase("U")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_red);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(r);
					mbtnRight.setBackgroundDrawable(r);
					mbtnUp.setBackgroundDrawable(d);
					mbtnDown.setBackgroundDrawable(r);
					if (mtxtBleData != null)
						mtxtBleData.setText("Up Pressed");
					// SEND VALUE TO THAT EDIT TEXT
					if (ic != null)
						ic.commitText("U", 1);
				} else if (mFirstCharacter.equalsIgnoreCase("D")) {
					Drawable d = getResources().getDrawable(
							R.drawable.gradient_red);
					Drawable r = getResources()
							.getDrawable(R.drawable.gradient);
					mbtnLeft.setBackgroundDrawable(r);
					mbtnRight.setBackgroundDrawable(r);
					mbtnUp.setBackgroundDrawable(r);
					mbtnDown.setBackgroundDrawable(d);
					if (mtxtBleData != null)
						mtxtBleData.setText("Down Pressed");
					// SEND VALUE TO THAT EDIT TEXT
					if (ic != null)
						ic.commitText("D", 1);
				}
			}

		}

	}

	class AdbReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Device found

			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				// Device is now connected
				isBluetoothConnected = true;

				changeImageBluetooth();

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// Done searching
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				// Device has disconnected
				// mImgViewBluetooth.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bluetoothon));
				isBluetoothConnected = false;
				changeImageBluetooth();
			} else if (action.equalsIgnoreCase("isBluetoothConnected")) {
				// Done searching
				isBluetoothConnected = false;
				changeImageBluetooth();
			} else if (action.equalsIgnoreCase("speedExceeded")) {
				/*
				 * if(isKeyboardActive){ isKeyboardActive=false; }else
				 * if(!isKeyboardActive)isKeyboardActive=true; showKeyboard();
				 */
				// values equal to textview
				// showKeyboard();
				SharedPreferences settings = getApplication()
						.getSharedPreferences("BTJOYSTICK1", 0);
				mKeyboardTime = settings.getString("KeyboardTimeout",
						"30 Seconds");
				if (mKeyboardTime.equalsIgnoreCase("Never")) {

				} else if (mKeyboardTime.equalsIgnoreCase("30 Seconds")) {
					mKeyboardTimeout = 30000;

				} else if (mKeyboardTime.equalsIgnoreCase("1 Minutes")) {
					mKeyboardTimeout = 60000;

				} else if (mKeyboardTime.equalsIgnoreCase("5 Minutes")) {
					mKeyboardTimeout = 300000;

				} else if (mKeyboardTime.equalsIgnoreCase("15 Minutes")) {
					mKeyboardTimeout = 900000;

				} else if (mKeyboardTime.equalsIgnoreCase("30 Minutes")) {
					mKeyboardTimeout = 1800000;

				} else if (mKeyboardTime.equalsIgnoreCase("1 Hour")) {
					mKeyboardTimeout = 3600000;

					
				}
				isKeyboardCountend = true;

				if (!intent.getStringExtra("Message").equalsIgnoreCase("")) {
					isBluetoothConnected = true;
					changeImageBluetooth();
					Log.d("JoystickIME", "JoystickIme message"+intent.getStringExtra("Message"));
					takeAction(intent.getStringExtra("Message"));
					isKeyboardCountend = false;
				} else if (intent.getStringExtra("Message")
						
						.equalsIgnoreCase("")) {
					new CountDownTimer(mKeyboardTimeout, 1000) {

						public void onTick(long millisUntilFinished) {
						}

						public void onFinish() {
							// mTextField.setText("done!");
							if (isKeyboardCountend) {

							}
						}
					}.start();

				}
			}

		}
	}
}