package com.example.maksim_zakharenka.flexiblespacewithimage.helper;

import android.view.View;

import static com.example.maksim_zakharenka.flexiblespacewithimage.util.anim.AnimatorProxy.NEEDS_PROXY;
import static com.example.maksim_zakharenka.flexiblespacewithimage.util.anim.AnimatorProxy.wrap;

public final class ViewHelper {

    public ViewHelper() {
    }

    public static float getAlpha(final View view) {
        return NEEDS_PROXY ? wrap(view).getAlpha() : Honeycomb.getAlpha(view);
    }

    public static void setAlpha(final View view, final float alpha) {
        if (NEEDS_PROXY) {
            wrap(view).setAlpha(alpha);
        } else {
            Honeycomb.setAlpha(view, alpha);
        }
    }

    public static float getPivotX(final View view) {
        return NEEDS_PROXY ? wrap(view).getPivotX() : Honeycomb.getPivotX(view);
    }

    public static void setPivotX(final View view, final float pivotX) {
        if (NEEDS_PROXY) {
            wrap(view).setPivotX(pivotX);
        } else {
            Honeycomb.setPivotX(view, pivotX);
        }
    }

    public static float getPivotY(final View view) {
        return NEEDS_PROXY ? wrap(view).getPivotY() : Honeycomb.getPivotY(view);
    }

    public static void setPivotY(final View view, final float pivotY) {
        if (NEEDS_PROXY) {
            wrap(view).setPivotY(pivotY);
        } else {
            Honeycomb.setPivotY(view, pivotY);
        }
    }

    public static float getRotation(final View view) {
        return NEEDS_PROXY ? wrap(view).getRotation() : Honeycomb.getRotation(view);
    }

    public static void setRotation(final View view, final float rotation) {
        if (NEEDS_PROXY) {
            wrap(view).setRotation(rotation);
        } else {
            Honeycomb.setRotation(view, rotation);
        }
    }

    public static float getRotationX(final View view) {
        return NEEDS_PROXY ? wrap(view).getRotationX() : Honeycomb.getRotationX(view);
    }

    public static void setRotationX(final View view, final float rotationX) {
        if (NEEDS_PROXY) {
            wrap(view).setRotationX(rotationX);
        } else {
            Honeycomb.setRotationX(view, rotationX);
        }
    }

    public static float getRotationY(final View view) {
        return NEEDS_PROXY ? wrap(view).getRotationY() : Honeycomb.getRotationY(view);
    }

    public static void setRotationY(final View view, final float rotationY) {
        if (NEEDS_PROXY) {
            wrap(view).setRotationY(rotationY);
        } else {
            Honeycomb.setRotationY(view, rotationY);
        }
    }

    public static float getScaleX(final View view) {
        return NEEDS_PROXY ? wrap(view).getScaleX() : Honeycomb.getScaleX(view);
    }

    public static void setScaleX(final View view, final float scaleX) {
        if (NEEDS_PROXY) {
            wrap(view).setScaleX(scaleX);
        } else {
            Honeycomb.setScaleX(view, scaleX);
        }
    }

    public static float getScaleY(final View view) {
        return NEEDS_PROXY ? wrap(view).getScaleY() : Honeycomb.getScaleY(view);
    }

    public static void setScaleY(final View view, final float scaleY) {
        if (NEEDS_PROXY) {
            wrap(view).setScaleY(scaleY);
        } else {
            Honeycomb.setScaleY(view, scaleY);
        }
    }

    public static float getScrollX(final View view) {
        return NEEDS_PROXY ? wrap(view).getScrollX() : Honeycomb.getScrollX(view);
    }

    public static void setScrollX(final View view, final int scrollX) {
        if (NEEDS_PROXY) {
            wrap(view).setScrollX(scrollX);
        } else {
            Honeycomb.setScrollX(view, scrollX);
        }
    }

    public static float getScrollY(final View view) {
        return NEEDS_PROXY ? wrap(view).getScrollY() : Honeycomb.getScrollY(view);
    }

