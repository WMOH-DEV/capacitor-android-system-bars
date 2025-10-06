package com.wael.capacitor.systembars;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.webkit.WebView;

/**
 * WebViewPaddingManager - Manages webview padding for Android < 35
 * 
 * CRITICAL: On Android < 35, we MUST manually add top padding to the webview
 * to prevent content from being hidden under the status bar.
 *
 * This is necessary because we CANNOT use overlay mode on Android < 35
 * without causing resize conflicts with keyboard and system events.
 */
public class WebViewPaddingManager {

    private final WebView webView;
    private int statusBarHeight = 0;
    private int navigationBarHeight = 0;

    public WebViewPaddingManager(WebView webView) {
        this.webView = webView;
        this.statusBarHeight = calculateStatusBarHeight();
        this.navigationBarHeight = calculateNavigationBarHeight();
    }

    /**
     * Calculate status bar height in pixels
     */
    private int calculateStatusBarHeight() {
        Resources resources = webView.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        // Fallback: 24dp converted to pixels
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (24 * metrics.density);
    }

    /**
     * Calculate navigation bar height in pixels
     */
    private int calculateNavigationBarHeight() {
        Resources resources = webView.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        // Fallback: 48dp converted to pixels
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (48 * metrics.density);
    }

    /**
     * Apply top padding to webview (Android < 35 only)
     */
    public void applyPadding() {
        if (Build.VERSION.SDK_INT < 35) {
            webView.post(() -> {
                // CRITICAL: Force layout update after padding change
                webView.setPadding(0, statusBarHeight, 0, 0);
                webView.requestLayout();
                webView.invalidate();
            });
        }
    }

    /**
     * Remove padding (for fullscreen mode)
     */
    public void removePadding() {
        webView.post(() -> {
            // CRITICAL: Force layout update after padding removal
            webView.setPadding(0, 0, 0, 0);
            webView.requestLayout();
            webView.invalidate();
        });
    }

    /**
     * Set custom padding
     */
    public void setPadding(int top, int bottom) {
        webView.post(() -> {
            // CRITICAL: Force layout update after padding change
            webView.setPadding(0, top, 0, bottom);
            webView.requestLayout();
            webView.invalidate();
        });
    }

    /**
     * Get status bar height
     */
    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    /**
     * Get navigation bar height
     */
    public int getNavigationBarHeight() {
        return navigationBarHeight;
    }
}