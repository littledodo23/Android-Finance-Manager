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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class IncomeFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private RecyclerView incomeRecyclerView;
    private FloatingActionButton addIncomeFab;
    private TransactionAdapter adapter;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private List<Transaction> incomeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        databaseHelper = new DatabaseHelper(getContext());

        incomeRecyclerView = view.findViewById(R.id.incomeRecyclerView);
        addIncomeFab = view.findViewById(R.id.addIncomeFab);

        setupRecyclerView();
        loadIncome();

        addIncomeFab.setOnClickListener(v -> showAddIncomeDialog(null));

        return view;
    }

    private void setupRecyclerView() {
        incomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeList = databaseHelper.getAllTransactions(userEmail, "income");
        adapter = new TransactionAdapter(incomeList, this);
        incomeRecyclerView.setAdapter(adapter);
    }

    private void loadIncome() {
        incomeList = databaseHelper.getAllTransactions(userEmail, "income");
        adapter.updateTransactions(incomeList);
    }

    private void showAddIncomeDialog(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);

        // Setup category spinner
        List<String> categories = databaseHelper.getCategories(userEmail, "income");
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

        builder.setTitle(transaction == null ? "Add Income" : "Edit Income");
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
                        category, description, "income") != -1;
            } else {
                success = databaseHelper.updateTransaction(transaction.getId(),
                        amount, date, category, description);
            }

            if (success) {
                Toast.makeText(getContext(),
                        transaction == null ? "Income added" : "Income updated",
                        Toast.LENGTH_SHORT).show();
                loadIncome();
            } else {
                Toast.makeText(getContext(), "Operation failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public void onEditClick(Transaction transaction) {
        showAddIncomeDialog(transaction);
    }

    @Override
    public void onDeleteClick(Transaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Income")
                .setMessage("Are you sure you want to delete this income?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteTransaction(transaction.getId());
                    if (success) {
                        Toast.makeText(getContext(), "Income deleted", Toast.LENGTH_SHORT).show();
                        loadIncome();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}