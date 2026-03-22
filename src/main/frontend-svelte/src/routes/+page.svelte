<script lang="ts">
  import { onMount } from 'svelte';
  import { Server } from '$lib/services/Server';
  import { goto } from '$app/navigation';
  import { session } from '$lib/state/session';
  import { logout, isAuthenticated } from '$lib/api/Auth';

  // Svelte 5 RUNES for reactive state
  let backendUrl = $state('');
  let isConnected = $state(false);

  // Restore session from localStorage on mount
  onMount(() => {
    testConnection();
  });

  async function testConnection() {
    try {
      const result = await Server.call('Test', 'ping', {});
      isConnected = result._Success;
      backendUrl = Server.getURL();
    } catch {
      isConnected = false;
      backendUrl = Server.getURL();
    }
  }

  function navigateToUsers() {
    goto('/users');
  }
  
  function navigateToLogin() {
    goto('/login');
  }
</script>

<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  <div class="text-center">
    <h2 class="text-3xl font-bold text-gray-900 mb-4">Welcome to KissOO Svelte 5</h2>
    <p class="text-lg text-gray-600 mb-8">
      Modern Svelte 5 frontend with Simple API Modules
    </p>
    
    <div class="mb-4">
      {#if isConnected}
        <span class="text-green-600">Connected to {backendUrl}</span>
      {:else}
        <span class="text-red-600">Not connected to backend</span>
      {/if}
    </div>
    
    <div class="space-y-4">
      {#if isAuthenticated()}
        <button 
          class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg"
          onclick={testConnection}
        >
          Test Connection
        </button>
        <button
          class="block w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-lg"
          onclick={navigateToUsers}
        >
          User Management
        </button>
      {:else}
        <p class="text-gray-600 mb-4">Please login to access the application.</p>
        <button
          class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg"
          onclick={navigateToLogin}
        >
          Login
        </button>
        <a
          href="/signup"
          class="block w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-lg"
        >
          Sign Up
        </a>
      {/if}
    </div>
  </div>
</main>
