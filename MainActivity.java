package com.finance.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private Button signInButton, signUpButton;

    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Singleton instances
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initializeViews();
        checkRememberedUser();

        signInButton.setOnClickListener(v -> signIn());
        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });
    }

    private void initializeViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
    }

    private void checkRememberedUser() {
        boolean remember = preferenceManager.isRememberMeEnabled();
        if (remember) {
            String savedEmail = preferenceManager.getRememberedEmail();
            emailInput.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void signIn() {
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate
        boolean isValid = true;

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Enter your password");
            isValid = false;
        }

        if (!isValid) return;

        // Disable button to prevent multiple clicks
        signInButton.setEnabled(false);

        // Check login in background thread
        executorService.execute(() -> {
            boolean loginSuccess = databaseHelper.checkUserLogin(email, password);

            runOnUiThread(() -> {
                signInButton.setEnabled(true);

                if (loginSuccess) {
                    // Save remember me preference using Singleton
                    preferenceManager.saveRememberMe(email, rememberMeCheckbox.isChecked());

                    // Navigate to Dashboard
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    intent.putExtra("userEmail", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
