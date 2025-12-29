package com.finance.manager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.finance.manager.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private Map<String, Double> spentAmounts;

    public BudgetAdapter(List<Budget> budgets, Map<String, Double> spentAmounts) {
        this.budgets = budgets;
        this.spentAmounts = spentAmounts;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        double spent = spentAmounts.getOrDefault(budget.getCategory(), 0.0);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

        holder.categoryText.setText(budget.getCategory() + " - " +
                budget.getMonthName() + " " + budget.getYear());
        holder.limitText.setText("Limit: " + formatter.format(budget.getLimitAmount()));
        holder.spentText.setText("Spent: " + formatter.format(spent));
        holder.remainingText.setText("Remaining: " +
                formatter.format(budget.getRemainingAmount(spent)));

        double percentage = budget.getPercentageSpent(spent);
        holder.percentageChip.setText(String.format("%.0f%%", percentage));
        holder.budgetProgressBar.setProgress((int) percentage);

        // Set progress bar color based on percentage
        if (percentage >= 100) {
            holder.budgetProgressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
        } else if (percentage >= budget.getAlertThreshold()) {
            holder.budgetProgressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
        } else {
            holder.budgetProgressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
        }

        // Show alert if threshold reached
        if (budget.shouldAlert(spent)) {
            holder.alertText.setVisibility(View.VISIBLE);
            if (percentage >= 100) {
                holder.alertText.setText("⚠️ Budget exceeded!");
            } else {
                holder.alertText.setText("⚠️ You've reached " +
                        budget.getAlertThreshold() + "% of your budget");
            }
        } else {
            holder.alertText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateBudgets(List<Budget> newBudgets, Map<String, Double> newSpentAmounts) {
        this.budgets = newBudgets;
        this.spentAmounts = newSpentAmounts;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, limitText;
        TextView spentText, remainingText, alertText;
        ProgressBar budgetProgressBar;
        com.google.android.material.chip.Chip percentageChip;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            limitText = itemView.findViewById(R.id.limitText);
            percentageChip = itemView.findViewById(R.id.percentageChip);
            spentText = itemView.findViewById(R.id.spentText);
            remainingText = itemView.findViewById(R.id.remainingText);
            alertText = itemView.findViewById(R.id.alertText);
            budgetProgressBar = itemView.findViewById(R.id.budgetProgressBar);
        }
    }
}