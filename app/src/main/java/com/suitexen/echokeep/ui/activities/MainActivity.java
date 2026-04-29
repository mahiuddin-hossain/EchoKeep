package com.suitexen.echokeep.ui.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.suitexen.echokeep.R;
import com.suitexen.echokeep.ui.fragments.HomeFragment;
import com.suitexen.echokeep.ui.fragments.InventoryFragment;
import com.suitexen.echokeep.ui.fragments.ProfileFragment;
import com.suitexen.echokeep.ui.fragments.RecipesFragment;
import com.suitexen.echokeep.ui.fragments.StatsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_inventory) {
                loadFragment(new InventoryFragment());
                return true;
            } else if (itemId == R.id.nav_recipes) {
                loadFragment(new RecipesFragment());
                return true;
            } else if (itemId == R.id.nav_stats) {
                loadFragment(new StatsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}