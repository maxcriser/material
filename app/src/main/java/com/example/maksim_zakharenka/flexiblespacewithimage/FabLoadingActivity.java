package com.example.maksim_zakharenka.flexiblespacewithimage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.maksim_zakharenka.flexiblespacewithimage.completefab.FABProgressCircle;
import com.example.maksim_zakharenka.flexiblespacewithimage.completefab.FABProgressListener;

public class FabLoadingActivity extends AppCompatActivity implements FABProgressListener {

    private FABProgressCircle fabProgressCircle;

    @Override
    public void onFABProgressAnimationEnd() {
        Snackbar.make(fabProgressCircle, "Complete", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_fab);

        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);

        fabProgressCircle.attachListener(this);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                fabProgressCircle.show();

                findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        fabProgressCircle.beginFinalAnimation();
                    }
                });
            }
        });
    }
}