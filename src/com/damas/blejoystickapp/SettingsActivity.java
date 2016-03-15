package com.damas.blejoystickapp;

import com.damas.blejoystickapp.bluetooth.CommService;
import com.damas.bluetoothkeyboard.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener {

	private static final int MY_CHILD_ACTIVITY = 1;
	private static final String TAG = "SettingsActivity";

	private TextView txtConnectBluetooth;
	private TextView txtKeyboardTimeout;
	private String mBleDeviceMac;
	private String mBleDeviceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		txtConnectBluetooth = (TextView) findViewById(R.id.txtConnectBluetooth);
		txtKeyboardTimeout = (TextView) findViewById(R.id.txtKeyboardTimeout);
		txtConnectBluetooth.setOnClickListener(this);
		txtKeyboardTimeout.setOnClickListener(this);

	}

	public void connectBT(String Mac, String deviceNameStr) {
		try {
			Intent intent = new Intent(getApplicationContext(),
					CommService.class);
			intent.putExtra("Mac", Mac);
			intent.putExtra("deviceNameStr", deviceNameStr);
			Toast.makeText(this, "service started! ", Toast.LENGTH_SHORT)
					.show();
			this.startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (MY_CHILD_ACTIVITY): {
			if (resultCode == Activity.RESULT_OK) {
				// TODO Extract the data returned from the child Activity.
				mBleDeviceName = data.getStringExtra("MacAddress");
				mBleDeviceMac = data.getStringExtra("DeviceName");
				Log.d("MainActivity", "deviceMac + deviceName " + mBleDeviceMac
						+ " + " + mBleDeviceName);
				connectBT(mBleDeviceName, mBleDeviceMac);
			}
			break;
		}
		}
	}

	public void showCustomDialog() {
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.keyboardtimeout);
		dialog.setTitle("Keyboard Timeout");

		// set the custom dialog components - text, image and button
		final NumberPicker mhoursNpick = (NumberPicker) dialog
				.findViewById(R.id.npicker_hours);
		final NumberPicker mminutesNpick = (NumberPicker) dialog
				.findViewById(R.id.npicker_minutes);
		final NumberPicker msecondsNpick = (NumberPicker) dialog
				.findViewById(R.id.npicker_seconds);
		mhoursNpick.setMinValue(0);
		mhoursNpick.setMaxValue(10);
		mhoursNpick.setWrapSelectorWheel(false);
		mminutesNpick.setMinValue(0);
		mminutesNpick.setMaxValue(10);
		mminutesNpick.setWrapSelectorWheel(false);
		msecondsNpick.setMinValue(0);
		msecondsNpick.setMaxValue(10);
		msecondsNpick.setWrapSelectorWheel(false);
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SharedPreferences Settings = getApplication()
						.getSharedPreferences("BTJOYSTICK1", 0);
				SharedPreferences.Editor editor = Settings.edit();
				editor.putString("Hours", mhoursNpick.toString());
				editor.putString("Minutes", mminutesNpick.toString());

				editor.putString("Seconds", msecondsNpick.toString());

				editor.commit();
				dialog.dismiss();

			}
		});

		dialog.show();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		switch (arg0.getId()) {
		case R.id.txtConnectBluetooth:
			// connectbluetooth
			Intent intent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(intent, MY_CHILD_ACTIVITY);

			break;
		case R.id.txtKeyboardTimeout:
			// connectbluetooth
			showCustomDialog();
			break;
		default:
			break;
		}
	}

}
