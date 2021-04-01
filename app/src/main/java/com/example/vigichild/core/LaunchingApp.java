package com.example.vigichild.core;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

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
        FirebaseApp.initializeApp(this);
        Log.w("LaunchingApp","Création de l'application");
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
            if(mode == null) {
                Log.w("launchingapp:failure", "Failed to retrieve mode");
                Intent intent = new Intent(this, SelectModeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                if(mode.equals("Child")) {
                    this.currentUser.setMode("Child");
                    //TODO lancer la bonne activité
                    return;
                }
                if(mode.equals("Parent")) {
                    this.currentUser.setMode("Parent");
                    //TODO lancer la bonne activité
                    return;
                }
            }
        }
        /**
         * TODO si l'utilisateur est déjà connecté, récupérer le mode, si le mode ne peut pas être récupéré lancer l'activité de sélection de mode
         * aussi non lancer l'activité liée au mode
         */
    }

    public void setCurrentUser(LoggedInUser user) {
        this.currentUser = user;
        this.retrieveID = currentUser.getRetrieveID();
    }

}
