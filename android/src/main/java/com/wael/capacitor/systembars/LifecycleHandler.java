package com.wael.capacitor.systembars;

import android.os.Build;
import android.util.Log;

/**
 * LifecycleHandler - Handles app lifecycle events to re-apply system UI state
 *
 * Android 35+: Re-applies window background color and icon styles.
 *   Capacitor's margin listener auto-handles spacing.
 * Android < 35: Re-applies padding + legacy colors.
 */
public class LifecycleHandler {

    private static final String TAG = "LifecycleHandler";

    private final SystemBarsManagerPlugin plugin;
    private boolean isAppInBackground = false;

    public LifecycleHandler(SystemBarsManagerPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPause() {
        isAppInBackground = true;
    }

    public void onResume() {
        if (isAppInBackground) {
            isAppInBackground = false;

            FullscreenManager fullscreenManager = plugin.getFullscreenManager();

            if (fullscreenManager.isFullscreenActive()) {
                fullscreenManager.reapplyFullscreenIfActive();
            } else {
                // Re-apply system UI state (colors, icon styles)
                SystemBarsManager systemBarsManager = plugin.getSystemBarsManager();
                systemBarsManager.reapplySystemUI();

                // Re-apply padding only for Android < 35
                if (Build.VERSION.SDK_INT < 35) {
                    plugin.getPaddingManager().applyPadding();
                }
            }

            Log.d(TAG, "Resumed from background, re-applied system UI state");
        }
    }
}
