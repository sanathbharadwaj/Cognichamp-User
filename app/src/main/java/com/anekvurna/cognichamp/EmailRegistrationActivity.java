package com.anekvurna.cognichamp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import static com.anekvurna.cognichamp.SanathUtilities.*;

public class EmailRegistrationActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_registration);
    }

    public void registerUser(View view)
    {
        setProgressBar(this, true, "Registering...");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = getStringFromEditText(R.id.email);
        String password = getStringFromEditText(R.id.password);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        setProgressBar(EmailRegistrationActivity.this, false, "Loading...");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendVerificationEmail();
                            /*SanathUtilities.loadActivityAndFinish(EmailRegistrationActivity.this,
                                    UserChoiceActivity.class);*/
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(EmailRegistrationActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void sendVerificationEmail()
    {
        setProgressBar(this, false, "Sending verification Email...");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //if(user== null) return;
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setProgressBar(EmailRegistrationActivity.this, false, "Loading...");
                        if (task.isSuccessful()) {
                            // email sent
                            Toast.makeText(EmailRegistrationActivity.this,
                                    "A verification message has been sent to your email", Toast.LENGTH_LONG)
                                    .show();
                            // after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                            loadActivityAndClearStack(EmailRegistrationActivity.this, UserChoiceActivity.class);
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do

                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                    }
                });
    }

    public String getStringFromEditText(int id)
    {
        EditText et = findViewById(id);
        return et.getText().toString();
    }

}
