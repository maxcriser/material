package com.example.maksim_zakharenka.flexiblespacewithimage.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.maksim_zakharenka.flexiblespacewithimage.R;
import com.example.maksim_zakharenka.flexiblespacewithimage.bottommenu.BottomDialog;
import com.example.maksim_zakharenka.flexiblespacewithimage.bottommenu.Item;

public class BottomMenuTest extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_menu_test);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                final BottomDialog dialog = new BottomDialog(BottomMenuTest.this);
                dialog.title(R.string.app_name);
                dialog.canceledOnTouchOutside(true);
                dialog.cancelable(true);
                dialog.inflateMenu(R.menu.menu_main);
                dialog.setOnItemSelectedListener(new BottomDialog.OnItemSelectedListener() {

                    @Override
                    public boolean onItemSelected(final int id) {
                        switch (id) {
                            case R.id.action_social_share:
                                final Intent s = new Intent(android.content.Intent.ACTION_SEND);
                                s.setType("text/plain");
                                s.putExtra(android.content.Intent.EXTRA_TEXT, "https://github.com/rebus007/BottomDialog/issues");
                                startActivity(Intent.createChooser(s, "action_social_share"));
                                return true;
                            case R.id.action_content_add:
                                final Item item = new Item();
                                item.setTitle("New element");
                                item.setIcon(getResources().getDrawable(R.drawable.ic_hotstop));
                                item.setId(100);
                                dialog.addItem(item);
                                return false;
                            case R.id.action_delete:
                                finish();
                                return true;
                            case R.id.action_bug_report:
                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://github.com/rebus007/BottomDialog/issues"));
                                startActivity(i);
                                return true;
                            case R.id.ic_github:
                                final Intent g = new Intent(Intent.ACTION_VIEW);
                                g.setData(Uri.parse("https://github.com/rebus007/BottomDialog"));
                                startActivity(g);
                                return true;
                            case 100:
                                Toast.makeText(BottomMenuTest.this, "New element clicked!!!", Toast.LENGTH_SHORT).show();
                                return false;
                            default:
                                return false;
                        }
                    }
                });
                dialog.show();
            }
        });
    }
}
