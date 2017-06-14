package com.example.maksim_zakharenka.flexiblespacewithimage.ui.activity;

import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.example.maksim_zakharenka.flexiblespacewithimage.R;
import com.example.maksim_zakharenka.flexiblespacewithimage.adapter.SimpleHeaderRecyclerAdapter;
import com.example.maksim_zakharenka.flexiblespacewithimage.adapter.SimpleRecyclerAdapter;

import java.util.ArrayList;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int NUM_OF_ITEMS = 100;
    private static final int NUM_OF_ITEMS_FEW = 3;

    protected int getActionBarSize() {
        final TypedValue typedValue = new TypedValue();
        final int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        final int indexOfAttrTextSize = 0;
        final TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        final int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);

        a.recycle();

        return actionBarSize;
    }

    protected int getScreenHeight() {
        return findViewById(android.R.id.content).getHeight();
    }

    public static ArrayList<String> getDummyData() {
        return getDummyData(NUM_OF_ITEMS);
    }

    public static ArrayList<String> getDummyData(final int num) {
        final ArrayList<String> items = new ArrayList<>();
        for (int i = 1; i <= num; i++) {
            items.add("Item " + i);
        }
        return items;
    }

    protected void setDummyData(final ListView listView) {
        setDummyData(listView, NUM_OF_ITEMS);
    }

    protected void setDummyDataFew(final ListView listView) {
        setDummyData(listView, NUM_OF_ITEMS_FEW);
    }

    protected void setDummyData(final ListView listView, final int num) {
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getDummyData(num)));
    }

    protected void setDummyDataWithHeader(final ListView listView, final int headerHeight) {
        setDummyDataWithHeader(listView, headerHeight, NUM_OF_ITEMS);
    }

    protected void setDummyDataWithHeader(final ListView listView, final int headerHeight, final int num) {
        final View headerView = new View(this);

        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerHeight));
        headerView.setMinimumHeight(headerHeight);
        headerView.setClickable(true);
        setDummyDataWithHeader(listView, headerView, num);
    }

    protected void setDummyDataWithHeader(final ListView listView, final View headerView, final int num) {
        listView.addHeaderView(headerView);
        setDummyData(listView, num);
    }

    protected void setDummyData(final GridView gridView) {
        gridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getDummyData()));
    }

    protected void setDummyData(final RecyclerView recyclerView) {
        setDummyData(recyclerView, NUM_OF_ITEMS);
    }

    protected void setDummyDataFew(final RecyclerView recyclerView) {
        setDummyData(recyclerView, NUM_OF_ITEMS_FEW);
    }

    protected void setDummyData(final RecyclerView recyclerView, final int num) {
        recyclerView.setAdapter(new SimpleRecyclerAdapter(this, getDummyData(num)));
    }

    protected void setDummyDataWithHeader(final RecyclerView recyclerView, final int headerHeight) {
        final View headerView = new View(this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerHeight));
        headerView.setMinimumHeight(headerHeight);
        headerView.setClickable(true);
        setDummyDataWithHeader(recyclerView, headerView);
    }

    protected void setDummyDataWithHeader(final RecyclerView recyclerView, final View headerView) {
        recyclerView.setAdapter(new SimpleHeaderRecyclerAdapter(this, getDummyData(), headerView));
    }
}
