package com.finance.manager;

import android.app.DatePickerDialog;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private RecyclerView expensesRecyclerView;
    private FloatingActionButton addExpenseFab;
    private TransactionAdapter adapter;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private List<Transaction> expensesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        databaseHelper = new DatabaseHelper(getContext());

        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView);
        addExpenseFab = view.findViewById(R.id.addExpenseFab);

        setupRecyclerView();
        loadExpenses();

        addExpenseFab.setOnClickListener(v -> showAddExpenseDialog(null));

        return view;
    }

    private void setupRecyclerView() {
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expensesList = databaseHelper.getAllTransactions(userEmail, "expense");
        adapter = new TransactionAdapter(expensesList, this);
        expensesRecyclerView.setAdapter(adapter);
    }

    private void loadExpenses() {
        expensesList = databaseHelper.getAllTransactions(userEmail, "expense");
        adapter.updateTransactions(expensesList);
    }

    private void showAddExpenseDialog(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);

        // Setup category spinner
        List<String> categories = databaseHelper.getCategories(userEmail, "expense");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Setup date picker
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        if (transaction != null) {
            // Edit mode
            amountInput.setText(String.valueOf(transaction.getAmount()));
            calendar.setTimeInMillis(transaction.getDate());
            dateInput.setText(dateFormat.format(calendar.getTime()));

            int categoryPosition = categories.indexOf(transaction.getCategory());
            if (categoryPosition >= 0) categorySpinner.setSelection(categoryPosition);

            descriptionInput.setText(transaction.getDescription());
        } else {
            dateInput.setText(dateFormat.format(calendar.getTime()));
        }

        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        dateInput.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setTitle(transaction == null ? "Add Expense" : "Edit Expense");
        builder.setPositiveButton(transaction == null ? "Add" : "Update", (dialog, which) -> {
            String amountStr = amountInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String description = descriptionInput.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long date = calendar.getTimeInMillis();

            boolean success;
            if (transaction == null) {
                success = databaseHelper.addTransaction(userEmail, amount, date,
                        category, description, "expense") != -1;
            } else {
                success = databaseHelper.updateTransaction(transaction.getId(),
                        amount, date, category, description);
            }

            if (success) {
                Toast.makeText(getContext(),
                        transaction == null ? "Expense added" : "Expense updated",
                        Toast.LENGTH_SHORT).show();

                // Check budget and show alert if exceeded
                checkBudgetAlert(category, date);

                loadExpenses();
            } else {
                Toast.makeText(getContext(), "Operation failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    /**
     * Check if the user has exceeded their budget for the given category
     */
    private void checkBudgetAlert(String category, long transactionDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(transactionDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        // Get budget for this category and month
        Budget budget = databaseHelper.getBudget(userEmail, category, month, year);

        if (budget != null) {
            // Get total spent in this category for this month
            double totalSpent = databaseHelper.getSpentInCategory(userEmail, category, month, year);
            double limitAmount = budget.getLimitAmount();
            double percentage = (totalSpent / limitAmount) * 100;

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

            // Show different messages based on budget status
            if (totalSpent > limitAmount) {
                // Budget exceeded
                double overAmount = totalSpent - limitAmount;
                Toast.makeText(getContext(),
                        "⚠️ Budget Exceeded! You're " + formatter.format(overAmount) +
                                " over your " + category + " budget",
                        Toast.LENGTH_LONG).show();

            } else if (percentage >= budget.getAlertThreshold()) {
                // Alert threshold reached
                double remaining = limitAmount - totalSpent;
                Toast.makeText(getContext(),
                        "⚠️ Budget Alert! You have " + formatter.format(remaining) +
                                " remaining in your " + category + " budget (" +
                                String.format("%.0f%%", percentage) + " used)",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onEditClick(Transaction transaction) {
        showAddExpenseDialog(transaction);
    }

    @Override
    public void onDeleteClick(Transaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteTransaction(transaction.getId());
                    if (success) {
                        Toast.makeText(getContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                        loadExpenses();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}