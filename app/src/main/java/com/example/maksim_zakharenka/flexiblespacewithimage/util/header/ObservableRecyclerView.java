package com.example.maksim_zakharenka.flexiblespacewithimage.util.header;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ObservableRecyclerView extends RecyclerView implements Scrollable {

    private static int recyclerViewLibraryVersion = 22;

    private int mPrevFirstVisiblePosition;
    private int mPrevFirstVisibleChildHeight = -1;
    private int mPrevScrolledChildrenHeight;
    private int mPrevScrollY;
    private int mScrollY;
    private SparseIntArray mChildrenHeights;

    private ObservableScrollViewCallbacks mCallbacks;
    private List<ObservableScrollViewCallbacks> mCallbackCollection;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;
    private boolean mIntercepted;
    private MotionEvent mPrevMoveEvent;
    private ViewGroup mTouchInterceptionViewGroup;

    public ObservableRecyclerView(final Context context) {
        super(context);
        init();
    }

    public ObservableRecyclerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ObservableRecyclerView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        final SavedState ss = (SavedState) state;
        mPrevFirstVisiblePosition = ss.prevFirstVisiblePosition;
        mPrevFirstVisibleChildHeight = ss.prevFirstVisibleChildHeight;
        mPrevScrolledChildrenHeight = ss.prevScrolledChildrenHeight;
        mPrevScrollY = ss.prevScrollY;
        mScrollY = ss.scrollY;
        mChildrenHeights = ss.childrenHeights;

        super.onRestoreInstanceState(ss.getSuperState());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);

        ss.prevFirstVisiblePosition = mPrevFirstVisiblePosition;
        ss.prevFirstVisibleChildHeight = mPrevFirstVisibleChildHeight;
        ss.prevScrolledChildrenHeight = mPrevScrolledChildrenHeight;
        ss.prevScrollY = mPrevScrollY;
        ss.scrollY = mScrollY;
        ss.childrenHeights = mChildrenHeights;

        return ss;
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (hasNoCallbacks()) {
            return;
        }

        if (getChildCount() > 0) {
            final int firstVisiblePosition = getChildAdapterPosition(getChildAt(0));
            final int lastVisiblePosition = getChildAdapterPosition(getChildAt(getChildCount() - 1));

            for (int i = firstVisiblePosition, j = 0; i <= lastVisiblePosition; i++, j++) {
                int childHeight = 0;
                final View child = getChildAt(j);
                if (child != null) {
                    if (mChildrenHeights.indexOfKey(i) < 0 || (child.getHeight() != mChildrenHeights.get(i))) {
                        childHeight = child.getHeight();
                    }
                }
                mChildrenHeights.put(i, childHeight);
            }

            final View firstVisibleChild = getChildAt(0);

            if (firstVisibleChild != null) {
                if (mPrevFirstVisiblePosition < firstVisiblePosition) {
                    int skippedChildrenHeight = 0;
                    if (firstVisiblePosition - mPrevFirstVisiblePosition != 1) {
                        for (int i = firstVisiblePosition - 1; i > mPrevFirstVisiblePosition; i--) {
                            if (mChildrenHeights.indexOfKey(i) > 0) {
                                skippedChildrenHeight += mChildrenHeights.get(i);
                            } else {
                                skippedChildrenHeight += firstVisibleChild.getHeight();
                            }
                        }
                    }
                    mPrevScrolledChildrenHeight += mPrevFirstVisibleChildHeight + skippedChildrenHeight;
                    mPrevFirstVisibleChildHeight = firstVisibleChild.getHeight();
                } else if (firstVisiblePosition < mPrevFirstVisiblePosition) {
                    int skippedChildrenHeight = 0;

                    if (mPrevFirstVisiblePosition - firstVisiblePosition != 1) {
                        for (int i = mPrevFirstVisiblePosition - 1; i > firstVisiblePosition; i--) {
                            if (mChildrenHeights.indexOfKey(i) > 0) {
                                skippedChildrenHeight += mChildrenHeights.get(i);
                            } else {
                                skippedChildrenHeight += firstVisibleChild.getHeight();
                            }
                        }
                    }
                    mPrevScrolledChildrenHeight -= firstVisibleChild.getHeight() + skippedChildrenHeight;
                    mPrevFirstVisibleChildHeight = firstVisibleChild.getHeight();
                } else if (firstVisiblePosition == 0) {
                    mPrevFirstVisibleChildHeight = firstVisibleChild.getHeight();
                    mPrevScrolledChildrenHeight = 0;
                }

                if (mPrevFirstVisibleChildHeight < 0) {
                    mPrevFirstVisibleChildHeight = 0;
                }

                mScrollY = mPrevScrolledChildrenHeight - firstVisibleChild.getTop() + getPaddingTop();
                mPrevFirstVisiblePosition = firstVisiblePosition;

                dispatchOnScrollChanged(mScrollY, mFirstScroll, mDragging);
                if (mFirstScroll) {
                    mFirstScroll = false;
                }

                if (mPrevScrollY < mScrollY) {
                    mScrollState = ScrollState.UP;
                } else if (mScrollY < mPrevScrollY) {
                    mScrollState = ScrollState.DOWN;
                } else {
                    mScrollState = ScrollState.STOP;
                }

                mPrevScrollY = mScrollY;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (hasNoCallbacks()) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mFirstScroll = mDragging = true;
                dispatchOnDownMotionEvent();
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        if (hasNoCallbacks()) {
            return super.onTouchEvent(ev);
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                mIntercepted = false;
                mDragging = false;
                dispatchOnUpOrCancelMotionEvent(mScrollState);

                break;

            case MotionEvent.ACTION_MOVE:
                if (mPrevMoveEvent == null) {
                    mPrevMoveEvent = ev;
                }

                final float diffY = ev.getY() - mPrevMoveEvent.getY();
                mPrevMoveEvent = MotionEvent.obtainNoHistory(ev);
                if (getCurrentScrollY() - diffY <= 0) {
                    if (mIntercepted) {
                        return false;
                    }

                    final ViewGroup parent;

                    if (mTouchInterceptionViewGroup == null) {
                        parent = (ViewGroup) getParent();
                    } else {
                        parent = mTouchInterceptionViewGroup;
                    }

                    float offsetX = 0;
                    float offsetY = 0;

                    for (View v = this; v != null && v != parent; v = (View) v.getParent()) {
                        offsetX += v.getLeft() - v.getScrollX();
                        offsetY += v.getTop() - v.getScrollY();
                    }

                    final MotionEvent event = MotionEvent.obtainNoHistory(ev);
                    event.offsetLocation(offsetX, offsetY);

                    if (parent.onInterceptTouchEvent(event)) {
                        mIntercepted = true;

                        event.setAction(MotionEvent.ACTION_DOWN);

                        post(new Runnable() {

                            @Override
                            public void run() {
                                parent.dispatchTouchEvent(event);
                            }
                        });
                        return false;
                    }

                    return super.onTouchEvent(ev);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setScrollViewCallbacks(final ObservableScrollViewCallbacks listener) {
        mCallbacks = listener;
    }

    @Override
    public void addScrollViewCallbacks(final ObservableScrollViewCallbacks listener) {
        if (mCallbackCollection == null) {
            mCallbackCollection = new ArrayList<>();
        }
        mCallbackCollection.add(listener);
    }

    @Override
    public void removeScrollViewCallbacks(final ObservableScrollViewCallbacks listener) {
        if (mCallbackCollection != null) {
            mCallbackCollection.remove(listener);
        }
    }

    @Override
    public void clearScrollViewCallbacks() {
        if (mCallbackCollection != null) {
            mCallbackCollection.clear();
        }
    }

    @Override
    public void setTouchInterceptionViewGroup(final ViewGroup viewGroup) {
        mTouchInterceptionViewGroup = viewGroup;
    }

    @Override
    public void scrollVerticallyTo(final int y) {
        final View firstVisibleChild = getChildAt(0);

        if (firstVisibleChild != null) {
            final int baseHeight = firstVisibleChild.getHeight();
            final int position = y / baseHeight;
            scrollVerticallyToPosition(position);
        }
    }

    public void scrollVerticallyToPosition(final int position) {
        final LayoutManager lm = getLayoutManager();

        if (lm instanceof LinearLayoutManager) {
            ((LinearLayoutManager) lm).scrollToPositionWithOffset(position, 0);
        } else {
            scrollToPosition(position);
        }
    }

    @Override
    public int getCurrentScrollY() {
        return mScrollY;
    }

    @SuppressWarnings("deprecation")
    public int getChildAdapterPosition(final View child) {
        if (recyclerViewLibraryVersion >= 22) {
            return super.getChildAdapterPosition(child);
        }
        return getChildPosition(child);
    }

    private void init() {
        mChildrenHeights = new SparseIntArray();
        checkLibraryVersion();
    }

    private void checkLibraryVersion() {
        try {
            super.getChildAdapterPosition(null);
        } catch (final NoSuchMethodError e) {
            recyclerViewLibraryVersion = 21;
        }
    }

    private void dispatchOnDownMotionEvent() {
        if (mCallbacks != null) {
            mCallbacks.onDownMotionEvent();
        }

        if (mCallbackCollection != null) {
            for (int i = 0; i < mCallbackCollection.size(); i++) {
                final ObservableScrollViewCallbacks callbacks = mCallbackCollection.get(i);

                callbacks.onDownMotionEvent();
            }
        }
    }

    private void dispatchOnScrollChanged(final int scrollY, final boolean firstScroll, final boolean dragging) {
        if (mCallbacks != null) {
            mCallbacks.onScrollChanged(scrollY, firstScroll, dragging);
        }

        if (mCallbackCollection != null) {
            for (int i = 0; i < mCallbackCollection.size(); i++) {
                final ObservableScrollViewCallbacks callbacks = mCallbackCollection.get(i);

                callbacks.onScrollChanged(scrollY, firstScroll, dragging);
            }
        }
    }

    private void dispatchOnUpOrCancelMotionEvent(final ScrollState scrollState) {
        if (mCallbacks != null) {
            mCallbacks.onUpOrCancelMotionEvent(scrollState);
        }

        if (mCallbackCollection != null) {
            for (int i = 0; i < mCallbackCollection.size(); i++) {
                final ObservableScrollViewCallbacks callbacks = mCallbackCollection.get(i);

                callbacks.onUpOrCancelMotionEvent(scrollState);
            }
        }
    }

    private boolean hasNoCallbacks() {
        return mCallbacks == null && mCallbackCollection == null;
    }

    private static class SavedState implements Parcelable {

        static final SavedState EMPTY_STATE = new SavedState() {

        };

        int prevFirstVisiblePosition;
        int prevFirstVisibleChildHeight = -1;
        int prevScrolledChildrenHeight;
        int prevScrollY;
        int scrollY;
        SparseIntArray childrenHeights;

        Parcelable superState;

        private SavedState() {
            superState = null;
        }

        SavedState(final Parcelable superState) {
            this.superState = superState != EMPTY_STATE ? superState : null;
        }

        private SavedState(final Parcel in) {
            final Parcelable superState = in.readParcelable(RecyclerView.class.getClassLoader());
            this.superState = superState != null ? superState : EMPTY_STATE;

            prevFirstVisiblePosition = in.readInt();
            prevFirstVisibleChildHeight = in.readInt();
            prevScrolledChildrenHeight = in.readInt();
            prevScrollY = in.readInt();
            scrollY = in.readInt();
            childrenHeights = new SparseIntArray();
            final int numOfChildren = in.readInt();

            if (numOfChildren > 0) {
                for (int i = 0; i < numOfChildren; i++) {
                    final int key = in.readInt();
                    final int value = in.readInt();
                    childrenHeights.put(key, value);
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            out.writeParcelable(superState, flags);

            out.writeInt(prevFirstVisiblePosition);
            out.writeInt(prevFirstVisibleChildHeight);
            out.writeInt(prevScrolledChildrenHeight);
            out.writeInt(prevScrollY);
            out.writeInt(scrollY);

            final int numOfChildren = childrenHeights == null ? 0 : childrenHeights.size();

            out.writeInt(numOfChildren);

            if (numOfChildren > 0) {
                for (int i = 0; i < numOfChildren; i++) {
                    out.writeInt(childrenHeights.keyAt(i));
                    out.writeInt(childrenHeights.valueAt(i));
                }
            }
        }

        Parcelable getSuperState() {
            return superState;
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(final Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };
    }
}
