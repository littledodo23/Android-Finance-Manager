package com.finance.manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    
    private RadioGroup themeRadioGroup;
    private RadioButton lightModeRadio, darkModeRadio;
    private Spinner defaultPeriodSpinner;
    private Button manageCategoriesButton;
    
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "FinanceManagerSettings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_DEFAULT_PERIOD = "defaultPeriod";
    
    private String userEmail;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }
        
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);
        
        initializeViews(view);
        loadSettings();
        
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lightModeRadio) {
                saveTheme("light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.darkModeRadio) {
                saveTheme("dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
        
        defaultPeriodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                saveDefaultPeriod(position);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        manageCategoriesButton.setOnClickListener(v -> showManageCategoriesDialog());
        
        return view;
    }
    
    private void initializeViews(View view) {
        themeRadioGroup = view.findViewById(R.id.themeRadioGroup);
        lightModeRadio = view.findViewById(R.id.lightModeRadio);
        darkModeRadio = view.findViewById(R.id.darkModeRadio);
        defaultPeriodSpinner = view.findViewById(R.id.defaultPeriodSpinner);
        manageCategoriesButton = view.findViewById(R.id.manageCategoriesButton);
        
        // Setup period spinner
        String[] periods = {"This Month", "Last Month", "Last 3 Months", "Last 6 Months", "This Year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, periods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        defaultPeriodSpinner.setAdapter(adapter);
    }
    
    private void loadSettings() {
        // Load theme
        String theme = sharedPreferences.getString(KEY_THEME, "light");
        if ("dark".equals(theme)) {
            darkModeRadio.setChecked(true);
        } else {
            lightModeRadio.setChecked(true);
        }
        
        // Load default period
        int defaultPeriod = sharedPreferences.getInt(KEY_DEFAULT_PERIOD, 0);
        defaultPeriodSpinner.setSelection(defaultPeriod);
    }
    
    private void saveTheme(String theme) {
        sharedPreferences.edit().putString(KEY_THEME, theme).apply();
        Toast.makeText(getContext(), "Theme changed", Toast.LENGTH_SHORT).show();
    }
    
    private void saveDefaultPeriod(int period) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_PERIOD, period).apply();
    }
    
    private void showManageCategoriesDialog() {
        String[] options = {"Add Income Category", "Add Expense Category", 
                           "Delete Income Category", "Delete Expense Category"};
        
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Manage Categories")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showAddCategoryDialog("income");
                            break;
                        case 1:
                            showAddCategoryDialog("expense");
                            break;
                        case 2:
                            showDeleteCategoryDialog("income");
                            break;
                        case 3:
                            showDeleteCategoryDialog("expense");
                            break;
                    }
                })
                .show();
    }
    
    private void showAddCategoryDialog(String type) {
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Category name");
        
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Add " + (type.equals("income") ? "Income" : "Expense") + " Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        DatabaseHelper db = new DatabaseHelper(getContext());
                        boolean success = db.addCategory(userEmail, categoryName, type);
                        if (success) {
                            Toast.makeText(getContext(), "Category added", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to add category", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showDeleteCategoryDialog(String type) {
        DatabaseHelper db = new DatabaseHelper(getContext());
        java.util.List<String> categories = db.getCategories(userEmail, type);
        
        if (categories.isEmpty()) {
            Toast.makeText(getContext(), "No categories to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] categoriesArray = categories.toArray(new String[0]);
        
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete " + (type.equals("income") ? "Income" : "Expense") + " Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    String categoryName = categoriesArray[which];
                    boolean success = db.deleteCategory(userEmail, categoryName, type);
                    if (success) {
                        Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
