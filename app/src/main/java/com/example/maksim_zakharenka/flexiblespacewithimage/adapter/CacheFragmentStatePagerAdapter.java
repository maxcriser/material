package com.example.maksim_zakharenka.flexiblespacewithimage.adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

abstract class CacheFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private static final String STATE_SUPER_STATE = "superState";
    private static final String STATE_PAGES = "pages";
    private static final String STATE_PAGE_INDEX_PREFIX = "pageIndex:";
    private static final String STATE_PAGE_KEY_PREFIX = "page:";

    private final FragmentManager mFm;
    private final SparseArray<Fragment> mPages;

    CacheFragmentStatePagerAdapter(final FragmentManager fm) {
        super(fm);
        mPages = new SparseArray<>();
        mFm = fm;
    }

    @Override
    public Parcelable saveState() {
        final Parcelable p = super.saveState();
        final Bundle bundle = new Bundle();

        bundle.putParcelable(STATE_SUPER_STATE, p);
        bundle.putInt(STATE_PAGES, mPages.size());

        if (mPages.size() > 0) {
            for (int i = 0; i < mPages.size(); i++) {
                final int position = mPages.keyAt(i);
                bundle.putInt(createCacheIndex(i), position);

                final Fragment f = mPages.get(position);
                mFm.putFragment(bundle, createCacheKey(position), f);
            }
        }
        return bundle;
    }

    @Override
    public void restoreState(final Parcelable state, final ClassLoader loader) {
        final Bundle bundle = (Bundle) state;
        final int pages = bundle.getInt(STATE_PAGES);

        if (pages > 0) {
            for (int i = 0; i < pages; i++) {
                final int position = bundle.getInt(createCacheIndex(i));
                final Fragment f = mFm.getFragment(bundle, createCacheKey(position));
                mPages.put(position, f);
            }
        }

        final Parcelable p = bundle.getParcelable(STATE_SUPER_STATE);
        super.restoreState(p, loader);
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment f = createItem(position);
        mPages.put(position, f);

        return f;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        if (mPages.indexOfKey(position) >= 0) {
            mPages.remove(position);
        }
        super.destroyItem(container, position, object);
    }

    public Fragment getItemAt(final int position) {
        return mPages.get(position);
    }

    protected abstract Fragment createItem(int position);

    private String createCacheIndex(final int index) {
        return STATE_PAGE_INDEX_PREFIX + index;
    }

    private String createCacheKey(final int position) {
        return STATE_PAGE_KEY_PREFIX + position;
    }
}
