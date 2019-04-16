package com.cpe.requench;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
        showNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
//            Log.i("Message data payload: ", remoteMessage.getData().toString());
//
////            if (/* Check if data needs to be processed by long running job */ true) {
////                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
////                scheduleJob();
////            } else {
////                // Handle message within 10 seconds
////                handleNow();
////            }
//
//        }
//
//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.i("Message Notification:",remoteMessage.getNotification().getBody());
//        }
    }

    public void showNotification(String title,String body){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"MyNotifications")
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(999,builder.build());
    }
}
