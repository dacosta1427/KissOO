<script lang="ts">
  import { signup, initBackend } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  // Svelte 5 RUNES for reactive state
  let username = $state('');
  let password = $state('');
  let confirmPassword = $state('');
  let loading = $state(false);
  let error = $state('');

  // DERIVED state - form validity
  let isValid = $derived(
    username.length >= 3 && 
    password.length >= 3 && 
    confirmPassword.length > 0 &&
    password === confirmPassword
  );

  // DERIVED state - password match check
  let passwordsMatch = $derived(password === confirmPassword);

  // Initialize backend on mount
  onMount(() => {
    initBackend();
  });

  async function handleSignup() {
    if (!isValid) return;
    
    loading = true;
    error = '';

    try {
      const res = await signup(username, password);

      if (res._Success) {
        goto('/');
      } else {
        error = res._ErrorMessage || 'Signup failed';
      }
    } catch (e: any) {
      error = 'Signup failed: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
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

    <form onsubmit={(e) => { e.preventDefault(); handleSignup(); }}>
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

      <div class="mb-4">
        <label for="password" class="block text-gray-700 text-sm font-bold mb-2">
          Password
        </label>
        <input
          type="password"
          id="password"
          bind:value={password}
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
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {confirmPassword && !passwordsMatch ? 'border-red-500' : ''}"
          placeholder="Confirm password"
          autocomplete="new-password"
        />
        {#if confirmPassword && !passwordsMatch}
          <p class="text-red-500 text-xs mt-1">Passwords do not match</p>
        {/if}
      </div>

      <button
        type="submit"
        disabled={loading || !isValid}
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
