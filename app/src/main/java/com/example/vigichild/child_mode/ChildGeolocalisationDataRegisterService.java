package com.example.vigichild.child_mode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.lang.UCharacterEnums;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.ServiceState;
import android.webkit.GeolocationPermissions;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.vigichild.R;

import java.io.Console;
import java.util.concurrent.TimeUnit;

public class ChildGeolocalisationDataRegisterService extends Service {

    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    private HandlerThread handlerThread;
    private Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand executed with id : " + startId);
        String action = intent.getAction();
        switch (action) {
            case "START":
                startService();
                break;
            case "STOP":
                stopService();
                break;
            default:
                System.out.println("This should never happen. No action in the received intent");
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("The service has been created");
        Notification notification = createNotification();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("The service has been destroyed");
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
        handlerThread.quit();
    }

    private void startService() {
        if (isServiceStarted) return;

        System.out.println("Starting the foreground geolocalisation task");
        Toast.makeText(this, "Starting gelocalisation task", Toast.LENGTH_SHORT).show();

        isServiceStarted = true;
        //TODO voir par rapport au code en ligne, il manque la ligne setServiceState(...)
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ChildGeolocalisationDataRegisterService.class.getName());
        wakeLock.acquire();

        /**
         handlerThread = new HandlerThread("MyLocationThread");
         handlerThread.setDaemon(true);
         handlerThread.start();
         handler = new Handler(handlerThread.getLooper());
         // Every other call is up to you. You can update the location,
         // do whatever you want after this part.

         // Sample code (which should call handler.postDelayed()
         // in the function as well to create the repetitive task.)
         handler.postDelayed(new Runnable() {
        @Override public void run() {
        System.out.println("Geolocalisation de l'enfant");
        //TODO code de géolocalisation
        }
        }, 6000);
         **/

        // Create the Handler object (on the main thread by default)
        Handler handler = new Handler();
        // Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                System.out.println("Geolocalisation de l'enfant de ses  morts");
                // Repeat this the same runnable code block again another 2 seconds
                // 'this' is referencing the Runnable object
                handler.postDelayed(this, 2000);
            }
        };
// Start the initial runnable task by posting through the handler
        handler.post(runnableCode);
    }

    private boolean myFuncToUpdateLocation() {
        return true;
    }

    private void stopService() {
        System.out.println("Stopping the foreground service");
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();
        try {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            System.out.println("Service stopped without being started:" + e.getMessage());
        }
        handlerThread.quit();
        isServiceStarted = false;
    }

    private Notification createNotification() {
        String channelID = "GeolocalisationChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, "Child Geolocalisation notifications channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Geolocalisation service channel");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, ChildMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new Notification.Builder(this, channelID) : new Notification.Builder(this);

        return builder.setContentTitle("Vigichils Geolocalisation")
                .setContentText("Your parents are watching you")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

    }
}
