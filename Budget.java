// ==================== Budget.java ====================
package com.finance.manager;

public class Budget {
    private int id;
    private String userEmail;
    private String category;
    private double limitAmount;
    private int alertThreshold;
    private int month;
    private int year;
    
    public Budget() {}
    
    public Budget(String userEmail, String category, double limitAmount, int alertThreshold, int month, int year) {
        this.userEmail = userEmail;
        this.category = category;
        this.limitAmount = limitAmount;
        this.alertThreshold = alertThreshold;
        this.month = month;
        this.year = year;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    
    public int getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(int alertThreshold) { this.alertThreshold = alertThreshold; }
    
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getMonthName() {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month - 1];
    }
    
    public double getPercentageSpent(double spent) {
        if (limitAmount == 0) return 0;
        return (spent / limitAmount) * 100;
    }
    
    public double getRemainingAmount(double spent) {
        return limitAmount - spent;
    }
    
    public boolean shouldAlert(double spent) {
        return getPercentageSpent(spent) >= alertThreshold;
    }
}
