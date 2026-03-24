<script lang="ts">
  import { session } from '$lib/state/session';
  import { logout } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';

  async function handleLogout() {
    await logout();
    goto(resolve('/'));
  }
  
  // Menu state
  let mobileMenuOpen = $state(false);
  let demoMenuOpen = $state(false);
  let cleaningMenuOpen = $state(false);
</script>

<header class="bg-white shadow-sm border-b">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-center h-16">
      <div class="flex items-center">
        <a href="/" class="text-2xl font-bold text-gray-900 hover:text-gray-700">
          KissOO Svelte 5
        </a>
      </div>
      
      <!-- Desktop Navigation -->
      <nav class="hidden md:flex items-center space-x-4">
        <a href="/" class="text-gray-600 hover:text-gray-900 font-medium">
          Home
        </a>
        
        {#if session.isAuthenticated}
          <a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">
            Users
          </a>
          <a href="/crud" class="text-gray-600 hover:text-gray-900 font-medium">
            CRUD
          </a>
          
          <!-- Cleaning Scheduler Dropdown -->
          <div class="relative">
            <button 
              onclick={() => cleaningMenuOpen = !cleaningMenuOpen}
              class="text-gray-600 hover:text-gray-900 font-medium flex items-center"
            >
              Cleaning
              <svg class="ml-1 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {#if cleaningMenuOpen}
              <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200">
                <a href="/cleaners" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Cleaners</a>
                <a href="/bookings" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Bookings</a>
                <a href="/schedules" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Schedules</a>
                <a href="/houses" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Houses</a>
                <div class="border-t border-gray-200 my-1"></div>
                <a href="/benchmark" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Benchmark</a>
                <a href="/rest-services" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">REST Services</a>
                <a href="/controls" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Controls</a>
              </div>
            {/if}
          </div>
          
          <button
            onclick={handleLogout}
            class="text-red-600 hover:text-red-800 font-medium"
          >
            Logout
          </button>
          <span class="text-green-600 text-sm">Authenticated</span>
        {:else}
          <a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">
            Login
          </a>
          <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">
            Sign Up
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
          <a href="/" class="text-gray-600 hover:text-gray-900 font-medium">Home</a>
          
          {#if session.isAuthenticated}
            <a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">Users</a>
            <a href="/crud" class="text-gray-600 hover:text-gray-900 font-medium">CRUD</a>
            <div class="border-t border-gray-200 my-2"></div>
            <a href="/cleaners" class="text-gray-600 hover:text-gray-900 font-medium">Cleaners</a>
            <a href="/bookings" class="text-gray-600 hover:text-gray-900 font-medium">Bookings</a>
            <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">Schedules</a>
            <a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">Houses</a>
            <div class="border-t border-gray-200 my-2"></div>
            <a href="/benchmark" class="text-gray-600 hover:text-gray-900 font-medium">Benchmark</a>
            <a href="/rest-services" class="text-gray-600 hover:text-gray-900 font-medium">REST Services</a>
            <a href="/controls" class="text-gray-600 hover:text-gray-900 font-medium">Controls</a>
            <div class="border-t border-gray-200 my-2"></div>
            <button
              onclick={handleLogout}
              class="text-red-600 hover:text-red-800 font-medium text-left"
            >
              Logout
            </button>
          {:else}
            <a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">Login</a>
            <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">Sign Up</a>
          {/if}
        </div>
      </div>
    {/if}
  </div>
</header>

<!-- Close dropdown when clicking outside -->
<svelte:window onclick={() => { if (demoMenuOpen) demoMenuOpen = false; if (cleaningMenuOpen) cleaningMenuOpen = false; }} />
