package com.anekvurna.userapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Admin on 1/7/2018.
 */

public class PushReceiver extends ParsePushBroadcastReceiver {
    private static int notificationId = 0;


    Context context;

    @Override
    protected void onPushReceive(Context mContext, Intent intent) {
       /* context = mContext;
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            final String title = json.getString("title");
            final String alert = json.getString("alert");
            if(title.equals("beginTrip"))
            {
                notifyTripStarted(alert);
                return;
            }

            addToMessageHistory(alert);
            notifyUser(title, alert);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

    private void notifyTripStarted(String driverId) {
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
        String alert = "Trip has been started";

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
        notificationId++;
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
        notificationId++;
    }
}