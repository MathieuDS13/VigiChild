package com.example.vigichild.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.vigichild.R;
import com.example.vigichild.core.LaunchingApp;
import com.example.vigichild.core.SelectModeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ProgressBar loadingProgressBar;
    FirebaseAuth mAuth;
    private final String TAG = "RegisterActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        final EditText usernameEditText = findViewById(R.id.username_register);
        final EditText passwordEditText = findViewById(R.id.password_register);
        final Button goToLogin = findViewById(R.id.change_to_login);
        final Button register = findViewById(R.id.validate_register);
        loadingProgressBar = findViewById(R.id.loading_register);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String mail = extras.getString("mail");
            usernameEditText.setText(mail);
        }

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                intent.putExtra("mail", usernameEditText.getText().toString());
                v.getContext().startActivity(intent);
            }
        });

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                register.setEnabled(loginFormState.isDataValid());
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

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                registerUser(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
    }

    private void registerUser(String toString, String toString1) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            mAuth.createUserWithEmailAndPassword(toString, toString1)
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
                                switchToNextActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("signInWithEmail:failure", task.getException());
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
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void switchToNextActivity() {
        Intent intent = new Intent(this, SelectModeActivity.class);
        startActivity(intent);
    }
}
