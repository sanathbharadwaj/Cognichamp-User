package com.anekvurna.userapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.anekvurna.userapp.SanathUtilities.*;

public class ProfileAddressActivity extends AppCompatActivity {

    private int place_picker_request;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_address);
        place_picker_request = 1;
        initializeCurrentUser();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        databaseReference = FirebaseDatabase.getInstance().getReference("customerProfiles").
                child(currentUser.getUid()).child("addressTag");
        requestPlace();
    }

    void requestPlace()
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), place_picker_request);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == place_picker_request) {
            if (resultCode == RESULT_OK) {
                setProgressBar(this, true, "Working...");
                Place place = PlacePicker.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                MyLocation myLocation = new MyLocation(latLng.latitude, latLng.longitude);
                databaseReference.setValue(myLocation, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        setProgressBar(ProfileAddressActivity.this, false, "Loading...");
                        if(databaseError != null)
                        {
                            showToast(ProfileAddressActivity.this, databaseError.getMessage());
                            showToast(ProfileAddressActivity.this, "Try again");
                            requestPlace();
                            return;
                        }
                        showToast(ProfileAddressActivity.this, "Location saved Successfully");
                        initializeSharedPrefs(ProfileAddressActivity.this);
                        setProfileStatus(3);
                        editor.putInt("profileStatus", 3);
                        editor.apply();
                        loadActivityAndClearStack(ProfileAddressActivity.this, MapsActivity.class);
                    }
                });
            }
        }
    }

    void setProfileStatus(int i)
    {
        DatabaseReference profileStatusReference = FirebaseDatabase.getInstance().getReference("customers")
                .child(currentUser.getUid()).child("profileStatus");
        profileStatusReference.setValue(i);
    }

    @Override
    public void onBackPressed() {
        //Disabled
    }
}
