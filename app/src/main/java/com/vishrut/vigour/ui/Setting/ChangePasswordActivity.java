package com.vishrut.vigour.ui.Setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextNewPass, editTextConfirmNewPass;
    private Button buttonChangePassword;
    private DatabaseReference mFireBaseRef;

    private String mCurrentUserID, mCurrentUserEmail;
    private ProgressBar progressBarChangePass;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);




        //editTextEmail = (EditText) findViewById(R.id.editTextChangePassEmail);
        editTextNewPass = (EditText) findViewById(R.id.editTextChangeNewPassword);
        editTextConfirmNewPass = (EditText) findViewById(R.id.editTextChangeConfirmNewPassword);
        buttonChangePassword = (Button) findViewById(R.id.buttonChangePassword);
        progressBarChangePass = (ProgressBar) findViewById(R.id.progressBarChangePassword);

        mFireBaseRef = FirebaseDatabase.getInstance().getReference();

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarChangePass.setVisibility(View.VISIBLE);


                String newPass = editTextNewPass.getText().toString();
                String confirmNewPass = editTextConfirmNewPass.getText().toString();

                if(newPass.isEmpty()||confirmNewPass.isEmpty()){
                    progressBarChangePass.setVisibility(View.GONE);
                    // Show message when field is empty
                    AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this).setMessage("Please make sure that you filled all the fields")
                            .setPositiveButton("OK",null).create();
                    alertDialog.show();
                }else {

                    if (newPass.equals(confirmNewPass)) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updatePassword(newPass)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("PasswordChange", "User password updated.");
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

}
