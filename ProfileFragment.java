package com.finance.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileFragment extends Fragment {
    
    private TextView emailText;
    private TextInputLayout firstNameLayout, lastNameLayout;
    private TextInputEditText firstNameInput, lastNameInput;
    private Button updateProfileButton, changePasswordButton;
    
    private DatabaseHelper databaseHelper;
    private String userEmail;
    private User currentUser;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        databaseHelper = DatabaseHelper.getInstance(requireContext());


        initializeViews(view);
        loadUserProfile();
        
        updateProfileButton.setOnClickListener(v -> updateProfile());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        
        return view;
    }
    
    private void initializeViews(View view) {
        emailText = view.findViewById(R.id.emailText);
        firstNameLayout = view.findViewById(R.id.firstNameLayout);
        lastNameLayout = view.findViewById(R.id.lastNameLayout);
        firstNameInput = view.findViewById(R.id.firstNameInput);
        lastNameInput = view.findViewById(R.id.lastNameInput);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
    }
    
    private void loadUserProfile() {
        currentUser = databaseHelper.getUserInfo(userEmail);
        
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail());
            firstNameInput.setText(currentUser.getFirstName());
            lastNameInput.setText(currentUser.getLastName());
        }
    }
    
    private void updateProfile() {
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
        
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        
        // Validate
        boolean isValid = true;
        
        if (firstName.isEmpty() || firstName.length() < 3 || firstName.length() > 10) {
            firstNameLayout.setError("First name must be 3-10 characters");
            isValid = false;
        }
        
        if (lastName.isEmpty() || lastName.length() < 3 || lastName.length() > 10) {
            lastNameLayout.setError("Last name must be 3-10 characters");
            isValid = false;
        }
        
        if (!isValid) return;
        
        // Update
        boolean success = databaseHelper.updateUserProfile(userEmail, firstName, lastName);
        
        if (success) {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            loadUserProfile();
        } else {
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showChangePasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = 
                new androidx.appcompat.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        
        TextInputLayout oldPasswordLayout = dialogView.findViewById(R.id.oldPasswordLayout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordLayout);
        
        TextInputEditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);
        
        builder.setTitle("Change Password");
        builder.setPositiveButton("Change", (dialog, which) -> {
            oldPasswordLayout.setError(null);
            newPasswordLayout.setError(null);
            confirmPasswordLayout.setError(null);
            
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            // Validate
            if (oldPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter current password", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter new password", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!isValidPassword(newPassword)) {
                Toast.makeText(getContext(), 
                        "Password must be 6-12 chars with 1 uppercase, 1 lowercase, and 1 number", 
                        Toast.LENGTH_LONG).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Update password
            boolean success = databaseHelper.updateUserPassword(userEmail, oldPassword, newPassword);
            
            if (success) {
                Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
    
    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 12) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
}
