package com.connexun.tracking.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

public class SQLiteListAdapter extends BaseAdapter {
	
    Context context;
    ArrayList<String> userID;
    ArrayList<String> UserName;
    ArrayList<String> User_PhoneNumber;
    ArrayList<String> User_Location;
    ArrayList<String> User_Location_long;
    ArrayList<String> User_battery;
    ArrayList<String> User_Accuracy;
    ArrayList<String> User_Provider;
    ArrayList<String> User_Address;
    ArrayList<String> User_RAM;
    //


    public SQLiteListAdapter(
    		Context context2,
    		ArrayList<String> id,
    		ArrayList<String> name,
    		ArrayList<String> phone,
            ArrayList<String> location,
            ArrayList<String> location_long,
            ArrayList<String> battery,
            ArrayList<String> accuracy,
            ArrayList<String> provider,
            ArrayList<String> address,
            ArrayList<String> ram
          //

    )
    {
        	
    	this.context = context2;
        this.userID = id;
        this.UserName = name;
        this.User_PhoneNumber = phone;
        this.User_Location = location;
        this.User_Location_long = location_long;
        this.User_battery = battery;
        this.User_Accuracy = accuracy;
        this.User_Provider = provider;
        this.User_Address = address;
        this.User_RAM=ram;
       //

    }

    public int getCount() {
        // TODO Auto-generated method stub
        return userID.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View child, ViewGroup parent) {
    	
        Holder holder;
        
        LayoutInflater layoutInflater;
        
        if (child == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            child = layoutInflater.inflate(R.layout.listviewdatalayout, null);
            holder = new Holder();
            holder.textviewid = child.findViewById(R.id.textViewID);
            holder.textviewname = child.findViewById(R.id.textViewNAME);
            holder.textviewphone_number = child.findViewById(R.id.textViewPHONE_NUMBER);
            holder.text_Location_activity = child.findViewById(R.id.text_Location_activity);
            holder.text_Location_long_activity= child.findViewById(R.id.text_Location_long_activity);
            holder.text_battery_stat = child.findViewById(R.id.text_battery_stat);
            holder.text_accuracy_activity = child.findViewById(R.id.text_accuracy_activity);
            holder.text_provider_activity = child.findViewById(R.id.text_provider_activity);
            holder.text_address_activity = child.findViewById(R.id.text_address_activity);
            holder.text_ram_info_stat = child.findViewById(R.id.text_ram_info_stat);
           //
            child.setTag(holder);
            
        } else {
        	
        	holder = (Holder) child.getTag();
        }
        holder.textviewid.setText(userID.get(position));
        holder.textviewname.setText(UserName.get(position));
        holder.textviewphone_number.setText(User_PhoneNumber.get(position));
        holder.text_Location_activity.setText(User_Location.get(position));
        holder.text_Location_long_activity.setText(User_Location_long.get(position));
        holder.text_battery_stat.setText(User_battery.get(position));
        holder.text_accuracy_activity.setText(User_Accuracy.get(position));
        holder.text_provider_activity.setText(User_Provider.get(position));
        holder.text_address_activity.setText(User_Address.get(position));
        holder.text_ram_info_stat.setText(User_RAM.get(position));
      //


        return child;
    }

    public class Holder {
        TextView textviewid;
        TextView textviewname;
        TextView textviewphone_number;
        TextView text_Location_activity;
        TextView text_Location_long_activity;
        TextView text_battery_stat;
        TextView text_accuracy_activity;
        TextView text_provider_activity;
        TextView text_address_activity;
        TextView text_ram_info_stat;
       //




    }

}