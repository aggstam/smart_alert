// -------------------------------------------------------------
//
// This is the main Activity, used to authenticate users before
// using the application.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginInit();
    }

    // Activity initialization method.
    // If user is already authenticated, user is redirected to SmartAlertActivity.
    private void loginInit() {
        Log.i("message","LoginInit method started.");
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                Log.i("message","User is already logged in.");
                ((EditText) findViewById(R.id.emailEditText)).setText(user.getEmail());
                Intent intent = new Intent(this, SmartAlertActivity.class);
                startActivity(intent);
            }
            findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginAction();
                }
            });
            findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signUpAction();
                }
            });
            findViewById(R.id.passwordResetButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passwordResetAction();
                }
            });
            String code = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0).getLanguage();
            if (code.equals("el")) {
                ((RadioButton) findViewById(R.id.elRadioButton)).setChecked(true);
            } else if (code.equals("ru")) {
                ((RadioButton) findViewById(R.id.ruRadioButton)).setChecked(true);
            } else {
                ((RadioButton) findViewById(R.id.enRadioButton)).setChecked(true);
            }
            setLocale(code);
            ((RadioGroup) findViewById(R.id.languageRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    localeChangeAction(group, checkedId);
                }
            });
            Log.i("message","LoginInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during LoginInit method:" + e.getMessage());
            Toast.makeText(this, R.string.exception, Toast.LENGTH_SHORT).show();
        }
    }

    // User Firebase login method.
    // On successful login, user is redirected to SmartAlertActivity.
    private void loginAction() {
        Log.i("message","LoginAction method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            if (checkNetworkProvider() && validateFields()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(((EditText) findViewById(R.id.emailEditText)).getText().toString(), ((EditText) findViewById(R.id.passwordEditText)).getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Log.i("message","Login action was successful!");
                                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.login_activity_login_success), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), SmartAlertActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.login_activity_login_unverified), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.i("message","Login action was unsuccessful. Error:" + task.getException().getMessage());
                                    Toast.makeText(getApplicationContext(), getString(R.string.login_activity_login_unsuccessful) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
                            }
                        });
            } else {
                findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            }
            Log.i("message","LoginAction method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during LoginAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // New Firebase user creation.
    // New users must verify their email address to validate their account.
    private void signUpAction() {
        Log.i("message","SignUpAction method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            if (checkNetworkProvider() && validateFields()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(((EditText) findViewById(R.id.emailEditText)).getText().toString(), ((EditText) findViewById(R.id.passwordEditText)).getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Log.i("message","Sign up action was successful!");
                                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                    Toast.makeText(getApplicationContext(), getString(R.string.login_activity_signup_success), Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.i("message","Sign up action was unsuccessful. Error:" + task.getException().getMessage());
                                    Toast.makeText(getApplicationContext(), getString(R.string.login_activity_signup_unsuccessful) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
                            }
                        });
            } else {
                findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            }
            Log.i("message","SignUpAction method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during SignUpAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Firebase user password reset.
    // An email is sent to user's mail address with instructions to change their password.
    private void passwordResetAction() {
        Log.i("message","PasswordResetAction method started.");
        try {
            findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
            EditText emailEditText = findViewById(R.id.emailEditText);
            if (emailEditText.getText().toString().trim().length() == 0) {
                emailEditText.setError(getString(R.string.login_activity_validation_error_email));
            } else if (checkNetworkProvider()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailEditText.getText().toString());
                Toast.makeText(getApplicationContext(), getString(R.string.login_activity_pass_reset_success), Toast.LENGTH_SHORT).show();
            }
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            Log.i("message","PasswordResetAction method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during PasswordResetAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
        }
    }

    // Validates user's submitted email and password.
    // Fields cannot be empty.
    private Boolean validateFields() {
        Boolean valid = true;
        EditText emailEditText = findViewById(R.id.emailEditText);
        if (emailEditText.getText().toString().trim().length() == 0) {
            emailEditText.setError(getString(R.string.login_activity_validation_error_email));
            valid = false;
        }
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        if (passwordEditText.getText().toString().trim().length() == 0) {
            passwordEditText.setError(getString(R.string.login_activity_validation_error_password));
            valid = false;
        }
        return valid;
    }

    // Check network provider availability.
    // On disabled network, user is informed with a toast message.
    private Boolean checkNetworkProvider() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null || cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(this, getString(R.string.internet_provider_disabled), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Retrieves user's locale selection and applies the new Locale.
    private void localeChangeAction(RadioGroup group, int checkedId) {
        Log.i("message","LocaleChangeAction method started.");
        try {
            RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
            if (radioButton.isChecked()) {
                String code = getResources().getResourceName(radioButton.getId()).split("/")[1].substring(0, 2);
                setLocale(code);
                Log.i("message", "Locale changed to: " + code);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
            Log.i("message","LocaleChangeAction method started.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during LocaleChangeAction method:" + e.getMessage());
            Toast.makeText(this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
        }
    }

    // Changes application Locale.
    private void setLocale(String code) {
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    // On activity restart, user is unauthenticated and has to re-login.
    @Override
    protected void onRestart() {
        super.onRestart();
        ((EditText) findViewById(R.id.passwordEditText)).getText().clear();
        FirebaseAuth.getInstance().signOut();
    }

}
