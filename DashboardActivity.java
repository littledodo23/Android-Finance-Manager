package com.finance.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    
    private String userEmail;
    private DatabaseHelper databaseHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        userEmail = getIntent().getStringExtra("userEmail");
        databaseHelper = new DatabaseHelper(this);
        
        initializeViews();
        setupNavigationDrawer();
        updateNavHeader();
        
        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Home");
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        setSupportActionBar(toolbar);
    }
    
    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        navigationView.setNavigationItemSelectedListener(this);
    }
    
    private void updateNavHeader() {
        User user = databaseHelper.getUserInfo(userEmail);
        if (user != null) {
            TextView nameText = navigationView.getHeaderView(0).findViewById(R.id.userNameText);
            TextView emailText = navigationView.getHeaderView(0).findViewById(R.id.userEmailText);
            
            nameText.setText(user.getFullName());
            emailText.setText(user.getEmail());
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        Fragment fragment = null;
        String title = "";
        
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
            title = "Home";
        } else if (id == R.id.nav_income) {
            fragment = new IncomeFragment();
            title = "Income";
        } else if (id == R.id.nav_expenses) {
            fragment = new ExpensesFragment();
            title = "Expenses";
        } else if (id == R.id.nav_budgets) {
            fragment = new BudgetsFragment();
            title = "Budgets & Goals";
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
            title = "Settings";
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            title = "Profile";
        } else if (id == R.id.nav_logout) {
            logout();
            return true;
        }
        
        if (fragment != null) {
            loadFragment(fragment, title);
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    
    private void loadFragment(Fragment fragment, String title) {
        Bundle bundle = new Bundle();
        bundle.putString("userEmail", userEmail);
        fragment.setArguments(bundle);
        
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
    
    private void logout() {
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
