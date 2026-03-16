// MyFirebaseMessagingService.java
package com.example.kepengendessboxapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "default_channel_id";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "FCM Token Baru: " + token);
        // Simpan token ke Firestore (opsional)
        saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Pesan diterima dari: " + remoteMessage.getFrom());

        String title = "Notifikasi";
        String message = "Ada pembaruan pesanan";

        // Ambil dari data payload
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("body");
        }

        // Ambil dari notification payload
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                message = remoteMessage.getNotification().getBody();
        }

        sendNotification(title, message);
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Buat ikon di res/drawable
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notifikasi Pesanan",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Menerima notifikasi pesanan");
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, notificationBuilder.build());
    }

    private void saveTokenToFirestore(String token) {
        if (getAuthUser() != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(getAuthUser().getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token FCM disimpan"))
                    .addOnFailureListener(e -> Log.e(TAG, "Gagal simpan token", e));
        }
    }

    private FirebaseUser getAuthUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}