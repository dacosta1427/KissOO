<script lang="ts">
  import { session } from '$lib/state/session';
  import { logout } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';

  async function handleLogout() {
    await logout();
    goto(resolve('/'));
  }
</script>

<header class="bg-white shadow-sm border-b">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-center h-16">
      <div class="flex items-center">
        <a href="/" class="text-2xl font-bold text-gray-900 hover:text-gray-700">
          KissOO Svelte 5
        </a>
      </div>
      
      <nav class="flex items-center space-x-4">
        <a href="/" class="text-gray-600 hover:text-gray-900 font-medium">
          Home
        </a>
        
        {#if session.isAuthenticated}
          <a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">
            Users
          </a>
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
    </div>
  </div>
</header>
