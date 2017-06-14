package com.example.maksim_zakharenka.flexiblespacewithimage.ui.activity;

import android.app.Activity;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.example.maksim_zakharenka.flexiblespacewithimage.adapter.SimpleHeaderRecyclerAdapter;
import com.example.maksim_zakharenka.flexiblespacewithimage.adapter.SimpleRecyclerAdapter;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.ObservableGridView;
import com.example.maksim_zakharenka.flexiblespacewithimage.R;

import java.util.ArrayList;

public abstract class BaseFragment extends Fragment {

    public static ArrayList<String> getDummyData() {
        return BaseActivity.getDummyData();
    }

    protected int getActionBarSize() {
        final Activity activity = getActivity();

        if (activity == null) {
            return 0;
        }

        final TypedValue typedValue = new TypedValue();
        final int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        final int indexOfAttrTextSize = 0;
        final TypedArray a = activity.obtainStyledAttributes(typedValue.data, textSizeAttr);
        final int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);

        a.recycle();

        return actionBarSize;
    }

    protected int getScreenHeight() {
        final Activity activity = getActivity();

        if (activity == null) {
            return 0;
        }

        return activity.findViewById(android.R.id.content).getHeight();
    }

    protected void setDummyData(final ListView listView) {
        listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getDummyData()));
    }

    protected void setDummyDataWithHeader(final ListView listView, final View headerView) {
        listView.addHeaderView(headerView);
        setDummyData(listView);
    }

    protected void setDummyData(final GridView gridView) {
        gridView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getDummyData()));
    }

    protected void setDummyDataWithHeader(final ObservableGridView gridView, final View headerView) {
        gridView.addHeaderView(headerView);
        setDummyData(gridView);
    }

    protected void setDummyData(final RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleRecyclerAdapter(getActivity(), getDummyData()));
    }

    protected void setDummyDataWithHeader(final RecyclerView recyclerView, final View headerView) {
        recyclerView.setAdapter(new SimpleHeaderRecyclerAdapter(getActivity(), getDummyData(), headerView));
    }
}
