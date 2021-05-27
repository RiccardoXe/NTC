package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import it.unipi.dii.iodetectionlib.IODetector;
import it.unipi.dii.iodetectionlib.IOStatus;

public class CalibrationRSSIReceiver extends BroadcastReceiver
{

	private static final String TAG = MainActivity.class.getName();
	private String calibrationTargetKey;
	private RSSIScan_Service rssiService;
	private String calibrationFileName = "CalibrationFile.csv";
	private Boolean indoor;


	public CalibrationRSSIReceiver(RSSIScan_Service rssiS, String calibrationTargetKey, Boolean indoor){
		rssiService = rssiS;
		this.calibrationTargetKey = calibrationTargetKey;
		this.indoor = indoor;
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.i(TAG, "onReceive CALIBRATION: Trovato qualcosa...action: " + action);

		Long tsLong = System.currentTimeMillis()/1000;
		String timestampCurrent = tsLong.toString();

		//Device Discovery phase
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Discovery has found a device. Get the BluetoothDevice
			// object and its info from the Intent.
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String deviceHardwareAddress = device.getAddress(); // MAC address
			int RSSIValue =  intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
			Log.i(TAG, "onReceive CALIBRATION: Trovato dispositivo " + RSSIValue);
			Log.i(TAG, "CALIBRATIONTARGETKEY:  " + calibrationTargetKey);
			Log.i(TAG, "deviceHardwareAddress:  " + deviceHardwareAddress);

			if (deviceHardwareAddress.equals(calibrationTargetKey)) {
				Log.i(TAG, "onReceive CALIBRATION: RRSI VALUE " + RSSIValue);
				Log.i(TAG, "onReceive CALIBRATION: device.getName() " + device.getName());
				Log.i(TAG, "onReceive CALIBRATION: deviceHardwareAddress " + deviceHardwareAddress);
				Log.i(TAG, "onReceive INDOOR: " + indoor.toString());

				try {
					updateCalibrationFile(timestampCurrent, deviceHardwareAddress, RSSIValue, indoor, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			Log.v("RSSI:","Discovery start back soon");
		}
	}
	public void updateCalibrationFile(String time, String deviceMAC, int RSSI, Boolean indoor, Context c) throws IOException
	{
		CSVWriter writer;
		CSVReader reader;

		File outputFile = new File(c.getFilesDir() + File.separator + calibrationFileName);

		//Check if CSV file exists if not create it
		if (!outputFile.exists()) {

			try {
				writer = new CSVWriter(new FileWriter(outputFile, false),
					',', '"', '\\', "\n");
				writer.writeNext(
					new String[]{"", "1.0", "0", "0", "OUTDOOR"},
					false);
				writer.writeNext(
					new String[]{"", "1.0", "0", "0", "INDOOR"},
					false);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		///Retrieve mean RSSI value from the CSV

		reader = new CSVReader(new FileReader(outputFile));
		List<String[]> csvBody = reader.readAll();

		int i=(indoor)?1:0;
		Double RSSIMean = Double.parseDouble(csvBody.get(i)[3]);
		int counter = Integer.parseInt(csvBody.get(i)[2]);
		String newRSSIMean = Double.toString(( counter*RSSIMean + RSSI )/ (counter + 1));

	//	new String[]{"TIME", "MAC", "COUNTER", "RSSI"}, csvBody.get(1)[0] = time;

		csvBody.get(i)[0] = time;
		csvBody.get(i)[1] = deviceMAC;
		csvBody.get(i)[2] = Integer.toString(counter + 1);
		csvBody.get(i)[3] = newRSSIMean;


		///Update the meanRSSI value
		writer = new CSVWriter(new FileWriter(outputFile, false),
			',', '"', '\\', "\n");
		writer.writeAll(csvBody);
	//	writer.flush(); ///???
		writer.close();



	}

}
