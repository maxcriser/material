package com.example.maksim_zakharenka.flexiblespacewithimage.util.header;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ObservableGridView extends GridView implements Scrollable {

    private int mPrevFirstVisiblePosition;
    private int mPrevFirstVisibleChildHeight = -1;
    private int mPrevScrolledChildrenHeight;
    private int mPrevScrollY;
    private int mScrollY;
    private SparseIntArray mChildrenHeights;

    // Fields that don't need to be saved onSaveInstanceState
    private ObservableScrollViewCallbacks mCallbacks;
    private List<ObservableScrollViewCallbacks> mCallbackCollection;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;
    private boolean mIntercepted;
    private MotionEvent mPrevMoveEvent;
    private ViewGroup mTouchInterceptionViewGroup;
    private ArrayList<FixedViewInfo> mHeaderViewInfos;
    private ArrayList<FixedViewInfo> mFooterViewInfos;

    private OnScrollListener mOriginalScrollListener;
    private final OnScrollListener mScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(final AbsListView view, final int scrollState) {
            if (mOriginalScrollListener != null) {
                mOriginalScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
            if (mOriginalScrollListener != null) {
                mOriginalScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            onScrollChanged();
        }
    };

    public ObservableGridView(final Context context) {
        super(context);
        init();
    }

    public ObservableGridView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ObservableGridView(final Context context, final AttributeSet attrs, final int defStyle) {
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

    public void addFooterView(final View v) {
        addFooterView(v, null, true);
    }

    public void addFooterView(final View v, final Object data, final boolean isSelectable) {
        final ListAdapter mAdapter = getAdapter();

        if (mAdapter != null && !(mAdapter instanceof HeaderViewGridAdapter)) {
            throw new IllegalStateException(
                    "Cannot add header view to grid -- setAdapter has already been called.");
        }

        final ViewGroup.LayoutParams lyp = v.getLayoutParams();
        final FixedViewInfo info = new FixedViewInfo();
        final FrameLayout fl = new FullWidthFixedViewLayout(getContext());

        if (lyp != null) {
            v.setLayoutParams(new FrameLayout.LayoutParams(lyp.width, lyp.height));
            fl.setLayoutParams(new AbsListView.LayoutParams(lyp.width, lyp.height));
        }

        fl.addView(v);
        info.view = v;
        info.viewContainer = fl;
        info.data = data;
        info.isSelectable = isSelectable;
        mFooterViewInfos.add(info);

        if (mAdapter != null) {
            ((HeaderViewGridAdapter) mAdapter).notifyDataSetChanged();
        }
    }

    public int getFooterViewCount() {
        return mFooterViewInfos.size();
    }

    public boolean removeFooterView(final View v) {
        if (!mFooterViewInfos.isEmpty()) {
            boolean result = false;
            final ListAdapter adapter = getAdapter();

            if (adapter != null && ((HeaderViewGridAdapter) adapter).removeFooter(v)) {
                result = true;
            }

            removeFixedViewInfo(v, mFooterViewInfos);

            return result;
        }

        return false;
    }

    @Override
    public void setOnScrollListener(final OnScrollListener l) {
        mOriginalScrollListener = l;
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
        scrollTo(0, y);
    }

    @Override
    public int getCurrentScrollY() {
        return mScrollY;
    }

    @Override
    public void setClipChildren(final boolean clipChildren) {

    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        if (!mHeaderViewInfos.isEmpty()) {
            final HeaderViewGridAdapter headerViewGridAdapter = new HeaderViewGridAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
            final int numColumns = getNumColumnsCompat();

            if (numColumns > 1) {
                headerViewGridAdapter.setNumColumns(numColumns);
            }

            super.setAdapter(headerViewGridAdapter);
        } else {
            super.setAdapter(adapter);
        }
    }

    public void addHeaderView(final View v, final Object data, final boolean isSelectable) {
        final ListAdapter adapter = getAdapter();

        if (adapter != null && !(adapter instanceof HeaderViewGridAdapter)) {
            throw new IllegalStateException("Cannot add header view to grid -- setAdapter has already been called.");
        }

        final FixedViewInfo info = new FixedViewInfo();
        final FrameLayout fl = new FullWidthFixedViewLayout(getContext());

        fl.addView(v);
        info.view = v;
        info.viewContainer = fl;
        info.data = data;
        info.isSelectable = isSelectable;
        mHeaderViewInfos.add(info);

        if (adapter != null) {
            ((HeaderViewGridAdapter) adapter).notifyDataSetChanged();
        }
    }

    public void addHeaderView(final View v) {
        addHeaderView(v, null, true);
    }

    public int getHeaderViewCount() {
        return mHeaderViewInfos.size();
    }

    public boolean removeHeaderView(final View v) {
        if (!mHeaderViewInfos.isEmpty()) {
            boolean result = false;
            final ListAdapter adapter = getAdapter();

            if (adapter instanceof HeaderViewGridAdapter && ((HeaderViewGridAdapter) adapter).removeHeader(v)) {
                result = true;
            }

            removeFixedViewInfo(v, mHeaderViewInfos);

            return result;
        }

        return false;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final ListAdapter adapter = getAdapter();

        if (adapter instanceof HeaderViewGridAdapter) {
            ((HeaderViewGridAdapter) adapter).setNumColumns(getNumColumnsCompat());
        }
    }

    private void init() {
        mChildrenHeights = new SparseIntArray();
        mHeaderViewInfos = new ArrayList<>();
        mFooterViewInfos = new ArrayList<>();

        super.setClipChildren(false);
        super.setOnScrollListener(mScrollListener);
    }

    private int getNumColumnsCompat() {
        if (Build.VERSION.SDK_INT >= 11) {
            return getNumColumns();
        } else {
            int columns = 0;

            if (getChildCount() > 0) {
                final int width = getChildAt(0).getMeasuredWidth();

                if (width > 0) {
                    columns = getWidth() / width;
                }
            }

            return columns > 0 ? columns : AUTO_FIT;
        }
    }

    private void onScrollChanged() {
        if (hasNoCallbacks()) {
            return;
        }

        if (getChildCount() > 0) {
            final int firstVisiblePosition = getFirstVisiblePosition();

            for (int i = getFirstVisiblePosition(), j = 0; i <= getLastVisiblePosition(); i++, j++) {
                if (mChildrenHeights.indexOfKey(i) < 0 || getChildAt(j).getHeight() != mChildrenHeights.get(i)) {
                    if (i % getNumColumnsCompat() == 0) {
                        mChildrenHeights.put(i, getChildAt(j).getHeight());
                    }
                }
            }

            final View firstVisibleChild = getChildAt(0);
            if (firstVisibleChild != null) {
                if (mPrevFirstVisiblePosition < firstVisiblePosition) {
                    int skippedChildrenHeight = 0;

                    if (firstVisiblePosition - mPrevFirstVisiblePosition != 1) {
                        for (int i = firstVisiblePosition - 1; i > mPrevFirstVisiblePosition; i--) {
                            if (mChildrenHeights.indexOfKey(i) > 0) {
                                skippedChildrenHeight += mChildrenHeights.get(i);
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

    private void removeFixedViewInfo(final View v, final List<FixedViewInfo> where) {
        final int len = where.size();

        for (int i = 0; i < len; ++i) {
            final FixedViewInfo info = where.get(i);

            if (info.view == v) {
                where.remove(i);

                break;
            }
        }
    }

    private boolean hasNoCallbacks() {
        return mCallbacks == null && mCallbackCollection == null;
    }

    private class FullWidthFixedViewLayout extends FrameLayout {

        public FullWidthFixedViewLayout(final Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            final int targetWidth = ObservableGridView.this.getMeasuredWidth()
                    - ObservableGridView.this.getPaddingLeft()
                    - ObservableGridView.this.getPaddingRight();
            final int widthMeasureSpec1 = MeasureSpec.makeMeasureSpec(targetWidth,
                    MeasureSpec.getMode(widthMeasureSpec));
            super.onMeasure(widthMeasureSpec1, heightMeasureSpec);
        }
    }

    private static class SavedState extends BaseSavedState {

        int prevFirstVisiblePosition;
        int prevFirstVisibleChildHeight = -1;
        int prevScrolledChildrenHeight;
        int prevScrollY;
        int scrollY;
        SparseIntArray childrenHeights;

        SavedState(final Parcelable superState) {
            super(superState);
        }

        private SavedState(final Parcel in) {
            super(in);

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
        public void writeToParcel(final Parcel out, final int flags) {
            super.writeToParcel(out, flags);
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

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {

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

    private static class FixedViewInfo {

        public View view;
        public ViewGroup viewContainer;
        public Object data;
        public boolean isSelectable;
    }

    private static class HeaderViewGridAdapter implements WrapperListAdapter, Filterable {

        private final DataSetObservable mDataSetObservable = new DataSetObservable();
        private final ListAdapter mAdapter;
        static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST = new ArrayList<>();

        ArrayList<FixedViewInfo> mHeaderViewInfos;
        ArrayList<FixedViewInfo> mFooterViewInfos;
        private int mNumColumns = 1;
        private int mRowHeight = -1;
        boolean mAreAllFixedViewsSelectable;
        private final boolean mIsFilterable;
        private final boolean mCachePlaceHoldView = true;
        private final boolean mCacheFirstHeaderView = false;

        HeaderViewGridAdapter(final ArrayList<FixedViewInfo> headerViewInfos, final ArrayList<FixedViewInfo> footViewInfos, final ListAdapter adapter) {
            mAdapter = adapter;
            mIsFilterable = adapter instanceof Filterable;
            if (headerViewInfos == null) {
                mHeaderViewInfos = EMPTY_INFO_LIST;
            } else {
                mHeaderViewInfos = headerViewInfos;
            }

            if (footViewInfos == null) {
                mFooterViewInfos = EMPTY_INFO_LIST;
            } else {
                mFooterViewInfos = footViewInfos;
            }

            mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos)
                    && areAllListInfosSelectable(mFooterViewInfos);
        }

        public int getNumColumns() {
            return mNumColumns;
        }

        void setNumColumns(final int numColumns) {
            if (numColumns < 1) {
                return;
            }
            if (mNumColumns != numColumns) {
                mNumColumns = numColumns;
                notifyDataSetChanged();
            }
        }

        public void setRowHeight(final int height) {
            mRowHeight = height;
        }

        int getHeadersCount() {
            return mHeaderViewInfos.size();
        }

        int getFootersCount() {
            return mFooterViewInfos.size();
        }

        @Override
        public boolean isEmpty() {
            return (mAdapter == null || mAdapter.isEmpty());
        }

        private boolean areAllListInfosSelectable(final ArrayList<FixedViewInfo> infos) {
            if (infos != null) {
                for (final FixedViewInfo info : infos) {
                    if (!info.isSelectable) {
                        return false;
                    }
                }
            }
            return true;
        }

        boolean removeHeader(final View v) {
            for (int i = 0; i < mHeaderViewInfos.size(); i++) {
                final FixedViewInfo info = mHeaderViewInfos.get(i);

                if (info.view == v) {
                    mHeaderViewInfos.remove(i);
                    mAreAllFixedViewsSelectable =
                            areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);
                    mDataSetObservable.notifyChanged();

                    return true;
                }
            }

            return false;
        }

        boolean removeFooter(final View v) {
            for (int i = 0; i < mFooterViewInfos.size(); i++) {
                final FixedViewInfo info = mFooterViewInfos.get(i);

                if (info.view == v) {
                    mFooterViewInfos.remove(i);
                    mAreAllFixedViewsSelectable =
                            areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);
                    mDataSetObservable.notifyChanged();

                    return true;
                }
            }

            return false;
        }

        @Override
        public int getCount() {
            if (mAdapter != null) {
                return (getFootersCount() + getHeadersCount()) * mNumColumns + getAdapterAndPlaceHolderCount();
            } else {
                return (getFootersCount() + getHeadersCount()) * mNumColumns;
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter == null || mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
        }

        private int getAdapterAndPlaceHolderCount() {
            return (int) (Math.ceil(1f * mAdapter.getCount() / mNumColumns) * mNumColumns);
        }

        @Override
        public boolean isEnabled(final int position) {
            final int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                return position % mNumColumns == 0
                        && mHeaderViewInfos.get(position / mNumColumns).isSelectable;
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;

            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition < adapterCount) {
                    return adjPosition < mAdapter.getCount() && mAdapter.isEnabled(adjPosition);
                }
            }

            final int footerPosition = adjPosition - adapterCount;
            return footerPosition % mNumColumns == 0
                    && mFooterViewInfos.get(footerPosition / mNumColumns).isSelectable;
        }

        @Override
        public Object getItem(final int position) {
            final int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;

            if (position < numHeadersAndPlaceholders) {
                if (position % mNumColumns == 0) {
                    return mHeaderViewInfos.get(position / mNumColumns).data;
                }
                return null;
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;

            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition < adapterCount) {
                    if (adjPosition < mAdapter.getCount()) {
                        return mAdapter.getItem(adjPosition);
                    } else {
                        return null;
                    }
                }
            }

            final int footerPosition = adjPosition - adapterCount;

            if (footerPosition % mNumColumns == 0) {
                return mFooterViewInfos.get(footerPosition).data;
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(final int position) {
            final int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;

            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                final int adjPosition = position - numHeadersAndPlaceholders;
                final int adapterCount = mAdapter.getCount();

                if (adjPosition < adapterCount) {
                    return mAdapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;

            if (position < numHeadersAndPlaceholders) {
                final View headerViewContainer = mHeaderViewInfos
                        .get(position / mNumColumns).viewContainer;

                if (position % mNumColumns == 0) {
                    return headerViewContainer;
                } else {
                    if (convertView == null) {
                        convertView = new View(parent.getContext());
                    }

                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setMinimumHeight(headerViewContainer.getHeight());

                    return convertView;
                }
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;

            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();

                if (adjPosition < adapterCount) {
                    if (adjPosition < mAdapter.getCount()) {
                        return mAdapter.getView(adjPosition, convertView, parent);
                    } else {
                        if (convertView == null) {
                            convertView = new View(parent.getContext());
                        }
                        convertView.setVisibility(View.INVISIBLE);
                        convertView.setMinimumHeight(mRowHeight);
                        return convertView;
                    }
                }
            }

            final int footerPosition = adjPosition - adapterCount;

            if (footerPosition < getCount()) {
                final View footViewContainer = mFooterViewInfos
                        .get(footerPosition / mNumColumns).viewContainer;

                if (position % mNumColumns == 0) {
                    return footViewContainer;
                } else {
                    if (convertView == null) {
                        convertView = new View(parent.getContext());
                    }

                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setMinimumHeight(footViewContainer.getHeight());
                    return convertView;
                }
            }
            throw new ArrayIndexOutOfBoundsException(position);
        }

        @Override
        public int getItemViewType(final int position) {

            final int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            final int adapterViewTypeStart = mAdapter == null ? 0 : mAdapter.getViewTypeCount() - 1;
            int type = AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
            if (mCachePlaceHoldView) {
                if (position < numHeadersAndPlaceholders) {
                    if (position == 0) {
                        if (mCacheFirstHeaderView) {
                            type = adapterViewTypeStart + mHeaderViewInfos.size() + mFooterViewInfos.size() + 1 + 1;
                        }
                    }

                    if (position % mNumColumns != 0) {
                        type = adapterViewTypeStart + (position / mNumColumns + 1);
                    }
                }
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;

            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition >= 0 && adjPosition < adapterCount) {
                    if (adjPosition < mAdapter.getCount()) {
                        type = mAdapter.getItemViewType(adjPosition);
                    } else {
                        if (mCachePlaceHoldView) {
                            type = adapterViewTypeStart + mHeaderViewInfos.size() + 1;
                        }
                    }
                }
            }

            if (mCachePlaceHoldView) {
                final int footerPosition = adjPosition - adapterCount;

                if (footerPosition >= 0 && footerPosition < getCount() && (footerPosition % mNumColumns) != 0) {
                    type = adapterViewTypeStart + mHeaderViewInfos.size() + 1 + (footerPosition / mNumColumns + 1);
                }
            }

            return type;
        }

        @Override
        public int getViewTypeCount() {
            int count = mAdapter == null ? 1 : mAdapter.getViewTypeCount();
            if (mCachePlaceHoldView) {
                int offset = mHeaderViewInfos.size() + 1 + mFooterViewInfos.size();
                if (mCacheFirstHeaderView) {
                    offset += 1;
                }
                count += offset;
            }

            return count;
        }

        @Override
        public void registerDataSetObserver(final DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);

            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        @Override
        public void unregisterDataSetObserver(final DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);

            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public Filter getFilter() {
            if (mIsFilterable) {
                return ((Filterable) mAdapter).getFilter();
            }

            return null;
        }

        @Override
        public ListAdapter getWrappedAdapter() {
            return mAdapter;
        }

        void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }
    }
}
