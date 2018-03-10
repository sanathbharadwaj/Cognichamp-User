package com.anekvurna.userapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.anekvurna.userapp.SanathUtilities.setProgressBar;


public class DriverListActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    List<LocalDriver> localDrivers;
    RecyclerView recyclerView;
    MyDriversAdapter myDriversAdapter;
    private PopupWindow popupWindow;
    private View popupView;
    public FirebaseUser currentUser;
    private DatabaseReference listRef;
    private DatabaseReference driverRef;
    private boolean isEditing;
    private int editPosition;
    public boolean startedTrip = false;
    static final int PICK_CONTACT=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        localDrivers = new ArrayList<>();
        initializeDatabaseReference();
        myDriversAdapter = new MyDriversAdapter(localDrivers, this);
        recyclerView.setAdapter(myDriversAdapter);
        setTitle("My Drivers");
        loadDrivers();
    }

    void initializeDatabaseReference()
    {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        listRef = FirebaseDatabase.getInstance().getReference("driverLists")
                .child(currentUser.getUid());
        //TODO: change to user profiles
        driverRef = FirebaseDatabase.getInstance().getReference("users");
    }

    void loadDrivers()
    {
        setProgressBar(this, true, "Loading...");
        localDrivers.clear();
        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setProgressBar(DriverListActivity.this, false, "Loading...");
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    DriverList driverList = dataSnapshot1.getValue(DriverList.class);
                    putUserDetails(driverList);
                }
                setDriversTrackable();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load data");
            }
        });


    }

    private void setDriversTrackable() {
            DatabaseReference trackReference = FirebaseDatabase.getInstance().getReference("trackables")
                    .child(currentUser.getUid());
            trackReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        boolean trackable = (boolean) dataSnapshot1.getValue();
                        LocalDriver localDriver = findLocalDriver(dataSnapshot1.getKey());
                        if(trackable) {
                            if(localDriver != null)
                            localDriver.setCurrent(true);
                        }
                        else
                        {
                            if(localDriver != null)
                                localDriver.setCurrent(false);
                        }
                    }
                    myDriversAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    LocalDriver findLocalDriver(String userId)
    {
        for(LocalDriver localDriver : localDrivers)
        {
            if(userId.equals(localDriver.getUserId()))
                return localDriver;
        }
        return null;
    }

    void putUserDetails(final DriverList driverList) {
        if (driverList == null) return;
        //TODO: change to user profiles
        LocalDriver localDriver = new LocalDriver(driverList.getUsername(), driverList.getMobile(), driverList.getUserId(), driverList.getElementId());
        localDrivers.add(localDriver);
        myDriversAdapter.notifyItemInserted(localDrivers.size() - 1);
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onAddNew(View view) {
        showPopup();
    }

    void showPopup()
    {
        isEditing = false;
        // get a reference to the already created main layout
        ConstraintLayout mainLayout = (ConstraintLayout)
                findViewById(R.id.user_list_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.BOTTOM, 0, 0);

    }

    public void pickContact(View view)
    {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c =  managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {


                        String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            cNumber = cNumber.replaceAll("\\s","");
                            int l = cNumber.length();
                            if(l<10){
                                showToast("Invalid phone number");
                                break;
                            }
                            String tenDigit = cNumber.substring(l-10, l);
                            getPopUpEditText(R.id.popup_mobile).setText(tenDigit);

                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        getPopUpEditText(R.id.popup_name).setText(name);
                    }
                }
                break;
        }
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }


    public void onAdd(View view)
    {
        showToast("Adding driver...");

        final String userName = getPopUpEditText(R.id.popup_name).getText().toString();
        final String mobile = getPopUpEditText(R.id.popup_mobile).getText().toString();
        popupWindow.dismiss();

        driverRef.orderByChild("mobile").equalTo(mobile).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isEmpty = true;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    User user = dataSnapshot1.getValue(User.class);
                    if (user == null) {
                        showToast("No driver with this mobile number");
                        return;
                    }
                    isEmpty = false;

                    String userId = dataSnapshot1.getKey();
                    String elementId;
                    if(isEditing)
                    {
                        elementId = localDrivers.get(editPosition).getElementId();
                    }
                    else
                        elementId = listRef.push().getKey();
                    DriverList driverList = new DriverList(userName, mobile, elementId, userId);
                    listRef.child(elementId).setValue(driverList);

                    LocalDriver localDriver = new LocalDriver(userName, mobile, userId, elementId);
                    if(isEditing)
                    {
                        editUser(localDriver);
                        return;
                    }
                    localDrivers.add(localDriver);
                    myDriversAdapter.notifyItemInserted(localDrivers.size() - 1);
                }
                if(isEmpty){
                    showToast("No driver with this mobile number");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void editUser(LocalDriver localDriver)
    {
        localDrivers.set(editPosition, localDriver);
        myDriversAdapter.notifyItemChanged(editPosition);
        isEditing = false;
    }

    public void deleteUser(final int position) {
        //TODO: change to user profiles
        final DatabaseReference listReference = FirebaseDatabase.getInstance().getReference("driverLists")
                .child(currentUser.getUid());
        listReference.child(localDrivers.get(position).getElementId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null)
                    databaseError.getMessage();
                else
                {
                    localDrivers.remove(position);
                    myDriversAdapter.notifyItemRemoved(position);
                }
            }
        });
    }


    public void showEditingPopup(LocalDriver localDriver, int position)
    {
        showPopup();
        getPopUpEditText(R.id.popup_mobile).setText(localDriver.getMobile());
        getPopUpEditText(R.id.popup_name).setText(localDriver.getName());
        isEditing = true;
        editPosition = position;
    }


    public void onCancel(View view)
    {
        popupWindow.dismiss();
    }

    EditText getPopUpEditText(int id)
    {
        return (EditText)popupView.findViewById(id);
    }

}
