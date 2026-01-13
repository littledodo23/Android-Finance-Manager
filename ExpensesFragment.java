package com.finance.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private RecyclerView expensesRecyclerView;
    private FloatingActionButton addExpenseFab;
    private SearchView searchView;
    private ChipGroup filterChipGroup;
    private CardView summaryCard;
    private TextView totalExpensesText, transactionCountText, budgetWarningText;
    private LinearLayout emptyStateLayout;
    private Button emptyStateAddButton;
    private Spinner sortSpinner;
    private TransactionAdapter adapter;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private List<Transaction> expensesList;
    private List<Transaction> filteredList;
    private String selectedCategory = "All";
    private int selectedSortOption = 0; // 0: Newest First, 1: Oldest First, 2: Highest Amount, 3: Lowest Amount

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        databaseHelper = DatabaseHelper.getInstance(requireContext());



        initializeViews(view);
        setupRecyclerView();
        setupSearchView();
        setupFilterChips();
        setupSortSpinner();
        setupSwipeToDelete();
        loadExpenses();

        addExpenseFab.setOnClickListener(v -> showAddExpenseDialog(null));
        emptyStateAddButton.setOnClickListener(v -> showAddExpenseDialog(null));

        return view;
    }

    private void initializeViews(View view) {
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView);
        addExpenseFab = view.findViewById(R.id.addExpenseFab);
        searchView = view.findViewById(R.id.searchView);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        summaryCard = view.findViewById(R.id.summaryCard);
        totalExpensesText = view.findViewById(R.id.totalExpensesText);
        transactionCountText = view.findViewById(R.id.transactionCountText);
        budgetWarningText = view.findViewById(R.id.budgetWarningText);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateAddButton = view.findViewById(R.id.emptyStateAddButton);
        sortSpinner = view.findViewById(R.id.sortSpinner);
    }

    private void setupRecyclerView() {
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expensesList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new TransactionAdapter(filteredList, this);
        expensesRecyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTransactions(newText, selectedCategory);
                return true;
            }
        });
    }

    private void setupFilterChips() {
        filterChipGroup.removeAllViews();

        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> {
            selectedCategory = "All";
            filterTransactions(searchView.getQuery().toString(), selectedCategory);
        });
        filterChipGroup.addView(allChip);

        List<String> categories = databaseHelper.getCategories(userEmail, "expense");
        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                selectedCategory = category;
                filterTransactions(searchView.getQuery().toString(), selectedCategory);
            });
            filterChipGroup.addView(chip);
        }
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Newest First", "Oldest First", "Highest Amount", "Lowest Amount"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedSortOption = position;
                sortTransactions();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void sortTransactions() {
        switch (selectedSortOption) {
            case 0: // Newest First
                Collections.sort(filteredList, new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        return Long.compare(t2.getDate(), t1.getDate());
                    }
                });
                break;
            case 1: // Oldest First
                Collections.sort(filteredList, new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        return Long.compare(t1.getDate(), t2.getDate());
                    }
                });
                break;
            case 2: // Highest Amount
                Collections.sort(filteredList, new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        return Double.compare(t2.getAmount(), t1.getAmount());
                    }
                });
                break;
            case 3: // Lowest Amount
                Collections.sort(filteredList, new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        return Double.compare(t1.getAmount(), t2.getAmount());
                    }
                });
                break;
        }
        adapter.updateTransactions(filteredList);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction transaction = filteredList.get(position);

                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean success = databaseHelper.deleteTransaction(transaction.getId());
                            if (success) {
                                Toast.makeText(getContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                                loadExpenses();
                            } else {
                                adapter.notifyItemChanged(position);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(expensesRecyclerView);
    }

    private void loadExpenses() {
        expensesList = databaseHelper.getAllTransactions(userEmail, "expense");
        filterTransactions(searchView.getQuery().toString(), selectedCategory);
        updateSummary();
        updateEmptyState();
        checkBudgetStatus();
    }

    private void filterTransactions(String query, String category) {
        filteredList.clear();

        for (Transaction transaction : expensesList) {
            boolean matchesQuery = query.isEmpty() ||
                    transaction.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                    transaction.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    transaction.getFormattedAmount().contains(query);

            boolean matchesCategory = category.equals("All") ||
                    transaction.getCategory().equals(category);

            if (matchesQuery && matchesCategory) {
                filteredList.add(transaction);
            }
        }

        sortTransactions();
        updateEmptyState();
    }

    private void updateSummary() {
        double total = 0;
        for (Transaction transaction : expensesList) {
            total += transaction.getAmount();
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        totalExpensesText.setText(formatter.format(total));
        transactionCountText.setText(expensesList.size() + " transactions");
    }

    private void checkBudgetStatus() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        int overBudgetCount = 0;
        List<String> categories = databaseHelper.getCategories(userEmail, "expense");

        for (String category : categories) {
            Budget budget = databaseHelper.getBudget(userEmail, category, currentMonth, currentYear);
            if (budget != null) {
                double spent = databaseHelper.getSpentInCategory(userEmail, category, currentMonth, currentYear);
                if (spent >= budget.getLimitAmount()) {
                    overBudgetCount++;
                }
            }
        }

        if (overBudgetCount > 0) {
            budgetWarningText.setVisibility(View.VISIBLE);
            budgetWarningText.setText("⚠️ " + overBudgetCount + " budget" +
                    (overBudgetCount > 1 ? "s" : "") + " exceeded this month");
        } else {
            budgetWarningText.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            expensesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            expensesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddExpenseDialog(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);

        List<String> categories = databaseHelper.getCategories(userEmail, "expense");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        if (transaction != null) {
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

                checkBudgetAlert(category, date);
                loadExpenses();
                setupFilterChips();
            } else {
                Toast.makeText(getContext(), "Operation failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void checkBudgetAlert(String category, long transactionDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(transactionDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        Budget budget = databaseHelper.getBudget(userEmail, category, month, year);

        if (budget != null) {
            double totalSpent = databaseHelper.getSpentInCategory(userEmail, category, month, year);
            double limitAmount = budget.getLimitAmount();
            double percentage = (totalSpent / limitAmount) * 100;

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

            if (totalSpent > limitAmount) {
                double overAmount = totalSpent - limitAmount;
                Toast.makeText(getContext(),
                        "⚠️ Budget Exceeded! You're " + formatter.format(overAmount) +
                                " over your " + category + " budget",
                        Toast.LENGTH_LONG).show();

            } else if (percentage >= budget.getAlertThreshold()) {
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