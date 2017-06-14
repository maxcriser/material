package com.example.maksim_zakharenka.flexiblespacewithimage.ui.activity;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import com.example.maksim_zakharenka.flexiblespacewithimage.adapter.NavigationAdapter;
import com.example.maksim_zakharenka.flexiblespacewithimage.helper.ViewHelper;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.ObservableScrollViewCallbacks;
import com.example.maksim_zakharenka.flexiblespacewithimage.R;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.ScrollState;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.ScrollUtils;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.Scrollable;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.SlidingTabLayout;
import com.example.maksim_zakharenka.flexiblespacewithimage.util.header.TouchInterceptionFrameLayout;

public class MainActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private static final int INVALID_POINTER = -1;

    private View mImageView;
    private View mOverlayView;
    private TextView mTitleView;
    private TouchInterceptionFrameLayout mInterceptionLayout;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private VelocityTracker mVelocityTracker;
    private OverScroller mScroller;
    private float mBaseTranslationY;
    private int mMaximumVelocity;
    private int mActivePointerId = INVALID_POINTER;
    private int mSlop;
    private int mFlexibleSpaceHeight;
    private int mTabHeight;
    private boolean mScrolled;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ViewCompat.setElevation(findViewById(R.id.header), getResources().getDimension(R.dimen.toolbar_elevation));

        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mImageView = findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mTabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        findViewById(R.id.pager_wrapper).setPadding(0, mFlexibleSpaceHeight, 0, 0);
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(getTitle());
        setTitle(null);

        final SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);
        ((ViewGroup.MarginLayoutParams) slidingTabLayout.getLayoutParams()).topMargin = mFlexibleSpaceHeight - mTabHeight;

        final ViewConfiguration vc = ViewConfiguration.get(this);
        mSlop = vc.getScaledTouchSlop();
        mMaximumVelocity = vc.getScaledMaximumFlingVelocity();
        mInterceptionLayout = (TouchInterceptionFrameLayout) findViewById(R.id.container);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);
        mScroller = new OverScroller(getApplicationContext());

        ScrollUtils.addOnGlobalLayoutListener(mInterceptionLayout, new Runnable() {

            @Override
            public void run() {
                final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = getScreenHeight() + mFlexibleSpaceHeight;
                mInterceptionLayout.requestLayout();

                updateFlexibleSpace();
            }
        });
    }

    @Override
    public void onScrollChanged(final int scrollY, final boolean firstScroll, final boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(final ScrollState scrollState) {

    }

    private final TouchInterceptionFrameLayout.TouchInterceptionListener mInterceptionListener = new TouchInterceptionFrameLayout.TouchInterceptionListener() {

        @Override
        public boolean shouldInterceptTouchEvent(final MotionEvent ev, final boolean moving, final float diffX, final float diffY) {
            if (!mScrolled && mSlop < Math.abs(diffX) && Math.abs(diffY) < Math.abs(diffX)) {
                return false;
            }

            final Scrollable scrollable = getCurrentScrollable();

            if (scrollable == null) {
                mScrolled = false;
                return false;
            }

            final int flexibleSpace = mFlexibleSpaceHeight - mTabHeight - mTabHeight - (int) getResources().getDimension(R.dimen.top_margin_for_title);
            final int translationY = (int) ViewHelper.getTranslationY(mInterceptionLayout);
            final boolean scrollingUp = diffY > 0;
            final boolean scrollingDown = diffY < 0;

            if (scrollingUp) {
                if (translationY < 0) {
                    mScrolled = true;

                    return true;
                }
            } else if (scrollingDown) {
                if (-flexibleSpace < translationY) {
                    mScrolled = true;

                    return true;
                }
            }

            mScrolled = false;

            return false;
        }

        @Override
        public void onDownMotionEvent(final MotionEvent ev) {
            mActivePointerId = ev.getPointerId(0);
            mScroller.forceFinished(true);

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            } else {
                mVelocityTracker.clear();
            }

            mBaseTranslationY = ViewHelper.getTranslationY(mInterceptionLayout);
            mVelocityTracker.addMovement(ev);
        }

        @Override
        public void onMoveMotionEvent(final MotionEvent ev, final float diffX, final float diffY) {
            final int flexibleSpace = mFlexibleSpaceHeight - mTabHeight - mTabHeight - (int) getResources().getDimension(R.dimen.top_margin_for_title);
            final float translationY = ScrollUtils.getFloat(ViewHelper.getTranslationY(mInterceptionLayout) + diffY, -flexibleSpace, 0);
            final MotionEvent e = MotionEvent.obtainNoHistory(ev);

            e.offsetLocation(0, translationY - mBaseTranslationY);
            mVelocityTracker.addMovement(e);
            updateFlexibleSpace(translationY);
        }

        @Override
        public void onUpOrCancelMotionEvent(final MotionEvent ev) {
            mScrolled = false;
            mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

            final int velocityY = (int) mVelocityTracker.getYVelocity(mActivePointerId);

            mActivePointerId = INVALID_POINTER;
            mScroller.forceFinished(true);

            final int baseTranslationY = (int) ViewHelper.getTranslationY(mInterceptionLayout);

            final int minY = -(mFlexibleSpaceHeight - mTabHeight - mTabHeight - (int) getResources().getDimension(R.dimen.top_margin_for_title));
            final int maxY = 0;

            mScroller.fling(0, baseTranslationY, 0, velocityY, 0, 0, minY, maxY);

            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    updateLayout();
                }
            });
        }
    };

    private void updateLayout() {
        boolean needsUpdate = false;
        float translationY = 0;

        if (mScroller.computeScrollOffset()) {
            translationY = mScroller.getCurrY();

            final int flexibleSpace = mFlexibleSpaceHeight - mTabHeight;

            if (-flexibleSpace <= translationY && translationY <= 0) {
                needsUpdate = true;
            } else if (translationY < -flexibleSpace) {
                translationY = -flexibleSpace;
                needsUpdate = true;
            } else if (translationY > 0) {
                translationY = 0;
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            updateFlexibleSpace(translationY);

            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    updateLayout();
                }
            });
        }
    }

    private Scrollable getCurrentScrollable() {
        final Fragment fragment = getCurrentFragment();

        if (fragment == null) {
            return null;
        }

        final View view = fragment.getView();

        if (view == null) {
            return null;
        }

        return (Scrollable) view.findViewById(R.id.scroll);
    }

    private void updateFlexibleSpace() {
        updateFlexibleSpace(ViewHelper.getTranslationY(mInterceptionLayout));
    }

    private void updateFlexibleSpace(final float translationY) {
        ViewHelper.setTranslationY(mInterceptionLayout, translationY);
        final int minOverlayTransitionY = getActionBarSize() - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-translationY / 2, minOverlayTransitionY, 0));

//         Change alpha of overlay
        final float flexibleRange = mFlexibleSpaceHeight - getActionBarSize() - mTabHeight - (int) getResources().getDimension(R.dimen.top_margin_for_title);
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat(-translationY / flexibleRange, 0, 1));
        final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        mlp.setMargins((int) ((getResources().getDimension(R.dimen.toolbar_height) * 0.8) * (-translationY / flexibleRange)), 0, 0, mTabHeight);
        mTitleView.setLayoutParams(mlp);

//         Scale title text
        float scale = (float) Math.sqrt(1 + (-translationY / flexibleRange));
        setPivotXToTitle();
        scale = (float) 2.44 - scale;
        ViewHelper.setScaleX(mTitleView, scale);
        ViewHelper.setScaleY(mTitleView, scale);
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setPivotXToTitle() {

        final Configuration config = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setPivotX(mTitleView, findViewById(android.R.id.content).getWidth());
        } else {
            ViewHelper.setPivotX(mTitleView, 0);
        }
    }
}
