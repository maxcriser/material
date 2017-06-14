package com.example.maksim_zakharenka.flexiblespacewithimage.util.header;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

public final class ScrollUtils {

    public ScrollUtils() {
    }

    public static float getFloat(final float value, final float minValue, final float maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }

    public static int getColorWithAlpha(final float alpha, final int baseColor) {
        final int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        final int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    public static void addOnGlobalLayoutListener(final View view, final Runnable runnable) {
        final ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                runnable.run();
            }
        });
    }

    public static int mixColors(final int fromColor, final int toColor, final float toAlpha) {
        final float[] fromCmyk = ScrollUtils.cmykFromRgb(fromColor);
        final float[] toCmyk = ScrollUtils.cmykFromRgb(toColor);
        final float[] result = new float[4];

        for (int i = 0; i < 4; i++) {
            result[i] = Math.min(1, fromCmyk[i] * (1 - toAlpha) + toCmyk[i] * toAlpha);
        }

        return 0xff000000 + (0x00ffffff & ScrollUtils.rgbFromCmyk(result));
    }

    private static float[] cmykFromRgb(final int rgbColor) {
        final int red = (0xff0000 & rgbColor) >> 16;
        final int green = (0xff00 & rgbColor) >> 8;
        final int blue = (0xff & rgbColor);
        final float black = Math.min(1.0f - red / 255.0f, Math.min(1.0f - green / 255.0f, 1.0f - blue / 255.0f));
        float cyan = 1.0f;
        float magenta = 1.0f;
        float yellow = 1.0f;

        if (black != 1.0f) {
            cyan = (1.0f - (red / 255.0f) - black) / (1.0f - black);
            magenta = (1.0f - (green / 255.0f) - black) / (1.0f - black);
            yellow = (1.0f - (blue / 255.0f) - black) / (1.0f - black);
        }

        return new float[]{cyan, magenta, yellow, black};
    }

    private static int rgbFromCmyk(final float[] cmyk) {
        final float cyan = cmyk[0];
        final float magenta = cmyk[1];
        final float yellow = cmyk[2];
        final float black = cmyk[3];
        final int red = (int) ((1.0f - Math.min(1.0f, cyan * (1.0f - black) + black)) * 255);
        final int green = (int) ((1.0f - Math.min(1.0f, magenta * (1.0f - black) + black)) * 255);
        final int blue = (int) ((1.0f - Math.min(1.0f, yellow * (1.0f - black) + black)) * 255);
        return ((0xff & red) << 16) + ((0xff & green) << 8) + (0xff & blue);
    }
}
