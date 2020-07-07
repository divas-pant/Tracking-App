package com.example.anna.activityapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Consants {
    Context ctx;

    public Consants(Context context){
           this.ctx= context;
    }

    private ArrayList<String> memInfo_array = new ArrayList<>();
    private int available,cached,buffers,free,total,used;


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

}
