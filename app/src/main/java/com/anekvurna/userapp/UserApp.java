package com.anekvurna.userapp;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

/**
 * Created by Admin on 1/23/2018.
 */

public class UserApp extends Application {
    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("pingMeId")
                .clientKey("thisIsNotSecret")
                .server("http://52.14.72.244:1338/parse")
                .enableLocalDataStore()
                .build()
        );

        ParsePush.subscribeInBackground("");

        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
