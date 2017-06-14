package com.example.maksim_zakharenka.flexiblespacewithimage.util.header;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

class SlidingTabStrip extends LinearLayout {

    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 0;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 3;
    private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;

    private final int mBottomBorderThickness;
    private final Paint mBottomBorderPaint;

    private final int mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;

    private int mSelectedPosition;
    private float mSelectionOffset;

    private SlidingTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer mDefaultTabColorizer;

    SlidingTabStrip(final Context context) {
        this(context, null);
    }

    SlidingTabStrip(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorForeground, outValue, true);
        final int themeForegroundColor =  outValue.data;

        final int defaultBottomBorderColor = setColorAlpha(themeForegroundColor,
                DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        mDefaultTabColorizer = new SimpleTabColorizer();
        mDefaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);

        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(defaultBottomBorderColor);

        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();
    }

    void setCustomTabColorizer(final SlidingTabLayout.TabColorizer customTabColorizer) {
        mCustomTabColorizer = customTabColorizer;
        invalidate();
    }

    void setSelectedIndicatorColors(final int... colors) {
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    void onViewPagerPageChanged(final int position, final float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;

        if (childCount > 0) {
            final View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
                final int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                }

                final View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * nextTitle.getLeft() +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTitle.getRight() +
                        (1.0f - mSelectionOffset) * right);
            }

            mSelectedIndicatorPaint.setColor(color);

            canvas.drawRect(left, height - mSelectedIndicatorThickness, right,
                    height, mSelectedIndicatorPaint);
        }

        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);
    }

    private static int setColorAlpha(final int color, final byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int blendColors(final int color1, final int color2, final float ratio) {
        final float inverseRation = 1f - ratio;
        final float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        final float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        final float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
        private int[] mIndicatorColors;

        @Override
        public final int getIndicatorColor(final int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        void setIndicatorColors(final int... colors) {
            mIndicatorColors = colors;
        }
    }
}
