<script lang="ts">
  import { getUsers, addUser, deleteUser, updateUser } from '$lib/api/Users';
  import { onMount } from 'svelte';
  import type { User } from '$lib/api/Users';
  import Modal from '$lib/components/Modal.svelte';
  import Form from '$lib/components/Form.svelte';
  import { Utils } from '$lib/utils/Utils';
  import { notificationActions } from '$lib/stores.svelte.js';
  import { t, currentLocale } from '$lib/i18n';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

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

  // Form section ref for scroll on small screens
  let formSection = $state<HTMLElement | null>(null);

  function scrollToEditForm() {
    // Scroll to edit form on small screens (mobile/tablet)
    if (window.innerWidth < 1024 && formSection) {
      formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  // Field definitions for Form component (reactive)
  let addUserFields = $derived([
    { name: 'username', label: t('users.enter_username'), type: 'text' as const, required: true, placeholder: t('users.enter_username'), helpText: t('users.minimum_3_chars') },
    { name: 'password', label: t('users.enter_password'), type: 'password' as const, required: true, placeholder: t('users.enter_password'), helpText: t('users.minimum_3_chars') }
  ]);

  let editUserFields = $derived([
    { name: 'username', label: t('users.enter_username'), type: 'text' as const, required: true, placeholder: t('users.enter_username'), helpText: t('users.minimum_3_chars') },
    { name: 'password', label: t('users.enter_password'), type: 'password' as const, required: true, placeholder: t('users.enter_password'), helpText: t('users.minimum_3_chars') },
    { name: 'active', label: t('common.active'), type: 'select' as const, required: true, options: [{ value: 'Y', label: t('common.active') }, { value: 'N', label: t('common.inactive') }] }
  ]);

  // editUserFields removed, using editUserFields constant defined earlier? Actually we still need it for edit form. We'll keep but rename to editFields. Let's just keep but ensure it's used. We'll keep as is.



  // Load data on mount
  onMount(() => {
    loadUsers();
  });

  async function loadUsers() {
    console.log('[users] loadUsers called');
    dataLoading = true;
    error = '';
    try {
      users = await getUsers();
      console.log('[users] loadUsers success, count:', users.length);
    } catch (e: any) {
      error = t('errors.failed_to_load') + ': ' + (e.message || 'Unknown error');
      console.error('[users] loadUsers error:', error);
    } finally {
      dataLoading = false;
    }
  }

  async function handleAddUser(data: Record<string, any>) {
    console.log('[users] handleAddUser called with:', data);
    if (!canAddUser) {
      console.log('[users] canAddUser false, aborting');
      return;
    }
    
    loading = true;
    error = '';

    try {
      const res = await addUser(data.username, data.password);
      console.log('[users] addUser response:', res);
      if (res.success) {
        notificationActions.success(t('users.title') + ' ' + t('notifications.created_successfully'));
        addFormData = { username: '', password: '' };
        console.log('[users] addUser success, reloading users...');
        await loadUsers();
      } else {
        error = res.error || t('errors.failed_to_save');
        console.error('[users] addUser failed:', error);
        notificationActions.error(error);
      }
    } catch (e: any) {
      error = t('errors.failed_to_save') + ': ' + (e.message || 'Unknown error');
      console.error('[users] addUser exception:', error);
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
    scrollToEditForm();
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
      loading={loading}
      submitLabel={tt('users.add_user')}
      onSubmit={handleAddUser}
    />
  </div>

  <!-- Users List -->
  <div>
    <h2 class="text-xl font-semibold mb-3">{tt('users.users_list')}</h2>
    {#if dataLoading}
      <p class="text-gray-500">{tt('users.loading_users')}</p>
    {:else if users.length === 0}
      <p class="text-gray-500">{tt('users.no_users')}</p>
    {:else}
      <div class="space-y-3">
        {#each users as user (user.id)}
          <!-- svelte-ignore a11y_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_static_element_interactions -->
          <div class="clickable flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border" onclick={() => openEditModal(user)} onkeydown={(e) => e.key === 'Enter' && openEditModal(user)}>
            <div>
              <p class="font-medium">{user.userName}</p>
              <p class="text-gray-600 text-sm">
                {tt('common.status')}: <span class={user.userActive === 'Y' ? 'text-green-600' : 'text-red-600'}>
                  {user.userActive === 'Y' ? t('common.active') : t('common.inactive')}
                </span>
              </p>
            </div>
            <div class="flex gap-2">
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

<!-- Edit User Modal -->
<Modal 
  bind:open={editModalOpen} 
  title={tt('users.edit_user')}
  onClose={() => editModalOpen = false}
  bind:this={formSection}
>
  <Form
    fields={editUserFields}
    bind:data={editFormData}
    loading={editLoading}
    submitLabel={tt('users.save')}
    cancelLabel={tt('common.cancel')}
    onSubmit={handleEditUser}
    onCancel={() => editModalOpen = false}
  />
</Modal>

<style>
  .clickable { cursor: pointer; }
  .clickable:hover { border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
</style>
