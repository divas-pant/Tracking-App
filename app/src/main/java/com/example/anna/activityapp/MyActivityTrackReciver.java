package com.example.anna.activityapp;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
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
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER;
import static com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT;
import static io.fabric.sdk.android.services.network.HttpRequest.post;


/*......
Background reciver to detect the state....and pass through event bus to activity during forground and insert data during background
*/
public class MyActivityTrackReciver extends BroadcastReceiver {
    String SQLiteQuery;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper SQLITEHELPER;
    Cursor cursor;
    long time = 0;
    private final long UPDATE_INTERVAL = 5000;
    private final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private final long MAX_WAIT_TIME = UPDATE_INTERVAL * 2;
    private Context mContext;
    Geocoder geocoder;
    static final String ACTION_ACTIVITY_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".ACTIVITY_PROCESS_UPDATES";
    List<Address> addresses = null;
    String errorMessage = "" ,userstate="", confidence="",battery_stat="",check_state="",latt="",lang="",accuracy="",provider=""
    ,activity_trans="",transitionType="",add = "",store_recent_activity_state="",activity_detected_time="",loc_event="",row;
    int confidence_score = 0;
    @SuppressLint("NewApi")
    private ArrayList<String> memInfo_array = new ArrayList<>();
    private int available,cached,buffers,free,total,used;
    private LocationRequest mLocationRequest;
    LocationManager locationManager;
    float range =0.5f; // kilo Meters
    private static final int TEN_MINUTES =4*60*1000;
    //private static final int TEN_MINUTES =5*1000;
    final static String KEY_Noti = "noti";
    double lat1, lon1,lattitude,lognitude, online_lat,online_long,location_network_lat,location_network_long;
    private NotificationManager mNotificationManager;
    final private static String PRIMARY_CHANNEL = "default";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Log.e("onRecive", String.valueOf(intent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                    context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }
        try{
            if (intent != null) {
                final String action = intent.getAction();
                if (ACTION_ACTIVITY_PROCESS_UPDATES.equals(action)) {
                    geocoder = new Geocoder(mContext, Locale.getDefault());
                    SQLITEHELPER = new SQLiteHelper(context);
                    if (ActivityTransitionResult.hasResult(intent)) {
                        ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                        for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                            activity_trans = toActivityString(event.getActivityType());
                            transitionType = toTransitionType(event.getTransitionType());
                            System.out.println(activity_trans);
                        }
                    }

                    if (ActivityRecognitionResult.hasResult(intent)) {
                        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                        row = handleDetectedActivities(result.getProbableActivities());
                        Log.e("onRecive", row);
                        StringTokenizer tokens = new StringTokenizer(row, ",");
                        if (!tokens.hasMoreElements()) {
                        } else {
                            activity_detected_time = tokens.nextToken();
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
                        //check_state = checkState();
                        if (!isNullOrEmpty(activity_trans)) {
                            callActivityTrans();

                        } else {
                            CallActivityRecog();
                            Log.e("onRecive", "recog");


                        }
                    } else {
                        String check_state = checkState();
                        EventBus.getDefault().post(new OnReceiverEvent(check_state));


                    }


                }

            }

        }catch (Exception e){
            Crashlytics.logException(e);
        }



    }



