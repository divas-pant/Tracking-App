package com.example.anna.activityapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000; // 1 minute
    private ActivityRecognitionClient activityRecognitionClient;
    Context mcontext;
    ArrayList transitions;
    @Override
    public void onReceive(Context context, Intent intent) {
        mcontext = context;
        activityRecognitionClient = ActivityRecognition.getClient(context);
        transitions = new ArrayList();

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            callActivity();

        }


    }


    public void callActivity()
    {
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Intent intent = new Intent(mcontext, MyActivityTrackReciver.class);
        intent.setAction(MyActivityTrackReciver.ACTION_ACTIVITY_PROCESS_UPDATES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mcontext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.getClient(mcontext).requestActivityUpdates(8000, pendingIntent);
        ActivityRecognition.getClient(mcontext).requestActivityTransitionUpdates(request, pendingIntent);
        Toast.makeText(mcontext,"Hello i am restart",Toast.LENGTH_LONG).show();

    }


}
