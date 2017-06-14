package com.example.maksim_zakharenka.flexiblespacewithimage.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.maksim_zakharenka.flexiblespacewithimage.ui.fragment.ViewPagerTab2GridViewFragment;
import com.example.maksim_zakharenka.flexiblespacewithimage.ui.fragment.ViewPagerTab2ListViewFragment;
import com.example.maksim_zakharenka.flexiblespacewithimage.ui.fragment.ViewPagerTab2RecyclerViewFragment;
import com.example.maksim_zakharenka.flexiblespacewithimage.ui.fragment.ViewPagerTab2ScrollViewFragment;
import com.example.maksim_zakharenka.flexiblespacewithimage.ui.fragment.ViewPagerTab2WebViewFragment;

public class NavigationAdapter extends CacheFragmentStatePagerAdapter {

    private static final String[] TITLES = new String[]{"Applepie", "Butter Cookie", "Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb", "Ice Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop"};

    public NavigationAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    protected Fragment createItem(final int position) {
        final Fragment f;
        final int pattern = position % 5;
        switch (pattern) {
            case 0:
                f = new ViewPagerTab2ScrollViewFragment();
                break;
            case 1:
                f = new ViewPagerTab2ListViewFragment();
                break;
            case 2:
                f = new ViewPagerTab2RecyclerViewFragment();
                break;
            case 3:
                f = new ViewPagerTab2GridViewFragment();
                break;
            case 4:
            default:
                f = new ViewPagerTab2WebViewFragment();
                break;
        }
        return f;
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return TITLES[position];
    }
}