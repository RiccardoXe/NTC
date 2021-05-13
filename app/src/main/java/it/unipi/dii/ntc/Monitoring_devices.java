package it.unipi.dii.ntc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Monitoring_devices extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring_devices);
	}

	public void goMainActivity(View vApp){
		Intent myIntent = new Intent(Monitoring_devices.this, MainActivity.class);
		Monitoring_devices.this.startActivity(myIntent);
	}

	//on destroy close monitoring (the monitoring should be performed in background)
}