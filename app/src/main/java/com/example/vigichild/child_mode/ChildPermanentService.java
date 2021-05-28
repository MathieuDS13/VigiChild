package com.example.vigichild.child_mode;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vigichild.R;
import com.example.vigichild.core.LaunchingApp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChildPermanentService extends Service {

    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    //private HandlerThread handlerThread;
    //private Handler handler;
    private static final String TAG = "CHILDGEOLOCALISATION";
    private static final int LOCATION_INTERVAL = 2000;//120000;
    private static final float LOCATION_DISTANCE = 5f;
    private MediaPlayer mPlayer;
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private LocationManager mLocationManager = null;
    private DatabaseReference mDatabase;
    private final int SERVERPORT = 50005;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand executed with id : " + startId);
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initializeLocationListener(); //On crée l'écouteur de position qui enregistre les données dans la BDD
        initializeAudioListener();

        Notification notification = createNotification();
        startForeground(1, notification);

    }

    private void initializeAudioListener() {
        DatabaseReference ref = mDatabase.child("Data").child("UserData").child("Audio").child(LaunchingApp.currentUser.getRetrieveID());

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i("Audio Player", "Update received playing audio");
                Uri uri = Uri.parse(snapshot.getValue(String.class));
                mPlayer = new MediaPlayer();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .build());
                }
                try {
                    mPlayer.setDataSource(uri.toString());
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (IOException e) {
                    Log.e("Audio PLayer", "Fail to play audio : " + uri.toString());
                    e.printStackTrace();
                }

                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //TODO ajouter un ChildEventListener	onChildAdded() qui écoute quand un audio est ajouté et le lit
    }

    private void initializeLocationListener() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        System.out.println("The service has been created");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("The service has been destroyed");
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        //handlerThread.quit();
    }

    /**
     * Crée la notification qui permet au service de tourner au premier plan en continu
     *
     * @return
     */
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

        return builder.setContentTitle("Vigichild Geolocalisation")
                .setContentText("Your parents are watching you")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

    }

    /**
     * Permet de redémarrer le service quand l'appli est fermée
     *
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent restartService = new Intent(getApplicationContext(), ChildPermanentService.class);
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePending = PendingIntent.getService(this, 1, restartService, PendingIntent.FLAG_ONE_SHOT);
        getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePending);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void writeGeodata(Location location) {
        LocationDTO loc = new LocationDTO(location);
        if (location != null) {

            mDatabase.child("Data").child("GeoData").child(LaunchingApp.currentUser.getRetrieveID()).setValue(loc);
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            writeGeodata(location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

}
