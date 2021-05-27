package it.unipi.dii.ntc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Calibration_Activity extends AppCompatActivity
{

	private static final String TAG = Calibration_Activity.class.getName();
	private boolean serviceCalibrationRunning = false;
	private static final int REQUEST_ENABLE_BT = 1234;
	private static final int ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE = 1024;
	private String prefFileName = "StoredDevices";
	private String calibrationTargetKey;
	private BroadcastBLTReceiver BLTReceiver;
	private RSSIScan_Service scanningService;
	private Intent intentRSSIScan;


	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);

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
/*
		intentRSSIScan = new Intent(Calibration_Activity.this, RSSIScan_Service.class);
		if (!bindService(intentRSSIScan, serviceConnection,BIND_AUTO_CREATE)) {
			Log.e(TAG, "Can not bind service.");
			return;
		}*/

		/* Register BLTIntentFilter to Find Bluetooth devices */
		IntentFilter BLTIntFilter = new IntentFilter();
		BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
		BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		TableLayout tView = findViewById(R.id.BLTDevices);
		Log.i(TAG, "onCreate: " + tView);
		BLTReceiver = new BroadcastBLTReceiver(tView, this);
		getApplicationContext().registerReceiver(BLTReceiver, BLTIntFilter);

		scanningService=MainActivity.scanningService;

		Button calibrationButton=findViewById(R.id.calibrationButton);
		calibrationButton.setEnabled(false);

	}
/*

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.i(TAG, "connection called");
			scanningService = ((RSSIScan_Service.ServiceBinder)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			scanningService = null;
		}

		@Override
		public void onBindingDied(ComponentName name)
		{
			scanningService = null;
		}
	};
*/

	public void setCalibrationButtonValue()
	{
		Button calibrationButton;
		calibrationButton = findViewById(R.id.calibrationButton);
		if (serviceCalibrationRunning == true) {
			calibrationButton.setText("STOP CALIBRATION");
		} else {
			calibrationButton.setText("START CALIBRATION");
		}

	}

	public void startCalibration(View vApp){
		if(serviceCalibrationRunning == false) {

			Log.i(TAG, "CALIBRATION HAS STARTED" );
			//startService(intentRSSIScan);
			scanningService.startPeriodicScan();
			scanningService.startCalibration(calibrationTargetKey);
		}
		else{
			//stopService(intentRSSIScan);
			Log.i(TAG, "STOPPING CALIBRATION SERVICE ");
			scanningService.stopPeriodicScan();
			scanningService.stopCalibration();
		}
		serviceCalibrationRunning = !serviceCalibrationRunning;
		setCalibrationButtonValue();
	}



	/**
	 * THis method fill the BLTFriends table with the data present in the Shared Preference
	 * file (MAC address - Device Name).
	 * This table contains all the matched devices.
	 * For these devices the distance should not be estimated
	 */
	public void addDeviceToCalibrationTarget(String key, String value){

		calibrationTargetKey = key;
		Log.d("Calibration target: ", key + ": " + value);
		TextView tx = findViewById(R.id.calibrationTargetDevice);
		String deviceName = value + "|" + key;

		tx.setText(deviceName);
		Button calibrationButton;
		calibrationButton = findViewById(R.id.calibrationButton);
		calibrationButton.setEnabled(true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		if (requestCode == REQUEST_ENABLE_BT)
			if (resultCode == RESULT_OK)
				Toast.makeText(Calibration_Activity.this, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
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