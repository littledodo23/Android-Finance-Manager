// ==================== Transaction.java ====================
package com.finance.manager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction {
    private int id;
    private String userEmail;
    private double amount;
    private long date;
    private String category;
    private String description;
    private String type; // "income" or "expense"
    
    public Transaction() {}
    
    public Transaction(String userEmail, double amount, long date, String category, String description, String type) {
        this.userEmail = userEmail;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
        this.type = type;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    // Helper methods for display
    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        return formatter.format(amount);
    }
    
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(date));
    }
    
    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(date));
    }
}
