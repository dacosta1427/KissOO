<script lang="ts">
  import { login, initBackend } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session';
  import { t, currentLocale } from '$lib/i18n';
  
  // Helper for reactive translations
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Svelte 5 RUNES for reactive state
  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  let rememberMe = $state(false);

  // DERIVED state - form validity
  let isValid = $derived(username.length > 0 && password.length > 0);

  // Initialize backend on mount and check for stored credentials
  onMount(async () => {
    initBackend();
    
    // Check for username in URL query parameter (from verification flow)
    const urlParams = new URLSearchParams(window.location.search);
    const urlUsername = urlParams.get('username');
    if (urlUsername) {
      username = urlUsername;
    }
    
    // Check if we have stored credentials (only if no URL username)
    if (!urlUsername && session.hasStoredCredentials()) {
      rememberMe = true;
      const credentials = await session.getStoredCredentials();
      if (credentials) {
        username = credentials.username;
        // Don't pre-fill password for security
      }
    }
  });

  async function handleLogin() {
    if (!isValid) return;
    
    loading = true;
    error = '';

    try {
      const res = await login(username, password);
      console.log('Login response:', res);

      if (res._Success) {
        // Store credentials if "Remember me" is checked
        if (rememberMe) {
          await session.storeCredentials(username, password);
        } else {
          session.clearCredentials();
        }
        goto(resolve('/'));
      } else {
        error = res._ErrorMessage || t('errors.login_failed');
        password = '';
      }
    } catch (e: any) {
      error = t('errors.login_failed') + ': ' + (e.message || t('errors.server_error'));
      console.error('Login error:', e);
    } finally {
      loading = false;
    }
  }
</script>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  <div class="flex items-center justify-center min-h-[60vh]">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
      <h1 class="text-2xl font-bold text-center mb-6">{tt('auth.login_title')}</h1>

      {#if error}
        <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      {/if}

      <form onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
        <div class="mb-4">
          <label for="username" class="block text-gray-700 text-sm font-bold mb-2">
            {tt('auth.username')}
          </label>
          <input
            type="text"
            id="username"
            bind:value={username}
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            placeholder={tt('auth.username')}
            autocomplete="username"
          />
        </div>

        <div class="mb-4">
          <label for="password" class="block text-gray-700 text-sm font-bold mb-2">
            {tt('auth.password')}
          </label>
          <input
            type="password"
            id="password"
            bind:value={password}
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            placeholder={tt('auth.password')}
            autocomplete="current-password"
          />
        </div>

        <div class="mb-4 flex items-center">
          <input
            type="checkbox"
            id="rememberMe"
            bind:checked={rememberMe}
            class="mr-2 leading-tight"
          />
          <label for="rememberMe" class="text-sm text-gray-700">
            {tt('auth.remember_me')}
          </label>
        </div>

        <button
          type="submit"
          disabled={loading || !isValid}
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50"
        >
          {loading ? t('auth.logging_in') : t('auth.login_button')}
        </button>
      </form>

      <p class="text-gray-500 text-xs text-center mt-4">
        {tt('auth.no_account')} <a href="/signup" class="text-blue-600 hover:text-blue-800">{tt('auth.sign_up_here')}</a>
      </p>
    </div>
  </div>
</div>
