package com.finance.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetsFragment extends Fragment {

    private RecyclerView budgetsRecyclerView;
    private FloatingActionButton addBudgetFab;
    private Spinner sortSpinner;
    private BudgetAdapter adapter;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private List<Budget> budgetList;
    private Map<String, Double> spentAmounts;
    private int selectedSortOption = 0;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_budgets, container, false);

        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        databaseHelper = DatabaseHelper.getInstance(requireContext());

        budgetsRecyclerView = view.findViewById(R.id.budgetsRecyclerView);
        addBudgetFab = view.findViewById(R.id.addBudgetFab);
        sortSpinner = view.findViewById(R.id.sortSpinner);

        setupRecyclerView();
        setupSortSpinner();
        loadBudgets();

        addBudgetFab.setOnClickListener(v -> showAddBudgetDialog());

        return view;
    }

    private void setupRecyclerView() {
        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        budgetList = new ArrayList<>();
        spentAmounts = new HashMap<>();

        adapter = new BudgetAdapter(
                budgetList,
                spentAmounts,
                new BudgetAdapter.BudgetActionListener() {
                    @Override
                    public void onEdit(Budget budget) {
                        showUpdateBudgetDialog(budget);
                    }

                    @Override
                    public void onDelete(Budget budget) {
                        showDeleteConfirmation(budget);
                    }
                }
        );

        budgetsRecyclerView.setAdapter(adapter);
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Category A-Z", "Category Z-A", "Highest Budget", "Lowest Budget", "Most Spent %", "Least Spent %"};

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, sortOptions) {

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Set text color based on theme
                int textColor = ContextCompat.getColor(getContext(), R.color.spinner_text);
                textView.setTextColor(textColor);

                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Set dropdown text and background colors based on theme
                int textColor = ContextCompat.getColor(getContext(), R.color.text_dark);
                int backgroundColor = ContextCompat.getColor(getContext(), R.color.card_white);

                textView.setTextColor(textColor);
                textView.setBackgroundColor(backgroundColor);
                textView.setPadding(32, 32, 32, 32);

                return view;
            }
        };

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedSortOption = position;
                sortBudgets();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void sortBudgets() {
        switch (selectedSortOption) {
            case 0: // Category A-Z
                Collections.sort(budgetList, new Comparator<Budget>() {
                    @Override
                    public int compare(Budget b1, Budget b2) {
                        return b1.getCategory().compareToIgnoreCase(b2.getCategory());
                    }
                });
                break;
            case 1: // Category Z-A
                Collections.sort(budgetList, new Comparator<Budget>() {
                    @Override
                    public int compare(Budget b1, Budget b2) {
                        return b2.getCategory().compareToIgnoreCase(b1.getCategory());
                    }
                });
                break;
            case 2: // Highest Budget
                Collections.sort(budgetList, new Comparator<Budget>() {
                    @Override
                    public int compare(Budget b1, Budget b2) {
                        return Double.compare(b2.getLimitAmount(), b1.getLimitAmount());
                    }
                });
                break;
            case 3: // Lowest Budget
                Collections.sort(budgetList, new Comparator<Budget>() {
                    @Override
                    public int compare(Budget b1, Budget b2) {
                        return Double.compare(b1.getLimitAmount(), b2.getLimitAmount());
                    }
                });
                break;
            case 4: // Most Spent %
                Collections.sort(budgetList, new Comparator<Budget>() {
                    @Override
                    public int compare(Budget b1, Budget b2) {
                        double spent1 = spentAmounts.getOrDefault(b1.getCategory(), 0.0);
                        double spent2 = spentAmounts.getOrDefault(b2.getCategory(), 0.0);
                        double percentage1 = b1.getPercentageSpent(spent1);
                        double percentage2 = b2.getPercentageSpent(spent2);
                        return Double.compare(percentage2, percentage1);
                    }
                });
                break;
            case 5: // Least Spent %
                Collections.sort(budgetList, new Comparator<Budget>() {
                    @Override
                    public int compare(Budget b1, Budget b2) {
                        double spent1 = spentAmounts.getOrDefault(b1.getCategory(), 0.0);
                        double spent2 = spentAmounts.getOrDefault(b2.getCategory(), 0.0);
                        double percentage1 = b1.getPercentageSpent(spent1);
                        double percentage2 = b2.getPercentageSpent(spent2);
                        return Double.compare(percentage1, percentage2);
                    }
                });
                break;
        }
        adapter.updateBudgets(budgetList, spentAmounts);
    }

    private void loadBudgets() {
        budgetList.clear();
        spentAmounts.clear();

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        List<String> categories = databaseHelper.getCategories(userEmail, "expense");

        for (String category : categories) {
            Budget budget = databaseHelper.getBudget(
                    userEmail, category, currentMonth, currentYear);

            if (budget != null) {
                budgetList.add(budget);
                double spent = databaseHelper.getSpentInCategory(
                        userEmail, category, currentMonth, currentYear);
                spentAmounts.put(category, spent);
            }
        }

        sortBudgets();
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText limitInput = dialogView.findViewById(R.id.limitInput);
        EditText alertThresholdInput = dialogView.findViewById(R.id.alertThresholdInput);

        List<String> categories = databaseHelper.getCategories(userEmail, "expense");

        // Theme-aware adapter for dialog spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categories) {

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                int textColor = ContextCompat.getColor(getContext(), R.color.spinner_text);
                textView.setTextColor(textColor);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                int textColor = ContextCompat.getColor(getContext(), R.color.text_dark);
                int backgroundColor = ContextCompat.getColor(getContext(), R.color.card_white);
                textView.setTextColor(textColor);
                textView.setBackgroundColor(backgroundColor);
                textView.setPadding(32, 32, 32, 32);
                return view;
            }
        };

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        builder.setTitle("Set Budget");

        builder.setPositiveButton("Set", (dialog, which) -> {
            String category = categorySpinner.getSelectedItem().toString();
            String limitStr = limitInput.getText().toString().trim();
            String thresholdStr = alertThresholdInput.getText().toString().trim();

            if (limitStr.isEmpty()) {
                Toast.makeText(getContext(),
                        "Please enter budget limit",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double limit;
            try {
                limit = Double.parseDouble(limitStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(),
                        "Invalid budget limit",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int threshold = thresholdStr.isEmpty()
                    ? 50
                    : Integer.parseInt(thresholdStr);

            if (threshold < 0 || threshold > 100) {
                Toast.makeText(getContext(),
                        "Threshold must be between 0 and 100",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            Budget existingBudget =
                    databaseHelper.getBudget(userEmail, category, month, year);

            if (existingBudget != null) {
                Toast.makeText(getContext(),
                        "You already set a budget for this category this month",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            long result = databaseHelper.addBudget(
                    userEmail,
                    category,
                    limit,
                    threshold,
                    month,
                    year
            );

            if (result != -1) {
                Toast.makeText(getContext(),
                        "Budget set successfully",
                        Toast.LENGTH_SHORT).show();
                loadBudgets();
            } else {
                Toast.makeText(getContext(),
                        "Failed to set budget",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showUpdateBudgetDialog(Budget budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater()
                .inflate(R.layout.dialog_add_budget, null);
        builder.setView(view);

        Spinner categorySpinner = view.findViewById(R.id.categorySpinner);
        EditText limitInput = view.findViewById(R.id.limitInput);
        EditText thresholdInput = view.findViewById(R.id.alertThresholdInput);

        categorySpinner.setEnabled(false);

        limitInput.setText(String.valueOf(budget.getLimitAmount()));
        thresholdInput.setText(String.valueOf(budget.getAlertThreshold()));

        builder.setTitle("Update Budget");

        builder.setPositiveButton("Update", (dialog, which) -> {
            double limit =
                    Double.parseDouble(limitInput.getText().toString());
            int threshold =
                    Integer.parseInt(thresholdInput.getText().toString());

            boolean updated = databaseHelper.updateBudget(
                    budget.getId(), limit, threshold);

            if (updated) {
                Toast.makeText(getContext(),
                        "Budget updated",
                        Toast.LENGTH_SHORT).show();
                loadBudgets();
            } else {
                Toast.makeText(getContext(),
                        "Update failed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmation(Budget budget) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted =
                            databaseHelper.deleteBudget(budget.getId());

                    if (deleted) {
                        Toast.makeText(getContext(),
                                "Budget deleted",
                                Toast.LENGTH_SHORT).show();
                        loadBudgets();
                    } else {
                        Toast.makeText(getContext(),
                                "Delete failed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}