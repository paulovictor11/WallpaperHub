package com.websistems.wallpaperhub.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.ads.MobileAds;
import com.websistems.wallpaperhub.R;
import com.websistems.wallpaperhub.fragments.FavouritesFragment;
import com.websistems.wallpaperhub.fragments.HomeFragment;
import com.websistems.wallpaperhub.fragments.SettingsFragment;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MobileAds.initialize(this, "ca-app-pub-7501591673955748~7254359982");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        displayFragment(new HomeFragment());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;

        switch (item.getItemId()){
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;

            case R.id.nav_fav:
                fragment = new FavouritesFragment();
                break;

            case R.id.nav_set:
                fragment = new SettingsFragment();
                break;

            default:
                fragment = new HomeFragment();
        }

        displayFragment(fragment);

        return true;
    }

    private void displayFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_area, fragment)
                .commit();
    }
}
