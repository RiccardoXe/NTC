package it.unipi.dii.ntc;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class Monitoring_devices_Activity extends AppCompatActivity
{

	private static final int REQUEST_ENABLE_BT = 1234;
	private static final int ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE = 1024;
	private static final String TAG = "Discovery activity";

	/**
	 * Checks if bluetooth is enabled
	 * And start the Service to monitor the bluetooth RSSI values
	 * @param savedInstanceState
	 */
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring_devices);

		/* ACTIVATE BLUETOOTH */
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		//Check if bluetooth is supported
		if (bluetoothAdapter == null)
			Log.e(TAG, "startMonitoring: Device doesn't support bluetooth.");

			//Check if bluetooth is enabled
			//TODO: Wait untill bluetooth is activated
		else if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);
		if (!bluetoothAdapter.isDiscovering()) {
			Log.i(TAG, "onCreate: isDiscovering " + bluetoothAdapter.startDiscovery());
		}


		Intent intentRSSIScan = new Intent(Monitoring_devices_Activity.this, RSSIScan_Service.class);
		startService(intentRSSIScan);
	}

	//on destroy close monitoring (the monitoring should be performed in background)
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Intent intent = new Intent(Monitoring_devices_Activity.this, RSSIScan_Service.class);
		stopService(intent);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		String permissionName = String.valueOf(requestCode);
		if (requestCode == ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE)
			permissionName = "ACCESS_FINE_LOCATION_STATE";
		boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
		Log.d(TAG, permissionName + " " + (granted ? "granted." : "denied."));
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private void checkPermission(String permission, int requestCode)
	{
		if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Permission " + permission + " already granted.");
			return;
		}
		requestPermissions(new String[] { permission }, requestCode);
	}
}