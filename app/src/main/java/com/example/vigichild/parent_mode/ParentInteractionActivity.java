package com.example.vigichild.parent_mode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.vigichild.R;
import com.example.vigichild.core.LaunchingApp;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class ParentInteractionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordActivity";

    private Button recordButton;
    private Button sendAudioButton;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    boolean startPlaying = true;
    boolean startRecording = true;
    private Button playButton;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_interaction);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance("gs://vigichild-b905b.appspot.com/").getReference();
        this.recordButton = findViewById(R.id.button_parent_record_audio);
        this.sendAudioButton = findViewById(R.id.button_parent_send_audio);

        this.playButton = findViewById(R.id.button_parent_play_audio);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecord.3gp";
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(startRecording);
                if (startRecording) {
                    recordButton.setText("Stop recording");
                } else {
                    recordButton.setText("Start recording");
                }
                startRecording = !startRecording;
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(startPlaying);
                if (startPlaying) {
                    playButton.setText("Stop playing");
                } else {
                    playButton.setText("Start playing");
                }
                startPlaying = !startPlaying;
            }
        });

        sendAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileName != null) {
                    sendAudioToServer();
                } else {
                    Toast.makeText(getApplicationContext(), "No audio to send", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendAudioToServer() {
        try {
            /**
             String key = mDatabase.child("Data").child("UserData").child(LaunchingApp.currentUser.getRetrieveID()).p;
             mDatabase.child("Data").child("UserData").child(LaunchingApp.currentUser.getRetrieveID()).setValue(loc);
             **/
            Toast.makeText(getApplicationContext(), "Sending audio", Toast.LENGTH_SHORT).show();
            Uri uri = Uri.fromFile(new File(fileName));
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/mpeg")
                    .build();
            StorageReference ref = mStorage.child("Audio").child(LaunchingApp.currentUser.getRetrieveID()).child("audio.3gp");
            ref.putFile(uri, metadata).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Failed to send audio", Toast.LENGTH_SHORT).show();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        mDatabase.child("Data").child("UserData").child("Audio").child(LaunchingApp.currentUser.getRetrieveID()).push().setValue(downloadUri.toString());
                        Toast.makeText(getApplicationContext(), "Audio sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to send audio", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No audio to send", Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "send audio to server failed");
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }


    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
