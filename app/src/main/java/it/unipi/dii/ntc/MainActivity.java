package it.unipi.dii.ntc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.button.MaterialButton;

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
	//From this is possible to call the Service methods
	public static RSSIScan_Service scanningService;
	private Intent intentRSSIScan;

	private final double[] distances = {0.5, 1, 1.5, 2};


	private boolean serviceMonitoringRunning = false;
	private boolean serviceLoggingRunning = false;

	/**
	 * On create of the main activity also the RSSIScan_Service is created and binded
	 * @param savedInstanceState
	 */
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check if the detection service is enabled
		serviceMonitoringRunning = isServiceRunning();
		Log.i("INFO", "THE SERVICE IS RUNNINIG" + serviceMonitoringRunning);

		intentRSSIScan = new Intent(MainActivity.this, RSSIScan_Service.class);

		//bind the service to the main activity
		if (!bindService(intentRSSIScan, serviceConnection,BIND_AUTO_CREATE)) {
			Log.e(TAG, "Can not bind service.");
			return;
		}

		setScanningButtonValue();
	}

	/**
	 * This is the Service connection
	 */
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.i(TAG, "connection called");
			scanningService = ((RSSIScan_Service.ServiceBinder)service).getService();
		}

		@RequiresApi(api = Build.VERSION_CODES.O)
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


	/**
	 * Function called by pressing Button:"CALIBRATION"
	 * Makes an intent to start Calibration_Activity
	 * @param vApp
	 */
	public void startCalibrationActivity(View vApp)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			checkPermission(Manifest.permission.ACTIVITY_RECOGNITION, ACTIVITY_RECOGNITION_PERMISSION_CODE);
		checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
			ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
				ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE);

		Intent myIntent = new Intent(MainActivity.this, Calibration_Activity.class);
		MainActivity.this.startActivity(myIntent);
	}

	/**
	 * Function called by pressing Button:"Add Devices"
	 * Makes an intent to start Add_device_activity (Discover devices and add to friends)
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

		Log.e(TAG, "startMonitoring: called");
		if(serviceMonitoringRunning == false) {

			//Check and request to enable bluetooth
			checkBluetoothEnabled();
			checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);

			//Start creates a thread that periodically will start a bluetooth discovery
			scanningService.startPeriodicScan();
			//Register RSSI Broadcast Receiver
			scanningService.startRSSIMonitoring();
		}
		else{
			//Remove the thread that periodically start the discovery
			scanningService.stopPeriodicScan();
			//Remove the RSSI Broadcast Receiver
			scanningService.stopRSSIMonitoring();
		}

		serviceMonitoringRunning = !serviceMonitoringRunning;
		setScanningButtonValue();
	}

	public void checkBluetoothEnabled(){
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//Check if bluetooth is supported
		if (bluetoothAdapter == null)
			Log.e(TAG, "startMonitoring: Device doesn't support bluetooth.");

			//Check if bluetooth is enabled
		else if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	/**
	 * Checks if the RSSIScan_Service is Running
	 * @return
	 */
	@RequiresApi(api = Build.VERSION_CODES.M)
	private boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) getSystemService(getApplicationContext().ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE))
			if (RSSIScan_Service.class.getName().equals(service.service.getClassName()))
				return true;
		return false;
	}

	/**
	 * This method change the text on buttons in order to
	 */
	public void setScanningButtonValue()
	{
		Button monitoringButton;
		monitoringButton = findViewById(R.id.monitoringButton);
		if (serviceMonitoringRunning == true) {
			monitoringButton.setText(R.string.STOP_MONITORING);
		} else {
			monitoringButton.setText(R.string.START_MONITORING);
		}

		monitoringButton = findViewById(R.id.rssilogger);
		if (serviceLoggingRunning == true) {
			monitoringButton.setText(R.string.STOP_LOG);
		} else {
			monitoringButton.setText(R.string.START_LOG);
		}

	}

	/**
	 * This methods takes the value of the seekbar
	 * Starts recording the samples
	 * @param vApp
	 */
	public void startRSSILogging(View vApp){
		if(serviceLoggingRunning == false) {
			SeekBar seekBarInfo = findViewById(R.id.prograssionBar);
			int accessIndex = seekBarInfo.getProgress();
			Log.i(TAG, "INDEX " + accessIndex);
			Log.i(TAG, "DISTANCE TO MEASURE " + distances[accessIndex]);
			//startService(intentRSSIScan);
			scanningService.startPeriodicScan();
			scanningService.startRSSILogging(distances[accessIndex]);
		}
		else{
			//stopService(intentRSSIScan);
			Log.i(TAG, "STOPPING LOGGING SERVICE ");
			scanningService.stopPeriodicScan();
			scanningService.stopRSSILogging();
		}
		serviceLoggingRunning = !serviceLoggingRunning;
		setScanningButtonValue();
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "Destroying and unbouning service");
		super.onDestroy();

		stopService(intentRSSIScan);
		unbindService(serviceConnection);

	}
}