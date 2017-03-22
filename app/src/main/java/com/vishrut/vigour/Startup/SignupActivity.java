package com.vishrut.vigour.Startup;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.vishrut.vigour.FireBase.ChatHelper;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;
import com.vishrut.vigour.ui.Tracking.StartTrackActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private SignupActivity signupActivity;
    private EditText SignUpName;
    private EditText SignUpEmail;
    private EditText SignUpPassword;
    private Button SignUpButton;
    private ProgressBar SignUpProgressBar;
    private Button SignUpCancel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Hide action bar
        // this.getActionBar().hide();// Handel this carefully


        SignUpName = (EditText) findViewById(R.id.sign_up_name);
        SignUpEmail = (EditText) findViewById(R.id.sign_up_email);
        SignUpPassword = (EditText) findViewById(R.id.sign_up_password);
        SignUpButton = (Button) findViewById(R.id.sign_up_button);
        SignUpProgressBar = (ProgressBar) findViewById(R.id.sign_up_progress);
        SignUpCancel = (Button) findViewById(R.id.sign_up_cancel);
        //final ProgressBar progressBar = (ProgressBar) findViewById(R.id.sign_up_progress);


        // Register user click listener
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpProgressBar.setVisibility(View.VISIBLE);

                /* validate input text */

                // Get name, email and password
                String userName = SignUpName.getText().toString();
                String userEmail = SignUpEmail.getText().toString();
                String userPassword = SignUpPassword.getText().toString();

                // Omit space
                userName = userName.trim();
                userEmail = userEmail.trim();
                userPassword = userPassword.trim();

                if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                    // Show message when field is empty
                    showErrorMessageToUser(getString(R.string.register_error_message));
                    SignUpProgressBar.setVisibility(View.VISIBLE);


                } else {
                    SignUpProgressBar.setVisibility(View.VISIBLE);

                    /* Create new user and allow user to log in if successfully created*/

                    // note from Firebase: Creating an account will not log that new account in
                    // so you have to log user in automatically when account is successfully created

                    final Firebase registerMChatUser = new Firebase(ReferenceUrl.FIREBASE_CHAT_URL);  // Get app main firebase url
                    final String finalUserEmail = userEmail;
                    final String finalUserPassword = userPassword;
                    final String finalUserName = userName;

                    // Create new user
                    registerMChatUser.createUser(userEmail, userPassword, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {

                            /* User registered successfully, so Log in automatically */

                            // Show a toast message for successfully registration
                            Toast.makeText(SignupActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();

                            // Note from firebase: Creating an account will not log that new account in.
                            // Successfully created user account, and log the user in automatically

                            registerMChatUser.authWithPassword(finalUserEmail, finalUserPassword, new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {

                                    // Store user data necessary for the chat app

                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put(ReferenceUrl.KEY_PROVIDER, authData.getProvider()); // The authentication method used
                                    map.put(ReferenceUrl.KEY_NAME, finalUserName);   // User name
                                    map.put(ReferenceUrl.KEY_USER_EMAIL, (String) authData.getProviderData().get(ReferenceUrl.KEY_EMAIL)); // User email address
                                    map.put(ReferenceUrl.CHILD_CONNECTION, ReferenceUrl.KEY_ONLINE);  // User status
                                    map.put(ReferenceUrl.KEY_AVATAR_ID, ChatHelper.generateRandomAvatarForUser()); // User avatar id

                                    // Time user date is stored in database
                                    long createTime = new Date().getTime();
                                    map.put(ReferenceUrl.KEY_TIMESTAMP, String.valueOf(createTime)); // Timestamp is string type

                                    // Store user data in the path https://<YOUR-FIREBASE-APP>.firebaseio.com/users/<uid>,
                                    // where users/ is any arbitrary path to store user data, and <uid> represents the
                                    // unique id obtained from the authentication data
                                    registerMChatUser.child(ReferenceUrl.CHILD_USERS).child(authData.getUid()).setValue(map);


                                    // After storing, go to main activity
                                    Intent intent = new Intent(SignupActivity.this, StartTrackActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }

                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                    // There is an error, and close the screen
                                    Toast.makeText(SignupActivity.this, "An error occurred!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });

                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // There is an error in creating a user
                            //Log.e(TAG, "error creating user");
                            showErrorMessageToUser(firebaseError.getMessage());
                        }
                    });


                }
//                progressBar.setVisibility(View.GONE);

                //   SignUpProgressBar.setVisibility(View.GONE);


            }
        });

        // Cancel registration, and go to LogIn screen
        SignUpCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //  SignUpProgressBar.setVisibility(View.GONE);
    }

    private void showErrorMessageToUser(String errorMessage) {
        //Create an AlertDialog to show error message
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setMessage(errorMessage)
                .setTitle(getString(R.string.login_error_title))
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
