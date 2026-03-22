<script>
  import { onMount } from 'svelte';
  import { Server } from '$lib/services/Server';
  import { goto } from '$app/navigation';

  let username = '';
  let password = '';
  let error = '';
  let loading = false;

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
  });

  async function login() {
    if (!username || !password) {
      error = 'Please enter username and password';
      return;
    }

    loading = true;
    error = '';

    try {
      const res = await Server.call('', 'Login', {
        username: username.toLowerCase(),
        password: password
      });

      if (res._Success) {
        Server.setUUID(res.uuid);
        goto('/');
      } else {
        error = 'Invalid username or password';
        password = '';
      }
    } catch (e) {
      error = 'Login failed: ' + e.message;
    } finally {
      loading = false;
    }
  }

  function handleKeydown(event) {
    if (event.key === 'Enter') {
      login();
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

    <form on:submit|preventDefault={login}>
      <div class="mb-4">
        <label for="username" class="block text-gray-700 text-sm font-bold mb-2">
          Username
        </label>
        <input
          type="text"
          id="username"
          bind:value={username}
          on:keydown={handleKeydown}
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
          on:keydown={handleKeydown}
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          placeholder="Enter password"
          autocomplete="current-password"
        />
      </div>

      <button
        type="submit"
        disabled={loading}
        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50"
      >
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>

    <p class="text-gray-500 text-xs text-center mt-4">
      Default: admin / admin
    </p>
  </div>
</div>