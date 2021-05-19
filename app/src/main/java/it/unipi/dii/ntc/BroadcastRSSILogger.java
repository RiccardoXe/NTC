package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;


public class BroadcastRSSILogger extends BroadcastReceiver
{

	private static final String TAG = MainActivity.class.getName();
	private RSSIScan_Service rssiService;
	private String prefFileName = "StoredDevices";
	private int TimeToSleep = 5000;
	private double referenceDistance;
	private String csvFileName = "RSSILoggingFile.csv";

	public BroadcastRSSILogger(RSSIScan_Service rssiS, double refDist){
		rssiService = rssiS;
		referenceDistance = refDist;

	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//Log.v(TAG," I AM THE LOGGER !!!!!!");
		int RSSIValue;
		Long tsLong = System.currentTimeMillis()/1000;
		String timestampCurrent = tsLong.toString();

		String action = intent.getAction();
		SharedPreferences sharedPref = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);

		//Device Discovery phase
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Discovery has found a device. Get the BluetoothDevice
			// object and its info from the Intent.
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String deviceHardwareAddress = device.getAddress(); // MAC address
			RSSIValue =  intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

			//Check if the device exists in the SharedPrefFile
			if (sharedPref.contains(" "+deviceHardwareAddress) == true) {
				//Write the data in a .csv for further analysis
				try {
					writeInCSV(timestampCurrent, deviceHardwareAddress, RSSIValue, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Log.i(TAG, "onReceive: Trovato dispositivo " + device.getName()
					+ " "+ deviceHardwareAddress +" "+ RSSIValue);
				rssiService.createNotification(device.getName() + "LOGGING NOTIFICATION");
			}
		}
		else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			Log.v(TAG,"Logging will start back soon ");
		}
	}

	public void writeInCSV(String time, String deviceMAC, int RSSI, Context c) throws IOException
	{
		CSVWriter writer;
		File outputFile = new File(c.getFilesDir() + File.separator + csvFileName);

		//Check if CSV file exists if not create it
		if (!outputFile.exists()) {

			try {
				writer = new CSVWriter(new FileWriter(outputFile, true),
					',', '"', '\\', "\n");
				writer.writeNext(
					new String[]{"TIME", "DISTANCE", "MAC", "RSSI"},
					false);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		///Write in the CSV
		writer = new CSVWriter(new FileWriter(outputFile, true),
			',', '"', '\\', "\n");
		writer.writeNext(
			new String[]{time, String.valueOf(referenceDistance), deviceMAC,String.valueOf(RSSI)},
			true);
		writer.close();


	}



}
