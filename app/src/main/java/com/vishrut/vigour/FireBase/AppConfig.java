package com.vishrut.vigour.FireBase;



import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;



public class AppConfig extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
    }
}
