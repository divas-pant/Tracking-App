package com.example.anna.activityapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    final long DETECTION_INTERVAL_IN_MILLISECONDS = 2 * 30 * 1000; // 1 minute
    private ActivityRecognitionClient activityRecognitionClient;
    Context mcontext;

    @Override
    public void onReceive(Context context, Intent intent) {
        activityRecognitionClient = ActivityRecognition.getClient(context);
        mcontext = context;


        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            requestActivityUpdatesButtonHandler();

        }


    }
    public void requestActivityUpdatesButtonHandler() {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getStartActivityUpdates());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //if this process failed then start the deprecated one..

            }
        });
    }
    public PendingIntent getStartActivityUpdates() {
        Intent intent = new Intent(mcontext, MyActivityTrackReciver.class);
        intent.setAction(MyActivityTrackReciver.ACTION_ACTIVITY_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(mcontext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
