package com.example.kepengendessboxapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {
    public static final String CHANNEL_ID_ADMIN = "admin_channel";
    public static final String CHANNEL_ID_USER = "user_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel untuk Admin
            NotificationChannel adminChannel = new NotificationChannel(
                    CHANNEL_ID_ADMIN,
                    "Notifikasi Admin",
                    NotificationManager.IMPORTANCE_HIGH
            );
            adminChannel.setDescription("Notifikasi untuk admin saat ada pesanan baru");

            // Channel untuk User
            NotificationChannel userChannel = new NotificationChannel(
                    CHANNEL_ID_USER,
                    "Notifikasi User",
                    NotificationManager.IMPORTANCE_HIGH
            );
            userChannel.setDescription("Notifikasi untuk user saat status pesanan berubah");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(adminChannel);
            manager.createNotificationChannel(userChannel);
        }
    }
}