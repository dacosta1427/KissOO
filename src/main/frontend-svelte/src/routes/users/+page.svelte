<script lang="ts">
  import { getUsers, addUser, deleteUser, toggleUserLogin } from '$lib/api/Users';
  import { onMount } from 'svelte';
  import type { User } from '$lib/api/Users';
  import Modal from '$lib/components/Modal.svelte';
  import Form from '$lib/components/Form.svelte';
  import { Utils } from '$lib/utils/Utils';
  import { notificationActions } from '$lib/stores.svelte.js';
  import { t, currentLocale } from '$lib/i18n';
  
  const tt = (key: string) => t(key, undefined, $currentLocale);

  let users = $state<User[]>([]);
  let allUsers = $state<User[]>([]);
  let loading = $state(false);
  let error = $state('');
  let dataLoading = $state(true);

  let editModalOpen = $state(false);
  let editingUser = $state<User | null>(null);
  
  let addFormData = $state<Record<string, any>>({ username: '', password: '' });
  let editFormData = $state<Record<string, any>>({ username: '', password: '' });
  let editLoading = $state(false);

  let canAddUser = $derived(
    (addFormData.username?.length ?? 0) >= 3 && (addFormData.password?.length ?? 0) >= 3
  );
  let canEditUser = $derived(
    (editFormData.username?.length ?? 0) >= 3 && (editFormData.password?.length ?? 0) >= 3
  );

  let formSection = $state<HTMLElement | null>(null);

  let filterMode = $state<'all' | 'Owner' | 'Cleaner'>('all');

  let filteredUsers = $derived(
    filterMode === 'all' 
      ? allUsers 
      : allUsers.filter(u => u.actorType === filterMode)
  );

  function scrollToEditForm() {
    if (window.innerWidth < 1024 && formSection) {
      formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  let addUserFields = $derived([
    { name: 'username', label: t('users.enter_username'), type: 'text' as const, required: true, placeholder: t('users.enter_username'), helpText: t('users.minimum_3_chars') },
    { name: 'password', label: t('users.enter_password'), type: 'password' as const, required: true, placeholder: t('users.enter_password'), helpText: t('users.minimum_3_chars') }
  ]);

  onMount(() => {
    loadUsers();
  });

  async function loadUsers() {
    console.log('[users] loadUsers called');
    dataLoading = true;
    error = '';
    try {
      allUsers = await getUsers();
      console.log('[users] loadUsers success, count:', allUsers.length);
    } catch (e: any) {
      error = t('errors.failed_to_load') + ': ' + (e.message || 'Unknown error');
      console.error('[users] loadUsers error:', error);
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
        notificationActions.success(t('users.title') + ' ' + t('notifications.created_successfully'));
        addFormData = { username: '', password: '' };
        await loadUsers();
      } else {
        error = res.error || t('errors.failed_to_save');
        notificationActions.error(error);
      }
    } catch (e: any) {
      error = t('errors.failed_to_save') + ': ' + (e.message || 'Unknown error');
      notificationActions.error(error);
    } finally {
      loading = false;
    }
  }

  function openEditModal(user: User) {
    editingUser = user;
    editFormData = {
      username: user.userName,
      password: user.userPassword
    };
    editModalOpen = true;
    scrollToEditForm();
  }

  async function handleEditUser(data: Record<string, any>) {
    if (!editingUser || !canEditUser) return;
    
    editLoading = true;
    error = '';

    try {
      const res = await toggleUserLogin(editingUser.id, data.active === 'Y');
      if (res.success) {
        notificationActions.success(t('users.title') + ' ' + t('notifications.updated_successfully'));
        editModalOpen = false;
        await loadUsers();
      } else {
        error = res.error || t('errors.failed_to_save');
        notificationActions.error(error);
      }
    } catch (e: any) {
      error = t('errors.failed_to_save') + ': ' + (e.message || 'Unknown error');
      notificationActions.error(error);
    } finally {
      editLoading = false;
    }
  }

  async function toggleUserLoginById(id: number, canLogin: boolean) {
    const idx = allUsers.findIndex(u => u.id === id);
    if (idx >= 0) {
      allUsers[idx] = { ...allUsers[idx], canLogin };
    }
    try {
      const res = await toggleUserLogin(id, canLogin);
      if (res.success) {
        notificationActions.success(res.message || (canLogin ? 'Login enabled' : 'Login disabled'));
      } else {
        if (idx >= 0) {
          allUsers[idx] = { ...allUsers[idx], canLogin: !canLogin };
        }
        notificationActions.error(res.error || 'Failed to toggle login');
      }
    } catch (err: any) {
      if (idx >= 0) {
        allUsers[idx] = { ...allUsers[idx], canLogin: !canLogin };
      }
      notificationActions.error(err.message || 'Failed to toggle login');
    }
  }

  async function handleDeleteUser(id: number) {
    await Utils.yesNo(
      t('common.confirm'),
      t('users.delete_confirm'),
      async () => {
        loading = true;
        error = '';

        try {
          const res = await deleteUser(id);
          if (res.success) {
            notificationActions.success(t('users.title') + ' ' + t('notifications.deleted_successfully'));
            await loadUsers();
          } else {
            error = res.error || t('errors.failed_to_delete');
            notificationActions.error(error);
          }
        } catch (e: any) {
          error = t('errors.failed_to_delete') + ': ' + (e.message || 'Unknown error');
          notificationActions.error(error);
        } finally {
          loading = false;
        }
      }
    );
  }
</script>

<div class="p-6 max-w-4xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">{tt('users.title')}</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  <!-- Add User Form -->
  <div class="mb-6">
    <h2 class="text-xl font-semibold mb-3">{tt('users.add_new_user')}</h2>
    <Form
      fields={addUserFields}
      bind:data={addFormData}
      {loading}
      submitLabel={tt('users.add_user')}
      onSubmit={handleAddUser}
    />
  </div>

  <!-- Filter Buttons -->
  <div class="flex gap-2 mb-4">
    <button
      class="filter-btn"
      class:active={filterMode === 'all'}
      onclick={() => filterMode = 'all'}
    >
      All
    </button>
    <button
      class="filter-btn"
      class:active={filterMode === 'Owner'}
      onclick={() => filterMode = 'Owner'}
    >
      Owners
    </button>
    <button
      class="filter-btn"
      class:active={filterMode === 'Cleaner'}
      onclick={() => filterMode = 'Cleaner'}
    >
      Cleaners
    </button>
  </div>

  <!-- Users List -->
  <div>
    <h2 class="text-xl font-semibold mb-3">{tt('users.users_list')}</h2>
    {#if dataLoading}
      <p class="text-gray-500">{tt('users.loading_users')}</p>
    {:else if filteredUsers.length === 0}
      <p class="text-gray-500">{tt('users.no_users')}</p>
    {:else}
      <div class="space-y-3">
        {#each filteredUsers as user (user.id)}
          <div class="user-card">
            <div class="user-info">
              <div class="user-main">
                <span class="font-medium">{user.userName}</span>
                {#if user.actorType}
                  <span class="actor-badge" class:owner={user.actorType === 'Owner'} class:cleaner={user.actorType === 'Cleaner'}>
                    {user.actorType}
                  </span>
                {/if}
              </div>
              <div class="user-details">
                <span class="text-gray-600 text-sm">
                  {tt('common.can_login')}: 
                  <span class={user.canLogin ? 'text-green-600' : 'text-red-600'}>
                    {user.canLogin ? t('common.yes') : t('common.no')}
                  </span>
                </span>
                <!-- Email verified indicator -->
                <span class="ml-2" title={user.emailVerified ? 'Email verified' : 'Email not verified'}>
                  {#if user.emailVerified}
                    <svg class="inline w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                      <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
                    </svg>
                  {:else}
                    <svg class="inline w-4 h-4 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                      <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
                    </svg>
                  {/if}
                </span>
                {#if user.email}
                  <span class="text-gray-400 ml-1">({user.email})</span>
                {/if}
              </div>
            </div>
            <div class="user-actions">
              <button
                type="button"
                class="card-toggle"
                class:active={user.canLogin}
                onclick={() => toggleUserLoginById(user.id, !user.canLogin)}
                title={user.canLogin ? 'Disable login' : 'Enable login'}
              ></button>
              <button
                onclick={(e) => { e.stopPropagation(); openEditModal(user); }}
                class="text-blue-600 hover:text-blue-800"
                disabled={loading}
              >
                {tt('common.edit')}
              </button>
              <button
                onclick={(e) => { e.stopPropagation(); handleDeleteUser(user.id); }}
                class="text-red-600 hover:text-red-800"
                disabled={loading}
              >
                {tt('common.delete')}
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<style>
  .filter-btn {
    padding: 0.5rem 1rem;
    border: 1px solid #e5e7eb;
    background: white;
    cursor: pointer;
    font-size: 0.875rem;
    border-radius: 6px;
  }
  .filter-btn.active {
    background: #3b82f6;
    color: white;
    border-color: #3b82f6;
  }

  .user-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 1rem;
    background: white;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
  }
  .user-card:hover {
    border-color: #3b82f6;
    box-shadow: 0 2px 8px rgba(59,130,246,0.2);
  }

  .user-info {
    flex: 1;
  }
  .user-main {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.25rem;
  }
  .user-details {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
  }

  .actor-badge {
    padding: 0.125rem 0.5rem;
    border-radius: 9999px;
    font-size: 0.625rem;
    font-weight: 500;
    text-transform: uppercase;
  }
  .actor-badge.owner {
    background: #dbeafe;
    color: #1e40af;
  }
  .actor-badge.cleaner {
    background: #d1fae5;
    color: #065f46;
  }

  .user-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .card-toggle {
    position: relative;
    width: 36px;
    height: 18px;
    border-radius: 9px;
    border: none;
    cursor: pointer;
    background: #fca5a5;
    transition: background 0.15s ease, opacity 0.15s ease;
    padding: 0;
    flex-shrink: 0;
  }
  .card-toggle::after {
    content: '';
    position: absolute;
    top: 2px;
    left: 2px;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: white;
    box-shadow: 0 1px 2px rgba(0,0,0,0.2);
    transition: transform 0.15s ease;
  }
  .card-toggle.active {
    background: #10b981;
  }
  .card-toggle.active::after {
    transform: translateX(18px);
  }
  .card-toggle:active {
    opacity: 0.7;
  }
</style>
