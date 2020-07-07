package com.example.anna.activityapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import com.example.anna.activityapp.db.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ListViewActivity extends Activity {
	
	SQLiteHelper SQLITEHELPER;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteListAdapter ListAdapter ;

    ArrayList<String> ID_ArrayList = new ArrayList<String>();
    ArrayList<String> NAME_ArrayList = new ArrayList<String>();
    ArrayList<String> PHONE_NUMBER_ArrayList = new ArrayList<String>();
    ArrayList<String> Location_ArrayList = new ArrayList<String>();
    ArrayList<String> Location_Long_ArrayList = new ArrayList<String>();
    ArrayList<String> Battery_ArrayList = new ArrayList<String>();
    ArrayList<String> Accuracy_ArrayList = new ArrayList<String>();

    ArrayList<String> Provider_ArrayList = new ArrayList<String>();
    ArrayList<String> Address_ArrayList = new ArrayList<String>();
    ArrayList<String> RAM_ArrayList = new ArrayList<String>();


    ListView LISTVIEW;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        LISTVIEW = findViewById(R.id.listView1);
        SQLITEHELPER = new SQLiteHelper(this);
        
    }

    @Override
    protected void onResume() {
        try {
            ShowSQLiteDBdata() ;
        }catch (Exception e){
            e.printStackTrace();
        }

    	super.onResume();
    }

    private void ShowSQLiteDBdata() {
    	try {
            SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();
            Cursor cursor = SQLITEDATABASE.rawQuery("SELECT * FROM demoTable_tracking ORDER BY id DESC ", null);
            ID_ArrayList.clear();
            NAME_ArrayList.clear();
            PHONE_NUMBER_ArrayList.clear();
            Location_ArrayList.clear();
            Location_Long_ArrayList.clear();
            Battery_ArrayList.clear();
            Accuracy_ArrayList.clear();
            Provider_ArrayList.clear();
            Address_ArrayList.clear();
            RAM_ArrayList.clear();


            if (cursor.moveToFirst()) {
                do {
                    ID_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ID)));
                    NAME_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Name)));
                    PHONE_NUMBER_ArrayList.add(String.valueOf(createDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_DETECTIONTIME))))));
                    Location_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Location)));
                    Location_Long_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Location_long)));
                    Battery_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Battery)));
                    Accuracy_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Accuracy)));
                    Provider_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Provider)));
                    Address_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Address)));
                    RAM_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_RAM)));
                    } while (cursor.moveToNext());
            }

            ListAdapter = new SQLiteListAdapter(ListViewActivity.this,

                    ID_ArrayList,
                    NAME_ArrayList,
                    PHONE_NUMBER_ArrayList,
                    Location_ArrayList,
                    Location_Long_ArrayList,
                    Battery_ArrayList,
                    Accuracy_ArrayList,
                    Provider_ArrayList,
                    Address_ArrayList,
                    RAM_ArrayList

            );

            LISTVIEW.setAdapter(ListAdapter);
            cursor.close();
        }catch (Exception e){
    	    e.printStackTrace();
        }


    }

    public CharSequence createDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");

        return sdf.format(d);
    }


}