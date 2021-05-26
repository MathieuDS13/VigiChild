package com.example.vigichild.parent_mode;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vigichild.R;

public class ParentInteractionActivity extends AppCompatActivity {

    private Button recordButton;
    private Button sendAudioButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_interaction);

        this.recordButton = findViewById(R.id.button_parent_record_audio);
        this.sendAudioButton = findViewById(R.id.button_parent_send_audio);

        //TODO mettre en place l'enregistrement
        //TODO mettre en place la lecture avant envoi
        //TODO mettre en place l'ajout de l'audio dans la base de données
    }
}
