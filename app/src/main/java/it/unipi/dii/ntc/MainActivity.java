package it.unipi.dii.ntc;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{

	private static final int ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE = 100;
	private static final int ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE = 101;
	private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 102;

	private static final String TAG = MainActivity.class.getName();
	private static final int REQUEST_ENABLE_BT = 1234;

	private Intent monitoringIntent;

	private boolean serviceRunning = false;

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check if the detecton service is enabled
		serviceRunning = isServiceRunning();
		Log.i("INFO", "THE SERVICE IS RUNNINIG" + serviceRunning);
		setScanningButtonValue();

	}


	/**
	 * Function called by pressing Button:"Add Devices"
	 * Makes an intent to start Add_device_activity
	 * @param vApp
	 */
	public void startBLTScan(View vApp)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			checkPermission(Manifest.permission.ACTIVITY_RECOGNITION, ACTIVITY_RECOGNITION_PERMISSION_CODE);
		checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
			ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
				ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE);

		Intent myIntent = new Intent(MainActivity.this, Add_devices_Activity.class);
		MainActivity.this.startActivity(myIntent);
	}

	private void checkPermission(String permission, int requestCode)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "Permission " + permission + " already granted.");
				return;
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(new String[]{permission}, requestCode);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		String permissionName = String.valueOf(requestCode);
		if (requestCode == ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE)
			permissionName = "ACCESS_FINE_LOCATION_STATE";
		else if (requestCode == ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE)
			permissionName = "ACCESS_BACKGROUND_LOCATION";
		else if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_CODE)
			permissionName = "ACTIVITY_RECOGNITION";
		boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
		Log.d(TAG, permissionName + " " + (granted ? "granted." : "denied."));
	}

	/**
	 * Method called pressing the button:"Start monitoring"
	 * Make an Intent and start Monitoring_devices_activity
	 * 	if the discovery sercice is not running -> Bluetooth enabled and monitoring started
	 * 	if the discovery service is already running -> Stop the service
	 * @param vApp
	 */
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void startMonitoring(View vApp){


		if(serviceRunning == false) {
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


			Intent intentRSSIScan = new Intent(MainActivity.this, RSSIScan_Service.class);
			startForegroundService(intentRSSIScan);
		}
		else{
			getApplicationContext().stopService(new Intent(MainActivity.this, RSSIScan_Service.class));
			//getApplicationContext().stopService(Intent(this, RSSIScan_Service::class.java));
		}

		serviceRunning = !serviceRunning;
		setScanningButtonValue();
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(getApplicationContext().ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE))
			if (RSSIScan_Service.class.getName().equals(service.service.getClassName()))
				return true;
		return false;
	}

	public void setScanningButtonValue()
	{
		Button monitoringButton;
		monitoringButton = (Button) findViewById(R.id.monitoringButton);
		if (serviceRunning == true) {
			monitoringButton.setText("STOP MONITORING");
		} else {
			monitoringButton.setText("START MONITORING");
		}
	}
}