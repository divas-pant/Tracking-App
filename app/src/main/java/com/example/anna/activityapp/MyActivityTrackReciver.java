package com.example.anna.activityapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;

import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.Api;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER;
import static com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT;


/*......
 Background reciver to detect the state....and pass through event bus to activity during forground and insert data during background
 */
public class MyActivityTrackReciver extends BroadcastReceiver {
    String SQLiteQuery;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper SQLITEHELPER;
    Cursor cursor;
    boolean location = false;
    long time = 0;
    int level;
    String loc_event = "";
    private  final long UPDATE_INTERVAL = 5000;
    private  final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private  final long MAX_WAIT_TIME = UPDATE_INTERVAL * 2;
    static final String ACTION_ACTIVITY_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".ACTIVITY_PROCESS_UPDATES";
    private Context mContext;
    String row;
    Geocoder geocoder;
    List<Address> addresses = null;
    String errorMessage = "";
    String add = "";
    String userstate = "";
    int confidence_score = 0;
    String confidence = "";
    String battery_stat = "";
    String check_state;
    @SuppressLint("NewApi")
    String latt = "";
    String lang = "";
    String accuracy = "";
    String provider = "";
    private ArrayList<String> memInfo_array = new ArrayList<>();
    private int available;
    private int cached;
    private int buffers;
    private int free;
    private int total;
    private int used;
    String activity_trans;
    String transitionType;
    private LocationRequest mLocationRequest;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (intent != null) {
            geocoder = new Geocoder(mContext, Locale.getDefault());
            final String action = intent.getAction();
            SQLITEHELPER = new SQLiteHelper(context);
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    activity_trans = toActivityString(event.getActivityType());
                    transitionType = toTransitionType(event.getTransitionType());
                }
            }
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                row = handleDetectedActivities(result.getProbableActivities());
                StringTokenizer tokens = new StringTokenizer(row, ",");
                if (!tokens.hasMoreElements()) {

                } else {
                    String time = tokens.nextToken();
                }

                if (!tokens.hasMoreElements()) {

                } else {
                    userstate = tokens.nextToken();

                }

                if (!tokens.hasMoreElements()) {

                } else {
                    confidence = tokens.nextToken();
                    confidence_score = Integer.parseInt(confidence);

                }
                check_state = checkState();
                if (!isNullOrEmpty(activity_trans)) {
                    callActivityTrans();

                } else {
                    CallActivityRecog();

                }
            } else {
                String check_state = checkState();
                EventBus.getDefault().post(new OnReceiverEvent(check_state));


            }


        }


    }



    private void CallActivityRecog() {
        if (!isNullOrEmpty(row) && !check_state.equals(userstate) && confidence_score > 10) {
            try {
                DetectCorrectActivity(userstate);
                EventBus.getDefault().post(new OnReceiverEvent(userstate));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EventBus.getDefault().post(new OnReceiverEvent(check_state));

        }


    }

    private void callActivityTrans() {
        if (!isNullOrEmpty(row) && !check_state.equals(userstate) && confidence_score > 90
                && userstate.equals(activity_trans)) {
            try {
                DetectCorrectActivity(userstate);
                EventBus.getDefault().post(new OnReceiverEvent(userstate));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!isNullOrEmpty(row) && confidence_score > 90 && !check_state.equals(userstate)
                && !activity_trans.equals(userstate)) {
            try {
                DetectCorrectActivity(activity_trans);
                EventBus.getDefault().post(new OnReceiverEvent(activity_trans));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EventBus.getDefault().post(new OnReceiverEvent(check_state));

        }


    }


    public boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private String handleDetectedActivities(List<DetectedActivity> probableActivities) {
        int max = 0;
        String act = "";
        for (DetectedActivity activity : probableActivities) {
            if (max < activity.getConfidence()) {
                max = activity.getConfidence();
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                time = System.currentTimeMillis();//format.format(new Date());
                switch (activity.getType()) {
                    case DetectedActivity.IN_VEHICLE: {
                        act = "IN_VEHICLE";
                        break;
                    }
                    case DetectedActivity.WALKING: {
                        act = "ON_FOOT";
                        break;
                    }

                    case DetectedActivity.ON_FOOT: {
                        act = "ON_FOOT";
                        break;
                    }
                    case DetectedActivity.STILL: {
                        act = "STILL";
                        break;
                    }
                }

            }

        }


        return " " + time + "," + act + "," + max;

    }

    public void DetectCorrectActivity(final String row) {
        String decision = "";
        try {
            String state = checkState();
            int count = checkPreviousState(row, state);
            if (count > 0) {
                location = true;
                createLocationRequest();
                requestLocationUpdates();
            } else {
                location=false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InsertStateDataIntoDB(row, System.currentTimeMillis(), "");
                Log.e("TAG", "jetat");

            }
        }, 18000);


    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }


    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {

            // LocationRequestHelper.setRequesting(this, true);
//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mGoogleApiClient, mLocationRequest, getPendingIntent());
            LocationServices.getFusedLocationProviderClient(mContext)
                    .requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            // LocationRequestHelper.setRequesting(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {

        // LocationRequestHelper.setRequesting(this, false);
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
//                getPendingIntent());
        LocationServices.getFusedLocationProviderClient(mContext).removeLocationUpdates(getPendingIntent());

    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Subscribe
    public void onLocationRecieved(OnLocationReciverEvent event) {
        loc_event = event.getLocation();



    }

    public void InsertStateDataIntoDB(String state, final long time, String message) {
        String checkfistRow = "";
        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
        cursor = SQLITEDATABASE.rawQuery("SELECT id,name from demoTable_tracking order by id DESC limit 1 ", null);
        StateResultHelper stateResultHelper = new StateResultHelper(
                mContext, state);
        stateResultHelper.saveActivityStateResults();
        if (cursor.moveToFirst()) {
            do {
                checkfistRow = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Name));
            } while (cursor.moveToNext());
        }
        try {
            if (isNullOrEmpty(checkfistRow)) {
                SubmitData2SQLiteDB(state, time, message);
                stateResultHelper.showNotification();

            } else if (state.equals(checkfistRow)) {
                ///do nothing..
            } else {
                SubmitData2SQLiteDB(state, time, message);
                stateResultHelper.showNotification();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursor.close();
    }

    public int checkPreviousState(String current, String previous) {
        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
        cursor = SQLITEDATABASE.rawQuery("select * from tblmovingStates where cstate=? and pstate=?",
                new String[]{current, previous});
        int count = cursor.getCount();

        return cursor.getCount();

    }

    public String checkState() {
        String state = "";
        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
        cursor = SQLITEDATABASE.rawQuery("SELECT id,name from demoTable_tracking order by id DESC limit 1 ", null);
        if (cursor.moveToFirst()) {
            do {
                state = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Name));
            } while (cursor.moveToNext());
        }


        return state;

    }

    public void SubmitData2SQLiteDB(String Name, long time, String msg) {
        battery_stat = String.valueOf(getBatteryPercentage(mContext) + "%");
        if (location == true) {
            try {
                String loc= LocationResultHelper.getSavedLocationResult(mContext);
                Log.e("locc",loc);
                SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
                if (!isNullOrEmpty(loc_event)) {
                    StringTokenizer tokenss = new StringTokenizer(loc_event, ",");
                    if (!tokenss.hasMoreElements()) {

                    } else {
                        latt = tokenss.nextToken();
                    }
                    if (!tokenss.hasMoreElements()) {

                    } else {
                        lang = tokenss.nextToken();
                    }

                    if (!tokenss.hasMoreElements()) {

                    } else {
                        accuracy = tokenss.nextToken();
                    }
                    if (!tokenss.hasMoreElements()) {

                    } else {
                        provider = tokenss.nextToken();
                    }
                    fetchAddress();
                } else if(!isNullOrEmpty(LocationResultHelper.getSavedLocationResult(mContext))){
                    String locationData = LocationResultHelper.getSavedLocationResult(mContext);
                    StringTokenizer tokens = new StringTokenizer(locationData, ",");
                    if (!tokens.hasMoreElements()) {

                    }else {
                        latt = tokens.nextToken();
                    }
                    if (!tokens.hasMoreElements()) {

                    }else {
                        lang = tokens.nextToken();
                    }

                    if (!tokens.hasMoreElements()) {

                    }else {
                        accuracy = tokens.nextToken();
                    }
                    if (!tokens.hasMoreElements()) {

                    }else {
                        provider = tokens.nextToken();
                    }
                    fetchAddress();

                }
                String Time = String.valueOf(createDate(time));
                String ram_in = readMemInfo();
                SQLiteQuery = "INSERT INTO demoTable_tracking (name,detection_time,lat,long,battery,accuracy,provider,address,ram) " +
                        "VALUES('" + Name + "','" + Time + "','"
                        + latt + "','"
                        + lang + "','"
                        + battery_stat + "','"
                        + accuracy + "','"
                        + provider + "','"
                        + add + "','"
                        + ram_in + "')";
                SQLITEDATABASE.execSQL(SQLiteQuery);


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                removeLocationUpdates();
                location = false;
                LocationResultHelper.clearData(mContext);

            }

        } else if (location == false) {
            String Time = String.valueOf(createDate(time));
            String ram_in = readMemInfo();
            try {
                SQLiteQuery = "INSERT INTO demoTable_tracking (name,detection_time,lat,long,battery,accuracy,provider,address,ram) " +
                        "VALUES('" + Name + "', '"
                        + Time + "','"
                        + "-" + "','"
                        + "-" + "','"
                        + battery_stat + "','"
                        + "-" + "','"
                        + "-" + "','"
                        + "-" + "','"
                        + ram_in + "')";
                SQLITEDATABASE.execSQL(SQLiteQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }


    public CharSequence createDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(d);
    }



    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public long checkStateTime() throws ParseException {
        String time = "";
        Date date = null;
        long a = 0;
        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
        cursor = SQLITEDATABASE.rawQuery("SELECT id,detection_time from demoTable_tracking order by id DESC limit 1 ", null);
        if (cursor.moveToFirst()) {
            do {
                time = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_DETECTIONTIME));
            } while (cursor.moveToNext());
        }
        if (!isNullOrEmpty(time)) {
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            date = formatter.parse(time);
            // System.out.println("Today is " + date.getTime());
            a = date.getTime();
        }


        return a;

    }


    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }


    private String getHealthString(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int health = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) : -1;


        String healthString = "Unknown";

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthString = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthString = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthString = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthString = "Over Heat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthString = "Failure";
                break;
        }

        return healthString;
    }

    public String readMemInfo() {
        String total_ram = "";
        String avail_ram = "";
        String free_ram = "";
        DecimalFormat twoDecimalForm = new DecimalFormat("#.#");
        double m;
        double g;
        double t;
        BufferedReader br = null;
        try {
            String fpath = "/proc/meminfo";
            try {
                br = new BufferedReader(new FileReader(fpath));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            String line;
            try {
                assert br != null;
                while ((line = br.readLine()) != null) {
//                    Log.d(TAG, line);
                    memInfo_array.add(line);
                }
            } catch (IOException eee) {
            }
        } catch (Exception masterTreta) {
        }

        for (int i = 0; i < memInfo_array.size(); i++) {
            if (Pattern.matches("MemTotal:.*", memInfo_array.get(i))) {
                total = filterText(memInfo_array.get(i));
                m = total / 1024.0;
                g = total / 1048576.0;
                t = total / 1073741824.0;
                if (t > 1) {
                    total_ram = twoDecimalForm.format(t).concat("TB");
                } else if (g > 1) {
                    total_ram = twoDecimalForm.format(g).concat("GB");
                } else if (m > 1) {
                    total_ram = twoDecimalForm.format(m).concat("MB");
                } else {
                    total_ram = twoDecimalForm.format(total).concat("KB");
                }
            }

            if (Pattern.matches("MemFree:.*", memInfo_array.get(i))) {
                String hrSize;
                free = filterText(memInfo_array.get(i));

            }

            if (Pattern.matches("Buffers:.*", memInfo_array.get(i))) {
                buffers = filterText(memInfo_array.get(i));
            }

            if (Pattern.matches("Cached:.*", memInfo_array.get(i))) {
                cached = filterText(memInfo_array.get(i));
            }
        }

        available = free + cached + buffers;
        m = available / 1024.0;
        g = available / 1048576.0;
        t = available / 1073741824.0;

        if (t > 1) {
            avail_ram = twoDecimalForm.format(t).concat("TB");
        } else if (g > 1) {
            avail_ram = twoDecimalForm.format(g).concat("GB");
        } else if (m > 1) {
            avail_ram = twoDecimalForm.format(m).concat("MB");
        } else {
            avail_ram = twoDecimalForm.format(available).concat("KB");
        }

        used = total - (free + cached + buffers);
        m = used / 1024.0;
        g = used / 1048576.0;
        t = used / 1073741824.0;
        if (t > 1) {
            free_ram = twoDecimalForm.format(t).concat("TB");
        } else if (g > 1) {
            free_ram = twoDecimalForm.format(g).concat("GB");
        } else if (m > 1) {
            free_ram = twoDecimalForm.format(m).concat("MB");
        } else {
            free_ram = twoDecimalForm.format(used).concat("KB");
        }
        String info_ram = "Total-" + total_ram + " " + "Free-" + avail_ram + " " + "Used-" + free_ram;
        return info_ram;
    }

    public int filterText(String str) {
        String str2 = str.replaceAll("\\s+", " ");
        String str3[] = str2.split(" ");
        return Integer.parseInt(str3[1]);
    }

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.ON_FOOT:
                return "WALKING";
            case DetectedActivity.WALKING:
                return "ON_FOOT";
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            default:
                return "UNKNOWN";
        }
    }


    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    public void fetchAddress(){
        try {
            addresses = geocoder.getFromLocation(
                    Double.parseDouble(latt),
                    Double.parseDouble(lang),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = mContext.getString(R.string.service_not_available);
            add=errorMessage;

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = mContext.getString(R.string.invalid_lat_long_used);
            add=errorMessage;


        }
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = mContext.getString(R.string.no_address_found);
                add=errorMessage;


            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            add= TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
        }
    }




}
