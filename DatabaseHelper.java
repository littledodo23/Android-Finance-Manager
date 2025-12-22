package com.finance.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "FinanceManager.db";
    private static final int DATABASE_VERSION = 1;
    
    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COL_EMAIL = "email";
    private static final String COL_FIRST_NAME = "firstName";
    private static final String COL_LAST_NAME = "lastName";
    private static final String COL_PASSWORD = "password";
    
    // Transactions Table
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COL_TRANS_ID = "id";
    private static final String COL_USER_EMAIL = "userEmail";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_DATE = "date";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_TYPE = "type";
    
    // Categories Table
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COL_CAT_ID = "id";
    private static final String COL_CAT_NAME = "categoryName";
    private static final String COL_CAT_TYPE = "type";
    
    // Budgets Table
    private static final String TABLE_BUDGETS = "budgets";
    private static final String COL_BUDGET_ID = "id";
    private static final String COL_LIMIT_AMOUNT = "limitAmount";
    private static final String COL_ALERT_THRESHOLD = "alertThreshold";
    private static final String COL_MONTH = "month";
    private static final String COL_YEAR = "year";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_EMAIL + " TEXT PRIMARY KEY, " +
                COL_FIRST_NAME + " TEXT NOT NULL, " +
                COL_LAST_NAME + " TEXT NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);
        
        // Create Transactions Table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_DATE + " INTEGER NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_TYPE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COL_EMAIL + "))";
        db.execSQL(createTransactionsTable);
        
        // Create Categories Table
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT NOT NULL, " +
                COL_CAT_NAME + " TEXT NOT NULL, " +
                COL_CAT_TYPE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COL_EMAIL + "))";
        db.execSQL(createCategoriesTable);
        
        // Create Budgets Table
        String createBudgetsTable = "CREATE TABLE " + TABLE_BUDGETS + " (" +
                COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_LIMIT_AMOUNT + " REAL NOT NULL, " +
                COL_ALERT_THRESHOLD + " INTEGER DEFAULT 50, " +
                COL_MONTH + " INTEGER NOT NULL, " +
                COL_YEAR + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COL_EMAIL + "))";
        db.execSQL(createBudgetsTable);
        
        // Insert default categories for easier start
        insertDefaultCategories(db);
    }
    
    private void insertDefaultCategories(SQLiteDatabase db) {
        // Default expense categories
        String[] expenseCategories = {"Food", "Transportation", "Bills", "Entertainment", "Shopping", "Health", "Other"};
        // Default income categories
        String[] incomeCategories = {"Salary", "Scholarship", "Freelance", "Investment", "Gift", "Other"};
        
        // These will be added per user when they sign up
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    // ==================== USER OPERATIONS ====================
    
    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (not recommended for production)
        }
    }
    
    public boolean addUser(String email, String firstName, String lastName, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME, lastName);
        values.put(COL_PASSWORD, hashPassword(password));
        
        long result = db.insert(TABLE_USERS, null, values);
        
        // Add default categories for this user
        if (result != -1) {
            addDefaultCategoriesForUser(email);
        }
        
        return result != -1;
    }
    
    private void addDefaultCategoriesForUser(String email) {
        String[] expenseCategories = {"Food", "Transportation", "Bills", "Entertainment", "Shopping", "Health", "Other"};
        String[] incomeCategories = {"Salary", "Scholarship", "Freelance", "Investment", "Gift", "Other"};
        
        for (String category : expenseCategories) {
            addCategory(email, category, "expense");
        }
        
        for (String category : incomeCategories) {
            addCategory(email, category, "income");
        }
    }
    
    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_EMAIL},
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, hashedPassword},
                null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_EMAIL},
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    
    public User getUserInfo(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
            user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME)));
            user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_NAME)));
        }
        cursor.close();
        return user;
    }
    
    public boolean updateUserProfile(String email, String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME, lastName);
        
        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }
    
    public boolean updateUserPassword(String email, String oldPassword, String newPassword) {
        // First verify old password
        if (!checkUserLogin(email, oldPassword)) {
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, hashPassword(newPassword));
        
        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }
    
    // ==================== TRANSACTION OPERATIONS ====================
    
    public long addTransaction(String userEmail, double amount, long date, String category, String description, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_EMAIL, userEmail);
        values.put(COL_AMOUNT, amount);
        values.put(COL_DATE, date);
        values.put(COL_CATEGORY, category);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_TYPE, type);
        
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }
    
    public List<Transaction> getAllTransactions(String userEmail, String type) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_TRANSACTIONS + 
                       " WHERE " + COL_USER_EMAIL + "=? AND " + COL_TYPE + "=?" +
                       " ORDER BY " + COL_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{userEmail, type});
        
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_ID)));
                transaction.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)));
                transaction.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DATE)));
                transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
                transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }
    
    public List<Transaction> getTransactionsByPeriod(String userEmail, String type, long startDate, long endDate) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_TRANSACTIONS + 
                       " WHERE " + COL_USER_EMAIL + "=? AND " + COL_TYPE + "=?" +
                       " AND " + COL_DATE + " BETWEEN ? AND ?" +
                       " ORDER BY " + COL_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{userEmail, type, String.valueOf(startDate), String.valueOf(endDate)});
        
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_ID)));
                transaction.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)));
                transaction.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DATE)));
                transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
                transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }
    
    public boolean updateTransaction(int id, double amount, long date, String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AMOUNT, amount);
        values.put(COL_DATE, date);
        values.put(COL_CATEGORY, category);
        values.put(COL_DESCRIPTION, description);
        
        int rows = db.update(TABLE_TRANSACTIONS, values, COL_TRANS_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
    
    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TRANSACTIONS, COL_TRANS_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
    
    public double getTotalAmount(String userEmail, String type, long startDate, long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                       " WHERE " + COL_USER_EMAIL + "=? AND " + COL_TYPE + "=?" +
                       " AND " + COL_DATE + " BETWEEN ? AND ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{userEmail, type, String.valueOf(startDate), String.valueOf(endDate)});
        
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
    
    // ==================== CATEGORY OPERATIONS ====================
    
    public boolean addCategory(String userEmail, String categoryName, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_EMAIL, userEmail);
        values.put(COL_CAT_NAME, categoryName);
        values.put(COL_CAT_TYPE, type);
        
        long result = db.insert(TABLE_CATEGORIES, null, values);
        return result != -1;
    }
    
    public List<String> getCategories(String userEmail, String type) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{COL_CAT_NAME},
                COL_USER_EMAIL + "=? AND " + COL_CAT_TYPE + "=?",
                new String[]{userEmail, type},
                null, null, COL_CAT_NAME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }
    
    public boolean deleteCategory(String userEmail, String categoryName, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CATEGORIES,
                COL_USER_EMAIL + "=? AND " + COL_CAT_NAME + "=? AND " + COL_CAT_TYPE + "=?",
                new String[]{userEmail, categoryName, type});
        return rows > 0;
    }
    
    // ==================== BUDGET OPERATIONS ====================
    
    public long addBudget(String userEmail, String category, double limitAmount, int alertThreshold, int month, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_EMAIL, userEmail);
        values.put(COL_CATEGORY, category);
        values.put(COL_LIMIT_AMOUNT, limitAmount);
        values.put(COL_ALERT_THRESHOLD, alertThreshold);
        values.put(COL_MONTH, month);
        values.put(COL_YEAR, year);
        
        return db.insert(TABLE_BUDGETS, null, values);
    }
    
    public Budget getBudget(String userEmail, String category, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_BUDGETS, null,
                COL_USER_EMAIL + "=? AND " + COL_CATEGORY + "=? AND " + COL_MONTH + "=? AND " + COL_YEAR + "=?",
                new String[]{userEmail, category, String.valueOf(month), String.valueOf(year)},
                null, null, null);
        
        Budget budget = null;
        if (cursor.moveToFirst()) {
            budget = new Budget();
            budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BUDGET_ID)));
            budget.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            budget.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
            budget.setLimitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LIMIT_AMOUNT)));
            budget.setAlertThreshold(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALERT_THRESHOLD)));
            budget.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MONTH)));
            budget.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COL_YEAR)));
        }
        cursor.close();
        return budget;
    }
    
    public double getSpentInCategory(String userEmail, String category, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Get first and last day of the month
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        long startDate = calendar.getTimeInMillis();
        
        calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59);
        long endDate = calendar.getTimeInMillis();
        
        String query = "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                       " WHERE " + COL_USER_EMAIL + "=? AND " + COL_CATEGORY + "=? AND " + COL_TYPE + "='expense'" +
                       " AND " + COL_DATE + " BETWEEN ? AND ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{userEmail, category, String.valueOf(startDate), String.valueOf(endDate)});
        
        double spent = 0;
        if (cursor.moveToFirst()) {
            spent = cursor.getDouble(0);
        }
        cursor.close();
        return spent;
    }
}
