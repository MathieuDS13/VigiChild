package com.example.vigichild.parent_mode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vigichild.R;

public class ParentMenuActivity extends AppCompatActivity {

    Button button_localise, button_info_list, button_interaction, button_access_data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_menu);

        button_localise = findViewById(R.id.button_parent_menu_localise);
        button_interaction = findViewById(R.id.button_parent_menu_interactions);

        button_localise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ParentLocalisationActivity.class);
                startActivity(intent);
            }
        });

        button_interaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ParentInteractionActivity.class);
                startActivity(intent);
            }
        });
    }
}
