package hu.szte.rubikscubecamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import hu.szte.rubikscubecamera.databinding.ActivityMainBinding;
import hu.szte.rubikscubecamera.ui.guide.GuideActivity;


public class MainActivity extends AppCompatActivity {

    private static final String FIRST_TIME_KEY = "isFirstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if it's the first time the app is opened
        SharedPreferences preferences = getSharedPreferences("PrefsFile", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);

        hu.szte.rubikscubecamera.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.getRoot();
        setContentView(binding.getRoot());

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_camera, R.id.navigation_cube, R.id.navigation_solution)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        if (isFirstTime) {
            preferences.edit().putBoolean(FIRST_TIME_KEY, false).apply();
            startActivity(new Intent(MainActivity.this, GuideActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navigation_guide) {
            Intent guideIntent = new Intent(MainActivity.this, GuideActivity.class);
            startActivity(guideIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}