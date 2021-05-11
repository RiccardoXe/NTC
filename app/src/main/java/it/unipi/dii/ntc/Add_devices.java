package it.unipi.dii.ntc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Add_devices extends AppCompatActivity
{

	private static final String TAG = Add_devices.class.getName();
	private static final int REQUEST_ENABLE_BT = 1234;
	private BroadcastBLTReceiver BLTReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_devices);
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null)
			Log.e(TAG, "startMonitoring: Device doesn't support bluetooth.");
		else if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		if (!bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.startDiscovery();
		}
		IntentFilter BLTIntFilter = new IntentFilter();
		BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		BLTIntFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		TableLayout tView = findViewById(R.id.BLTDevices);
		Log.i(TAG, "onCreate: " + tView);
		BLTReceiver = new BroadcastBLTReceiver(tView);
		getApplicationContext().registerReceiver(BLTReceiver, BLTIntFilter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		if (requestCode == REQUEST_ENABLE_BT)
			if (resultCode == RESULT_OK)
				Toast.makeText(Add_devices.this, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
			else
				Log.e(TAG, "onActivityResult: Can't turn on the bluetooth.");
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		getApplicationContext().unregisterReceiver(BLTReceiver);
	}
}