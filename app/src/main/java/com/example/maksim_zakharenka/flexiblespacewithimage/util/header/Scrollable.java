package com.example.maksim_zakharenka.flexiblespacewithimage.util.header;

import android.view.ViewGroup;

public interface Scrollable {

    @Deprecated
    void setScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    void addScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    void removeScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    void clearScrollViewCallbacks();

    void scrollVerticallyTo(int y);

    int getCurrentScrollY();

    void setTouchInterceptionViewGroup(ViewGroup viewGroup);
}