    public static void setScrollY(final View view, final int scrollY) {
        if (NEEDS_PROXY) {
            wrap(view).setScrollY(scrollY);
        } else {
            Honeycomb.setScrollY(view, scrollY);
        }
    }

    public static float getTranslationX(final View view) {
        return NEEDS_PROXY ? wrap(view).getTranslationX() : Honeycomb.getTranslationX(view);
    }

    public static void setTranslationX(final View view, final float translationX) {
        if (NEEDS_PROXY) {
            wrap(view).setTranslationX(translationX);
        } else {
            Honeycomb.setTranslationX(view, translationX);
        }
    }

    public static float getTranslationY(final View view) {
        return NEEDS_PROXY ? wrap(view).getTranslationY() : Honeycomb.getTranslationY(view);
    }

    public static void setTranslationY(final View view, final float translationY) {
        if (NEEDS_PROXY) {
            wrap(view).setTranslationY(translationY);
        } else {
            Honeycomb.setTranslationY(view, translationY);
        }
    }

    public static float getX(final View view) {
        return NEEDS_PROXY ? wrap(view).getX() : Honeycomb.getX(view);
    }

    public static void setX(final View view, final float x) {
        if (NEEDS_PROXY) {
            wrap(view).setX(x);
        } else {
            Honeycomb.setX(view, x);
        }
    }

    public static float getY(final View view) {
        return NEEDS_PROXY ? wrap(view).getY() : Honeycomb.getY(view);
    }

    public static void setY(final View view, final float y) {
        if (NEEDS_PROXY) {
            wrap(view).setY(y);
        } else {
            Honeycomb.setY(view, y);
        }
    }

    private static final class Honeycomb {

        static float getAlpha(final View view) {
            return view.getAlpha();
        }

        static void setAlpha(final View view, final float alpha) {
            view.setAlpha(alpha);
        }

        static float getPivotX(final View view) {
            return view.getPivotX();
        }

        static void setPivotX(final View view, final float pivotX) {
            view.setPivotX(pivotX);
        }

        static float getPivotY(final View view) {
            return view.getPivotY();
        }

        static void setPivotY(final View view, final float pivotY) {
            view.setPivotY(pivotY);
        }

        static float getRotation(final View view) {
            return view.getRotation();
        }

        static void setRotation(final View view, final float rotation) {
            view.setRotation(rotation);
        }

        static float getRotationX(final View view) {
            return view.getRotationX();
        }

        static void setRotationX(final View view, final float rotationX) {
            view.setRotationX(rotationX);
        }

        static float getRotationY(final View view) {
            return view.getRotationY();
        }

        static void setRotationY(final View view, final float rotationY) {
            view.setRotationY(rotationY);
        }

        static float getScaleX(final View view) {
            return view.getScaleX();
        }

        static void setScaleX(final View view, final float scaleX) {
            view.setScaleX(scaleX);
        }

        static float getScaleY(final View view) {
            return view.getScaleY();
        }

        static void setScaleY(final View view, final float scaleY) {
            view.setScaleY(scaleY);
        }

        static float getScrollX(final View view) {
            return view.getScrollX();
        }

        static void setScrollX(final View view, final int scrollX) {
            view.setScrollX(scrollX);
        }

        static float getScrollY(final View view) {
            return view.getScrollY();
        }

        static void setScrollY(final View view, final int scrollY) {
            view.setScrollY(scrollY);
        }

        static float getTranslationX(final View view) {
            return view.getTranslationX();
        }

        static void setTranslationX(final View view, final float translationX) {
            view.setTranslationX(translationX);
        }

        static float getTranslationY(final View view) {
            return view.getTranslationY();
        }

        static void setTranslationY(final View view, final float translationY) {
            view.setTranslationY(translationY);
        }

        static float getX(final View view) {
            return view.getX();
        }

        static void setX(final View view, final float x) {
            view.setX(x);
        }

        static float getY(final View view) {
            return view.getY();
        }

        static void setY(final View view, final float y) {
            view.setY(y);
        }
    }
}
