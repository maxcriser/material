package com.example.maksim_zakharenka.flexiblespacewithimage.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.maksim_zakharenka.flexiblespacewithimage.R;

public class DemoMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_menu);
    }

    public void openMainActivity(final View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void openBottomMenu(final View view) {
        startActivity(new Intent(this, BottomMenuTest.class));
    }

    public void openLoadingFB(final View view) {
        startActivity(new Intent(this, FabLoadingActivityTest.class));
    }

}
