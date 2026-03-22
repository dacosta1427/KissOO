<script>
  import { onMount } from 'svelte';
  import { Server } from '$lib/services/Server';

  let users = $state([]);
  let loading = $state(false);
  let newUser = $state({ name: '', email: '' });

  onMount(async () => {
    await loadUsers();
  });

  async function loadUsers() {
    loading = true;
    try {
      const result = await Server.call('Users', 'getRecords', {});
      if (result._Success) {
        users = result.data || [];
      } else {
        console.error('Failed to load users:', result._ErrorMessage);
      }
    } catch (error) {
      console.error('Error loading users:', error);
    } finally {
      loading = false;
    }
  }

  async function createUser() {
    if (!newUser.name || !newUser.email) return;

    loading = true;
    try {
      const result = await Server.call('Users', 'addRecord', newUser);
      if (result._Success) {
        await loadUsers();
        newUser = { name: '', email: '' };
      } else {
        console.error('Failed to create user:', result._ErrorMessage);
      }
    } catch (error) {
      console.error('Error creating user:', error);
    } finally {
      loading = false;
    }
  }

  async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) return;

    loading = true;
    try {
      const result = await Server.call('Users', 'deleteRecord', { id: userId });
      if (result._Success) {
        await loadUsers();
      } else {
        console.error('Failed to delete user:', result._ErrorMessage);
      }
    } catch (error) {
      console.error('Error deleting user:', error);
    } finally {
      loading = false;
    }
  }
</script>

<div class="p-6 max-w-4xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">User Management</h1>

  <div class="mb-6">
    <h2 class="text-xl font-semibold mb-3">Add New User</h2>
    <div class="grid grid-cols-2 gap-4">
      <input
        type="text"
        placeholder="Name"
        bind:value={newUser.name}
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <input
        type="email"
        placeholder="Email"
        bind:value={newUser.email}
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
    <button
      onclick={createUser}
      class="mt-3 bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
      disabled={loading}
    >
      {loading ? 'Creating...' : 'Create User'}
    </button>
  </div>

  <div>
    <h2 class="text-xl font-semibold mb-3">Users</h2>
    {#if loading}
      <p class="text-gray-500">Loading users...</p>
    {/if}
    <div class="space-y-3">
      {#each users as user}
        <div class="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm">
          <div>
            <p class="font-medium">{user.name}</p>
            <p class="text-gray-600 text-sm">{user.email}</p>
          </div>
          <button
            onclick={() => deleteUser(user.id)}
            class="text-red-600 hover:text-red-800"
            disabled={loading}
          >
            Delete
          </button>
        </div>
      {/each}
    </div>
  </div>
</div>
