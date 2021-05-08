package it.unipi.dii.ntc;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;

import java.util.HashMap;

public class Add_devices extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName() ;
    private static final int REQUEST_ENABLE_BT = 1234;
    private BroadcastBLTReceiver BLTReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_devices);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            Log.e(TAG, "startMonitoring: Device doesn't support bluetooth.");
        else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            if( !bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.startDiscovery();
            }
            IntentFilter BLTIntFilter  = new IntentFilter();
            BLTIntFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            BLTIntFilter.addAction(BluetoothDevice.ACTION_FOUND);
            TableLayout tView = findViewById(R.id.BLTDevices);
            BLTReceiver = new BroadcastBLTReceiver(tView);
            getApplicationContext().registerReceiver(BLTReceiver, BLTIntFilter);
        }

    }

    public void updateTable(HashMap<String,String> BLTDiscovered){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(BLTReceiver);
    }
}