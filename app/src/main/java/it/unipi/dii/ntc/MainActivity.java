package it.unipi.dii.ntc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{

	private static final int ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE = 100;
	private static final int ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE = 101;
	private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 102;

	private static final String TAG = MainActivity.class.getName();
	private static final int REQUEST_ENABLE_BT = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

    public void showStoredDevices(View vApp){
        Intent myIntent = new Intent(MainActivity.this, Show_devices.class);
        MainActivity.this.startActivity(myIntent);
    }


	public void startBLTScan(View vApp)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			checkPermission(Manifest.permission.ACTIVITY_RECOGNITION, ACTIVITY_RECOGNITION_PERMISSION_CODE);
		checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
			ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
				ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE);

		Intent myIntent = new Intent(MainActivity.this, Add_devices.class);
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
}