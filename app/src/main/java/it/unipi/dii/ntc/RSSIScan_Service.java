package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TableLayout;

import androidx.core.app.NotificationCompat;

public class RSSIScan_Service extends Service
{
	private static final String TAG = "RSSI-SERVICE" ;
	private BroadcastRSSIReceiver RSSIReceiver;
	private static final long PERIODIC_DELAY = 20000;

	public RSSIScan_Service()
	{
	}


	/**
	 * The background service should start a bluetoot scanning
	 * to check RSSI values associated with devices
	 */
	@Override
	public void onCreate(){
		Log.i("BACKGROUND", "Scanning Started");
		createNotification("RSSI scanning started");
		/* Register BLTIntentFilter to Find Bluetooth devices */
		//BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//if (!bluetoothAdapter.isDiscovering()) {
		//	Log.i("AAA", "onCreate: isDiscovering " + bluetoothAdapter.startDiscovery());
		//}

		IntentFilter BLTIntFilter = new IntentFilter();
		BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
		BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		RSSIReceiver = new BroadcastRSSIReceiver(this);
		getApplicationContext().registerReceiver(RSSIReceiver, BLTIntFilter);

		Handler periodicHandler = new Handler(Looper.getMainLooper());
		Runnable periodicRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				periodicHandler.postDelayed(this, PERIODIC_DELAY);
				if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
					Log.w(TAG, "Bluetooth is not enabled.");
				else if (!BluetoothAdapter.getDefaultAdapter().isDiscovering())
					if (!BluetoothAdapter.getDefaultAdapter().startDiscovery())
						Log.e(TAG, "Failed to start BT discovery.");
			}
		};
		periodicHandler.post(periodicRunnable);

	}
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public void createNotification(String NotifyToSend)
	{
		String notificationChannelId = "RSSI-BLT NOTIFICATION CHANNEL";

		// depending on the Android API that we're dealing with we will have
		// to use a specific method to create the notification
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
			NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, "BLT - RSSI MONITORING", NotificationManager.IMPORTANCE_HIGH);
			notificationChannel.setLightColor(Color.RED);
			notificationChannel.setDescription("BLT - RSSI Monitoring");
			notificationChannel.enableLights(true);
			notificationChannel.enableVibration(true);
			notificationManager.createNotificationChannel(notificationChannel);
		}

		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			builder = new Notification.Builder(this, notificationChannelId);
		else
			builder = new Notification.Builder(this);

		builder.setContentTitle("NTC-Not Too Close");
		builder.setContentText(NotifyToSend);
		builder.setContentIntent(pendingIntent);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setPriority(Notification.PRIORITY_HIGH);

		startForeground(1, builder.build());
	}

	@Override
	public void onDestroy()
	{
		Log.i("RSSI_SERVICE", "DESTROYED SERVICE");
		super.onDestroy();
		getApplicationContext().unregisterReceiver(RSSIReceiver);
	}
}