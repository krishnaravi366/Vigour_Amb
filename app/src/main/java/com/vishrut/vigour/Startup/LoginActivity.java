package com.vishrut.vigour.Startup;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;
import com.vishrut.vigour.ui.Tracking.StartTrackActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "MEHHHH";
    private SignupActivity signupActivity;
    private EditText SignInEmail;
    private EditText SignInPassword;
    private Button SignInButton;
    private Button SignInCancel;
    private ProgressBar ProgressBar;

    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public LoginActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent=new Intent(LoginActivity.this, StartTrackActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        SignInEmail = (EditText) findViewById(R.id.sign_in_email);
        SignInPassword = (EditText) findViewById(R.id.sign_in_password);
        SignInButton = (Button) findViewById(R.id.sign_in_button);
        ProgressBar = (ProgressBar) findViewById(R.id.sign_in_progress);
        SignInCancel =(Button)findViewById(R.id.sign_in_cancel);


        // Log In click listener
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressBar.setVisibility(View.VISIBLE);

                /* Validate input text */

                // Get user email and password
                String userName = SignInEmail.getText().toString();
                String passWord = SignInPassword.getText().toString();

                // Omit space
                userName = userName.trim();
                passWord = passWord.trim();

                // validate fields
                if (userName.isEmpty() || passWord.isEmpty()) {
                    // show message when field is empty
                    showErrorMessageToUser(getString(R.string.login_error_message));

                } else {
                    // Log in
                    ProgressBar.setVisibility(View.VISIBLE);
                    DatabaseReference authenticateUser = mDatabase.getReference(); // Get app main firebase url
                    mAuth.signInWithEmailAndPassword(userName, passWord)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "signInWithEmail", task.getException());
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    // ...
                                }
                            });
                }
                //ProgressBar.setVisibility(View.GONE);
            }
        });
        SignInCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, HomePageFragment.class));
            }
        });


    }



    private void showErrorMessageToUser(String errorMessage){
        // Create an AlertDialog to show error message
        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(errorMessage)
                .setTitle(getString(R.string.login_error_title))
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}