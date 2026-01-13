package com.finance.manager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    // ================= INTERFACE =================
    public interface BudgetActionListener {
        void onEdit(Budget budget);
        void onDelete(Budget budget);
    }

    private List<Budget> budgets;
    private Map<String, Double> spentAmounts;
    private BudgetActionListener listener;

    // ================= CONSTRUCTOR =================
    public BudgetAdapter(List<Budget> budgets,
                         Map<String, Double> spentAmounts,
                         BudgetActionListener listener) {
        this.budgets = budgets;
        this.spentAmounts = spentAmounts;
        this.listener = listener;
    }

    // ================= VIEW HOLDER =================
    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    // ================= BIND =================
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        double spent = spentAmounts.getOrDefault(budget.getCategory(), 0.0);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

        holder.categoryText.setText(
                budget.getCategory() + " - " +
                        budget.getMonthName() + " " +
                        budget.getYear()
        );

        holder.limitText.setText("Limit: " + formatter.format(budget.getLimitAmount()));
        holder.spentText.setText("Spent: " + formatter.format(spent));
        holder.remainingText.setText(
                "Remaining: " + formatter.format(budget.getRemainingAmount(spent))
        );

        double percentage = budget.getPercentageSpent(spent);
        holder.percentageChip.setText(String.format("%.0f%%", percentage));
        holder.budgetProgressBar.setProgress((int) percentage);

        // ===== Progress color =====
        if (percentage >= 100) {
            holder.budgetProgressBar.setProgressTintList(
                    ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
        } else if (percentage >= budget.getAlertThreshold()) {
            holder.budgetProgressBar.setProgressTintList(
                    ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
        } else {
            holder.budgetProgressBar.setProgressTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
        }

        // ===== Alert text =====
        if (budget.shouldAlert(spent)) {
            holder.alertText.setVisibility(View.VISIBLE);
            if (percentage >= 100) {
                holder.alertText.setText("⚠️ Budget exceeded!");
            } else {
                holder.alertText.setText(
                        "⚠️ You've reached " + budget.getAlertThreshold() + "% of your budget"
                );
            }
        } else {
            holder.alertText.setVisibility(View.GONE);
        }

        // ================= BUTTON ACTIONS =================
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(budget);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    // ================= UPDATE DATA =================
    public void updateBudgets(List<Budget> newBudgets,
                              Map<String, Double> newSpentAmounts) {
        this.budgets = newBudgets;
        this.spentAmounts = newSpentAmounts;
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER CLASS =================
    static class BudgetViewHolder extends RecyclerView.ViewHolder {

        TextView categoryText, limitText;
        TextView spentText, remainingText, alertText;
        ProgressBar budgetProgressBar;
        Chip percentageChip;

        View btnEdit, btnDelete;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryText = itemView.findViewById(R.id.categoryText);
            limitText = itemView.findViewById(R.id.limitText);
            spentText = itemView.findViewById(R.id.spentText);
            remainingText = itemView.findViewById(R.id.remainingText);
            alertText = itemView.findViewById(R.id.alertText);
            budgetProgressBar = itemView.findViewById(R.id.budgetProgressBar);
            percentageChip = itemView.findViewById(R.id.percentageChip);

            btnEdit = itemView.findViewById(R.id.editBudgetButton);
            btnDelete = itemView.findViewById(R.id.deleteBudgetButton);
        }
    }
}
