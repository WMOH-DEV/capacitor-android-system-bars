export interface AndroidSystemBarsPlugin {
  /**
   * Initialize plugin and get device info
   */
  initialize(): Promise<InitializeResult>;

  // === UNIFIED SYSTEM BARS API ===

  /**
   * Set both status bar AND navigation bar style/color in one call
   * This is the recommended method for most use cases
   */
  setSystemBarsStyle(options: SetSystemBarsStyleOptions): Promise<void>;

  // === INDIVIDUAL BAR CONTROL ===

  /**
   * Set ONLY status bar style and color
   */
  setStatusBarStyle(options: SetStatusBarStyleOptions): Promise<void>;

  /**
   * Set ONLY navigation bar style and color
   */
  setNavigationBarStyle(options: SetNavigationBarStyleOptions): Promise<void>;

  // === STATUS BAR VISIBILITY ===

  /**
   * Hide status bar
   */
  hideStatusBar(): Promise<void>;

  /**
   * Show status bar
   */
  showStatusBar(): Promise<void>;

  // === NAVIGATION BAR VISIBILITY ===

  /**
   * Hide navigation bar
   */
  hideNavigationBar(): Promise<void>;

  /**
   * Show navigation bar
   */
  showNavigationBar(): Promise<void>;

  // === FULLSCREEN MODE ===

  /**
   * Enter fullscreen mode (hides both status and navigation bars)
   */
  enterFullscreen(options: EnterFullscreenOptions): Promise<void>;

  /**
   * Exit fullscreen mode and restore system bars
   */
  exitFullscreen(options?: ExitFullscreenOptions): Promise<void>;

  /**
   * Check if fullscreen mode is currently active
   */
  isFullscreenActive(): Promise<{ active: boolean }>;

  /**
   * Force exit fullscreen mode (emergency fallback)
   */
  forceExitFullscreen(): Promise<void>;

  // === ADVANCED FEATURES ===

  /**
   * Set overlay mode (Android 35+ only)
   */
  setOverlay(options: SetOverlayOptions): Promise<void>;

  /**
   * Get current window insets information
   */
  getInsets(): Promise<InsetsResult>;

  // === DEPRECATED METHODS (for backward compatibility) ===

  /**
   * @deprecated Use setStatusBarStyle() instead
   * Set status bar style and color
   */
  setStyle(options: SetStatusBarStyleOptions): Promise<void>;

  /**
   * @deprecated Use hideStatusBar() instead
   * Hide status bar
   */
  hide(): Promise<void>;

  /**
   * @deprecated Use showStatusBar() instead
   * Show status bar
   */
  show(): Promise<void>;
}

export interface InitializeResult {
  /**
   * Android API level
   */
  apiLevel: number;

  /**
   * Whether device is running Android 35+
   */
  isAndroid35Plus: boolean;

  /**
   * Whether device supports edge-to-edge natively
   */
  supportsEdgeToEdge: boolean;

  /**
   * Whether device supports WindowInsets API
   */
  supportsWindowInsets: boolean;

  /**
   * Status bar height in pixels
   */
  statusBarHeight: number;

  /**
   * Navigation bar height in pixels
   */
  navigationBarHeight: number;
}

// === UNIFIED SYSTEM BARS ===

export interface SetSystemBarsStyleOptions {
  /**
   * Status bar configuration
   */
  statusBar?: {
    style?: 'LIGHT' | 'DARK' | 'DEFAULT';
    color?: string;
  };

  /**
   * Navigation bar configuration
   */
  navigationBar?: {
    style?: 'LIGHT' | 'DARK' | 'DEFAULT';
    color?: string;
  };

  /**
   * Apply same style to both bars (shorthand)
   * If specified, overrides individual statusBar/navigationBar style
   */
  style?: 'LIGHT' | 'DARK' | 'DEFAULT';

  /**
   * Apply same color to both bars (shorthand)
   * If specified, overrides individual statusBar/navigationBar color
   */
  color?: string;
}

// === INDIVIDUAL BAR CONTROL ===

export interface SetStatusBarStyleOptions {
  /**
   * Status bar style
   */
  style: 'LIGHT' | 'DARK' | 'DEFAULT';

  /**
   * Status bar background color (hex format: #RRGGBB or #AARRGGBB)
   */
  color?: string;
}

export interface SetNavigationBarStyleOptions {
  /**
   * Navigation bar style
   */
  style: 'LIGHT' | 'DARK' | 'DEFAULT';

  /**
   * Navigation bar background color (hex format: #RRGGBB or #AARRGGBB)
   */
  color?: string;
}

// === FULLSCREEN MODE ===

export interface EnterFullscreenOptions {
  /**
   * Fullscreen mode type
   */
  mode: 'IMMERSIVE' | 'LEAN';
}

export interface ExitFullscreenOptions {
  /**
   * System bars configuration to restore after exiting fullscreen
   * If not provided, will restore to system default
   */
  restore?: {
    /**
     * Status bar configuration to restore
     */
    statusBar?: {
      style?: 'LIGHT' | 'DARK' | 'DEFAULT';
      color?: string;
    };

    /**
     * Navigation bar configuration to restore
     */
    navigationBar?: {
      style?: 'LIGHT' | 'DARK' | 'DEFAULT';
      color?: string;
    };

    /**
     * Apply same style to both bars (shorthand)
     */
    style?: 'LIGHT' | 'DARK' | 'DEFAULT';

    /**
     * Apply same color to both bars (shorthand)
     */
    color?: string;
  };
}

// === ADVANCED FEATURES ===

export interface SetOverlayOptions {
  /**
   * Whether to enable overlay mode (Android 35+ only)
   */
  overlay: boolean;
}

export interface InsetsResult {
  /**
   * Top inset (status bar area)
   */
  top: number;

  /**
   * Bottom inset (navigation bar area)
   */
  bottom: number;

  /**
   * Left inset
   */
  left: number;

  /**
   * Right inset
   */
  right: number;

  /**
   * Whether status bar is visible
   */
  statusBarVisible: boolean;

  /**
   * Whether navigation bar is visible
   */
  navigationBarVisible: boolean;
}
