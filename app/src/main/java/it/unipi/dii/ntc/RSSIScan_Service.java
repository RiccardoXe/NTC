package it.unipi.dii.ntc;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TableLayout;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import it.unipi.dii.iodetectionlib.IODetector;

public class RSSIScan_Service extends Service
{
	private static final String TAG = RSSIScan_Service.class.getName();
	private BroadcastRSSIReceiver RSSIReceiver;
	private BroadcastRSSILogger RSSILogger;
	private static final long PERIODIC_DELAY = 20000;
	private PowerManager.WakeLock mWakeLock;
	private static final long WAKELOCK_TIMEOUT = 2 * 60 * 1000;
	private Handler periodicHandler;
	private Runnable periodicRunnable;
	private IODetector ioDetector;
	private final IBinder binder = new ServiceBinder();



	/**
	 * The background service should start a bluetoot scanning
	 * to check RSSI values associated with devices
	 */
	@Override
	public void onCreate(){
		Log.i("BACKGROUND", "Scanning Started");
		createNotification("RSSI scanning started");


		//IntentFilter BLTIntFilter = new IntentFilter();
		//BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
		//BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//ioDetection = new IODetection(getApplicationContext());
		//RSSIReceiver = new BroadcastRSSIReceiver(this, ioDetection);
		//getApplicationContext().registerReceiver(RSSIReceiver, BLTIntFilter);

		//Once create periodically perform a startDiscovery operation
		periodicHandler = new Handler(Looper.getMainLooper());
		periodicRunnable = new Runnable()
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
				if (!mWakeLock.isHeld())
					mWakeLock.acquire(WAKELOCK_TIMEOUT);
			}
		};
		//periodicHandler.post(periodicRunnable);
		//PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		//mWakeLock.acquire(WAKELOCK_TIMEOUT);

	}

	public void startPeriodicScan(){
		periodicHandler.post(periodicRunnable);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire(WAKELOCK_TIMEOUT);
	}

	public void stopPeriodicScan(){
		periodicHandler.removeCallbacks(periodicRunnable);
	}

	/**
	 *  startRSSILogging register the RSSILogger
	 *  	Write .csv data about the recorded RSSI values
	 * @param distance_to_monitor
	 */
	public void startRSSILogging(double distance_to_monitor){
		IntentFilter BLTIntFilter = new IntentFilter();
		BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
		BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		getApplicationContext().registerReceiver(RSSILogger, BLTIntFilter);
		RSSILogger = new BroadcastRSSILogger(this, distance_to_monitor);
		getApplicationContext().registerReceiver(RSSILogger, BLTIntFilter);
	}

	public void stopRSSILogging(){
		Log.i(TAG, "------------------ Unregister called ------------------------");
		getApplicationContext().unregisterReceiver(RSSILogger);
		RSSILogger = null;
	}

	/**
	 *  startRSSIMonitoring register the RSSIReciver
	 *  	Scan the devices to find if some of if is near
	 */
	public void startRSSIMonitoring(){
		Log.i(TAG, "RSSI Call Register");
		IntentFilter BLTIntFilter = new IntentFilter();
		BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
		BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		try {
			ioDetector = new IODetector(getApplicationContext());
		} catch (IOException e) {
			return; // TODO: handle
		}
		RSSIReceiver = new BroadcastRSSIReceiver(this, ioDetector);
		getApplicationContext().registerReceiver(RSSIReceiver, BLTIntFilter);
	}

	public void stopRSSIMonitoring(){
		Log.i(TAG, "------------------ Unregister called ------------------------");
		ioDetector.close();
		getApplicationContext().unregisterReceiver(RSSIReceiver);
		RSSIReceiver = null;
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

		if(RSSIReceiver != null)
			getApplicationContext().unregisterReceiver(RSSIReceiver);
		if(RSSILogger != null)
			getApplicationContext().unregisterReceiver(RSSILogger);
		if (periodicHandler != null && periodicRunnable != null)
			periodicHandler.removeCallbacks(periodicRunnable);
		if (mWakeLock != null && mWakeLock.isHeld())
			mWakeLock.release();
		if(ioDetector != null)
			ioDetector.close();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	public class ServiceBinder extends Binder
	{
		RSSIScan_Service getService()
		{
			return RSSIScan_Service.this;
		}
	}
}