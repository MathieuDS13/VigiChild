package com.example.vigichild.child_mode;

import android.content.Intent;
import android.os.Build;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vigichild.R;

public class ChildMenuActivity extends AppCompatActivity {

    private Button sendAlert;
    private Button accountSettings;

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_child_menu);
        sendAlert = findViewById(R.id.button_child_alert_parent);
        accountSettings = findViewById(R.id.button_child_menu_account);
        Intent intent = new Intent(this, ChildGeolocalisationDataRegisterService.class);
        intent.setAction(Actions.START.name());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            return;
        }

        startService(intent);
        //TODO lancer le service qui enregistre les localisations en ligne
    }
}
