package com.example.anna.activityapp;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.anna.activityapp.MyActivityTrackReciver;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ArrayList transitions = new ArrayList();

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Intent intent = new Intent(getApplicationContext(), MyActivityTrackReciver.class);
        intent.setAction(MyActivityTrackReciver.ACTION_ACTIVITY_PROCESS_UPDATES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.getClient(this).requestActivityUpdates(60000, pendingIntent);
        ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request, pendingIntent);
    }


}
