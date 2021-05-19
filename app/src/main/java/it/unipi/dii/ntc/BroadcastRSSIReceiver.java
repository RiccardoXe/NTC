package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import it.unipi.dii.iodetectionlib.IODetection;

public class BroadcastRSSIReceiver extends BroadcastReceiver
{

	private static final String TAG = MainActivity.class.getName();
	private final IODetection ioDetection;
	private RSSIScan_Service rssiService;
	private String prefFileName = "StoredDevices";
	private int TimeToSleep = 5000;

	public BroadcastRSSIReceiver(RSSIScan_Service rssiS, IODetection ioDetection){
		rssiService = rssiS;
		this.ioDetection = ioDetection;
	}
	@SuppressLint("SetTextI18n")
	@Override
	public void onReceive(Context context, Intent intent)
	{
		double f;
		int deviceCounter = 0;
		String action = intent.getAction();
		SharedPreferences sharedPref = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
		//Device Discovery phase
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Discovery has found a device. Get the BluetoothDevice
			// object and its info from the Intent.
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String deviceName = device.getName();
			String deviceHardwareAddress = device.getAddress(); // MAC address
			int RSSIValue =  intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

			//Log.i(TAG, "Distance Estimation" + (double)(-69 - RSSIValue)/(10*2));
			// TODO: Put a dynamic Threshold instead of -45 and check if stored device
			if (/*RSSIValue > -65 && */sharedPref.contains(" "+deviceHardwareAddress) == true) {
				//if(sharedPref.contains(" "+deviceHardwareAddress) == true){
					//rssiService.createNotification(device.getName() + " :) FRIEND" );
				//	Log.i(TAG, "1m RSSI values monitoring");
				//	try {
				//		writeInCSV(deviceHardwareAddress, RSSIValue, context);
				//	} catch (IOException e) {
				//		e.printStackTrace();
				//	}
				//}
				//else {
				Log.i(TAG, "onReceive: Trovato dispositivo " + RSSIValue);
				Log.i(TAG, "onReceive: Trovato dispositivo " + device.getName());
				Log.i(TAG, "onReceive: Trovato dispositivo " + deviceHardwareAddress);
				if(ioDetection.get()){
					Log.i(TAG, "onReceive: INDOOR");
				}else{
					Log.i(TAG, "onReceive: OUTDOOR");
				}
				f = Math.pow(10, (double) (-69 - RSSIValue)/(10*5));
				Log.i(TAG, "Distance Estimation"+ f);
				rssiService.createNotification(device.getName() + " UNKNOWN DEVICE!!!");
				//}
			}
		}
		else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			Log.v("RSSI:","Discovery start back in 5s ");
			//try {
			//	Thread.sleep(TimeToSleep);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}

		}
	}

}
