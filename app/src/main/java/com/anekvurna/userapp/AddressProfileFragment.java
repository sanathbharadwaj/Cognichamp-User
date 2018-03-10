package com.anekvurna.userapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.anekvurna.userapp.SanathUtilities.*;

//TODO: Implement in DRY fashion
public class AddressProfileFragment extends Fragment {

    private View inflatedView;

    public AddressProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_address_profile, container, false);
        //TextView textView = inflatedView.findViewById(R.id.view_name);
        //textView.setText("My Profile");
        return inflatedView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProfileDetails();
    }

    TextView getTextView(int id) {
        return inflatedView.findViewById(id);
    }

    void getProfileDetails() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customerProfiles").child(currentUser.getUid()).child("address");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AddressProfile addressProfile = dataSnapshot.getValue(AddressProfile.class);
                setProfileDetails(addressProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error loading data");
            }
        });
    }

    /* private void loadUserImage() {
         if (getContext() == null) {
             showToast("Failed to load image");
             return;
         }
         StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("driverImage.jpg");
         Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).into(getImageView(R.id.view_image));
     }
 */
    private void setProfileDetails(AddressProfile addressProfile) {
        getTextView(R.id.view_line1).setText(addressProfile.getAddressLine1());
        getTextView(R.id.view_line2).setText(addressProfile.getAddressLine2());
        if(addressProfile.getAddressLine2() == null || addressProfile.getAddressLine2().equals(""))
            getTextView(R.id.view_line2).setVisibility(View.GONE);
        getTextView(R.id.view_city).setText(addressProfile.getCity());
        String[] states = getResources().getStringArray(R.array.india_states);
        getTextView(R.id.view_state).setText(states[addressProfile.getState()]);
        getTextView(R.id.view_pin_code).setText(addressProfile.getPinCode());
    }

    void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
