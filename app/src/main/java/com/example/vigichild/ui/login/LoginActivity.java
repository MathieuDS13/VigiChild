package com.example.vigichild.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.vigichild.R;
import com.example.vigichild.core.LaunchingApp;
import com.example.vigichild.core.SelectModeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private FirebaseAuth mAuth;
    private final String TAG = "LoginActivity";
    private ProgressBar loadingProgressBar;
    private final String PREF_FILE = "preferences";
    private final String MODE = "mode";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password_login);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading_login);
        final Button changeToRegisterButton = findViewById(R.id.swich_to_register);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String mail = extras.getString("mail");
            usernameEditText.setText(mail);
        }

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginUser(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        changeToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                intent.putExtra("mail", usernameEditText.getText().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    private void loginUser(String toString, String toString1) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            mAuth.signInWithEmailAndPassword(toString, toString1)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                LaunchingApp app = (LaunchingApp) getApplicationContext();
                                app.setCurrentUser(new LoggedInUser(user.getDisplayName(), user.getEmail(), user.getUid()));
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                                //TODO modifier si jamais le mode exise déjà ?
                                switchToNextActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("signInWithEmail:failure", task.getException());
                                Log.w("signInWithEmail:failure", "username " + toString + " pass " + toString1);
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        } else {
            Log.w("signInWithEmail:error", "User already logged in");
            LaunchingApp app = (LaunchingApp) getApplicationContext();
            app.setCurrentUser(new LoggedInUser(currentUser.getDisplayName(), currentUser.getEmail(), currentUser.getUid()));
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            String mode = sharedPreferences.getString(MODE, null);
            if (mode == null) {
                Log.w("launchingapp:failure", "Failed to retrieve mode");
                Intent intent = new Intent(this, SelectModeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void switchToNextActivity() {
        Intent intent = new Intent(this, SelectModeActivity.class);
        startActivity(intent);
        //TODO implémenter le switch vers l'activité de sélection de type (parent / enfant), penser à sauvegarder l'état et le n° d'enregistrement
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}