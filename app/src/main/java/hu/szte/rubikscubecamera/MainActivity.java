package hu.szte.rubikscubecamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import hu.szte.rubikscubecamera.databinding.ActivityMainBinding;
import hu.szte.rubikscubecamera.ui.guide.GuideActivity;

/**
 * Starting acvitity of the app. This activity contains
 * every fragment.
 */
public class MainActivity extends AppCompatActivity {
    private static final String FIRST_TIME_KEY = "isFirstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        SharedPreferences preferences = getSharedPreferences("PrefsFile", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);

        if (isFirstTime) {
            preferences.edit().putBoolean(FIRST_TIME_KEY, false).apply();
            startActivity(new Intent(MainActivity.this, GuideActivity.class));
        }
    }

    /**
     * If a MenuItem is selected, go to the correct activity.
     * In this case, the only activity is the guideActivity.
     *
     * @param item This item was selected by the user.
     * @return Returns true of selection was successful.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navigation_guide) {
            Intent guideIntent = new Intent(MainActivity.this, GuideActivity.class);
            startActivity(guideIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_nav_menu, menu);
        return true;
    }
}