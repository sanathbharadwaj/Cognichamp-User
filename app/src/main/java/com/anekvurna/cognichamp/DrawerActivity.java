package com.anekvurna.cognichamp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import static com.anekvurna.cognichamp.SanathUtilities.*;

public class DrawerActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NetworkStateReceiver networkStateReceiver;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onCreateDrawer()
    {

        //checkForNetConnection();
        initializeSharedPrefs(this);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


        final Context context = this;

      /*  if(getSupportActionBar() == null) {
           // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //setSupportActionBar(toolbar);
        }*/

        if(getSupportActionBar() == null) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if(toolbar!=null)
                setSupportActionBar(toolbar);
        }
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        mDrawerLayout.closeDrawers();

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()){
                   case(R.id.notification_history):
                        loadActivity(context, NotificationHistoryActivity.class);break;
                    case (R.id.my_profile):
                        loadActivity(context, ViewTabbedActivity.class);break;
                    case(R.id.logout):
                        FirebaseAuth.getInstance().signOut();
                        editor.putInt("profileStatus", 0);
                        editor.apply();
                        loadActivityAndClearStack(context, UserChoiceActivity.class);break;

                    case R.id.track_driver_menu:
                        loadActivity(context , DriverListActivity.class);break;

                    case R.id.share_app:
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Hello there! Check out this new and innovative riding " +
                                "application CogniChamp";
                        String shareSub = "CogniChamp";
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share using")); break;

                    case R.id.edit_pickup_location:
                        loadActivity(context, ProfileAddressActivity.class);break;

                    default:showToast(DrawerActivity.this, "Error running");
                }
                return true;
            }
        });
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        onCreateDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if(mToggle.onOptionsItemSelected(item))
            return true;

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    void checkForNetConnection()
    {
        if(!isInternetAvailable())
        {
            showNoInternetAlert();
        }
    }

    boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        // There are no active networks.
        return ni != null && ni.isConnected();

    }

    public void showNoInternetAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);

        // Setting Dialog Title
        alertDialogBuilder.setTitle("Network unavailable");

        // Setting Dialog Message
        alertDialogBuilder.setMessage("Internet is not available. Please connect to the internet");

        // On pressing Settings button
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                finish();
            }
        });

        dialog = alertDialogBuilder.show();
    }


    @Override
    public void networkAvailable() {
        if(dialog !=null)
            dialog.dismiss();
    }

    @Override
    public void networkUnavailable() {
        showNoInternetAlert();
    }
}
