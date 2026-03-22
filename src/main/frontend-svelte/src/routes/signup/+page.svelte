<script lang="ts">
  import { signupForm } from '$lib/remote/auth.remote';
  
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
    <h1 class="text-2xl font-bold text-center mb-6">Sign Up</h1>
    
    <form {...signupForm} class="space-y-4">
      <div class="mb-4">
        <label for="username" class="block text-gray-700 text-sm font-bold mb-2">
          Username
        </label>
        <input
          {...signupForm.fields.username.as('text')}
          id="username"
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
          {...signupForm.fields.password.as('password')}
          id="password"
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
          {...signupForm.fields.confirmPassword.as('password')}
          id="confirmPassword"
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          placeholder="Confirm password"
          autocomplete="new-password"
        />
      </div>

      <button
        type="submit"
        class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
      >
        Sign Up
      </button>
    </form>

    <p class="text-gray-500 text-xs text-center mt-4">
      Already have an account? <a href="/login" class="text-blue-600 hover:text-blue-800">Login</a>
    </p>
  </div>
</div>
