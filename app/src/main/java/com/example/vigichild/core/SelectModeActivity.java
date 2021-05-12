package com.example.vigichild.core;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vigichild.R;
import com.example.vigichild.parent_mode.ParentMenuActivity;

public class SelectModeActivity extends AppCompatActivity {

    private final String PREF_FILE = "preferences";
    private final String MODE = "mode";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        final Button childModeButton = findViewById(R.id.child_mode_button);
        final Button parentModeButton = findViewById(R.id.parent_mode_button);

        childModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(MODE, "Child");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Preference set to child mode", Toast.LENGTH_LONG).show();
                childModeButton.setActivated(false);
                parentModeButton.setActivated(false);
            }
        });

        parentModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(MODE, "Parent");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Preference set to parent mode", Toast.LENGTH_LONG).show();
                childModeButton.setActivated(false);
                parentModeButton.setActivated(false);
                Intent intent = new Intent(v.getContext(), ParentMenuActivity.class);
                startActivity(intent);
                //TODO lancer la bonne activité
            }
        });
    }
}
