<script lang="ts">
  import { session } from '$lib/state/session';
  import { logout } from '$lib/api/Auth';
  import { t, currentLocale } from '$lib/i18n';
  import LanguageSwitcher from './LanguageSwitcher.svelte';
  
  // Helper for reactive translations
  const tt = (key: string) => t(key, undefined, $currentLocale);

  async function handleLogout() {
    await logout();
  }
  
  // Menu state
  let mobileMenuOpen = $state(false);
</script>

<header class="sticky top-0 z-50 bg-white shadow-sm border-b">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-center h-16">
      <div class="flex items-center">
        <a href="/" class="text-2xl font-bold text-gray-900 hover:text-gray-700">
          KissOO Svelte 5
        </a>
      </div>
      
      <!-- Desktop Navigation -->
      <nav class="hidden md:flex items-center space-x-4">
        <!-- Login Status Indicator -->
        <div class="flex items-center mr-2" title={session.isAuthenticated ? 'Logged in' : 'Not logged in'}>
          {#if session.isAuthenticated}
            <span class="inline-block w-3 h-3 rounded-full bg-green-500" title="Logged in"></span>
          {:else}
            <span class="inline-block w-3 h-3 rounded-full bg-red-500" title="Not logged in"></span>
          {/if}
        </div>
        
        <a href="/" class="text-gray-600 hover:text-gray-900 font-medium">
          {tt('nav.home')}
        </a>
        
        {#if session.isAuthenticated}
          <!-- DEBUG -->
          <span class="text-xs text-red-500 mr-2">isAdmin={session.isAdmin} ownerOid={session.ownerOid} cleanerOid={session.cleanerOid}</span>
          <!-- Admin sees all links -->
          {#if session.isAdmin}
            <a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.houses')}
            </a>
            <a href="/owners" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.owners')}
            </a>
            <a href="/cleaners" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.cleaners')}
            </a>
            <a href="/bookings" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.bookings')}
            </a>
            <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.schedules')}
            </a>
            <a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.users')}
            </a>
          <!-- Owner sees their own houses, bookings, schedules -->
          {:else if session.ownerOid > 0}
            <a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.houses')}
            </a>
            <a href="/bookings" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.bookings')}
            </a>
            <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.schedules')}
            </a>
          <!-- Cleaner sees their own schedules -->
          {:else if session.cleanerOid > 0}
            <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">
              {tt('nav.schedules')}
            </a>
          {/if}
          
          <LanguageSwitcher />
          
          <button
            onclick={handleLogout}
            class="text-red-600 hover:text-red-800 font-medium"
          >
            {tt('nav.logout')}
          </button>
          <span class="text-green-600 text-sm">{session.username || 'User'}</span>
        {:else}
          <LanguageSwitcher />
          <a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">
            {tt('auth.login_button')}
          </a>
          <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">
            {tt('auth.signup_button')}
          </a>
        {/if}
      </nav>
      
      <!-- Mobile menu button -->
      <div class="md:hidden">
        <button 
          onclick={() => mobileMenuOpen = !mobileMenuOpen}
          class="text-gray-600 hover:text-gray-900"
        >
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            {#if mobileMenuOpen}
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            {:else}
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            {/if}
          </svg>
        </button>
      </div>
    </div>
    
    <!-- Mobile Navigation -->
    {#if mobileMenuOpen}
      <div class="md:hidden pb-4">
        <div class="flex flex-col space-y-2">
          <!-- Login Status Indicator (Mobile) -->
          <div class="flex items-center py-2" title={session.isAuthenticated ? 'Logged in' : 'Not logged in'}>
            {#if session.isAuthenticated}
              <span class="inline-block w-3 h-3 rounded-full bg-green-500 mr-2"></span>
              <span class="text-sm text-gray-600">{session.username || 'User'}</span>
            {:else}
              <span class="inline-block w-3 h-3 rounded-full bg-red-500 mr-2"></span>
              <span class="text-sm text-gray-600">Not logged in</span>
            {/if}
          </div>
          
          <a href="/" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.home')}</a>
          
          {#if session.isAuthenticated}
            <a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.houses')}</a>
            <a href="/owners" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.owners')}</a>
            <a href="/cleaners" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.cleaners')}</a>
            <a href="/bookings" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.bookings')}</a>
            <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.schedules')}</a>
            <a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">{tt('nav.users')}</a>
            <div class="border-t border-gray-200 my-2"></div>
            <div class="py-2"><LanguageSwitcher /></div>
            <button
              onclick={handleLogout}
              class="text-red-600 hover:text-red-800 font-medium text-left"
            >
              {tt('nav.logout')}
            </button>
            <span class="text-green-600 text-sm mt-2">{session.username || 'User'}</span>
          {:else}
            <div class="py-2"><LanguageSwitcher /></div>
            <a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">{tt('auth.login_button')}</a>
            <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">{tt('auth.signup_button')}</a>
          {/if}
        </div>
      </div>
    {/if}
  </div>
</header>

<!-- Close mobile menu when clicking outside -->
<svelte:window onclick={() => { if (mobileMenuOpen) mobileMenuOpen = false; }} />
