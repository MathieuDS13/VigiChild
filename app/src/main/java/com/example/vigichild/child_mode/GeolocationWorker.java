package com.example.vigichild.child_mode;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class GeolocationWorker extends Worker {

    public GeolocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //TODO enregistrer la position GPS dans la base de données
        System.out.println("J'enregistre la position de l'enfant dans la base de données");
        return Result.success();
    }
}
