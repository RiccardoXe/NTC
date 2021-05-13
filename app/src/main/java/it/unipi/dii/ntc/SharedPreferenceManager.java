package it.unipi.dii.ntc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceManager
{
	/**
	 * This method stores the key-value pair in the Shared Preference File
	 * @param key - MAC address of a Device
	 * @param value - Name of the device (if not presente NO_DEVICE_NAME by default)
	 */
	public void addElementToShared(String key, String value, Context c){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
