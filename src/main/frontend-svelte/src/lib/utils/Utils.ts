/**
 * Utils.ts - Utility Functions for KissOO Svelte 5 Frontend
 * 
 * Provides modal dialogs, loading overlays, and other common utilities.
 */

import { modal } from '$lib/state/modalStore';
import { validators, commonValidations } from './validation';
import { notificationActions } from '$lib/state/notificationStore.svelte.ts';

// Legacy compatibility - these will be replaced with Modal component
export const Utils = {
  /**
   * Show an alert message
   * @param title - Message title
   * @param message - Message content
   * @returns Promise that resolves when user dismisses
   */
  async showMessage(title: string, message: string): Promise<void> {
    await modal.alert(title, message);
  },

  /**
   * Show a confirmation dialog
   * @param title - Dialog title
   * @param message - Question to ask
   * @param onYes - Callback when user says yes
   * @param onNo - Optional callback when user says no
   */
  async yesNo(
    title: string,
    message: string,
    onYes: () => void | Promise<void>,
    onNo?: () => void | Promise<void>
  ): Promise<void> {
    const confirmed = await modal.confirm(title, message);
    if (confirmed) {
      await onYes();
    } else if (onNo) {
      await onNo();
    }
  },

  /**
   * Show a loading/wait message
   * @param message - Loading message to display
   */
  waitMessage(message: string): void {
    // TODO: Implement loading overlay
    console.log(`Loading: ${message}`);
  },

  /**
   * Hide the loading/wait message
   */
  waitMessageEnd(): void {
    console.log('Loading complete');
  },

  /**
   * Legacy popup functions (for compatibility with legacy code pattern)
   * These will be replaced with Modal component
   */
  popup_open(id: string, _focusId?: string): void {
    console.warn(`popup_open(${id}) - Not yet implemented with Modal component`);
  },

  popup_close(): void {
    console.warn('popup_close() - Not yet implemented with Modal component');
  },

  /**
   * Show a report URL in a new window
   * @param url - URL to open
   */
  showReport(url: string): void {
    if (url) {
      window.open(url, '_blank');
    }
  },

  /**
   * Utility to clean up context (called before loading new page)
   * In SvelteKit, navigation handles this automatically
   */
  cleanup(): void {
    // No-op in SvelteKit
  },

  /**
   * Load a page (legacy compatibility)
   * In SvelteKit, use goto() instead
   */
  loadPage(page: string, target?: string, focusId?: string): void {
    console.warn(`loadPage(${page}) - Use SvelteKit navigation instead`);
  },

  /**
   * Prevent navigation (for unsaved changes)
   * @param prevent - true to prevent, false to allow
   * @param callback - Optional callback when navigation is prevented
   */
  preventNavigation(prevent: boolean, callback?: () => void): void {
    // In SvelteKit, use beforeNavigate() instead
    console.warn('preventNavigation - Use SvelteKit beforeNavigate() instead');
  },

  /**
   * Validation utilities
   */
  validate: {
    required: validators.required,
    email: validators.email,
    phone: validators.phone,
    date: validators.date,
    number: validators.number,
    length: validators.length,
    pattern: validators.pattern,
    ...commonValidations
  },

  /**
   * Notification utilities
   */
  notify: {
    success: notificationActions.success,
    error: notificationActions.error,
    warning: notificationActions.warning,
    info: notificationActions.info,
    clear: notificationActions.clear
  }
};

export default Utils;