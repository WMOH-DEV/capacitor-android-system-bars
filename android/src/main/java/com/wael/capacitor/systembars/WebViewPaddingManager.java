package com.wael.capacitor.systembars;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;

/**
 * WebViewPaddingManager - Manages WebView padding for Android < 35 ONLY.
 *
 * On Android < 35: Top padding pushes content below the status bar overlay.
 * On Android 35+: Capacitor's adjustMarginsForEdgeToEdge="auto" handles
 *   spacing via WebView margins. No padding needed from us.
 */
public class WebViewPaddingManager {

    private static final String TAG = "WebViewPaddingManager";

    private final WebView webView;
    private int statusBarHeight = 0;
    private int navigationBarHeight = 0;

    public WebViewPaddingManager(WebView webView) {
        this.webView = webView;
        this.statusBarHeight = calculateStatusBarHeight();
        this.navigationBarHeight = calculateNavigationBarHeight();

        Log.d(TAG, "Calculated bar heights: statusBar=" + statusBarHeight
                + "px, navigationBar=" + navigationBarHeight + "px");
    }

    private int calculateStatusBarHeight() {
        Resources resources = webView.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (24 * metrics.density);
    }

    private int calculateNavigationBarHeight() {
        Resources resources = webView.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (48 * metrics.density);
    }

    /**
     * Apply padding to WebView.
     * Android 35+: No-op (Capacitor handles margins).
     * Android < 35: Top-only padding for status bar overlay.
     */
    public void applyPadding() {
        if (Build.VERSION.SDK_INT >= 35) {
            Log.d(TAG, "Android 35+: Skipping padding (Capacitor margins handle spacing)");
            return;
        }

        webView.post(() -> {
            Log.d(TAG, "Applying legacy padding: top=" + statusBarHeight);
            webView.setPadding(0, statusBarHeight, 0, 0);
            webView.requestLayout();
            webView.invalidate();
        });
    }

    /**
     * Remove padding (for fullscreen mode).
     * Android 35+: No-op (Capacitor handles margins).
     */
    public void removePadding() {
        if (Build.VERSION.SDK_INT >= 35) {
            Log.d(TAG, "Android 35+: Skipping removePadding (Capacitor margins handle spacing)");
            return;
        }

        webView.post(() -> {
            Log.d(TAG, "Removing all padding");
            webView.setPadding(0, 0, 0, 0);
            webView.requestLayout();
            webView.invalidate();
        });
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public int getNavigationBarHeight() {
        return navigationBarHeight;
    }
}
