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

/**
 * MainActivity - Login screen
 * Demonstrates proper usage of Singleton patterns
 */
public class MainActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private Button signInButton, signUpButton;

    // Singleton instances
    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize singletons using getInstance()
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Initialize executor service for background operations
        executorService = Executors.newSingleThreadExecutor();

        initializeViews();
        checkRememberedUser();
        setupClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
    }

    /**
     * Check if user should be remembered
     * Uses PreferenceManager singleton
     */
    private void checkRememberedUser() {
        if (preferenceManager.isRememberMeEnabled()) {
            String savedEmail = preferenceManager.getRememberedEmail();
            emailInput.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
        }
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> signIn());
        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });
    }

    /**
     * Handle sign in
     * Performs validation and database check in background thread
     */
    private void signIn() {
        // Clear previous errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (!validateInput(email, password)) {
            return;
        }

        // Disable button to prevent multiple clicks
        signInButton.setEnabled(false);

        // Perform login check in background thread
        executorService.execute(() -> {
            // Database operation in background
            boolean loginSuccess = databaseHelper.checkUserLogin(email, password);

            // Update UI on main thread
            runOnUiThread(() -> {
                signInButton.setEnabled(true);

                if (loginSuccess) {
                    handleSuccessfulLogin(email);
                } else {
                    handleFailedLogin();
                }
            });
        });
    }

    /**
     * Validate input fields
     *
     * @param email User email
     * @param password User password
     * @return true if validation passes
     */
    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        // Validate email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordLayout.setError("Enter your password");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Handle successful login
     *
     * @param email User email
     */
    private void handleSuccessfulLogin(String email) {
        // Save remember me preference
        preferenceManager.saveRememberMe(email, rememberMeCheckbox.isChecked());

        // Navigate to Dashboard
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("userEmail", email);
        startActivity(intent);
        finish();
    }

    /**
     * Handle failed login
     */
    private void handleFailedLogin() {
        Toast.makeText(MainActivity.this,
                "Invalid email or password",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}