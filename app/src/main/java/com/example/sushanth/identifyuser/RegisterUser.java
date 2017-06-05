package com.example.sushanth.identifyuser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RegisterUser extends AppCompatActivity implements Countryfragment.returnCountryAndState, SetLocation.returnLocation {

    public static String CountryReceived, StateReceived;
    public static Double xlatitude = 0.0;
    public static Double xlongitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        android.support.v4.app.FragmentManager fragments = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragments.beginTransaction();
        postUserFragment fragment = new postUserFragment();
        fragmentTransaction.replace(R.id.fragmentContainer1, fragment);
        fragmentTransaction.commit();

    }

    public void printCountryAndState(String country, String state) {
        CountryReceived = country;
        StateReceived = state;

    }

    public void printlocation(Double mlatitude, Double mlongitude) {
        xlatitude = mlatitude;
        xlongitude = mlongitude;

    }
}
