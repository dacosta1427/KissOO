<script lang="ts">
  import { getUsers, addUserForm, deleteUserForm } from '$lib/remote/users.remote';
  import { logoutAction } from '$lib/remote/auth.remote';
  
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
  
  // Get users query - automatically loads on component mount
  const users = getUsers();
</script>

<div class="p-6 max-w-4xl mx-auto">
  <div class="flex justify-between items-center mb-6">
    <h1 class="text-2xl font-bold">User Management</h1>
    <form {...logoutAction}>
      <button type="submit" class="text-red-600 hover:text-red-800">
        Logout
      </button>
    </form>
  </div>

  <!-- Add User Form -->
  <div class="mb-6">
    <h2 class="text-xl font-semibold mb-3">Add New User</h2>
    <form {...addUserForm} class="space-y-4">
      <div class="grid grid-cols-2 gap-4">
        <input
          {...addUserForm.fields.userName.as('text')}
          placeholder="Username"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <input
          {...addUserForm.fields.userPassword.as('password')}
          placeholder="Password"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <button
        type="submit"
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
      >
        Add User
      </button>
    </form>
  </div>

  <!-- Users List -->
  <div>
    <h2 class="text-xl font-semibold mb-3">Users</h2>
    {#await users}
      <p class="text-gray-500">Loading users...</p>
    {:then userList}
      {#if userList.length === 0}
        <p class="text-gray-500">No users found.</p>
      {:else}
        <div class="space-y-3">
          {#each userList as user}
            <div class="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border">
              <div>
                <p class="font-medium">{user.userName}</p>
                <p class="text-gray-600 text-sm">
                  Status: <span class={user.userActive === 'Y' ? 'text-green-600' : 'text-red-600'}>
                    {user.userActive === 'Y' ? 'Active' : 'Inactive'}
                  </span>
                </p>
              </div>
              <form {...deleteUserForm}>
                <input type="hidden" name="id" value={user.id} />
                <button
                  type="submit"
                  class="text-red-600 hover:text-red-800"
                >
                  Delete
                </button>
              </form>
            </div>
          {/each}
        </div>
      {/if}
    {:catch error}
      <p class="text-red-500">Error loading users: {error.message}</p>
    {/await}
  </div>
  
  <!-- Navigation -->
  <div class="mt-8 pt-4 border-t">
    <a href="/" class="text-blue-600 hover:text-blue-800">← Back to Home</a>
  </div>
</div>
