package com.anekvurna.userapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.anekvurna.userapp.SanathUtilities.loadActivity;
import static com.anekvurna.userapp.SanathUtilities.loadActivityAndFinish;
import static com.anekvurna.userapp.SanathUtilities.setProgressBar;

public class ProfileBasicActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private EditText name;
    private EditText email;
    private EditText alternate;
    private EditText landline;
    private EditText stdCode;
    boolean uDriverImage = true;
   /* static final int PICK_IMAGE = 2;
    final int PIC_CROP = 1;*/
    private BasicProfile basicProfile;
    /*final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    private int buttonId;
    Bitmap driverImage;
*/
   /* private int totalImagesUploaded;*/
    boolean isPreviousProfile = false;
   /* private Uri uri;*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_basic);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        initializeEditTexts();
        checkForPreviousProfile();
        name = getEditText(R.id.profile_name);
        email = getEditText(R.id.profile_email);
        alternate = getEditText(R.id.profile_alternate_mobile);
        landline = getEditText(R.id.profile_landline);
        stdCode = getEditText(R.id.profile_std_code);
        showEditTextsAsMandatory(name, email, alternate);

    }

    private void initializeEditTexts() {
        //TODO: store edit texts to temporary
    }

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference(getString(R.string.customer_profiles)).child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("basic"))
                {
                    uDriverImage = false;
                    isPreviousProfile = true;
                    populateData(dataSnapshot.child("basic").getValue(BasicProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showEditTextsAsMandatory ( EditText... ets )
    {
        for ( EditText et : ets )
        {
            String hint = et.getHint ().toString ();

            et.setHint ( Html.fromHtml ( "<font color=\"#ff0000\">" + "* " + "</font>" + hint ) );
        }
    }

    private void populateData(BasicProfile basicProfile) {
        name.setText(basicProfile.getName());
        email.setText(basicProfile.getEmail());
        alternate.setText(basicProfile.getAlternateNumber());
        stdCode.setText(basicProfile.getStdCode());
        landline.setText(basicProfile.getLandline());



    }

    public void onSaveBasic(View view)
    {
        if(!isValid()) return;
        basicProfile = new BasicProfile();
        basicProfile.setName(name.getText().toString());
        basicProfile.setEmail(email.getText().toString());
        basicProfile.setAlternateNumber(alternate.getText().toString());
        basicProfile.setLandline(landline.getText().toString());
        basicProfile.setStdCode(stdCode.getText().toString());
            register();
    }

    void register()
    {
        setProgressBar(this, true, "Saving...");
        databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.customer_profiles))
                .child(currentUser.getUid()).child("basic");
        databaseReference.setValue(basicProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setProgressBar(ProfileBasicActivity.this, false, "Loading...");
                if(databaseError!=null){
                    showToast("Error saving data!");
                    showToast(databaseError.getMessage());
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.userapp", MODE_PRIVATE).edit();
                Intent intent  = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 1);
                    editor.apply();
                    setProfileStatus(1);
                    loadActivity(ProfileBasicActivity.this, ProfileManualAddressActivity.class);
                }
                else
                {
                    finish();
                }
            }
        });
    }

    void setProfileStatus(int i)
    {
        DatabaseReference profileStatusReference = FirebaseDatabase.getInstance().getReference("customers")
                .child(currentUser.getUid()).child("profileStatus");
        profileStatusReference.setValue(i);
    }



    boolean isValid()
    {
        final int MOBILE_LIMIT = 10, LANDLINE_LIMIT = 11;
        boolean valid = true;
        if(getEditText(R.id.profile_name).getText().toString().equals(""))
        {
            showToast("Please enter valid name");
            valid = false;
        }

        String email = getEditText(R.id.profile_email).getText().toString();
        if(email.equals("") || !emailValidator(email))
        {
            showToast("Please enter valid email");
            valid = false;
        }
        String alternateMobile = getEditText(R.id.profile_alternate_mobile).getText().toString();
        if(alternateMobile.equals("") || alternateMobile.length() != MOBILE_LIMIT )
        {
            showToast("Please enter valid alternate mobile");
            valid = false;
        }
        String stdCode, landline;
        stdCode = getEditText(R.id.profile_std_code).getText().toString();
        landline = getEditText(R.id.profile_landline).getText().toString();
        if(stdCode.equals("") || landline.equals("") || stdCode.length() + landline.length() != LANDLINE_LIMIT)
        {
            showToast("Please enter valid landline");
            valid = false;
        }


        return valid;
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }


}
