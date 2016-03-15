package com.damas.blejoystickapp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import com.damas.blejoystickapp.bluetooth.CommService;
import com.damas.blejoystickapp.bluetooth.CustomizedBluetoothDevice;
import com.damas.bluetoothkeyboard.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DeviceListActivity extends Activity {

	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	private static final int REQUEST_ENABLE_BT = 2;
	protected static final String PREFS_NAME = "Antonio081014 Bluetooth Android";
	protected static final String PREFS_DEVICE_ADDR = "Antonio081014 Bluetooth Address";

	private ArrayList<CustomizedBluetoothDevice> mDeviceList;

	private BluetoothAdapter mBluetoothAdapter;
	private ListView mListViewDeviceList;
	private Button mButtonStartScan;
	private int currentPosition;
	
	private boolean isDeviceinRange=false;

	private SharedPreferences settings;

	BaseAdapter mBaseAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater mInflater = (LayoutInflater) getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			CustomizedBluetoothDevice device = mDeviceList.get(position);

			View rowView = mInflater.inflate(R.layout.macaddr1, parent, false);
			if (device.isStatusPaired() == false)
				rowView = mInflater.inflate(R.layout.macaddr2, parent, false);

			TextView name = (TextView) rowView.findViewById(R.id.tv_addr_Name);
			TextView addr = (TextView) rowView.findViewById(R.id.tv_addr_ID);
			String status = device.isStatusPaired() ? "Paired" : "Not Paired";

			name.setText(status + ": " + device.getName() + ".");
			addr.setText(device.getAddress());
			return rowView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return mDeviceList.get(position);
		}

		@Override
		public int getCount() {
			if (mDeviceList != null)
				return mDeviceList.size();
			return 0;
		}
	};

	private void updateUI() {
		mBaseAdapter.notifyDataSetChanged();
	}

	private void getPairedDevice() {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				CustomizedBluetoothDevice customizedDevice = new CustomizedBluetoothDevice(
						device);
				mDeviceList.add(customizedDevice);
			}
		}
	}

	private void initialization() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			finish();
			return;
		}

		mListViewDeviceList = (ListView) findViewById(R.id.listview_devicelist);
		mListViewDeviceList.setAdapter(mBaseAdapter);
		mListViewDeviceList.setOnItemClickListener(mDeviceClickListener);

		mButtonStartScan = (Button) findViewById(R.id.btn_startScan);
		mButtonStartScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setup();
				doDiscovery();
			}
		});

		settings = getSharedPreferences(DeviceListActivity.PREFS_NAME, 0);

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);
		
		filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
		this.registerReceiver(mReceiver, filter);
		
		filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		this.registerReceiver(mReceiver,filter);
		
	}

	private void startConnect(CustomizedBluetoothDevice device) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(DeviceListActivity.PREFS_DEVICE_ADDR,
				device.getAddress());
		editor.commit();

	}

	// The on-click listener for all devices in the ListViews
	// It will auto-connect with the device.
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int position,
				long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			Intent resultIntent = new Intent();
			// TODO Add extras or a data URI to this intent as appropriate.

			mBluetoothAdapter.cancelDiscovery();
			BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(mDeviceList.get(position).getAddress());
			resultIntent.putExtra("MacAddress", device.getAddress());
			resultIntent.putExtra("DeviceName", device.getName());
			// check if device is still active or not

			currentPosition = position;
			if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
				// unpairDevice(device);
				//setResult(Activity.RESULT_OK, resultIntent);
				//finish();
				connectBT(device.getAddress(), device.getName());
			} else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
				if (D)
					Log.d(TAG, "device not bond");
				pairDevice(device);
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					//setResult(Activity.RESULT_OK, resultIntent);
					//finish();
					connectBT(device.getAddress(), device.getName());
				} else {
					Toast.makeText(getApplicationContext(),
							"Please pair your device!", Toast.LENGTH_LONG)
							.show();
				}

			}
		}
	};
	
	public void connectBT(String Mac, String deviceNameStr) {
		try {
			Intent intent = new Intent(getApplicationContext(),
					CommService.class);
			intent.putExtra("Mac", Mac);
			intent.putExtra("deviceNameStr", deviceNameStr);
			//Toast.makeText(this, "service started! ", Toast.LENGTH_SHORT).show();
			this.startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		// If we're already discovering, stop it
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();
	}

	private void pairDevice(BluetoothDevice device) {
		try {
			if (D)
				Log.d(TAG, "Start Pairing...");

			Method m = device.getClass()
					.getMethod("createBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass()
					.getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				CustomizedBluetoothDevice mDevice = new CustomizedBluetoothDevice(
						device);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

					if (mDeviceList.contains(mDevice) == false) {
						mDeviceList.add(mDevice);
						updateUI();
					}
				}
			}else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				// Device is now connected
				isDeviceinRange=true;
				Toast.makeText(getApplicationContext(), "Service Started Successfully", Toast.LENGTH_LONG).show();
				finish();
			}else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				// Device is now connected
				Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
				//finish();
			}
			
			// When the device bond state changed.
			else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				int prevBondState = intent.getIntExtra(
						BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
				int bondState = intent.getIntExtra(
						BluetoothDevice.EXTRA_BOND_STATE, -1);


				if (prevBondState == BluetoothDevice.BOND_BONDED
						&& bondState == BluetoothDevice.BOND_NONE) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (currentPosition != -1
							&& currentPosition < mDeviceList.size()) {
						CustomizedBluetoothDevice mDevice = mDeviceList
								.get(currentPosition);
						if (device.getAddress().compareTo(mDevice.getAddress()) == 0) {
							mDevice.setStatusPaired(false);
							updateUI();
							pairDevice(device);
						}
					}
				} else if (prevBondState == BluetoothDevice.BOND_BONDING
						&& bondState == BluetoothDevice.BOND_BONDED) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (currentPosition != -1
							&& currentPosition < mDeviceList.size()) {
						CustomizedBluetoothDevice mDevice = mDeviceList
								.get(currentPosition);
						if (device.getAddress().compareTo(mDevice.getAddress()) == 0) {
							mDevice.setStatusPaired(true);
							updateUI();
							startConnect(mDevice);
						}
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);
		initialization();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent,
					DeviceListActivity.REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			setup();
		}
	}

	private void setup() {
		mDeviceList = new ArrayList<CustomizedBluetoothDevice>();
		currentPosition = -1;
		getPairedDevice();
		updateUI();
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getMenuInflater().inflate(R.menu., menu); return true; }
	 */

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case DeviceListActivity.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setup();
			} else {
				// User did not enable Bluetooth or an error occured
				if (D)
					Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}
}