    private void CallActivityRecog() {
        String check_state = checkState();

        if (!isNullOrEmpty(row) && confidence_score > 70) {
            try {
                if(userstate.equals("STILL")){
                    Log.e("onRecive", "activityrecog"+userstate);
                    DetectCorrectActivity(userstate);
                    EventBus.getDefault().post(new OnReceiverEvent("STILL"));

                }else {
                    if( userstate.equals("IN_VEHICLE") || userstate.equals("ON_FOOT") && confidence_score > 95){
                     // Toast.makeText(mContext,"clearPref",Toast.LENGTH_LONG).show();
                       SharedPreferences prefs = mContext.getSharedPreferences("localdbstate", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.commit();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }else{
            System.out.println("2");
            EventBus.getDefault().post(new OnReceiverEvent( "STILL"));

        }

    }

    private void callActivityTrans() {
        if (!isNullOrEmpty(activity_trans)) {
            try {
                if(activity_trans.equals("STILL")){
                    Log.e("onRecive", "activityrecog"+activity_trans);
                    DetectCorrectActivity(activity_trans);
                    EventBus.getDefault().post(new OnReceiverEvent(activity_trans));
                }else{
                    Log.e("onRecive", "activitytransElse"+ activity_trans);
                    SharedPreferences prefs = mContext.getSharedPreferences("localdbstate", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.commit();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("4");
            EventBus.getDefault().post(new OnReceiverEvent(activity_trans));
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
                SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
                time = System.currentTimeMillis();//format.format(new Date());
                switch (activity.getType()) {
                    case DetectedActivity.STILL: {
                        act = "STILL";
                        break;
                    }
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




                }

            }

        }
        return " " + time + "," + act + "," + max;

    }


    public void DetectCorrectActivity(final String row) throws ParseException, InterruptedException {
        if(checkState().isEmpty()){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    callLocationFromNetwork();
               try {
                   InsertStateDataIntoDB(userstate, System.currentTimeMillis(), "");
                }catch (Exception e){
                 Crashlytics.logException(e);
                     }


                }
            }, 8000);
        }else {
          SharedPreferences prefs = mContext.getSharedPreferences("localdbstate", MODE_PRIVATE);
            store_recent_activity_state= prefs.getString("localstate", "");
            if(!row.equals(store_recent_activity_state)){
                long time= System.currentTimeMillis()+TEN_MINUTES;
                System.out.println("AddTime---"+time);
                SharedPreferences.Editor edit = mContext.getSharedPreferences("localdbstate", MODE_PRIVATE).edit();
                edit.putString("localstate",row);
                edit.putLong("localtime",time);
                edit.commit();

            }
            long storedtimeofprevtime=prefs.getLong("localtime", 0);
            if(System.currentTimeMillis() > storedtimeofprevtime) {
                Log.e("onRecive", "detect-if-timecheck"+storedtimeofprevtime);
                String[] values = checkPrevStateLoc().split("-");
                ArrayList list = new ArrayList(Arrays.asList(values));
                if(!list.isEmpty() && list.size()!=0) {
                    lattitude = Double.parseDouble(String.valueOf(list.get(0)));
                    lognitude = Double.parseDouble(String.valueOf(list.get(1)));
                    callLocation();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkdistance(lattitude,lognitude);
                        }
                    }, 12000);


                }else {
                    LocationFinder finder;
                    finder = new LocationFinder(mContext);
                    if (finder.canGetLocation()) {
                        lat1 = Double.parseDouble(String.valueOf(finder.getLatitude()));
                        lon1 = Double.parseDouble(String.valueOf(finder.getLongitude()));
                        InsertStateDataIntoDB(store_recent_activity_state, System.currentTimeMillis(), "");
                    } else {
                        finder.showSettingsAlert();
                    }

                }

            }
        }

    }

    public void callLocation(){
        createLocationRequest();
        requestLocationUpdates();


    }

    public void callLocationFromNetwork(){
        LocationFinder finder;
        finder = new LocationFinder(mContext);
        if (finder.canGetLocation()) {
            latt = String.valueOf(finder.getLatitude());
            lang = String.valueOf(finder.getLongitude());
        } else {
            finder.showSettingsAlert();
        }
    }

    public void removeDataFromPref(){
        removeLocationUpdates();
       // LocationResultHelper.clearData(mContext);
    }

