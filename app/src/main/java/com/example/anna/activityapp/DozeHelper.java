package com.example.anna.activityapp;

import android.Manifest;
import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;


public class DozeHelper {
    public enum WhiteListedInBatteryOptimizations {
        WHITE_LISTED, NOT_WHITE_LISTED, ERROR_GETTING_STATE, UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING, IRRELEVANT_OLD_ANDROID_API
    }



    @NonNull
    public static WhiteListedInBatteryOptimizations getisBatteryOptimizations(@NonNull Context context, @NonNull String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return WhiteListedInBatteryOptimizations.IRRELEVANT_OLD_ANDROID_API;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return WhiteListedInBatteryOptimizations.UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING;
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return WhiteListedInBatteryOptimizations.ERROR_GETTING_STATE;
        return pm.isIgnoringBatteryOptimizations(packageName) ? WhiteListedInBatteryOptimizations.WHITE_LISTED : WhiteListedInBatteryOptimizations.NOT_WHITE_LISTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    @Nullable
    public static Intent prepareBatteryOptimization(@NonNull Context context, @NonNull String packageName, boolean alsoWhenWhiteListed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED)
            return null;
        final WhiteListedInBatteryOptimizations ispowersave = getisBatteryOptimizations(context, packageName);
        Intent intent = null;
        switch (ispowersave) {
            case WHITE_LISTED:
                if (alsoWhenWhiteListed)
                    intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                break;
            case NOT_WHITE_LISTED:
                intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + packageName));
                break;
            case ERROR_GETTING_STATE:
            case UNKNOWN_TOO_OLD_ANDROID_API_FOR_CHECKING:
            case IRRELEVANT_OLD_ANDROID_API:
            default:
                break;
        }
        return intent;
    }




}
