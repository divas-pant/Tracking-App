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

package com.example.anna.activityapp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Class to process location results.
 */
class LocationResultHelper {

    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final static String KEY_TIME = "time";
    final static String KEY_Noti = "noti";

    final private static String PRIMARY_CHANNEL = "default";


    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;

    LocationResultHelper(Context context, List<Location> locations) {

        mContext = context;
        mLocations = (List<Location>) locations;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                    context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }

    }





    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     * @param loc
     */
    void saveResults(String loc) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, loc)
                .apply();
    }

    void saveTime(String time){
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putString(KEY_TIME, time)
                .apply();
    }

    static String getKeyTime(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TIME,"");
    }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    static void clearData(Context context){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().remove(KEY_LOCATION_UPDATES_RESULT)
                .clear()
                .apply();
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * Displays a notification with the location results.
     * @param loc
     */
    public void showNotification(String loc) {
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);
        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);
        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long time = new Date().getTime();
            String tmpStr = String.valueOf(time);
            String last4Str = tmpStr.substring(tmpStr.length() - 5);
            int notificationId = Integer.valueOf(last4Str);

            notificationBuilder = new Notification.Builder(mContext,
                    PRIMARY_CHANNEL)
                    .setContentTitle("Location")
                    .setContentText(loc)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPendingIntent);
            getNotificationManager().notify(notificationId, notificationBuilder.build());
        } else {
            long time = new Date().getTime();
            String tmpStr = String.valueOf(time);
            String last4Str = tmpStr.substring(tmpStr.length() - 5);
            int notificationId = Integer.valueOf(last4Str);
//            int notificationId = 124;

            /*
             * Getting intent for start on click the notification
             * */
            //  notificationIntent = new Intent(mContext, MainActivity.class);
            notificationIntent.putExtra("filter", "/home/notifications");
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            /*
             * Creating intent with unique id
             * */
            PendingIntent intent =
                    PendingIntent.getActivity(mContext, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    mContext);
            Notification notification = builder.setContentIntent(intent)
                    //.setSmallIcon(Icon.createWithBitmap(bitmap))
                    .setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker(latitude + "-" + longitude + "-Speed:" + speed)
                    //.setWhen(when)
                    .setAutoCancel(true).setContentTitle("Get Location")
                    //.setGroup(GROUP_KEY_EMAILS)
//                .setChannelId(CHANNEL_ID)
//                .setContentText(latitude + "-" + longitude + "" + "-Speed:" + speed+"\n"+"Time:"+(String) android.text.format.DateFormat.format(delegate, Calendar.getInstance().getTime())).build();
                    .setContentTitle("Location")
                    .setContentText(loc).build();
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);
            notificationManager.notify(notificationId, notification);
        }
    }



    public static CharSequence createDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(d);
    }



}
