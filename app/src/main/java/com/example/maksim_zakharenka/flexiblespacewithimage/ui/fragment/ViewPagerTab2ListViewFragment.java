package com.example.maksim_zakharenka.flexiblespacewithimage.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.maksim_zakharenka.flexiblespacewithimage.ui.activity.BaseFragment;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.ObservableRecyclerView;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.ObservableScrollViewCallbacks;
import com.example.maksim_zakharenka.flexiblespacewithimage.R;

public class ViewPagerTab2ListViewFragment extends BaseFragment {
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        final Activity parentActivity = getActivity();
        final ObservableRecyclerView recyclerView = (ObservableRecyclerView) view.findViewById(R.id.scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(parentActivity));
        recyclerView.setHasFixedSize(false);
        setDummyData(recyclerView);
        recyclerView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            recyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }
        return view;
    }
}
