package com.anekvurna.userapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import static com.anekvurna.userapp.SanathUtilities.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class MobileVerificationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String mPhoneNumber;
    String mVerificationId = "";
    FirebaseUser currentUser;
    int profileStatus;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("customers");
        userReference.child(currentUser.getUid()).keepSynced(true);
        profileStatus = 0;
        populateMobileNumber();
    }

    void populateMobileNumber()
    {
        String phoneNumber = currentUser.getPhoneNumber();
        if(phoneNumber == null || phoneNumber.equals(""))
            return;
        int length = phoneNumber.length();
        String tenDigits = phoneNumber.substring(length-10,length);
        getEditText(R.id.mobile_number).setText(tenDigits);
    }




    public void onVerify(View view)
    {
        mPhoneNumber = getEditText(R.id.mobile_number).getText().toString();
        showToast("Automatically detecting SMS sent to your mobile");
        phoneAuthenticate("+91" + mPhoneNumber);
        //showPopup();
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showToast("Registering user...");
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showToast("Sign in failed. Invalid credential");
                            }
                        }
                    }
                });
    }

    void updatePhoneNumber(PhoneAuthCredential credential)
    {
        setProgressBar(this, true, "Working..");
        FirebaseUser user = mAuth.getCurrentUser();
        user.updatePhoneNumber(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setProgressBar(MobileVerificationActivity.this, false, "Loading...");
                        if (!task.isSuccessful()) {
                            showToast(task.getException().getMessage());
                            return;
                        }
                        fetchUserPreviousData();
                    }
                });
    }

    private void fetchUserPreviousData() {
        setProgressBar(MobileVerificationActivity.this, true, "Working...");
        userReference = FirebaseDatabase.getInstance().getReference("customers");
        userReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setProgressBar(MobileVerificationActivity.this, false, "Loading...");
                User user = dataSnapshot.getValue(User.class);
                if(user != null)
                    profileStatus = user.getProfileStatus();
                initializeSharedPrefs(MobileVerificationActivity.this);
                editor.putInt("profileStatus", profileStatus);
                editor.commit();
                saveUser();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error connecting with the server");
            }
        });
    }

    private void saveUser() {
        setProgressBar(this, true, "Working...");
        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null) return;
        String phoneNumber = mPhoneNumber;
        userReference.child(currentUser.getUid()).keepSynced(false);
        //String password = getEditText(R.id.password).getText().toString();
        User user1 = new User(phoneNumber.substring(phoneNumber.length() - 10, phoneNumber.length()), user.getEmail(), profileStatus);
        userReference.child(user.getUid()).setValue(user1, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //showToast("Verification successful");
                setProgressBar(MobileVerificationActivity.this, false, "Loading...");
                if(databaseError != null)
                {
                    showToast(databaseError.getMessage());
                    return;
                }
                loadToNextActivity();
            }
        });
    }


    void phoneAuthenticate(String phoneNumber)
    {
        setProgressBar(this, true, "Verifying SMS...");
        PhoneAuthProvider.OnVerificationStateChangedCallbacks myCallBacks;
        myCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                setProgressBar(MobileVerificationActivity.this, false, "Loading...");
                showToast("Successfully verified mobile number");
                updatePhoneNumber(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                setProgressBar(MobileVerificationActivity.this, false, "Loading...");
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    showToast("Verification failed invalid credential");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    showToast("Verification failed too many requests");
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                //showToast(verificationId);
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                myCallBacks
        );
    }


    public void verifyManually(View view)
    {
        String code = getEditText(R.id.manual).getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        //signInWithPhoneAuthCredential(credential);
    }

    void showPopup()
    {
        // get a reference to the already created main layout
        ConstraintLayout mainLayout = (ConstraintLayout)
                findViewById(R.id.user_list_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.manual_verification_popup, null);

        // create the popup window
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;// lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public EditText getEditText(int id)
    {
        return (EditText)findViewById(id);
    }


    public void loadToNextActivity()
    {
        initializeSharedPrefs(this);
        editor.putBoolean("mobileVerified", true);
        editor.apply();
        int profileStatus = preferences.getInt("profileStatus", 0);
        switch (profileStatus)
        {
            case 0 : loadActivityAndFinish(this, ProfileBasicActivity.class);break;
            case 1 : loadActivityAndFinish(this, ProfileAddressActivity.class);break;
            default: loadActivityAndFinish(this, MapsActivity.class);break;
        }
    }

    public void onOrLogin(View view)
    {
        loadActivityAndFinish(this, LogInActivity.class);
    }
}
