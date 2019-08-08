package com.example.anna.activityapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;


import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.StringTokenizer;


/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";
    double store_lat, store_lon;
    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("onRecive", "onRecive");
        if (intent != null) {
            String loc="";
            Log.e("IntentNotNull", "IntentNotNull");

            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Log.e("resultnotnull", "resultnotnull");
                    Location location= result.getLocations().get(0);
                    LocationResultHelper locationResultHelper = new LocationResultHelper(
                            context, location);
                    loc=location.getLatitude() +","+location.getLongitude()+
                            ","+location.getAccuracy()+","+location.getProvider();
                    StringTokenizer tokenss = new StringTokenizer(LocationResultHelper.getSavedLocationResult(context), ",");
                    if (!tokenss.hasMoreElements()) { } else {
                        store_lat= Double.parseDouble(tokenss.nextToken());
                    }if (!tokenss.hasMoreElements()) { } else {
                        store_lon= Double.parseDouble(tokenss.nextToken());
                    }

                    if(NetworkUtil.isNullOrEmpty(LocationResultHelper.getSavedLocationResult(context))
                    || NetworkUtil.isNullOrEmpty(String.valueOf(tokenss))){

                        if( Double.compare(store_lat, location.getLatitude()) == 0
                                && Double.compare(store_lon, location.getLongitude())==0){

                        }else {
                            EventBus.getDefault().post(new OnLocationReciverEvent(loc));
                            locationResultHelper.saveResults(loc);
                            System.out.println("Locaton--"+ loc);
                            locationResultHelper.showNotification(loc);
                        }

                    }


                }
          }
        }
    }
}
