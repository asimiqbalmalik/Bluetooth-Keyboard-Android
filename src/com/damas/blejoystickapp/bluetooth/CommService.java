package com.damas.blejoystickapp.bluetooth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;




import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class CommService extends Service {

	private final static String TAG = "CommService";
	//final Handler handler = new Handler();
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;


	private BluetoothSocket mSocket;
	private InputStream mIstream;
	private OutputStream mOstream;
	// private Handler mHandler;
	private String mMac;
	private String mDeviceName;
	private BluetoothAdapter mBluetoothAdapter;

	private StringBuffer sb;
	private boolean isAppSyncCompleted;
	
	private final static int THREAD_RECEIVE_DATA=1;
	private final static int THREAD_SEND_DATA=2;


	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "msg.what in handleMessage "+msg.what);
			switch(msg.what){
			case THREAD_RECEIVE_DATA:
				getData(msg);
				break;
			case THREAD_SEND_DATA:
				while(sb.length()>0){
					sendAppSyncBroadcast();
				}
				if(!(sb.length()>0))
					isAppSyncCompleted=true;
				break;
			}
			
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//Toast.makeText(getApplicationContext(), "Service is started!",Toast.LENGTH_SHORT).show();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent != null) {
			SharedPreferences Settings = getApplication().getSharedPreferences(
					"BTJOYSTICK0", 0);
			SharedPreferences.Editor editor = Settings.edit();
			editor.putString("Mac", intent.getStringExtra("Mac"));
			editor.putString("deviceNameStr",
					intent.getStringExtra("deviceNameStr"));
			editor.commit();
		}
		//Toast.makeText(getApplicationContext(), "Service is running!",Toast.LENGTH_SHORT).show();

		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		Message msg = mServiceHandler.obtainMessage();
		msg.what = THREAD_RECEIVE_DATA;
		msg.arg1=startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		final Intent intent = new Intent("isBluetoothConnected");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		stopSelf();
	}

	public void getData(Message msg) {
		// get mac and devicename

		SharedPreferences settings = getApplication().getSharedPreferences(
				"BTJOYSTICK0", 0);
		this.mMac = settings.getString("Mac", "");
		this.mDeviceName = settings.getString("deviceNameStr", "");

		if (mBluetoothAdapter == null)
			return;

		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		BluetoothDevice device = null;
		try {
			for (BluetoothDevice curDevice : devices) {
				if (curDevice.getName().matches(mDeviceName)) {

					device = curDevice;

					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (device == null)
			try {
				device = mBluetoothAdapter.getRemoteDevice(mMac);
			} catch (Exception e) {
				e.printStackTrace();
			}

		try {
			mSocket = device.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			mSocket.connect();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Device not in Range!",Toast.LENGTH_SHORT).show();
			mSocket = null;
		}
		if (mSocket == null)
			return;

		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		try {
			tmpIn = mSocket.getInputStream();
			tmpOut = mSocket.getOutputStream();
		} catch (IOException e) {
		}

		
		HandlerThread thread = new HandlerThread("AppSyncServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		mIstream = tmpIn;
		mOstream = tmpOut;
		sb = new StringBuffer();
		byte[] buffer = new byte[1024]; // buffer store for the stream
		int bytes; // bytes returned from read()
		isAppSyncCompleted=true;

		while (true) {
			try {

				// Read from the InputStream
				bytes = mIstream.read(buffer);
				sb.append(new String(buffer, 0, bytes));
				Log.d("iSTREAM", "...Data from arduino Stringbuffers "+sb);
				int endindex=sb.indexOf(";");
				if(endindex!=-1){
					//Execute background task if not empty	
					if(isAppSyncCompleted)
						startAppSyncThread();				
				}else if(endindex<1){
					sb.replace(0, endindex+1, "");
				}
				
			} catch (IOException e) {
				break;
			}
		}
		// Stop the service using the startId, so that we don't stop
		// the service in the middle of handling another job
		stopSelf(msg.arg1);
	}
	
	private void startAppSyncThread() {
		
		Log.d(TAG, "startAppSyncThread() is called");
		Message msg = mServiceHandler.obtainMessage();
		msg.what = THREAD_SEND_DATA;
		mServiceHandler.sendMessage(msg);
	} 
	private void sendAppSyncBroadcast() {
		isAppSyncCompleted=false;
		String message;
		int endindex=sb.indexOf(";");
		if(endindex!=-1){
			message=sb.substring(0, endindex);
			Log.d(TAG, "data from arduino message" + message);
			
			if (!message.equalsIgnoreCase("")) {

				final Intent intent = new Intent("speedExceeded");
				intent.putExtra("Message", message);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
						intent);
				sb.replace(0, endindex+1, "");
			}else if(endindex<1){
				sb.replace(0, endindex+1, "");
			}
			
		}
	} 

	/* Call this from the main Activity to shutdown the connection */
	public void cancel() {
		try {
			if (mSocket != null)
				mSocket.close();
		} catch (IOException e) {
		}
	}
	

}
