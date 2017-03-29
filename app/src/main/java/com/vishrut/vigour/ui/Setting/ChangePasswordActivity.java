package com.vishrut.vigour.ui.Setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextOldPass, editTextNewPass, editTextConfirmNewPass;
    private Button buttonChangePassword;
    private Firebase mFireBaseRef;
    private Firebase.AuthStateListener mAuthStateListener;
    private String mCurrentUserID, mCurrentUserEmail;
    private ProgressBar progressBarChangePass;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        showInterstitial();
        mInterstitialAd.show();

        editTextOldPass = (EditText) findViewById(R.id.editTextChangeOldPassword);
        //editTextEmail = (EditText) findViewById(R.id.editTextChangePassEmail);
        editTextNewPass = (EditText) findViewById(R.id.editTextChangeNewPassword);
        editTextConfirmNewPass = (EditText) findViewById(R.id.editTextChangeConfirmNewPassword);
        buttonChangePassword = (Button) findViewById(R.id.buttonChangePassword);
        progressBarChangePass = (ProgressBar) findViewById(R.id.progressBarChangePassword);

        mFireBaseRef = new Firebase(ReferenceUrl.FIREBASE_DATABASE_URL);

        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    mCurrentUserEmail = (String) authData.getProviderData().get(ReferenceUrl.KEY_EMAIL);
                }
            }
        };
        mFireBaseRef.addAuthStateListener(mAuthStateListener);

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarChangePass.setVisibility(View.VISIBLE);
               /* mAuthStateListener = new Firebase.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(AuthData authData) {
                        mCurrentUserID = authData.getUid();
                        mCurrentUserEmail = (String) authData.getProviderData().get(ReferenceUrl.KEY_EMAIL);
                    }
                };
                mFireBaseRef.addAuthStateListener(mAuthStateListener);*/
                String oldPass = editTextOldPass.getText().toString();
                String newPass = editTextNewPass.getText().toString();
                String confirmNewPass = editTextConfirmNewPass.getText().toString();

                if(oldPass.isEmpty()||newPass.isEmpty()||confirmNewPass.isEmpty()){
                    progressBarChangePass.setVisibility(View.GONE);
                    // Show message when field is empty
                    AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this).setMessage("Please make sure that you filled all the fields")
                            .setPositiveButton("OK",null).create();
                    alertDialog.show();
                }else {

                    if (newPass.equals(confirmNewPass)) {
                        mFireBaseRef.changePassword(mCurrentUserEmail, editTextOldPass.getText().toString(), editTextNewPass.getText().toString(), new Firebase.ResultHandler() {
                            @Override
                            public void onSuccess() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                                builder.setMessage("Your password is changed.")
                                        .setTitle("Change Password")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                progressBarChangePass.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(FirebaseError firebaseError) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                                builder.setMessage(firebaseError.getMessage())
                                        .setTitle("Error")
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                progressBarChangePass.setVisibility(View.GONE);

                            }
                        });

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                        builder.setMessage("Password doesn't match.")
                                .setTitle("Change Password")
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        progressBarChangePass.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        showInterstitial();
        //super.onBackPressed();
        finish();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdClosed() {
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
          }
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }
}
