/**
 * Translation helper - exports i18n functions
 * Usage in Svelte 5 components:
 * 
 * <script>
 *   import { t, initLocale, setLocale, getLocale, locales } from '$lib/i18n';
 * </script>
 * 
 * <h1>{t('auth.login_title')}</h1>
 */
import { t, setLocale, getLocale, initLocale, locales } from './index';

export { t, setLocale, getLocale, initLocale, locales };

/**
 * Get all available locales with their display names
 */
export function getLocaleOptions(): { value: string; label: string }[] {
  return [
    { value: 'en', label: 'English' },
    { value: 'nl', label: 'Nederlands' },
    { value: 'de', label: 'Deutsch' }
  ];
}

/**
 * Get locale display name
 */
export function getLocaleLabel(locale: string): string {
  const labels: Record<string, string> = {
    en: 'English',
    nl: 'Nederlands',
    de: 'Deutsch'
  };
  return labels[locale] || locale;
}
