/**
 * i18n library for Svelte 5
 * Uses Svelte stores for cross-module reactivity
 */

import { browser } from '$app/environment';
import { writable, derived } from 'svelte/store';

type TranslationDictionary = Record<string, string | TranslationDictionary>;
type Messages = Record<string, string | TranslationDictionary>;

// Import translation files
import en from './messages/en.json';
import nl from './messages/nl.json';
import de from './messages/de.json';

export const locales = ['en', 'nl', 'de'] as const;
export type Locale = (typeof locales)[number];

const messages: Record<Locale, Messages> = { en, nl, de };

// Writable store for locale - reactive across all components
export const currentLocale = writable<Locale>('en');

/**
 * Get current locale value (non-reactive, for one-time reads)
 */
export function getLocale(): Locale {
  let value: Locale = 'en';
  currentLocale.subscribe(v => value = v)();
  return value;
}

/**
 * Set current locale - updates store, all subscribers react
 */
export function setLocale(locale: Locale): void {
  if (locales.includes(locale)) {
    currentLocale.set(locale);
    // Save to cookie (only in browser)
    if (browser) {
      document.cookie = `locale=${locale};path=/;max-age=31536000`;
    }
  }
}

/**
 * Initialize locale from cookie or browser
 */
export function initLocale(preferredLanguage?: string): void {
  if (browser) {
    // Priority: 1. User preference (from backend), 2. Cookie, 3. Browser language
    if (preferredLanguage && locales.includes(preferredLanguage as Locale)) {
      currentLocale.set(preferredLanguage as Locale);
    } else {
      // Check cookie
      const match = document.cookie.match(/locale=([^;]+)/);
      if (match && locales.includes(match[1] as Locale)) {
        currentLocale.set(match[1] as Locale);
      } else {
        // Check browser language
        const browserLang = navigator.language.split('-')[0];
        if (locales.includes(browserLang as Locale)) {
          currentLocale.set(browserLang as Locale);
        }
      }
    }
  }
}

/**
 * Get a translation by key path
 * @param key - dot-separated translation key
 * @param params - optional parameters for interpolation
 * @param locale - optional locale (for reactivity, pass $currentLocale)
 */
export function t(key: string, params?: Record<string, string | number>, locale?: Locale): string {
  // Use provided locale or get current value
  const currentLoc: Locale = locale ?? (() => {
    let v: Locale = 'en';
    currentLocale.subscribe(val => v = val)();
    return v;
  })();
  
  const keys = key.split('.');
  let value: string | TranslationDictionary | undefined = messages[currentLoc];
  
  for (const k of keys) {
    if (typeof value === 'object' && value !== null) {
      value = value[k];
    } else {
      value = undefined;
      break;
    }
  }
  
  if (typeof value !== 'string') {
    // Fallback to English
    value = messages.en;
    for (const k of keys) {
      if (typeof value === 'object' && value !== null) {
        value = value[k];
      } else {
        return key; // Return key if not found
      }
    }
    if (typeof value !== 'string') return key;
  }
  
  // Interpolate parameters
  if (params) {
    for (const [k, v] of Object.entries(params)) {
      value = value.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v));
    }
  }
  
  return value;
}

/**
 * Get all available locale options for UI
 */
export function getLocaleOptions(): { value: string; label: string }[] {
  return [
    { value: 'en', label: 'English' },
    { value: 'nl', label: 'Nederlands' },
    { value: 'de', label: 'Deutsch' }
  ];
}

/**
 * Reactive translation helper - use in components for auto-updates
 * Usage: const { tt } = createReactiveTranslator();
 *        <span>{tt('key')}</span>
 */
export function createReactiveTranslator() {
  return {
    tt: (key: string, params?: Record<string, string | number>) => {
      let locale: Locale = 'en';
      currentLocale.subscribe(v => locale = v)();
      
      const keys = key.split('.');
      let value: string | TranslationDictionary | undefined = messages[locale];
      
      for (const k of keys) {
        if (typeof value === 'object' && value !== null) {
          value = value[k];
        } else {
          return key;
        }
      }
      
      if (typeof value !== 'string') {
        value = messages.en;
        for (const k of keys) {
          if (typeof value === 'object' && value !== null) {
            value = value[k];
          } else {
            return key;
          }
        }
        if (typeof value !== 'string') return key;
      }
      
      if (params) {
        for (const [k, v] of Object.entries(params)) {
          value = value.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v));
        }
      }
      
      return value;
    }
  };
}
