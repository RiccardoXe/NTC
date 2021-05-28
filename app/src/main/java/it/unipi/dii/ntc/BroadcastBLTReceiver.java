package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Set;

public class BroadcastBLTReceiver extends BroadcastReceiver
{

	private static final String TAG = MainActivity.class.getName();
	private final HashMap<String, String> discoveredBLT = new HashMap<>();
	private final TableLayout BLTTable;
	private final Add_devices_Activity aDevices;
	private final Calibration_Activity cDevices;

	public BroadcastBLTReceiver(TableLayout BLTTable, Add_devices_Activity a)
	{
		this.BLTTable = BLTTable;
		aDevices = a;
		cDevices = null;
	}
	public BroadcastBLTReceiver(TableLayout BLTTable, Calibration_Activity a)
	{
		this.BLTTable = BLTTable;
		cDevices = a;
		aDevices = null;
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
			Log.i(TAG, "onReceive: Trovato dispositivo " + deviceName);
			Log.i(TAG, "onReceive: Trovato dispositivo " + deviceHardwareAddress);
			//Log.i(TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + device.getUuids());
			discoveredBLT.put(deviceHardwareAddress, deviceName);
		}
		//Show the discovered devices on the table in Add_devices object - UI update
		else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			Set<String> keys = discoveredBLT.keySet();
			BLTTable.removeAllViews();
			for (String key : keys) {
				TableRow row = new TableRow(context);
				TextView tx = new TextView(context);
				String tmp = discoveredBLT.get(key);
				if (tmp == null) {
					tx.setText("NAME_NOT_AVAILABLE"+ " | " + key );
				} else {
					tx.setText(tmp  + " | " + key);
				}
				//This adds to the row a click listener
				// (Need to save device data on database)
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
						//These data are stored in the shared key-value db
						if(cDevices == null){
							aDevices.addElementToShared(MacName[1],MacName[0]);
							aDevices.fillTableMatchedDevices();
						}
						else{
							MacName[1]=MacName[1].replaceAll("\\s+","");
							MacName[0]=MacName[0].replaceAll("\\s+","");
							cDevices.addDeviceToCalibrationTarget(MacName[1],MacName[0]);
						}



					}
				});
				row.addView(tx);
				BLTTable.addView(row);
				deviceCounter++;
			}
			//discoveredBLT.clear();
		}
	}


	/*public void sendBluetoothMessage(){
		BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
		if (blueAdapter != null) {
			if (blueAdapter.isEnabled()) {
				Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

				if(bondedDevices.size() > 0) {
					Object[] devices = (Object []) bondedDevices.toArray();
					BluetoothDevice device = (BluetoothDevice) devices[position];
					ParcelUuid[] uuids = device.getUuids();
					BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
					socket.connect();
					outputStream = socket.getOutputStream();
					inStream = socket.getInputStream();
				}

				Log.e("error", "No appropriate paired devices.");
			} else {
				Log.e("error", "Bluetooth is disabled.");
			}
		}
	}*/
}
