package hu.szte.rubikscubecamera.ui.guide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import java.util.Objects;

import hu.szte.rubikscubecamera.R;

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        Toolbar guideToolbar = (Toolbar) findViewById(R.id.guide_toolbar);
        setSupportActionBar(guideToolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}