    private void checkdistance(double lattitude , double lognitude) {
        StringTokenizer tokenss = new StringTokenizer(LocationResultHelper.getSavedLocationResult(mContext), ",");
        if (!tokenss.hasMoreElements()) { } else {
            online_lat = Double.parseDouble(tokenss.nextToken());
        }
        if (!tokenss.hasMoreElements()) { } else {
            online_long = Double.parseDouble(tokenss.nextToken());
        }
        System.out.println("The online_lat online_long--" + online_lat+" --"+ online_long);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(online_lat!=0 && online_long!=0){
                    LocationFinder finder;
                    finder = new LocationFinder(mContext);
                    if (finder.canGetLocation()) {
                        location_network_lat = Double.parseDouble(String.valueOf(finder.getLatitude()));
                        location_network_long = Double.parseDouble(String.valueOf(finder.getLongitude()));
                        System.out.println("The location_network_lat location_network_long" + location_network_lat+" --"+ location_network_long);
                    } else {
                        finder.showSettingsAlert();
                    }

                }
            }
        }, 6000);

        if(online_lat!=0 && online_long!=0){
             DecimalFormat formatter = new DecimalFormat("0.0000");
             float distance= Float.parseFloat(formatter.format(distance(lattitude,lognitude,online_lat,online_long)));
             System.out.println("The1" + BigDecimal.valueOf(distance).toPlainString());
            //Toast.makeText(mContext,"Online--"+String.valueOf(distance),Toast.LENGTH_LONG).show();

            int retval = Float.compare(range, distance);
             if(Float.parseFloat(formatter.format(distance(lattitude,lognitude,online_lat,online_long)))>=range){
                 try {
                     showNotification(online_lat,online_long);
                     InsertStateDataIntoDB(store_recent_activity_state, System.currentTimeMillis(), "");
                 }catch (Exception e){
                     Crashlytics.logException(e);
                 }
             }else {
                 removeDataFromPref();

             }
         }else if(location_network_lat!=0 && location_network_long!=0) {
             DecimalFormat formatter = new DecimalFormat("0.0000");
             float dis= Float.parseFloat(formatter.format(distance(lattitude,lognitude,location_network_lat,location_network_long)));
             //Toast.makeText(mContext,"Network--"+String.valueOf(dis),Toast.LENGTH_LONG).show();

             System.out.println("The2" + BigDecimal.valueOf(dis).toPlainString());
             int retval = Float.compare(range, dis);

               if(Float.parseFloat(formatter.format(distance(lattitude,lognitude,location_network_lat,location_network_long)))>=range){
                 try {
                     InsertStateDataIntoDB(store_recent_activity_state, System.currentTimeMillis(), "");
                 }catch (Exception e){
                     Crashlytics.logException(e);
                 }

             }else {
                 removeDataFromPref();

             }
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
// LocationServices.FusedLocationApi.requestLocationUpdates(
// mGoogleApiClient, mLocationRequest, getPendingIntent());
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
// LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
// getPendingIntent());
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
        try {
            DetectCorrectActivity(userstate);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("MyActivityLocEvent"+ loc_event);


    }

    public void InsertStateDataIntoDB(String state, final long time, String message) {
        StateResultHelper stateResultHelper = new StateResultHelper(
                mContext, state);
        stateResultHelper.saveActivityStateResults();
        try {
            SubmitData2SQLiteDB(state);
            stateResultHelper.showNotification();

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        cursor.close();



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

    public String checkPreviousActivityTime() {
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


    public String checkPrevStateLoc(){
        String loc = "",loc1="",loc2="",loc3="";
        String location;
        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
        cursor = SQLITEDATABASE.rawQuery("SELECT id,name,lat,long,detection_time from demoTable_tracking order by id DESC limit 1 ", null);
        if(cursor==null)
            return "";
        if (cursor.moveToFirst()) {
            do {
                loc = cursor.getString(Integer.parseInt(String.valueOf(cursor.getColumnIndex(SQLiteHelper.KEY_Name))));
                loc1= cursor.getString(Integer.parseInt(String.valueOf(cursor.getColumnIndex(SQLITEHELPER.KEY_DETECTIONTIME))));
                loc2= cursor.getString(Integer.parseInt(String.valueOf(cursor.getColumnIndex(SQLITEHELPER.KEY_Location))));
                loc3= cursor.getString(Integer.parseInt(String.valueOf(cursor.getColumnIndex(SQLITEHELPER.KEY_Location_long))));
            } while (cursor.moveToNext());
        }
        location= loc2+"-"+ loc3;
        return location;
    }

    public void SubmitData2SQLiteDB(String Name) {
        SharedPreferences prefs = mContext.getSharedPreferences("localdbstate", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        battery_stat = String.valueOf(getBatteryPercentage(mContext) + "%");
        try {
            SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
            StringTokenizer tokenss = new StringTokenizer(LocationResultHelper.getSavedLocationResult(mContext), ",");
            if (!tokenss.hasMoreElements()) { } else {
                latt = tokenss.nextToken();
            }
            if (!tokenss.hasMoreElements()) { } else {
                lang = tokenss.nextToken();
            }

            if (!tokenss.hasMoreElements()) { } else {
                accuracy = tokenss.nextToken();
            }
            if (!tokenss.hasMoreElements()) { } else {
                provider = tokenss.nextToken();
            }
            fetchAddress();
            System.out.println("Location--"+ latt+"--"+"--"+lang+"--"+ accuracy+"---"+ provider );
            String Time = String.valueOf(System.currentTimeMillis());  //String.valueOf(createDate(System.currentTimeMillis()));
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
            LocationResultHelper.clearData(mContext);
            editor.clear();
            editor.commit();

        }




    }










    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
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
// Log.d(TAG, line);
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
                return "STILL";default:
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
        if (addresses == null || addresses.size() == 0) {
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



   private double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radious of the earth
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;
        //Toast.makeText(mContext,String.valueOf(distance),Toast.LENGTH_LONG).show();
        System.out.println("The distance between two lat and long is::" + distance);
        return distance;
        //return (Math.round(distance * 100D) / 100D);
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }






    public void showNotification(double lat, double lang) {
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
                    .setContentText(lat+" , "+ lang)
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
                    .setContentText(lat+" , "+lang).build();
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);
            notificationManager.notify(notificationId, notification);
        }
    }
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
}

