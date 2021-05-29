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

import it.unipi.dii.iodetectionlib.IODetectionListener;
import it.unipi.dii.iodetectionlib.IODetectionResult;
import it.unipi.dii.iodetectionlib.IODetector;
import it.unipi.dii.iodetectionlib.IOStatus;


public class BroadcastRSSIReceiver extends BroadcastReceiver
{

	private static final String TAG = MainActivity.class.getName();
	private final IODetector ioDetector;
	private RSSIScan_Service rssiService;
	private String prefFileName = "StoredDevices";
	private String calibrationFileName = "CalibrationFile.csv";
	private Double outdoorThreshold;
	private Double indoorThreshold;
	private Context context;
	private IOListener listener;

	public BroadcastRSSIReceiver(RSSIScan_Service rssiS, IODetector ioDetection, Context context){
		rssiService = rssiS;
		this.ioDetector = ioDetection;
		this.context = context;
		try {
			this.outdoorThreshold = getMeanRSSI(false, context);
			this.indoorThreshold = getMeanRSSI(true, context);

		} catch (IOException e) {
			e.printStackTrace();
		}

		listener = new IOListener();
		this.ioDetector.registerIODetectionListener(listener, 1000);
		Log.i(TAG, "BroadcastRSSIReceiver: outdoorThreshold " + outdoorThreshold);
		Log.i(TAG, "BroadcastRSSIReceiver: indoorThreshold " + indoorThreshold);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//double f;
		String action = intent.getAction();
		SharedPreferences sharedPref = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
		//Device Discovery phase
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Discovery has found a device. Get the BluetoothDevice
			// object and its info from the Intent.
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String deviceHardwareAddress = device.getAddress(); // MAC address
			int RSSIValue =  intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

			IOStatus ioStatus = listener.getStatus();
			//IOStatus ioStatus = ioDetector.detect().getIOStatus();
			//check if we are indoor or outdoor
			Double meanRSSI;
			if( ioStatus == IOStatus.INDOOR){ // TODO: use ioDetection.registerIODetectionListener
				Log.i(TAG, "onReceive: INDOOR");
				meanRSSI = indoorThreshold;

			}else{
				Log.i(TAG, "onReceive: OUTDOOR");
				// TODO:meanRSSi
				//meanRSSI = outdoorThreshold;
				meanRSSI = indoorThreshold;

			}

			// TODO: Put a dynamic Threshold instead of -45 and check if stored device
			if (RSSIValue > meanRSSI && (sharedPref.contains(" "+deviceHardwareAddress) == false)) {
				Log.i(TAG, "onReceive: Trovato dispositivo " + RSSIValue);
				Log.i(TAG, "onReceive: Trovato dispositivo " + device.getName());
				Log.i(TAG, "onReceive: Trovato dispositivo " + deviceHardwareAddress);

		//		f = Math.pow(10, (double) (-meanRSSI - RSSIValue)/(10*5));
		//		Log.i(TAG, "Distance Estimation"+ f);
				String notifyToSend = "";
				if( device.getName() != null){
					notifyToSend += device.getName() + " ";
				}
				notifyToSend += "Unknown device too close";
				rssiService.createNotification(notifyToSend);
			}
		}
		else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			Log.v("RSSI:","Discovery start back soon");
		}
	}

	public Double getMeanRSSI(Boolean indoor, Context c) throws IOException
	{
		CSVReader reader;
		File inputfile = new File(c.getFilesDir() + File.separator + calibrationFileName);

		//Check if CSV file exists if not create it
		if (!inputfile.exists()) {

			return -69.0;	// This value is considered ff calibration has not been performed
		}

		///Retrieve mean RSSI value from the CSV
		reader = new CSVReader(new FileReader(inputfile));
		List<String[]> csvBody = reader.readAll();

		int i=(indoor)?1:0;
		Double RSSIMean = Double.parseDouble(csvBody.get(i)[3]);
		return RSSIMean;

	}

	private class IOListener implements IODetectionListener
	{
		private IOStatus status;

		public IOStatus getStatus()
		{
			return status;
		}

		@Override
		public void onDetectionChange(IODetectionResult ioDetectionResult)
		{
			status = ioDetectionResult.getIOStatus();
		}
	}

}
