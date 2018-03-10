package com.anekvurna.userapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import static com.anekvurna.userapp.SanathUtilities.*;

import java.util.List;

public class MapsActivity extends DrawerActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean first = true;
    private ValueEventListener listener;
    private DatabaseReference locationReference;
    Marker marker;
    boolean showDropLocation = false;
    private LatLng dropLatLng;
    private String driverMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //locationReference = FirebaseDatabase.getInstance().getReference("drivers").child("WfbgCN1hcEUjfDZSs17lcSFrBWD2").child("location");
        initializeCurrentUser();
        storeInstallationId();
        saveToken();
        checkForTracking();
        checkForDropLocationView();
        getPickUpLocation();
    }

    private void checkForDropLocationView() {
        Intent intent = getIntent();
        if(intent == null || !intent.getBooleanExtra("droppedPassenger", false))
            return;
        showDropLocation = true;
        double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        double longitude = Double.parseDouble(intent.getStringExtra("longitude"));

        dropLatLng = new LatLng(latitude, longitude);

    }

    private void checkForTracking() {
        Intent intent = getIntent();
        if(intent == null || !intent.getBooleanExtra("trackDriver", false))
            return;
        checkForTripRunning(intent.getStringExtra("driverId"));
    }

    private void checkForTripRunning(final String driverId) {
        final DatabaseReference trackableReference = FirebaseDatabase.getInstance().getReference("trackables")
                .child(currentUser.getUid());
        trackableReference.keepSynced(true);
        trackableReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isRunning = dataSnapshot.child(driverId).getValue(Boolean.class);
                if(isRunning == null || !isRunning)
                {
                    showToast(MapsActivity.this, "Ride has been ended");
                    return;
                }
                findViewById(R.id.track_driver).setVisibility(View.GONE);
                findViewById(R.id.tracking_driver_card).setVisibility(View.VISIBLE);
                getDriverDetails(driverId);
                getDriverLocation(driverId);
                trackableReference.keepSynced(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverDetails(final String driverId) {
        DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(driverId).child("basic");
        driverReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                driverMobile = dataSnapshot.child("mobile").getValue(String.class);
                setDriverDetails(name, driverMobile);
                loadDriverImage(driverId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadDriverImage(String driverId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("driverProfiles").child(driverId)
                .child("driverImage.jpg");

        ImageView driverImage = findViewById(R.id.driver_dp);

         Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(storageRef)
                 .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(driverImage);
    }

    private void setDriverDetails(String name, String mobile) {
        getTextView(R.id.driver_name).setText(name);
        getTextView(R.id.driver_mobile).setText(mobile);
    }

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }

    private void saveToken() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens").child(currentUser.getUid());
        reference.setValue(FirebaseInstanceId.getInstance().getToken());
    }

    public void callDriver(View view)
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.setData(Uri.parse("tel:" + driverMobile));
        startActivity(intent);
    }

    private void getDriverLocation(String driverId) {
        if(listener != null && locationReference != null)
            locationReference.removeEventListener(listener);
        locationReference = FirebaseDatabase.getInstance().getReference("drivers").child(driverId).child("location");
        listener = locationReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyLocation location = dataSnapshot.getValue(MyLocation.class);
                if(location == null) return;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if(marker != null) marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker at Driver Location"));
                if(first) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    first = false;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void getPickUpLocation()
    {
        DatabaseReference pickupReference = FirebaseDatabase.getInstance().getReference("customerProfiles").child(currentUser.getUid())
                .child("addressTag");
        pickupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyLocation myLocation = dataSnapshot.getValue(MyLocation.class);
                if(myLocation == null) {
                    showToast(MapsActivity.this, "Could not retrieve pickup location");
                    return;
                }
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                moveCameraToPickUpLocation(latLng);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void moveCameraToPickUpLocation(LatLng latLng)
    {
        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("PickUp Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void storeInstallationId() {
        final String insId = ParseInstallation.getCurrentInstallation().getInstallationId();
        ParseQuery<ParseObject> query = new ParseQuery<>("FirebaseUser");
        query.whereEqualTo("userId", currentUser.getUid());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!= null) return;
                if(objects.size()!=0) {
                    objects.get(0).put("insId", insId);
                    objects.get(0).saveEventually();
                }
                else {
                    ParseObject parseObject = new ParseObject("FirebaseUser");
                    parseObject.put("userId", currentUser.getUid());
                    parseObject.put("insId", insId);
                    parseObject.saveEventually();
                }
            }
        });

    }

    public void onTrackDriver(View view)
    {
        loadActivity(this, DriverListActivity.class);
       /* AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.list_name_et);

        dialogBuilder.setTitle("Track Driver");
        dialogBuilder.setMessage("Driver phone");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                trackDriver(editText.getText().toString());
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();*/
    }

    private void trackDriver(String mobile) {
        DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference("users");
        driverReference.orderByChild("mobile").equalTo(mobile).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            User user = dataSnapshot1.getValue(User.class);
                            if (user == null) {
                                showToast(MapsActivity.this, "Driver not found");
                                return;
                            }
                            String driverId = dataSnapshot1.getKey();
                            getDriverLocation(driverId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showToast(MapsActivity.this, databaseError.getMessage());
                    }
                });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        if(showDropLocation)
        {
            marker = mMap.addMarker(new MarkerOptions().position(dropLatLng).title("Drop Location"));
        }

    }


}
