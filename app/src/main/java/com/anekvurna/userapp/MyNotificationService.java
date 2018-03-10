package com.anekvurna.userapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Admin on 2/28/2018.
 */

public class MyNotificationService extends FirebaseMessagingService {
    private static int notificationId = 0;


    Context context;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        context = this;
        try {
            //JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Map<String, String> payload = remoteMessage.getData();
            final String title = payload.get("title");
            final String alert = payload.get("body");
            if(title.equals("beginTrip"))
            {
                String name = payload.get("driverName");
                notifyTripStarted(alert, name);
                return;
            }

            if(title.equals("Dropped Passenger"))
            {
                String latitude = payload.get("latitude");
                String longitude = payload.get("longitude");
                notifyTripEnded(latitude, longitude);
            }

            //addToMessageHistory(alert);
            notifyUser(title, alert);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void notifyTripStarted(String driverId, String driverName) {
        Notification notification;

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(context, MapsActivity.class);
        resultIntent.putExtra("trackDriver", true);
        resultIntent.putExtra("driverId", driverId);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        String title = "Trip Started";
        String alert = "Trip started by " + driverName;

        Notification.Builder builder;
        builder = new Notification.Builder(context)
                .setSound(uri)
                .setContentTitle(title)
                .setContentText(alert)
                .setSmallIcon(R.mipmap.ic_launcher);
        if(Build.VERSION.SDK_INT < 20) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.addAction(R.drawable.ic_location_searching_black_24dp, "Track Driver", resultPendingIntent);
            }
        }
        else
        {
            Notification.Action action = new Notification.Action
                    .Builder(R.drawable.ic_location_searching_black_24dp, "Track Driver", resultPendingIntent)
                    .build();
            builder.addAction(action);
        }

        if(Build.VERSION.SDK_INT < 16) notification = builder.getNotification();
        else
            notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,notification);
    }

    public void addToMessageHistory(String message)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser==null) return;
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("customerMessages").child(currentUser.getUid());
        String id = messageReference.push().getKey();
        messageReference.child(id).setValue(message);
    }

    void notifyUser(String title, String alert)
    {
        Notification notification;

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(context, NotificationHistoryActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if(Build.VERSION.SDK_INT < 16) {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle(title)
                    .setContentText(alert)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).getNotification();
        }
        else
        {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle(title)
                    .setContentText(alert)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        }
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,notification);
    }

    void notifyTripEnded(String lat, String lon)
    {
        Notification notification;

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(context, MapsActivity.class);
        resultIntent.putExtra("droppedPassenger", true);
        resultIntent.putExtra("latitude", lat);
        resultIntent.putExtra("longitude", lon);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        String title = "Trip Ended";
        String alert = "Trip has been ended";

        Notification.Builder builder;
        builder = new Notification.Builder(context)
                .setSound(uri)
                .setContentTitle(title)
                .setContentText(alert)
                .setSmallIcon(R.mipmap.ic_launcher);
        if(Build.VERSION.SDK_INT < 20) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.addAction(R.drawable.ic_location_on_black_24dp, "See Drop Location", resultPendingIntent);
            }
        }
        else
        {
            Notification.Action action = new Notification.Action
                    .Builder(R.drawable.ic_location_on_black_24dp, "See Drop Location", resultPendingIntent)
                    .build();
            builder.addAction(action);
        }

        if(Build.VERSION.SDK_INT < 16) notification = builder.getNotification();
        else
            notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,notification);
    }

}
