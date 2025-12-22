// ==================== Category.java ====================
package com.finance.manager;

public class Category {
    private int id;
    private String userEmail;
    private String categoryName;
    private String type; // "income" or "expense"
    
    public Category() {}
    
    public Category(String userEmail, String categoryName, String type) {
        this.userEmail = userEmail;
        this.categoryName = categoryName;
        this.type = type;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
