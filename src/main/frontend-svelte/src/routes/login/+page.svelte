<script lang="ts">
  import { login, initBackend } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';
  import { onMount } from 'svelte';

  // Svelte 5 RUNES for reactive state
  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');

  // DERIVED state - form validity
  let isValid = $derived(username.length > 0 && password.length > 0);

  // Initialize backend on mount
  onMount(() => {
    initBackend();
  });

  async function handleLogin() {
    if (!isValid) return;
    
    loading = true;
    error = '';

    try {
      const res = await login(username, password);

      if (res._Success) {
        goto(resolve('/'));
      } else {
        error = res._ErrorMessage || 'Invalid username or password';
        password = '';
      }
    } catch (e: any) {
      error = 'Login failed: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }
</script>

<div class="min-h-screen bg-gray-50 flex items-center justify-center">
  <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
    <h1 class="text-2xl font-bold text-center mb-6">KissOO Login</h1>

    {#if error}
      <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {error}
      </div>
    {/if}

    <form onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
      <div class="mb-4">
        <label for="username" class="block text-gray-700 text-sm font-bold mb-2">
          Username
        </label>
        <input
          type="text"
          id="username"
          bind:value={username}
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          placeholder="Enter username"
          autocomplete="username"
        />
      </div>

      <div class="mb-6">
        <label for="password" class="block text-gray-700 text-sm font-bold mb-2">
          Password
        </label>
        <input
          type="password"
          id="password"
          bind:value={password}
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          placeholder="Enter password"
          autocomplete="current-password"
        />
      </div>

      <button
        type="submit"
        disabled={loading || !isValid}
        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50"
      >
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>

    <p class="text-gray-500 text-xs text-center mt-4">
      Don't have an account? <a href="/signup" class="text-blue-600 hover:text-blue-800">Sign Up</a>
    </p>
  </div>
</div>
