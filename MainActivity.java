package com.finance.manager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private Button signInButton, signUpButton;
    
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "FinanceManagerPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_REMEMBER = "remember";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
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
        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
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
        
        // Check login
        if (databaseHelper.checkUserLogin(email, password)) {
            // Save remember me preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (rememberMeCheckbox.isChecked()) {
                editor.putBoolean(KEY_REMEMBER, true);
                editor.putString(KEY_EMAIL, email);
            } else {
                editor.putBoolean(KEY_REMEMBER, false);
                editor.remove(KEY_EMAIL);
            }
            editor.apply();
            
            // Navigate to Dashboard
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("userEmail", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
