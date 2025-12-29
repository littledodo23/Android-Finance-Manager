package com.finance.manager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        databaseHelper = new DatabaseHelper(requireContext());

        initializeViews(view);
        setupPeriodSpinner();

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDashboardData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, periods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);
    }

    private void loadDashboardData(int periodPosition) {
        Calendar calendar = Calendar.getInstance();
        long startDate, endDate;

        // Calculate date range based on selected period
        switch (periodPosition) {
            case 0: // This Month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTimeInMillis();

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTimeInMillis();
                break;

            case 1: // Last Month
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTimeInMillis();

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTimeInMillis();
                break;

            case 2: // Last 3 Months
                calendar.add(Calendar.MONTH, -3);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTimeInMillis();
                endDate = System.currentTimeMillis();
                break;

            case 3: // Last 6 Months
                calendar.add(Calendar.MONTH, -6);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTimeInMillis();
                endDate = System.currentTimeMillis();
                break;

            case 4: // This Year
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTimeInMillis();
                endDate = System.currentTimeMillis();
                break;

            default:
                startDate = 0;
                endDate = System.currentTimeMillis();
        }

        // Load financial data
        double totalIncome = databaseHelper.getTotalAmount(userEmail, "income", startDate, endDate);
        double totalExpenses = databaseHelper.getTotalAmount(userEmail, "expense", startDate, endDate);
        double balance = totalIncome - totalExpenses;

        // Update UI
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        totalIncomeText.setText(formatter.format(totalIncome));
        totalExpensesText.setText(formatter.format(totalExpenses));
        balanceText.setText(formatter.format(balance));

        // Set balance color
        if (balance >= 0) {
            balanceText.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            balanceText.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // Load charts
        setupPieChart(totalIncome, totalExpenses);
        setupCategoryBarChart(startDate, endDate);
    }

    private void setupPieChart(double income, double expenses) {
        List<PieEntry> entries = new ArrayList<>();

        if (income > 0) {
            entries.add(new PieEntry((float) income, "Income"));
        }
        if (expenses > 0) {
            entries.add(new PieEntry((float) expenses, "Expenses"));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No data available");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupCategoryBarChart(long startDate, long endDate) {
        List<Transaction> expenses = databaseHelper.getTransactionsByPeriod(userEmail, "expense", startDate, endDate);

        // Group expenses by category
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Transaction transaction : expenses) {
            String category = transaction.getCategory();
            float amount = (float) transaction.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }

        if (categoryTotals.isEmpty()) {
            categoryBarChart.clear();
            categoryBarChart.setNoDataText("No expense data available");
            return;
        }

        // Create bar entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        categoryBarChart.setData(data);
        categoryBarChart.getDescription().setEnabled(false);
        categoryBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        categoryBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        categoryBarChart.getXAxis().setGranularity(1f);
        categoryBarChart.getAxisRight().setEnabled(false);
        categoryBarChart.animateY(1000);
        categoryBarChart.invalidate();
    }
}