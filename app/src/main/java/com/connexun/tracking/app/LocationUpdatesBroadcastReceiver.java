/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connexun.tracking.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;


import com.google.android.gms.location.LocationResult;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

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
    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    public static final String ACTION = "com.example.anna.activityapp.LocationUpdatesBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("onRecive", "onRecive");
        Log.e("onRecive", String.valueOf(intent));
        if (intent != null) {
            String loc="";
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Log.e("resultnotnull", "resultnotnull");
                    List<Location> locations = result.getLocations();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(
                            context, locations);
                    // Save the location data to SharedPreferences.
                    loc=locations.get(0).getLatitude() +","+locations.get(0).getLongitude()+
                            ","+locations.get(0).getAccuracy()+","+locations.get(0).getProvider();
                    EventBus.getDefault().post(new OnLocationReciverEvent(loc));
                    System.out.println("Loc check"+PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LOCATION_UPDATES_RESULT,""));
                    //locationResultHelper.showNotification(loc);
                    locationResultHelper.saveResults(loc);


                }
            }
        }
    }
}


/*
this is online loc - 28.4468637 --77.3092656  and this one offline(network)-   28.4468628----77.3092747   returns me - 5.564843320174258E-4 */
