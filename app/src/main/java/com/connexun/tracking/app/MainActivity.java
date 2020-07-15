package com.connexun.tracking.app;

import android.Manifest;


import android.app.AlertDialog;


import android.app.PendingIntent;
import android.content.ActivityNotFoundException;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.connexun.tracking.app.db.SQLiteHelper;
import com.google.android.gms.location.ActivityRecognition;
import com.opencsv.CSVWriter;

import io.fabric.sdk.android.Fabric;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.File;

import java.io.FileWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView data;
    private int version = 1, icon;
    private Button save, fab;
    ImageView img_activity;
    String SQLiteQuery;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper SQLITEHELPER;
    Cursor cursor;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
            .ACCESS_FINE_LOCATION};
    AlertDialog.Builder mBuilder;
    AlertDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        save = findViewById(R.id.btn_save);
        fab = findViewById(R.id.fab);
        data = findViewById(R.id.Result);
        data.setText("Waiting for Activity Detection");
        img_activity = findViewById(R.id.img_activity);
        mBuilder = new AlertDialog.Builder(MainActivity.this);
        mDialog = mBuilder.create();
        SQLITEHELPER = new SQLiteHelper(getApplicationContext());
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        // Check if the user revoked runtime permissions.
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
        //callActivity();
        initView();
    }


    public void initView(){

        if (arePermissionsEnabled()) { } else {
            requestMultiplePermissions();
        }
        if (Build.BRAND.equalsIgnoreCase("xiaomi") || (Build.BRAND.equalsIgnoreCase("Letv"))
                || (Build.BRAND.equalsIgnoreCase("huawei")) || Build.BRAND.equalsIgnoreCase("oppo")
                || Build.BRAND.equalsIgnoreCase("vivo")) {
            ///////////////////////////////////-------------------/////////////////////////
            testAutostart();
        }
        IgnoreBattery();
        //getRecentStateEntry();
        getAllState();
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
        mDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DetectActivity(checkState());


    }

    @Subscribe
    public void onPhoneNumberReceived(OnReceiverEvent event) {
        final String row = event.getActivityState();
        try {
            if (!isNullOrEmpty(row)) {
                DetectActivity(row);
            } else {
                callActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    /*...
    detect state through google activity reco during forground..
     */

    public String DetectActivity(final String row) {
        if (row.equals("STILL")) {
            icon = R.drawable.ic_still;
        }
        data.setText(row);
        img_activity.setImageResource(icon);
        return row;
    }


    public void exportDB() {
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
                    String time = String.valueOf(createDate(Long.parseLong(curCSV.getString(2))));
                    //Which column you want to exprort
                    String arrStr[] = {curCSV.getString(0), curCSV.getString(1), time, curCSV.getString(3), curCSV.getString(4)
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
                DetectActivity(row);
            } else {
                callActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public ArrayList<HashMap<String,String>> getAllState(){
       return SQLITEHELPER.getAllState();

    }


    public  ArrayList<HashMap<String,String>> getRecentStateEntry(){
        return SQLITEHELPER.getRecentEntry();

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

        mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_app_updates, null);
        CheckBox mCheckBox = mView.findViewById(R.id.checkBox);

        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            mBuilder.setTitle(R.string.permission_title);
            mBuilder.setMessage(R.string.permission_text);
        } else if (Build.BRAND.equalsIgnoreCase("Letv")) {

            mBuilder.setTitle(R.string.permission_title);
            mBuilder.setMessage(R.string.permission_text);

        } else if (Build.BRAND.equalsIgnoreCase("huawei")) {

            mBuilder.setTitle(R.string.permission_title);
            mBuilder.setMessage(R.string.permission_text);
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

        mDialog = mBuilder.create();
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

    public void showbatterypermissionPopuptext() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Want Activity on to go?");
        alertDialog.setMessage(R.string.battery_permission_text);
        alertDialog.setPositiveButton("Update Setting", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = DozeHelper.prepareBatteryOptimization(MainActivity.this, getPackageName(), true);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        alertDialog.show();
    }

    public void IgnoreBattery() {
        String detect_whitelist = String.valueOf(DozeHelper.getisBatteryOptimizations(getApplicationContext(), getPackageName().toString()));
        if (detect_whitelist.equals("NOT_WHITE_LISTED")) {
            showbatterypermissionPopuptext();
        }

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


    public CharSequence createDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");

        return sdf.format(d);
    }


    public void callActivity() {
        Intent intent = new Intent(getApplicationContext(), MyActivityTrackReciver.class);
        intent.setAction(MyActivityTrackReciver.ACTION_ACTIVITY_PROCESS_UPDATES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.getClient(this).requestActivityUpdates(8000, pendingIntent);
        // Toast.makeText(getApplicationContext(),"Hello i am restart from activity",Toast.LENGTH_LONG).show();

    }
}


