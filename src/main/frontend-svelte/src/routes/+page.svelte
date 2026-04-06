<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';
  import { session } from '$lib/state/session.svelte';
  import { login, isAuthenticated } from '$lib/api/Auth';
  import { Server } from '$lib/services/Server';
  import { t, initLocale, currentLocale } from '$lib/i18n';
  
  // Helper for reactive translations
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Login form state
  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  let rememberMe = $state(false);

  // Role detection from session (set during login)
  let isAdmin = $derived(session.isAdmin === true);
  let isSystemAdmin = $derived(isAdmin && session.adminType === 'system');
  let isContentAdmin = $derived(isAdmin && session.adminType === 'content');
  let isOwner = $derived(!isAdmin && session.ownerOid > 0);
  let isCleaner = $derived(!isAdmin && !isOwner && session.cleanerOid > 0);
  
  // Test data loading state
  let loadingData = $state(false);
  let dataMessage = $state('');

  // Form validity
  let isValid = $derived(username.length > 0 && password.length > 0);

  onMount(async () => {
    // Restore language preference
    const userPreferredLanguage = session.restoreLanguage();
    initLocale(userPreferredLanguage);
    
    // Check if we have stored credentials
    if (session.hasStoredCredentials()) {
      rememberMe = true;
      const credentials = await session.getStoredCredentials();
      if (credentials) {
        username = credentials.username;
      }
    }
  });

  async function handleLogin() {
    if (!isValid) return;
    
    loading = true;
    error = '';

    try {
      const res = await login(username, password);

      if (res._Success) {
        // Store credentials if "Remember me" is checked
        if (rememberMe) {
          await session.storeCredentials(username, password);
        } else {
          session.clearCredentials();
        }
        // Refresh page to show dashboard
        window.location.reload();
      } else {
        error = res._ErrorMessage || t('errors.login_failed');
        password = '';
      }
    } catch (e: any) {
      error = t('errors.login_failed') + ': ' + (e.message || t('errors.server_error'));
    } finally {
      loading = false;
    }
  }
  
  async function loadTestData() {
    loadingData = true;
    dataMessage = '';
    
    try {
      const res = await Server.call('services.LoadTestdata', 'load', {});
      console.log('Load test data response:', res);
      
      if (res._Success) {
        dataMessage = 'Test data loaded successfully!';
      } else {
        dataMessage = 'Error: ' + (res.error || res._ErrorMessage || 'Unknown error');
      }
    } catch (e: any) {
      dataMessage = 'Error: ' + (e.message || 'Failed to load test data');
    } finally {
      loadingData = false;
    }
  }
  
  async function clearTestData() {
    loadingData = true;
    dataMessage = '';
    
    try {
      const res = await Server.call('services.LoadTestdata', 'clear', {});
      console.log('Clear test data response:', res);
      
      if (res._Success) {
        dataMessage = 'Test data cleared!';
      } else {
        dataMessage = 'Error: ' + (res.error || res._ErrorMessage || 'Unknown error');
      }
    } catch (e: any) {
      dataMessage = 'Error: ' + (e.message || 'Failed to clear test data');
    } finally {
      loadingData = false;
    }
  }
</script>

{#if isAuthenticated()}
  <!-- Dashboard for logged-in users -->
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">{tt('nav.home')}</h1>
    
    {#if isSystemAdmin}
      <!-- System Admin Dashboard (full access) -->
      
      <!-- Test Data Actions -->
      <div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <h3 class="text-lg font-semibold text-yellow-800 mb-3">Test Data Management</h3>
        <div class="flex flex-wrap gap-3">
          <button
            onclick={loadTestData}
            disabled={loadingData}
            title={loadingData ? tt('hints.loading') : 'Create sample owners, houses, bookings, cleaners, and schedules for testing'}
            class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
          >
            {loadingData ? 'Loading...' : 'Load Test Data'}
          </button>
          <button
            onclick={clearTestData}
            disabled={loadingData}
            title={loadingData ? tt('hints.loading') : 'Remove all test data (cleaners, bookings, schedules, houses, owners)'}
            class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
          >
            {loadingData ? 'Clearing...' : 'Clear Test Data'}
          </button>
        </div>
        {#if dataMessage}
          <p class="mt-3 text-sm {dataMessage.includes('Error') ? 'text-red-600' : 'text-green-600'}">
            {dataMessage}
          </p>
        {/if}
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <a href="/users" title="Add, edit, and delete system users" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.users')}</h2>
          <p class="text-gray-600">Manage system users</p>
        </a>
        
        <a href="/owners" title="Manage property owners and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.owners')}</h2>
          <p class="text-gray-600">Manage property owners</p>
        </a>
        
        <a href="/houses" title="Manage rental properties and their details" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.houses')}</h2>
          <p class="text-gray-600">Manage all houses</p>
        </a>
        
        <a href="/bookings" title="View and manage guest bookings" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.bookings')}</h2>
          <p class="text-gray-600">View all bookings</p>
        </a>
        
        <a href="/cleaners" title="Manage cleaning staff and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.cleaners')}</h2>
          <p class="text-gray-600">Manage cleaners</p>
        </a>
        
        <a href="/schedules" title="View and manage cleaning schedules" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.schedules')}</h2>
          <p class="text-gray-600">View cleaning schedules</p>
        </a>
      </div>
    {:else if isContentAdmin}
      <!-- Content Admin Dashboard (no user management) -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <a href="/owners" title="Manage property owners and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.owners')}</h2>
          <p class="text-gray-600">Manage property owners</p>
        </a>
        
        <a href="/houses" title="Manage rental properties and their details" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.houses')}</h2>
          <p class="text-gray-600">Manage all houses</p>
        </a>
        
        <a href="/bookings" title="View and manage guest bookings" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.bookings')}</h2>
          <p class="text-gray-600">View all bookings</p>
        </a>
        
        <a href="/cleaners" title="Manage cleaning staff and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.cleaners')}</h2>
          <p class="text-gray-600">Manage cleaners</p>
        </a>
        
        <a href="/schedules" title="View and manage cleaning schedules" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.schedules')}</h2>
          <p class="text-gray-600">View cleaning schedules</p>
        </a>
      </div>
    {:else}
      <!-- Regular User Dashboard -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <a href="/houses" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.houses')}</h2>
          <p class="text-gray-600">View your houses</p>
        </a>
        
        <a href="/bookings" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.bookings')}</h2>
          <p class="text-gray-600">View your bookings</p>
        </a>
        
        <a href="/schedules" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">{tt('nav.schedules')}</h2>
          <p class="text-gray-600">View your schedules</p>
        </a>
      </div>
    {/if}
  </main>
{:else}
  <!-- Login form for non-authenticated users -->
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-center min-h-[60vh]">
      <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h1 class="text-2xl font-bold text-center mb-6">{tt('auth.login_title')}</h1>

        {#if error}
          <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        {/if}

        <form onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
          <div class="mb-6">
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

          <div class="mb-6">
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

          <div class="mb-6 flex items-center">
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
  </main>
{/if}
