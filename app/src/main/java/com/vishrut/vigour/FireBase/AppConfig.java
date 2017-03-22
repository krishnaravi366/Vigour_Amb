package com.vishrut.vigour.FireBase;



import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.Firebase;


public class AppConfig extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
    }
}
