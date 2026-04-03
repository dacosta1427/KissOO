/**
 * Utils.ts - Utility Functions for KissOO Svelte 5 Frontend
 * 
 * Provides modal dialogs, loading overlays, and other common utilities.
 * Includes date format conversion between backend (YYYYMMDD) and frontend (YYYY-MM-DD).
 */

import { modal } from '$lib/state/modalStore';
import { validators, commonValidations } from './validation';
import { notificationActions } from '$lib/stores.svelte.js';

// ==================== DATE FORMAT CONVERSION ====================

/**
 * Convert YYYYMMDD (backend format) to YYYY-MM-DD (HTML date input format)
 * @param yyyymmdd - Date string in format YYYYMMDD (e.g., "20260426")
 * @returns Date string in format YYYY-MM-DD (e.g., "2026-04-26")
 */
export function toInputDateFormat(yyyymmdd: string): string {
  if (!yyyymmdd || yyyymmdd.length !== 8) return '';
  const year = yyyymmdd.substring(0, 4);
  const month = yyyymmdd.substring(4, 6);
  const day = yyyymmdd.substring(6, 8);
  return `${year}-${month}-${day}`;
}

/**
 * Convert YYYY-MM-DD (HTML date input format) to YYYYMMDD (backend format)
 * @param yyyyMmDd - Date string in format YYYY-MM-DD (e.g., "2026-04-26")
 * @returns Date string in format YYYYMMDD (e.g., "20260426")
 */
export function toBackendDateFormat(yyyyMmDd: string): string {
  if (!yyyyMmDd || yyyyMmDd.length !== 10) return '';
  return yyyyMmDd.replace(/-/g, '');
}

/**
 * Format YYYYMMDD for display (localized)
 * @param yyyymmdd - Date string in format YYYYMMDD
 * @param locale - Optional locale (default: 'en')
 * @returns Localized date string (e.g., "Apr 26, 2026")
 */
export function toDisplayDateFormat(yyyymmdd: string, locale: string = 'en'): string {
  if (!yyyymmdd || yyyymmdd.length !== 8) return '';
  const year = parseInt(yyyymmdd.substring(0, 4));
  const month = parseInt(yyyymmdd.substring(4, 6)) - 1;
  const day = parseInt(yyyymmdd.substring(6, 8));
  const date = new Date(year, month, day);
  return date.toLocaleDateString(locale, { month: 'short', day: 'numeric', year: 'numeric' });
}

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