package com.example.sushanth.identifyuser;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by sushanth on 5/1/2017.
 */

public class SetContextFirebase extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}