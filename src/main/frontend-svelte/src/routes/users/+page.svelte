<script lang="ts">
  import { getUsers, addUser, deleteUser, updateUser } from '$lib/api/Users';
  import { onMount } from 'svelte';
  import type { User } from '$lib/api/Users';
  import Modal from '$lib/components/Modal.svelte';
  import { Utils } from '$lib/utils/Utils';

  // Svelte 5 RUNES for reactive state
  let users = $state<User[]>([]);
  let loading = $state(false);
  let newUserName = $state('');
  let newUserPassword = $state('');
  let error = $state('');
  let dataLoading = $state(true);

  // Edit modal state
  let editModalOpen = $state(false);
  let editingUser = $state<User | null>(null);
  let editUserName = $state('');
  let editUserPassword = $state('');
  let editUserActive = $state<'Y' | 'N'>('Y');
  let editLoading = $state(false);

  // DERIVED state - form validity
  let canAddUser = $derived(newUserName.length >= 3 && newUserPassword.length >= 3);
  let canEditUser = $derived(editUserName.length >= 3 && editUserPassword.length >= 3);

  // Load data on mount
  onMount(() => {
    loadUsers();
  });

  async function loadUsers() {
    dataLoading = true;
    error = '';
    try {
      users = await getUsers();
    } catch (e: any) {
      error = 'Failed to load users: ' + (e.message || 'Unknown error');
    } finally {
      dataLoading = false;
    }
  }

  async function handleAddUser() {
    if (!canAddUser) return;
    
    loading = true;
    error = '';

    try {
      const res = await addUser(newUserName, newUserPassword);
      if (res.success) {
        newUserName = '';
        newUserPassword = '';
        await loadUsers();
      } else {
        error = res.error || 'Failed to add user';
      }
    } catch (e: any) {
      error = 'Failed to add user: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }

  function openEditModal(user: User) {
    editingUser = user;
    editUserName = user.userName;
    editUserPassword = user.userPassword;
    editUserActive = user.userActive;
    editModalOpen = true;
  }

  async function handleEditUser() {
    if (!editingUser || !canEditUser) return;
    
    editLoading = true;
    error = '';

    try {
      const res = await updateUser(
        editingUser.id,
        editUserName,
        editUserPassword,
        editUserActive
      );
      if (res.success) {
        editModalOpen = false;
        await loadUsers();
      } else {
        error = res.error || 'Failed to update user';
      }
    } catch (e: any) {
      error = 'Failed to update user: ' + (e.message || 'Unknown error');
    } finally {
      editLoading = false;
    }
  }

  async function handleDeleteUser(id: number) {
    await Utils.yesNo(
      'Confirm',
      'Are you sure you want to delete this user?',
      async () => {
        loading = true;
        error = '';

        try {
          const res = await deleteUser(id);
          if (res.success) {
            await loadUsers();
          } else {
            error = res.error || 'Failed to delete user';
          }
        } catch (e: any) {
          error = 'Failed to delete user: ' + (e.message || 'Unknown error');
        } finally {
          loading = false;
        }
      }
    );
  }
</script>

<div class="p-6 max-w-4xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">User Management</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  <!-- Add User Form -->
  <div class="mb-6">
    <h2 class="text-xl font-semibold mb-3">Add New User</h2>
    <div class="space-y-4">
      <div class="grid grid-cols-2 gap-4">
        <input
          type="text"
          placeholder="Username"
          bind:value={newUserName}
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <input
          type="password"
          placeholder="Password"
          bind:value={newUserPassword}
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <button
        onclick={handleAddUser}
        disabled={loading || !canAddUser}
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        {loading ? 'Adding...' : 'Add User'}
      </button>
    </div>
  </div>

  <!-- Users List -->
  <div>
    <h2 class="text-xl font-semibold mb-3">Users</h2>
    {#if dataLoading}
      <p class="text-gray-500">Loading users...</p>
    {:else if users.length === 0}
      <p class="text-gray-500">No users found.</p>
    {:else}
      <div class="space-y-3">
        {#each users as user (user.id)}
          <div class="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border">
            <div>
              <p class="font-medium">{user.userName}</p>
              <p class="text-gray-600 text-sm">
                Status: <span class={user.userActive === 'Y' ? 'text-green-600' : 'text-red-600'}>
                  {user.userActive === 'Y' ? 'Active' : 'Inactive'}
                </span>
              </p>
            </div>
            <div class="flex gap-2">
              <button
                onclick={() => openEditModal(user)}
                class="text-blue-600 hover:text-blue-800"
                disabled={loading}
              >
                Edit
              </button>
              <button
                onclick={() => handleDeleteUser(user.id)}
                class="text-red-600 hover:text-red-800"
                disabled={loading}
              >
                Delete
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<!-- Edit User Modal -->
<Modal 
  bind:open={editModalOpen} 
  title="Edit User"
  onClose={() => editModalOpen = false}
>
  <div class="space-y-4">
    <div>
      <label class="block text-sm font-medium text-gray-700">Username</label>
      <input
        type="text"
        bind:value={editUserName}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700">Password</label>
      <input
        type="password"
        bind:value={editUserPassword}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700">Active</label>
      <select
        bind:value={editUserActive}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <option value="Y">Active</option>
        <option value="N">Inactive</option>
      </select>
    </div>
  </div>
  
  {#snippet footer()}
    <button
      type="button"
      class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
      onclick={handleEditUser}
      disabled={editLoading || !canEditUser}
    >
      {editLoading ? 'Saving...' : 'Save'}
    </button>
    <button
      type="button"
      class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
      onclick={() => editModalOpen = false}
    >
      Cancel
    </button>
  {/snippet}
</Modal>
