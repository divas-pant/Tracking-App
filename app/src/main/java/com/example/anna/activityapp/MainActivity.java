package com.example.anna.activityapp;

import android.Manifest;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;


import android.app.PendingIntent;
import android.content.ActivityNotFoundException;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.opencsv.CSVWriter;

import io.fabric.sdk.android.Fabric;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.List;
import java.util.Locale;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private GoogleApiClient mGoogleApiClient;
    private TextView data;
    private int version = 1;
    private Button save, fab;
    ImageView img_activity;
    int icon;
    String SQLiteQuery;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper SQLITEHELPER;
    boolean accelerometer = true;
    Cursor cursor;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
            .ACCESS_FINE_LOCATION};
    Geocoder geocoder;
    private ArrayList<String> memInfo_array = new ArrayList<>();
    private int available;
    private int cached;
    private int buffers;
    private int free;
    private int total;
    private int used;
    private LocationRequest mLocationRequest;
    private  final long UPDATE_INTERVAL = 5000;
    private  final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private  final long MAX_WAIT_TIME = UPDATE_INTERVAL * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        save = findViewById(R.id.btn_save);
        fab = findViewById(R.id.fab);
        data = findViewById(R.id.Result);
        fab.setEnabled(true);
        img_activity = findViewById(R.id.img_activity);
        SQLITEHELPER = new SQLiteHelper(getApplicationContext());
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        geocoder = new Geocoder(this, Locale.getDefault());
        // Check if the user revoked runtime permissions.
        if (arePermissionsEnabled()) {
            //permissions granted, continue flow normally

        } else {
            requestMultiplePermissions();
        }
        //insert static data for first time only
        checkFirstRun();
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(MainActivity.this, ListViewActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportDB();

            }
        });
        /*.....
        request for activity transition...
         */

        //IgnoreBattery();
        if (Build.BRAND.equalsIgnoreCase("xiaomi") || (Build.BRAND.equalsIgnoreCase("Letv"))
                || (Build.BRAND.equalsIgnoreCase("huawei")) || Build.BRAND.equalsIgnoreCase("oppo")
                || Build.BRAND.equalsIgnoreCase("vivo")) {
            ///////////////////////////////////-------------------/////////////////////////
            testAutostart();
        }
        IgnoreBattery();
        ActivityManager activity_manager = (ActivityManager) MainActivity.this
                .getSystemService(Activity.ACTIVITY_SERVICE);


    }



    @Override
    protected void onStart() {
        super.onStart();
        try {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .registerOnSharedPreferenceChangeListener(this);
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "Tag");
        DetectCorrectActivity(checkState());


    }

    public static CharSequence createDate(long timestamp) {
        SimpleDateFormat sdf = null;
        Date d = null;
        try {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            d = c.getTime();
            sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return sdf.format(d);
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
            LocationServices.getFusedLocationProviderClient(getApplicationContext())
                    .requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            // LocationRequestHelper.setRequesting(this, false);
            e.printStackTrace();
            removeLocationUpdates();
            requestLocationUpdates();
        }
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {

        // LocationRequestHelper.setRequesting(this, false);
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
//                getPendingIntent());
        LocationServices.getFusedLocationProviderClient(getApplicationContext()).removeLocationUpdates(getPendingIntent());

    }
    /*....
    detect state through eventbus..
     */
    @Subscribe
    public void onPhoneNumberReceived(OnReceiverEvent event) {
        final String row = event.getActivityState();
        try {
            if (!isNullOrEmpty(row)) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DetectCorrectActivity(row);
                        accelerometer = false;
                    }
                }, 3000);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    /*...
    detect state through google activity reco during forground..
     */
    public void DetectCorrectActivity(final String row) {
        String acc_decision = "";
        if (row.equals("STILL")) {
            icon = R.drawable.ic_still;
        } else if (row.equals("ON_FOOT")) {
            icon = R.drawable.ic_walking;
        } else if (row.equals("IN_VEHICLE")) {
            icon = R.drawable.ic_driving;
        }
        data.setText(row);
        img_activity.setImageResource(icon);
    }


    private void exportDB() {
        try {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "csvname.csv");
            try {
                file.createNewFile();

                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = SQLITEHELPER.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM demoTable_tracking", null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    //Which column you want to exprort
                    String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4)
                            , curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
            } catch (Exception sqlEx) {
                sqlEx.printStackTrace();
                Crashlytics.logException(sqlEx);
            }

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(android.content.Intent.EXTRA_SUBJECT, "Activity Recognition Report version " + version);
            email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:" + file));
            email.setType("vnd.android.cursor.dir/email");
            //  email.putExtra(Intent.EXTRA_TEXT, "Data from "+startTime + " to "+endTime+".\n");
            startActivity(Intent.createChooser(email, "Send Report"));
            version++;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


    }


    public void checkFirstRun() {
        try {
            boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
            if (isFirstRun) {
            /*..
            create static table for state in which loction would be through...
             */
                StaticDBCreate();
                // Place your static state data
                insertDataIntoStaticTable();
                getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit()
                        .putBoolean("isFirstRun", false)
                        .apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


    }

    private void insertDataIntoStaticTable() {
        /*........
        state in which location would be throws...
         */
        try {
            SQLiteQuery = "INSERT INTO tblMovingStates (cstate,pstate) " +
                    "VALUES" +
                    "('" + "IN_VEHICLE" + "', '" + "STILL" + "')" +
                    ",('" + "ON_FOOT" + "', '" + "STILL" + "')" +
                    ",('" + "STILL" + "', '" + "IN_VEHICLE" + "')" +
                    ",('" + "STILL" + "', '" + "ON_FOOT" + "')";
            SQLITEDATABASE.execSQL(SQLiteQuery);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


    }

    public void StaticDBCreate() {
        try {
            SQLITEDATABASE = openOrCreateDatabase("TrackingDb", Context.MODE_PRIVATE, null);
            SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS tblMovingStates" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, cstate VARCHAR," +
                    " pstate VARCHAR);");

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        /*...
        if data is in insert in shared pref duruing forground process...
         */
        try {
            if (s.equals(StateResultHelper.KEY_ACTIVITY_STATE_RESULT)) {
                final String row = StateResultHelper.getSavedActivityState(getApplicationContext());
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DetectCorrectActivity(row);
                        accelerometer = false;
                        //stoptimertask();
                    }
                }, 3000);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }


    private boolean arePermissionsEnabled() {
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    remainingPermissions.add(permission);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissions[i])) {
                            new android.support.v7.app.AlertDialog.Builder(this)
                                    .setMessage("Please enable all permission to run this app")
                                    .setPositiveButton("Allow", (dialog, which) -> requestMultiplePermissions())
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .create()
                                    .show();
                        }
                    }
                    return;
                }
            }

            //all is good, continue flow
        }
    }

    public void testAutostart() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_app_updates, null);
        CheckBox mCheckBox = mView.findViewById(R.id.checkBox);

        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            mBuilder.setTitle("Add Activity App to autostart");
            mBuilder.setMessage(String.format("%s requires to be enabled in 'autostart'" +
                    " to send notifications.%n", getString(R.string.app_name)));
        } else if (Build.BRAND.equalsIgnoreCase("Letv")) {

            mBuilder.setTitle("Add Activity App to autostart");
            mBuilder.setMessage(String.format("%s requires to be enabled in 'autostart'" +
                    " to send notifications.%n", getString(R.string.app_name)));

        } else if (Build.BRAND.equalsIgnoreCase("huawei")) {

            mBuilder.setTitle("Add Activity App to Protected Apps");
            mBuilder.setMessage(String.format("%s requires to be enabled in 'Protected Apps'" +
                    " to send notifications.%n", getString(R.string.app_name)));
        }

        mBuilder.setView(mView);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
                        try {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);

                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    } else if (Build.BRAND.equalsIgnoreCase("Letv")) {
                        try {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                        } catch (Exception e) {
                            e.printStackTrace();

                        }

                    } else if (Build.BRAND.equalsIgnoreCase("huawei")) {
                        try {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    } else if (Build.BRAND.equalsIgnoreCase("vivo")) {
                        try {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    } else if (Build.BRAND.equalsIgnoreCase("oppo")) {
                        try {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }


                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();

                } catch (Exception e) {
                    e.printStackTrace();

                }
                // dialogInterface.dismiss();
            }
        });

        mBuilder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    storeDialogStatus(true);
                } else {
                    storeDialogStatus(false);
                }
            }
        });

        if (getDialogStatus()) {
            mDialog.hide();
        } else {
            mDialog.show();
        }
    }

    private void storeDialogStatus(boolean isChecked) {
        SharedPreferences mSharedPreferences = getSharedPreferences("CheckItem", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("item", isChecked);
        mEditor.apply();
    }

    private boolean getDialogStatus() {
        SharedPreferences mSharedPreferences = getSharedPreferences("CheckItem", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("item", false);
    }


    public void IgnoreBattery() {
        String detect_whitelist = String.valueOf(DozeHelper.getisBatteryOptimizations(getApplicationContext(), getPackageName().toString()));
        if (detect_whitelist.equals("NOT_WHITE_LISTED")) {
            Intent intent = DozeHelper.prepareBatteryOptimization(MainActivity.this, getPackageName(), true);
            startActivity(intent);
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

        String info_ram = null;
        try {
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
            info_ram = "Total-" + total_ram + " " + "Free-" + avail_ram + " " + "Used-" + free_ram;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return info_ram;
    }

    public int filterText(String str) {
        String str2 = str.replaceAll("\\s+", " ");
        String str3[] = str2.split(" ");
        return Integer.parseInt(str3[1]);
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
}


