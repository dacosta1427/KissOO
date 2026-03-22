<script lang="ts">
  import { onMount } from 'svelte';
  import { Server } from '$lib/services/Server';
  import { goto } from '$app/navigation';
  import { session } from '$lib/state/session.svelte';
  import { logoutAction, checkAuth } from '$lib/remote/auth.remote';
  
  let backendUrl = $state('');
  let isConnected = $state(false);
  
  // Check authentication status
  const authStatus = checkAuth();

  onMount(() => {
    if (window.location.protocol === 'file:') {
      Server.setURL('http://localhost:8080');
    } else {
      const port = parseInt(window.location.port || '0');
      if (port === 5173) {
        Server.setURL('http://localhost:8080');
      } else {
        Server.setURL(window.location.origin);
      }
    }
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

<div class="min-h-screen bg-gray-50">
  <header class="bg-white shadow-sm border-b">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <div class="flex items-center">
          <h1 class="text-2xl font-bold text-gray-900">KissOO Svelte 5</h1>
        </div>
        <div class="flex items-center space-x-4">
          {#await authStatus then auth}
            {#if auth?.authenticated}
              <span class="text-green-600">Authenticated</span>
              <form {...logoutAction} class="inline">
                <button type="submit" class="text-red-600 hover:text-red-800">
                  Logout
                </button>
              </form>
            {:else}
              <a href="/login" class="text-blue-600 hover:text-blue-800">Login</a>
              <a href="/signup" class="text-blue-600 hover:text-blue-800">Sign Up</a>
            {/if}
          {/await}
          <div class="text-sm text-gray-500">
            {#if isConnected}
              <span class="text-green-600">Connected to {backendUrl}</span>
            {:else}
              <span class="text-red-600">Not connected</span>
            {/if}
          </div>
        </div>
      </div>
    </div>
  </header>

  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="text-center">
      <h2 class="text-3xl font-bold text-gray-900 mb-4">Welcome to KissOO Svelte 5</h2>
      <p class="text-lg text-gray-600 mb-8">
        Modern Svelte 5 frontend with Remote Functions
      </p>
      
      {#await authStatus then auth}
        <div class="space-y-4">
          {#if auth?.authenticated}
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
      {/await}
    </div>
  </main>
</div>
