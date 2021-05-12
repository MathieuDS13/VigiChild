package com.example.vigichild.core;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.vigichild.parent_mode.ParentMenuActivity;
import com.example.vigichild.ui.login.LoggedInUser;
import com.example.vigichild.ui.login.LoginActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LaunchingApp extends Application {

    String retrieveID;
    FirebaseAuth mAuth;
    LoggedInUser currentUser;
    private final String PREF_FILE = "preferences";
    private final String MODE = "mode";
    //TODO ajouter les données que l'on souhaite persistantes à travers l'application


    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);

        FirebaseApp.initializeApp(this);

        forcePortraitMode();

        Log.w("LaunchingApp", "Création de l'application");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.w("LaunchingApp", "User already logged");
            this.currentUser = new LoggedInUser(currentUser.getDisplayName(), currentUser.getEmail(), currentUser.getUid());
            this.retrieveID = currentUser.getUid();
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            String mode = sharedPreferences.getString(MODE, null);
            if (mode == null) {
                Log.w("launchingapp:failure", "Failed to retrieve mode");
                Intent intent = new Intent(this, SelectModeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                if (mode.equals("Child")) {
                    this.currentUser.setMode("Child");
                    //TODO lancer la bonne activité
                    return;
                }
                if (mode.equals("Parent")) {
                    this.currentUser.setMode("Parent");
                    Intent intent = new Intent(this, ParentMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    //TODO lancer la bonne activité
                    return;
                }
            }
        }
    }

    public void setCurrentUser(LoggedInUser user) {
        this.currentUser = user;
        this.retrieveID = currentUser.getRetrieveID();
    }

    private void forcePortraitMode() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                // for each activity this function is called and so it is set to portrait mode
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

}
