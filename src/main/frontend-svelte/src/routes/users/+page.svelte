<script lang="ts">
  import { getUsers, addUser, deleteUser, updateUser } from '$lib/api/Users';
  import { onMount } from 'svelte';
  import type { User } from '$lib/api/Users';
  import Modal from '$lib/components/Modal.svelte';
  import Form from '$lib/components/Form.svelte';
  import { Utils } from '$lib/utils/Utils';
  import { notificationActions } from '$lib/stores.svelte.js';

  // Svelte 5 RUNES for reactive state
  let users = $state<User[]>([]);
  let loading = $state(false);
  // old variables removed, now using addFormData
  let error = $state('');
  let dataLoading = $state(true);

  // Edit modal state
  let editModalOpen = $state(false);
  let editingUser = $state<User | null>(null);
  let editUserName = $state('');
  let editUserPassword = $state('');
  let editUserActive = $state<'Y' | 'N'>('Y');
  
  // Form data objects
  let addFormData = $state<Record<string, any>>({ username: '', password: '' });
  let editFormData = $state<Record<string, any>>({ username: '', password: '', active: 'Y' });
  let editLoading = $state(false);

  // DERIVED state - form validity
  let canAddUser = $derived(
    (addFormData.username?.length ?? 0) >= 3 && (addFormData.password?.length ?? 0) >= 3
  );
  let canEditUser = $derived(
    (editFormData.username?.length ?? 0) >= 3 && (editFormData.password?.length ?? 0) >= 3
  );

  // Field definitions for Form component
  const addUserFields: Array<{name: string, label: string, type: 'text' | 'password', required: boolean, placeholder: string, helpText: string}> = [
    { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'Enter username', helpText: 'Minimum 3 characters' },
    { name: 'password', label: 'Password', type: 'password', required: true, placeholder: 'Enter password', helpText: 'Minimum 3 characters' }
  ];

  const editUserFields = [
    { name: 'username', label: 'Username', type: 'text' as const, required: true, placeholder: 'Enter username', helpText: 'Minimum 3 characters' },
    { name: 'password', label: 'Password', type: 'password' as const, required: true, placeholder: 'Enter password', helpText: 'Minimum 3 characters' },
    { name: 'active', label: 'Active', type: 'select' as const, required: true, options: [{ value: 'Y', label: 'Active' }, { value: 'N', label: 'Inactive' }] }
  ];

  // editUserFields removed, using editUserFields constant defined earlier? Actually we still need it for edit form. We'll keep but rename to editFields. Let's just keep but ensure it's used. We'll keep as is.



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

  async function handleAddUser(data: Record<string, any>) {
    if (!canAddUser) return;
    
    loading = true;
    error = '';

    try {
      const res = await addUser(data.username, data.password);
      if (res.success) {
        notificationActions.success('User added successfully');
        addFormData = { username: '', password: '' };
        await loadUsers();
      } else {
        error = res.error || 'Failed to add user';
        notificationActions.error(error);
      }
    } catch (e: any) {
      error = 'Failed to add user: ' + (e.message || 'Unknown error');
      notificationActions.error(error);
    } finally {
      loading = false;
    }
  }

  function openEditModal(user: User) {
    editingUser = user;
    editFormData = {
      username: user.userName,
      password: user.userPassword,
      active: user.userActive
    };
    editModalOpen = true;
  }

  async function handleEditUser(data: Record<string, any>) {
    if (!editingUser || !canEditUser) return;
    
    editLoading = true;
    error = '';

    try {
      const res = await updateUser(
        editingUser.id,
        data.username,
        data.password,
        data.active
      );
      if (res.success) {
        notificationActions.success('User updated successfully');
        editModalOpen = false;
        await loadUsers();
      } else {
        error = res.error || 'Failed to update user';
        notificationActions.error(error);
      }
    } catch (e: any) {
      error = 'Failed to update user: ' + (e.message || 'Unknown error');
      notificationActions.error(error);
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
            notificationActions.success('User deleted successfully');
            await loadUsers();
          } else {
            error = res.error || 'Failed to delete user';
            notificationActions.error(error);
          }
        } catch (e: any) {
          error = 'Failed to delete user: ' + (e.message || 'Unknown error');
          notificationActions.error(error);
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
    <Form
      fields={addUserFields}
      bind:data={addFormData}
      loading={loading}
      submitLabel="Add User"
      onSubmit={handleAddUser}
    />
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
  <Form
    fields={editUserFields}
    bind:data={editFormData}
    loading={editLoading}
    submitLabel="Save"
    cancelLabel="Cancel"
    onSubmit={handleEditUser}
    onCancel={() => editModalOpen = false}
  />
</Modal>
