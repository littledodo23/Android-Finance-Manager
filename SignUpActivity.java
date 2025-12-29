package com.finance.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, firstNameLayout, lastNameLayout;
    private TextInputLayout passwordLayout, confirmPasswordLayout;
    private TextInputEditText emailInput, firstNameInput, lastNameInput;
    private TextInputEditText passwordInput, confirmPasswordInput;
    private Button signUpButton, backToSignInButton;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();

        signUpButton.setOnClickListener(v -> signUp());
        backToSignInButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        emailLayout = findViewById(R.id.emailLayout);
        firstNameLayout = findViewById(R.id.firstNameLayout);
        lastNameLayout = findViewById(R.id.lastNameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        emailInput = findViewById(R.id.emailInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        signUpButton = findViewById(R.id.signUpButton);
        backToSignInButton = findViewById(R.id.backToSignInButton);
    }

    private void signUp() {
        // Clear previous errors
        emailLayout.setError(null);
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        String email = emailInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        boolean isValid = true;

        // Validate email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            isValid = false;
        } else if (databaseHelper.isEmailExists(email)) {
            emailLayout.setError("Email already registered");
            isValid = false;
        }

        // Validate first name
        if (firstName.isEmpty() || firstName.length() < 3 || firstName.length() > 10) {
            firstNameLayout.setError("First name must be 3-10 characters");
            isValid = false;
        }

        // Validate last name
        if (lastName.isEmpty() || lastName.length() < 3 || lastName.length() > 10) {
            lastNameLayout.setError("Last name must be 3-10 characters");
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordLayout.setError("Enter a password");
            isValid = false;
        } else if (!isValidPassword(password)) {
            passwordLayout.setError("Password must be 6-12 chars with 1 uppercase, 1 lowercase, and 1 number");
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) return;

        // Register user
        boolean success = databaseHelper.addUser(email, firstName, lastName, password);

        if (success) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to Dashboard
            Intent intent = new Intent(SignUpActivity.this, DashboardActivity.class);
            intent.putExtra("userEmail", email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 12) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            if (Character.isLowerCase(c)) hasLowercase = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUppercase && hasLowercase && hasDigit;
    }
}