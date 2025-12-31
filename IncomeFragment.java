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
import java.util.List;
import java.util.Locale;

public class IncomeFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private RecyclerView incomeRecyclerView;
    private FloatingActionButton addIncomeFab;
    private SearchView searchView;
    private ChipGroup filterChipGroup;
    private CardView summaryCard;
    private TextView totalIncomeText, transactionCountText;
    private LinearLayout emptyStateLayout;
    private Button emptyStateAddButton;
    private TransactionAdapter adapter;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private List<Transaction> incomeList;
    private List<Transaction> filteredList;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        databaseHelper = new DatabaseHelper(getContext());

        initializeViews(view);
        setupRecyclerView();
        setupSearchView();
        setupFilterChips();
        setupSwipeToDelete();
        loadIncome();

        // FAB button click
        addIncomeFab.setOnClickListener(v -> showAddIncomeDialog(null));

        // Empty state button click
        emptyStateAddButton.setOnClickListener(v -> showAddIncomeDialog(null));

        return view;
    }

    private void initializeViews(View view) {
        incomeRecyclerView = view.findViewById(R.id.incomeRecyclerView);
        addIncomeFab = view.findViewById(R.id.addIncomeFab);
        searchView = view.findViewById(R.id.searchView);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        summaryCard = view.findViewById(R.id.summaryCard);
        totalIncomeText = view.findViewById(R.id.totalIncomeText);
        transactionCountText = view.findViewById(R.id.transactionCountText);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateAddButton = view.findViewById(R.id.emptyStateAddButton);
    }

    private void setupRecyclerView() {
        incomeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new TransactionAdapter(filteredList, this);
        incomeRecyclerView.setAdapter(adapter);
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

        // Add "All" chip
        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> {
            selectedCategory = "All";
            filterTransactions(searchView.getQuery().toString(), selectedCategory);
        });
        filterChipGroup.addView(allChip);

        // Add category chips
        List<String> categories = databaseHelper.getCategories(userEmail, "income");
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

                // Show confirmation dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Income")
                        .setMessage("Are you sure you want to delete this income?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean success = databaseHelper.deleteTransaction(transaction.getId());
                            if (success) {
                                Toast.makeText(getContext(), "Income deleted", Toast.LENGTH_SHORT).show();
                                loadIncome();
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
        itemTouchHelper.attachToRecyclerView(incomeRecyclerView);
    }

    private void loadIncome() {
        incomeList = databaseHelper.getAllTransactions(userEmail, "income");
        filterTransactions(searchView.getQuery().toString(), selectedCategory);
        updateSummary();
        updateEmptyState();
    }

    private void filterTransactions(String query, String category) {
        filteredList.clear();

        for (Transaction transaction : incomeList) {
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

        adapter.updateTransactions(filteredList);
        updateEmptyState();
    }

    private void updateSummary() {
        double total = 0;
        for (Transaction transaction : incomeList) {
            total += transaction.getAmount();
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        totalIncomeText.setText(formatter.format(total));
        transactionCountText.setText(incomeList.size() + " transactions");
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            incomeRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            incomeRecyclerView.setVisibility(View.VISIBLE);
        }
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
                setupFilterChips(); // Refresh chips in case new category was added
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