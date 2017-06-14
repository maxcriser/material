package com.example.maksim_zakharenka.flexiblespacewithimage.util.header;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class TouchInterceptionFrameLayout extends FrameLayout {

    public interface TouchInterceptionListener {

        boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffX, float diffY);

        void onDownMotionEvent(MotionEvent ev);

        void onMoveMotionEvent(MotionEvent ev, float diffX, float diffY);

        void onUpOrCancelMotionEvent(MotionEvent ev);
    }

    private boolean mIntercepting;
    private boolean mDownMotionEventPended;
    private boolean mBeganFromDownMotionEvent;
    private boolean mChildrenEventsCanceled;
    private PointF mInitialPoint;
    private MotionEvent mPendingDownMotionEvent;
    private TouchInterceptionListener mTouchInterceptionListener;

    public TouchInterceptionFrameLayout(final Context context) {
        super(context);
    }

    public TouchInterceptionFrameLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchInterceptionFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchInterceptionFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollInterceptionListener(final TouchInterceptionListener listener) {
        mTouchInterceptionListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (mTouchInterceptionListener == null) {
            return false;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialPoint = new PointF(ev.getX(), ev.getY());
                mPendingDownMotionEvent = MotionEvent.obtainNoHistory(ev);
                mDownMotionEventPended = true;
                mIntercepting = mTouchInterceptionListener.shouldInterceptTouchEvent(ev, false, 0, 0);
                mBeganFromDownMotionEvent = mIntercepting;
                mChildrenEventsCanceled = false;
                return mIntercepting;
            case MotionEvent.ACTION_MOVE:
                if (mInitialPoint == null) {
                    mInitialPoint = new PointF(ev.getX(), ev.getY());
                }

                final float diffX = ev.getX() - mInitialPoint.x;
                final float diffY = ev.getY() - mInitialPoint.y;
                mIntercepting = mTouchInterceptionListener.shouldInterceptTouchEvent(ev, true, diffX, diffY);
                return mIntercepting;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        if (mTouchInterceptionListener != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (mIntercepting) {
                        mTouchInterceptionListener.onDownMotionEvent(ev);
                        duplicateTouchEventForChildren(ev);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mInitialPoint == null) {
                        mInitialPoint = new PointF(ev.getX(), ev.getY());
                    }

                    float diffX = ev.getX() - mInitialPoint.x;
                    float diffY = ev.getY() - mInitialPoint.y;
                    mIntercepting = mTouchInterceptionListener.shouldInterceptTouchEvent(ev, true, diffX, diffY);
                    if (mIntercepting) {
                        if (!mBeganFromDownMotionEvent) {
                            mBeganFromDownMotionEvent = true;

                            final MotionEvent event = MotionEvent.obtainNoHistory(mPendingDownMotionEvent);
                            event.setLocation(ev.getX(), ev.getY());
                            mTouchInterceptionListener.onDownMotionEvent(event);

                            mInitialPoint = new PointF(ev.getX(), ev.getY());
                            diffX = diffY = 0;
                        }

                        if (!mChildrenEventsCanceled) {
                            mChildrenEventsCanceled = true;
                            duplicateTouchEventForChildren(obtainMotionEvent(ev, MotionEvent.ACTION_CANCEL));
                        }

                        mTouchInterceptionListener.onMoveMotionEvent(ev, diffX, diffY);

                        mDownMotionEventPended = true;

                        return true;
                    } else {
                        if (mDownMotionEventPended) {
                            mDownMotionEventPended = false;
                            final MotionEvent event = MotionEvent.obtainNoHistory(mPendingDownMotionEvent);
                            event.setLocation(ev.getX(), ev.getY());
                            duplicateTouchEventForChildren(ev, event);
                        } else {
                            duplicateTouchEventForChildren(ev);
                        }

                        mBeganFromDownMotionEvent = false;

                        mChildrenEventsCanceled = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mBeganFromDownMotionEvent = false;
                    if (mIntercepting) {
                        mTouchInterceptionListener.onUpOrCancelMotionEvent(ev);
                    }

                    if (!mChildrenEventsCanceled) {
                        mChildrenEventsCanceled = true;
                        if (mDownMotionEventPended) {
                            mDownMotionEventPended = false;
                            final MotionEvent event = MotionEvent.obtainNoHistory(mPendingDownMotionEvent);
                            event.setLocation(ev.getX(), ev.getY());
                            duplicateTouchEventForChildren(ev, event);
                        } else {
                            duplicateTouchEventForChildren(ev);
                        }
                    }
                    return true;
            }
        }
        return super.onTouchEvent(ev);
    }

    private MotionEvent obtainMotionEvent(final MotionEvent base, final int action) {
        final MotionEvent ev = MotionEvent.obtainNoHistory(base);
        ev.setAction(action);
        return ev;
    }

    private void duplicateTouchEventForChildren(final MotionEvent ev, final MotionEvent... pendingEvents) {
        if (ev == null) {
            return;
        }
        for (int i = getChildCount() - 1; 0 <= i; i--) {
            final View childView = getChildAt(i);
            if (childView != null) {
                final Rect childRect = new Rect();
                childView.getHitRect(childRect);
                final MotionEvent event = MotionEvent.obtainNoHistory(ev);
                if (!childRect.contains((int) event.getX(), (int) event.getY())) {
                    continue;
                }
                final float offsetX = -childView.getLeft();
                final float offsetY = -childView.getTop();
                boolean consumed = false;
                if (pendingEvents != null) {
                    for (final MotionEvent pe : pendingEvents) {
                        if (pe != null) {
                            final MotionEvent peAdjusted = MotionEvent.obtainNoHistory(pe);
                            peAdjusted.offsetLocation(offsetX, offsetY);
                            consumed |= childView.dispatchTouchEvent(peAdjusted);
                        }
                    }
                }
                event.offsetLocation(offsetX, offsetY);
                consumed |= childView.dispatchTouchEvent(event);
                if (consumed) {
                    break;
                }
            }
        }
    }
}
