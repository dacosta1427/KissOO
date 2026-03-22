<script lang="ts">
  import { loginForm } from '$lib/remote/auth.remote';
  
  // Initialize backend URL on mount
  import { onMount } from 'svelte';
  import { Server } from '$lib/services/Server';
  
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
</script>

<div class="min-h-screen bg-gray-50 flex items-center justify-center">
  <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
    <h1 class="text-2xl font-bold text-center mb-6">KissOO Login</h1>
    
    <form {...loginForm} class="space-y-4">
      <div class="mb-4">
        <label for="username" class="block text-gray-700 text-sm font-bold mb-2">
          Username
        </label>
        <input
          {...loginForm.fields.username.as('text')}
          id="username"
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
          {...loginForm.fields.password.as('password')}
          id="password"
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          placeholder="Enter password"
          autocomplete="current-password"
        />
      </div>

      <button
        type="submit"
        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
      >
        Login
      </button>
    </form>

    <p class="text-gray-500 text-xs text-center mt-4">
      Don't have an account? <a href="/signup" class="text-blue-600 hover:text-blue-800">Sign Up</a>
    </p>
  </div>
</div>
