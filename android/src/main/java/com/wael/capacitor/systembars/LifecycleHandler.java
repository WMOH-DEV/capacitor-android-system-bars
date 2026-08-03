package com.wael.capacitor.systembars;

import android.os.Build;
import android.util.Log;

/**
 * LifecycleHandler - Handles app lifecycle events to re-apply system UI state
 *
 * Android 35+: Re-applies window background color and icon styles.
 *   The plugin's base inset listener handles spacing.
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
        if (!isAppInBackground) return;

        isAppInBackground = false;
        reapplySystemUIState();

        Log.d(TAG, "Resumed from background, re-applied system UI state");
    }

    // post() defers past Capacitor's SystemBars plugin, which re-applies its configured style synchronously in handleOnConfigurationChanged.
    public void onConfigurationChanged() {
        plugin.getBridge().getWebView().post(() -> {
            reapplySystemUIState();
            Log.d(TAG, "Configuration changed, re-applied system UI state");
        });
    }

    private void reapplySystemUIState() {
        FullscreenManager fullscreenManager = plugin.getFullscreenManager();

        if (fullscreenManager.isFullscreenActive()) {
            fullscreenManager.reapplyFullscreenIfActive();
            return;
        }

        plugin.getSystemBarsManager().reapplySystemUI();

        if (Build.VERSION.SDK_INT < 35) {
            plugin.getPaddingManager().applyPadding();
        }
    }
}
