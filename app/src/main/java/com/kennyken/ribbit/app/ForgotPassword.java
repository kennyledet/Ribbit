package com.kennyken.ribbit.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class ForgotPassword extends Activity {
    protected EditText mEmailField;
    protected Button mPasswordResetBtn;

    String TAG = ForgotPassword.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordResetBtn = (Button) findViewById(R.id.passwordResetBtn);

        mPasswordResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailField.getText().toString();
                email = email.trim();

                if ( email.isEmpty() ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
                    builder.setMessage(R.string.forgot_pw_error_msg)
                            .setTitle(R.string.oops)
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {  // try to send password reset email
                    ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                        @Override
                        public void done(ParseException e) {
                            if ( e  == null ) {
                                Toast.makeText(ForgotPassword.this, R.string.pw_reset_success, Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.oops)
                                        .setPositiveButton(android.R.string.ok, null);

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });


    }



}
