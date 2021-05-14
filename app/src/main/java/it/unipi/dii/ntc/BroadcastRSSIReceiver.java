package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Set;

public class BroadcastRSSIReceiver extends BroadcastReceiver
{

	private static final String TAG = MainActivity.class.getName();
	private RSSIScan_Service rssiService;
	private int TimeToSleep = 5000;

	public BroadcastRSSIReceiver(RSSIScan_Service rssiS){
		rssiService = rssiS;
	}
	@SuppressLint("SetTextI18n")
	@Override
	public void onReceive(Context context, Intent intent)
	{
		int deviceCounter = 0;
		String action = intent.getAction();
		//Device Discovery phase
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Discovery has found a device. Get the BluetoothDevice
			// object and its info from the Intent.
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String deviceName = device.getName();
			String deviceHardwareAddress = device.getAddress(); // MAC address
			int RSSIValue =  intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
			Log.i(TAG, "onReceive: Trovato dispositivo " + RSSIValue);
			Log.i(TAG, "onReceive: Trovato dispositivo " + deviceHardwareAddress);
			// TODO: Put a dynamic Threshold instead of -45 and check if stored device
			if(RSSIValue > -45){
				rssiService.createNotification("AN UNKNOWN DEVICE IS NEAR!!!");
			}
		}
		else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			Log.v("RSSI:","Discovery start back in 5s ");
			SystemClock.sleep(TimeToSleep);
			BluetoothAdapter.getDefaultAdapter().startDiscovery();
		}
	}


}
