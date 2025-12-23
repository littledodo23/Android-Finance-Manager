package com.finance.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.*;

public class BudgetsFragment extends Fragment {
    
    private RecyclerView budgetsRecyclerView;
    private FloatingActionButton addBudgetFab;
    private BudgetAdapter adapter;
    
    private DatabaseHelper databaseHelper;
    private String userEmail;
    private List<Budget> budgetList;
    private Map<String, Double> spentAmounts;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);
        
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }
        
        databaseHelper = new DatabaseHelper(getContext());
        
        budgetsRecyclerView = view.findViewById(R.id.budgetsRecyclerView);
        addBudgetFab = view.findViewById(R.id.addBudgetFab);
        
        setupRecyclerView();
        loadBudgets();
        
        addBudgetFab.setOnClickListener(v -> showAddBudgetDialog());
        
        return view;
    }
    
    private void setupRecyclerView() {
        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        budgetList = new ArrayList<>();
        spentAmounts = new HashMap<>();
        adapter = new BudgetAdapter(budgetList, spentAmounts);
        budgetsRecyclerView.setAdapter(adapter);
    }
    
    private void loadBudgets() {
        budgetList.clear();
        spentAmounts.clear();
        
        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        
        // Get all expense categories
        List<String> categories = databaseHelper.getCategories(userEmail, "expense");
        
        for (String category : categories) {
            Budget budget = databaseHelper.getBudget(userEmail, category, currentMonth, currentYear);
            if (budget != null) {
                budgetList.add(budget);
                double spent = databaseHelper.getSpentInCategory(userEmail, category, currentMonth, currentYear);
                spentAmounts.put(category, spent);
            }
        }
        
        adapter.updateBudgets(budgetList, spentAmounts);
    }
    
    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);
        
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText limitInput = dialogView.findViewById(R.id.limitInput);
        EditText alertThresholdInput = dialogView.findViewById(R.id.alertThresholdInput);
        
        // Setup category spinner
        List<String> categories = databaseHelper.getCategories(userEmail, "expense");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        builder.setTitle("Set Budget");
        builder.setPositiveButton("Set", (dialog, which) -> {
            String category = categorySpinner.getSelectedItem().toString();
            String limitStr = limitInput.getText().toString().trim();
            String thresholdStr = alertThresholdInput.getText().toString().trim();
            
            if (limitStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter budget limit", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double limit = Double.parseDouble(limitStr);
            int threshold = thresholdStr.isEmpty() ? 50 : Integer.parseInt(thresholdStr);
            
            // Validate threshold
            if (threshold < 0 || threshold > 100) {
                Toast.makeText(getContext(), "Threshold must be between 0-100", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            
            long result = databaseHelper.addBudget(userEmail, category, limit, threshold, month, year);
            
            if (result != -1) {
                Toast.makeText(getContext(), "Budget set successfully", Toast.LENGTH_SHORT).show();
                loadBudgets();
            } else {
                Toast.makeText(getContext(), "Failed to set budget", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
