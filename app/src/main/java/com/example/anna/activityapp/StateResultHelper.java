package com.example.anna.activityapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import java.util.Date;

/*.........................

For Storing state and show notification

 */


public class StateResultHelper {
    private Context mContext;
    private String mstate;
    private NotificationManager mNotificationManager;
    final private static String PRIMARY_CHANNEL = "default";
    final static String KEY_ACTIVITY_STATE_RESULT = "activity-state-result";

    StateResultHelper(Context context, String state) {
        mContext = context;
        mstate = state;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                    context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }

    }

    void saveActivityStateResults() {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(KEY_ACTIVITY_STATE_RESULT, mstate).apply();
    }
     static String getSavedActivityState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_ACTIVITY_STATE_RESULT, "");
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    void showNotification() {
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
                    .setContentTitle("ActivityState")
                    .setContentText(mstate)
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
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true).setContentTitle("Get Location")
                    .setContentTitle("Activity State")
                    .setContentText(mstate).build();
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);
            notificationManager.notify(notificationId, notification);
        }


    }

}
