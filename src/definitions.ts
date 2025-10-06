export interface AndroidSystemBarsPlugin {
  /**
   * Initialize plugin and get device info
   */
  initialize(): Promise<InitializeResult>;

  /**
   * Set status bar style and color
   */
  setStyle(options: SetStyleOptions): Promise<void>;

  /**
   * Hide status bar
   */
  hide(): Promise<void>;

  /**
   * Show status bar
   */
  show(): Promise<void>;

  /**
   * Enter fullscreen mode
   */
  enterFullscreen(options: EnterFullscreenOptions): Promise<void>;

  /**
   * Exit fullscreen mode
   */
  exitFullscreen(options: ExitFullscreenOptions): Promise<void>;

  /**
   * Set overlay mode (Android 35+ only)
   */
  setOverlay(options: SetOverlayOptions): Promise<void>;

  /**
   * Get current window insets information
   */
  getInsets(): Promise<InsetsResult>;

  /**
   * Set navigation bar style and color
   */
  setNavigationBarStyle(options: SetNavigationBarStyleOptions): Promise<void>;

  /**
   * Hide navigation bar
   */
  hideNavigationBar(): Promise<void>;

  /**
   * Show navigation bar
   */
  showNavigationBar(): Promise<void>;
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

export interface SetStyleOptions {
  /**
   * Status bar style
   */
  style: 'LIGHT' | 'DARK' | 'DEFAULT';

  /**
   * Status bar background color (hex format: #RRGGBB or #AARRGGBB)
   */
  color?: string;
}

export interface EnterFullscreenOptions {
  /**
   * Fullscreen mode type
   */
  mode: 'IMMERSIVE' | 'LEAN';
}

export interface ExitFullscreenOptions {
  /**
   * Status bar style to restore
   */
  style: 'LIGHT' | 'DARK' | 'DEFAULT';

  /**
   * Status bar color to restore
   */
  color?: string;
}

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


