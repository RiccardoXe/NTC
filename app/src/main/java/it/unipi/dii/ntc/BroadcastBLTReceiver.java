package it.unipi.dii.ntc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Set;

public class BroadcastBLTReceiver extends BroadcastReceiver {

    private HashMap<String,String> discoveredBLT= new HashMap<>();
    private TableLayout BLTTable;
    private static final String TAG = MainActivity.class.getName();

    public BroadcastBLTReceiver(TableLayout BLTTable) {
        this.BLTTable = BLTTable;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {

            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            Log.i(TAG, "onReceive: Trovato dispositivo " + deviceName);
            Log.i(TAG, "onReceive: Trovato dispositivo " + deviceHardwareAddress);
            discoveredBLT.put(deviceHardwareAddress, deviceName);
        }
        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            //Update grafic interface

            Set<String> keys = discoveredBLT.keySet();
            for (String key: keys){
                TableRow row = new TableRow(context);
                TextView tx = new TextView(context);
                String tmp = discoveredBLT.get(key);
                if (tmp.equals(null)){
                    tx.setText(key);
                }else{
                    tx.setText(tmp);
                }
                row.addView(tx);
                BLTTable.addView(row);
            }
        }
    }
}
