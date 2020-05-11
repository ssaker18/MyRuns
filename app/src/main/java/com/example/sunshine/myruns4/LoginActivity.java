package com.example.sunshine.myruns4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sunshine.myruns4.utils.Preferences;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailTextInputView;
    private EditText mPasswordTextInputView;
    private LinearLayout mFormView;
    private ProgressBar mProgressBar;
    private TextInputLayout mPassWordLayout;
    private Preferences mPreferences;
    public String SOURCE = "Source";
    public static final String ACTIVITY_NAME = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Sign In");

        mEmailTextInputView = findViewById(R.id.email_input);
        mPasswordTextInputView = findViewById(R.id.password_input);
        mFormView = findViewById(R.id.form_login);
        mProgressBar = findViewById(R.id.progressBar);
        mPassWordLayout = findViewById(R.id.password_input_layout);

        mPreferences = new Preferences(this);

        setSignIn();// For simpler sign In
    }

    private void setSignIn() {
        mEmailTextInputView.setText(mPreferences.getEmail());
        mPasswordTextInputView.setText(mPreferences.getPassWord());
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFormView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /*
     * User can signIn on valid email & password to get to the MainActivity
     * Only fires off new intent to MainActivity, if intent is null
     * if intent came from RegisterActivity, we simply call finish()
     * returning to MainActivity by default
     */
    public void onSignInClicked(View view) {
        if (ValidatedSignIn()) {
            mFormView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = getIntent();
                    String source = intent.getStringExtra(SOURCE);

                    if (source != null) {
                        if (source.equals(RegisterActivity.ACTIVITY_NAME)) {
                            finish();
                        }
                    } else {
                        // launch intent to mainActivity
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(SOURCE, LoginActivity.ACTIVITY_NAME);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 600);

        }
    }

    public void onRegisterClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.putExtra(SOURCE, LoginActivity.ACTIVITY_NAME);
        startActivity(intent);
    }

    public boolean ValidatedSignIn() {
        return isEmailValid() && isPasswordValid();
    }

    private boolean isPasswordValid() {
        mPasswordTextInputView.setError(null);
        String password = mPasswordTextInputView.getText().toString().trim();

        if (password.isEmpty()) {
            mPassWordLayout.setError(getString(R.string.error_field_required));
            mPasswordTextInputView.requestFocus();
            return false;
        } else if (password.length() < 6) {
            mPassWordLayout.setError(getString(R.string.error_password_too_short));
            mPasswordTextInputView.requestFocus();
            return false;
        } else if (!isPasswordInSharedPreference(password)) {
            mPassWordLayout.setError(getString(R.string.error_invalid_password));
            mPasswordTextInputView.requestFocus();
            return false;
        } else {
            mPassWordLayout.setError(null);
            return true;
        }

    }

    /*
     * return Value:Is Password corresponding to email, in shared reference
     */
    private boolean isPasswordInSharedPreference(String password) {

        if (mPreferences.getPassWord().equals(password.trim())) {
            return true;
        } else {
            Toast.makeText(this, "Email or Password is Incorrect", Toast.LENGTH_SHORT).show();
            Log.d("Shared preference ", mPreferences.toString());
            return false;
        }
    }

    private boolean isEmailValid() {
        // grab email
        String email = mEmailTextInputView.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        TextInputLayout emailLayout = findViewById(R.id.email_input_layout);

        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.error_field_required));
            mEmailTextInputView.requestFocus();
            return false;
        } else if (!email.matches(emailPattern)) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            mEmailTextInputView.requestFocus();
            return false;
        } else if (!isEmailInSharedPreference(email)) {
            return false;
        } else {
            // valid email!
            emailLayout.setError(null);
            return true;
        }
    }

    private boolean isEmailInSharedPreference(String email) {
        if (mPreferences.getEmail().equals(email.trim())) {
            return true;
        } else {
            Toast.makeText(this, "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
            Log.d("Shared preference ", mPreferences.toString());
            return false;
        }
    }
}
