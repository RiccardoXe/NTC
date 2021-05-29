package it.unipi.dii.ntc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class Add_devices_Activity extends AppCompatActivity
{

	private static final String TAG = Add_devices_Activity.class.getName();
	private static final int REQUEST_ENABLE_BT = 1234;
	private static final int ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE = 1024;
	private String prefFileName = "StoredDevices";
	private BroadcastBLTReceiver BLTReceiver;

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_devices);
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		//Check if bluetooth is supported
		if (bluetoothAdapter == null)
			Log.e(TAG, "startMonitoring: Device doesn't support bluetooth.");

		//Check if bluetooth is enabled or else request to activate it
		else if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);
		if (!bluetoothAdapter.isDiscovering()) {
				Log.i(TAG, "onCreate: isDiscovering " + bluetoothAdapter.startDiscovery());
		}

		//Fill the Matched device table with the stored data
		fillTableMatchedDevices();

		/* Register BLTIntentFilter to Find Bluetooth devices */
		IntentFilter BLTIntFilter = new IntentFilter();
		BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
		BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		TableLayout tView = findViewById(R.id.BLTDevices);
		Log.i(TAG, "onCreate: " + tView);
		BLTReceiver = new BroadcastBLTReceiver(tView, this);
		getApplicationContext().registerReceiver(BLTReceiver, BLTIntFilter);
	}

	/**
	 * This method stores the key-value pair in the Shared Preference File
	 * @param key - MAC address of a Device
	 * @param value - Name of the device (if not presente NO_DEVICE_NAME by default)
	 */
	public void addElementToShared(String key, String value){
		SharedPreferences sharedPref = getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * This method given a key deletes the data in the Shared Preference File
	 * @param key - MAC address of the device to be no longer recognized
	 */
	public void deleteElementFromShared(String key){
		SharedPreferences sharedPref = getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.remove(key);
		editor.commit();
	}

	/**
	 * THis method fill the BLTFriends table with the data present in the Shared Preference
	 * file (MAC address - Device Name).
	 * This table contains all the matched devices.
	 * For these devices the distance should not be estimated
	 */
	public void fillTableMatchedDevices(){
		int deviceCounter = 0;
		SharedPreferences sharedPref = getSharedPreferences(prefFileName, Context.MODE_PRIVATE);

		//Find the tableLayout BLTFriend
		TableLayout tViewMatched = findViewById(R.id.BLTFriends);
		tViewMatched.removeAllViews();
		String deviceName ="";

		//Retrive all the informations in the shared Preference File (MAC add - Device Name)
		Map<String, ?> storedDevicesMap = sharedPref.getAll();

		//For each tuple show it in the BLTFriends TableLayout
		// 1- Create a TableRow
		// 2- Create a TextView containing the "DeviceName + MAC "
		// 3- For that TextView set an on click listener to delete a device from the
		//	the recognized devices
		// 4- Append the TextView to the TableRow
		// 5- Append the TableRow to the TableLayout
		for (Map.Entry<String, ?> entry : storedDevicesMap.entrySet()) {
			Log.d("map values", entry.getKey() + ": " + entry.getValue());
			TableRow row = new TableRow(getApplicationContext());
			TextView tx = new TextView(getApplicationContext());

			deviceName = entry.getValue().toString() + "|" + entry.getKey();

			tx.setText(deviceName);
			tx.setId(deviceCounter);
			tx.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView selectedTx = (TextView) v.findViewById(v.getId());
					String selectedTxText = (String) selectedTx.getText();
					String[] MacName = selectedTxText.split("\\|");
					Log.i(TAG, "Cliccato " + selectedTx.getText());
					//MacName[0] contains the Name of the device
					//MacName[1] contains the MAC of the device
					//Delete the selected MAC from the Shared File
					deleteElementFromShared(MacName[1]);
					fillTableMatchedDevices();

				}
			});
			row.addView(tx);
			tViewMatched.addView(row);
			deviceCounter++;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		if (requestCode == REQUEST_ENABLE_BT)
			if (resultCode == RESULT_OK)
				Toast.makeText(Add_devices_Activity.this, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
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