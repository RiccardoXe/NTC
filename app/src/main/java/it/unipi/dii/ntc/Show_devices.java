package it.unipi.dii.ntc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Show_devices extends AppCompatActivity {

    public static final String DATABASE_NAME = "NTC_Database";
    public static final String USER_REGISTERED_NAME = "UsersRegister";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_devices);
    }
}