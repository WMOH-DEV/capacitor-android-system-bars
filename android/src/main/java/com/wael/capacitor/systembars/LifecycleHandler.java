package com.wael.capacitor.systembars;

import android.os.Build;
import com.getcapacitor.PluginCall;

/**
 * LifecycleHandler - Handles app lifecycle events to re-apply system UI state
 * 
 * CRITICAL: Screen lock/unlock can reset system UI flags on Android < 35
 * 
 * Note: In Capacitor 7.0, lifecycle events are handled differently.
 * We'll implement this using the plugin's own onPause/onResume methods.
 */
public class LifecycleHandler {

    private final SystemBarsManagerPlugin plugin;
    private boolean isAppInBackground = false;

    public LifecycleHandler(SystemBarsManagerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when the app goes into the background
     */
    public void onPause() {
        isAppInBackground = true;
    }

    /**
     * Called when the app comes back to the foreground
     */
    public void onResume() {
        if (isAppInBackground) {
            isAppInBackground = false;

            // CRITICAL: Re-apply system UI state after resume
            // This fixes the issue where screen unlock resets overlay mode
            plugin.getSystemBarsManager().reapplySystemUI();

            // Re-apply webview padding on Android < 35
            if (Build.VERSION.SDK_INT < 35) {
                plugin.getPaddingManager().applyPadding();
            }
        }
    }
}