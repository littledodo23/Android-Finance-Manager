package com.finance.manager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.NumberFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    
    private Spinner periodSpinner;
    private TextView totalIncomeText, totalExpensesText, balanceText;
    private PieChart pieChart;
    private BarChart categoryBarChart;
    
    private DatabaseHelper databaseHelper;
    private String userEmail;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }
        
        databaseHelper = new DatabaseHelper(getContext());
        
        initializeViews(view);
        setupPeriodSpinner();
        
        periodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadDashboardData(position);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        return view;
    }
    
    private void initializeViews(View view) {
        periodSpinner = view.findViewById(R.id.periodSpinner);
        totalIncomeText = view.findViewById(R.id.totalIncomeText);
        totalExpensesText = view.findViewById(R.id.totalExpensesText);
        balanceText = view.findViewById(R.id.balanceText);
        pieChart = view.findViewById(R.id.pieChart);
        categoryBarChart = view.findViewById(R.id.categoryBarChart);
    }
    
    private void setupPeriodSpinner() {
        String[] periods = {"This Month", "Last Month", "Last 3 Months", "Last 6 Months", "This Year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, periods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);
    }
    
    private void loadDashboardData(int periodPosition) {
        long[] dateRange = getDateRange(periodPosition);
        long startDate = dateRange[0];
        long endDate = dateRange[1];
        
        double totalIncome = databaseHelper.getTotalAmount(userEmail, "income", startDate, endDate);
        double totalExpenses = databaseHelper.getTotalAmount(userEmail, "expense", startDate, endDate);
        double balance = totalIncome - totalExpenses;
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        totalIncomeText.setText(formatter.format(totalIncome));
        totalExpensesText.setText(formatter.format(totalExpenses));
        balanceText.setText(formatter.format(balance));
        
        // Set balance color
        if (balance >= 0) {
            balanceText.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            balanceText.setTextColor(Color.parseColor("#F44336"));
        }
        
        setupPieChart(totalIncome, totalExpenses);
        setupCategoryBarChart(startDate, endDate);
    }
    
    private long[] getDateRange(int position) {
        Calendar cal = Calendar.getInstance();
        long endDate = cal.getTimeInMillis();
        
        switch (position) {
            case 0: // This Month
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                break;
            case 1: // Last Month
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case 2: // Last 3 Months
                cal.add(Calendar.MONTH, -3);
                break;
            case 3: // Last 6 Months
                cal.add(Calendar.MONTH, -6);
                break;
            case 4: // This Year
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                break;
        }
        
        long startDate = cal.getTimeInMillis();
        return new long[]{startDate, endDate};
    }
    
    private void setupPieChart(double income, double expenses) {
        List<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Income"));
        if (expenses > 0) entries.add(new PieEntry((float) expenses, "Expenses"));
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.invalidate();
    }
    
    private void setupCategoryBarChart(long startDate, long endDate) {
        List<Transaction> expenses = databaseHelper.getTransactionsByPeriod(
                userEmail, "expense", startDate, endDate);
        
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Transaction t : expenses) {
            float current = categoryTotals.getOrDefault(t.getCategory(), 0f);
            categoryTotals.put(t.getCategory(), current + (float) t.getAmount());
        }
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
            labels.add(entry.getKey());
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        
        BarData data = new BarData(dataSet);
        categoryBarChart.setData(data);
        categoryBarChart.getDescription().setEnabled(false);
        categoryBarChart.getXAxis().setGranularity(1f);
        categoryBarChart.invalidate();
    }
}
