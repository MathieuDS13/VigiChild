package com.example.vigichild.child_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import static android.content.Context.MODE_PRIVATE;

public class StartReceiver extends BroadcastReceiver {

    private final String PREF_FILE = "preferences";
    private final String MODE = "mode";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String mode = sharedPreferences.getString(MODE, null);
        if (mode == "Child") {

            if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
                Intent toLaunch = new Intent(context, ChildPermanentService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(toLaunch);
                } else {
                    context.startService(toLaunch);
                }
            }
        }

    }
}
