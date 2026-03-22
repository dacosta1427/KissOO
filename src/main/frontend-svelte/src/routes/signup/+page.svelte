<script lang="ts">
  import { onMount } from 'svelte';
  import { init, login, addUser } from '$lib/api';
  import { goto } from '$app/navigation';

  let username = '';
  let password = '';
  let confirmPassword = '';
  let error = '';
  let loading = false;

  onMount(() => {
    if (window.location.protocol === 'file:') {
      init('http://localhost:8080');
    } else {
      const port = parseInt(window.location.port || '0');
      if (port === 5173) {
        init('http://localhost:8080');
      } else {
        init(window.location.origin);
      }
    }
  });

  async function handleSignup() {
    if (!username || !password || !confirmPassword) {
      error = 'Please fill in all fields';
      return;
    }

    if (password !== confirmPassword) {
      error = 'Passwords do not match';
      return;
    }

    if (password.length < 3) {
      error = 'Password must be at least 3 characters';
      return;
    }

    loading = true;
    error = '';

    try {
      const res = await addUser({
        userName: username.toLowerCase(),
        userPassword: password,
        userActive: 'Y'
      });

      if (res._Success || res.success) {
        const loginRes = await login(username, password);

        if (loginRes._Success) {
          goto('/');
        } else {
          error = 'Account created. Please login.';
          goto('/login');
        }
      } else {
        error = res._ErrorMessage || res.error || 'Signup failed';
      }
    } catch (e) {
      error = 'Signup failed: ' + e.message;
    } finally {
      loading = false;
    }
  }

  function handleKeydown(event) {
    if (event.key === 'Enter') {
      handleSignup();
    }
  }
</script>

<div class="min-h-screen bg-gray-50 flex items-center justify-center">
  <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
    <h1 class="text-2xl font-bold text-center mb-6">Sign Up</h1>

    {#if error}
      <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {error}
      </div>
    {/if}

    <form on:submit|preventDefault={handleSignup}>
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

      <div class="mb-4">
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
          autocomplete="new-password"
        />
      </div>

      <div class="mb-6">
        <label for="confirmPassword" class="block text-gray-700 text-sm font-bold mb-2">
          Confirm Password
        </label>
        <input
          type="password"
          id="confirmPassword"
          bind:value={confirmPassword}
          on:keydown={handleKeydown}
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          placeholder="Confirm password"
          autocomplete="new-password"
        />
      </div>

      <button
        type="submit"
        disabled={loading}
        class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50"
      >
        {loading ? 'Creating account...' : 'Sign Up'}
      </button>
    </form>

    <p class="text-gray-500 text-xs text-center mt-4">
      Already have an account? <a href="/login" class="text-blue-600 hover:text-blue-800">Login</a>
    </p>
  </div>
</div>