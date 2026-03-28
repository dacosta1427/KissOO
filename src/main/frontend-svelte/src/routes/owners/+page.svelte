<script lang="ts">
	import { ownersAPI, housesAPI, type Owner, type House } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
  import { t, currentLocale } from '$lib/i18n';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

  let owners = $state<Owner[]>([]);
  let ownerHouses = $state<House[]>([]);
  let loading = $state(false);
  let error = $state<string | null>(null);
  let showForm = $state(false);
  let editingOwner = $state<Owner | null>(null);
  let selectedOwner = $state<Owner | null>(null);

	let formData = $state({
		name: '',
		email: '',
		phone: '',
		address: ''
	});

	async function loadOwners() {
		loading = true;
		error = null;

		try {
			owners = await ownersAPI.getAll();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}
	
	async function viewOwnerHouses(owner: Owner) {
		selectedOwner = owner;
		try {
			ownerHouses = await housesAPI.getByOwner(owner.id);
		} catch (err: any) {
			ownerHouses = [];
			notificationActions.error(t('errors.failed_to_load'));
		}
	}
	
	function closeOwnerDetail() {
		selectedOwner = null;
		ownerHouses = [];
	}

	function openAddForm() {
		editingOwner = null;
		formData = { name: '', email: '', phone: '', address: '' };
		showForm = true;
	}

	function openEditForm(owner: Owner) {
		editingOwner = owner;
		formData = { 
			name: owner.name, 
			email: owner.email || '', 
			phone: owner.phone || '', 
			address: owner.address || '' 
		};
		showForm = true;
	}

	function handleFormCancel() {
		showForm = false;
		editingOwner = null;
		formData = { name: '', email: '', phone: '', address: '' };
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		
		try {
			if (editingOwner) {
				await ownersAPI.update(editingOwner.id, formData);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.updated_successfully'));
			} else {
				await ownersAPI.create(formData);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.created_successfully'));
			}

			showForm = false;
			editingOwner = null;
			formData = { name: '', email: '', phone: '', address: '' };
			await loadOwners();
		} catch (err: any) {
			notificationActions.error(err.message || t('errors.failed_to_save'));
		}
	}

	async function handleDelete(owner: Owner) {
		if (confirm(t('owners.delete_confirm').replace('"${owner.name}"', `"${owner.name}"`))) {
			try {
				await ownersAPI.delete(owner.id);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.deleted_successfully'));
				await loadOwners();
			} catch (err: any) {
				notificationActions.error(err.message || t('errors.failed_to_delete'));
			}
		}
	}

	$effect(() => {
		loadOwners();
	});
</script>

<div class="owners-page">
	<div class="page-header">
		<h1>{tt('owners.title')}</h1>
		<button class="btn btn-primary" onclick={openAddForm}>{tt('owners.add_owner')}</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			{tt('common.loading')}
		</div>
	{/if}

	{#if error}
		<div class="error-message">{error}</div>
	{/if}

	{#if showForm}
		<div class="form-section">
			<h3 class="form-title">{editingOwner ? t('owners.edit_owner') : t('owners.add_new_owner')}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field">
						<label for="name">{tt('common.name')} <span class="required">*</span></label>
						<input 
							type="text" 
							id="name" 
							bind:value={formData.name} 
							placeholder={tt('owners.enter_owner_name')}
							required 
						/>
					</div>

					<div class="form-field">
						<label for="email">{tt('common.email')}</label>
						<input 
							type="email" 
							id="email" 
							bind:value={formData.email} 
							placeholder={tt('owners.enter_email_address')}
						/>
					</div>

					<div class="form-field">
						<label for="phone">{tt('common.phone')}</label>
						<input 
							type="tel" 
							id="phone" 
							bind:value={formData.phone} 
							placeholder={tt('owners.enter_phone_number')}
						/>
					</div>

					<div class="form-field full-width">
						<label for="address">{tt('common.address')}</label>
						<input 
							type="text" 
							id="address" 
							bind:value={formData.address} 
							placeholder={tt('owners.enter_address')}
						/>
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{editingOwner ? t('common.update') : t('common.add')} {tt('owners.title')}
					</button>
				</div>
			</form>
		</div>
	{/if}

	<div class="owners-grid">
		{#if owners.length === 0}
			<div class="empty-message">{tt('owners.no_owners')}</div>
		{:else}
			{#each owners as owner}
				<div class="owner-card">
					<h3 class="owner-name">{owner.name}</h3>
					{#if owner.email}
						<p class="owner-email">{owner.email}</p>
					{/if}
					{#if owner.phone}
						<p class="owner-phone">{owner.phone}</p>
					{/if}
					{#if owner.address}
						<p class="owner-address">{owner.address}</p>
					{/if}
				<div class="owner-actions">
					<button class="btn btn-primary btn-sm" onclick={() => viewOwnerHouses(owner)}>
						{tt('owners.view_houses')}
					</button>
					<button class="btn btn-secondary btn-sm" onclick={() => openEditForm(owner)}>
						{tt('common.edit')}
					</button>
					<button class="btn btn-danger btn-sm" onclick={() => handleDelete(owner)}>
						{tt('common.delete')}
					</button>
				</div>
				</div>
			{/each}
		{/if}
	</div>
	
	{#if selectedOwner}
		<div class="modal-overlay" onclick={closeOwnerDetail}>
			<div class="modal-content" onclick={(e) => e.stopPropagation()}>
				<div class="modal-header">
					<h2>{selectedOwner.name} - {tt('owners.houses')}</h2>
					<button class="close-btn" onclick={closeOwnerDetail}>&times;</button>
				</div>
				<div class="modal-body">
					{#if ownerHouses.length === 0}
						<div class="empty-message">{tt('owners.no_houses')}</div>
					{:else}
						<div class="houses-list">
							{#each ownerHouses as house}
								<div class="house-item">
									<div class="house-info">
										<h4>{house.name}</h4>
										{#if house.address}
											<p>{house.address}</p>
										{/if}
									</div>
									<span class="house-status" class:active={house.active} class:inactive={!house.active}>
										{house.active ? tt('common.active') : tt('common.inactive')}
									</span>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>
		</div>
	{/if}
</div>

<style>
	.owners-page {
		padding: 2rem;
		max-width: 1200px;
		margin: 0 auto;
	}

	.page-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
		border-bottom: 2px solid var(--border-color, #e5e7eb);
		padding-bottom: 1rem;
	}

	.page-header h1 {
		margin: 0;
		color: var(--text-color, #111827);
	}

	.loading-spinner {
		position: fixed;
		top: 1rem;
		right: 1rem;
		z-index: 50;
		background: #3b82f6;
		color: white;
		padding: 0.5rem 1rem;
		border-radius: 6px;
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.spinner {
		display: inline-block;
		width: 1rem;
		height: 1rem;
		border: 2px solid white;
		border-top-color: transparent;
		border-radius: 50%;
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		to { transform: rotate(360deg); }
	}

	.error-message {
		background: #fee2e2;
		border: 1px solid #fca5a5;
		color: #991b1b;
		padding: 1rem;
		border-radius: 6px;
		margin-bottom: 1rem;
	}

	.form-section {
		background: white;
		border: 1px solid var(--border-color, #e5e7eb);
		border-radius: 8px;
		padding: 1.5rem;
		margin-bottom: 2rem;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	.form-title {
		margin: 0 0 1.5rem 0;
		font-size: 1.125rem;
		font-weight: 600;
	}

	.form-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
	}

	.form-field {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.form-field.full-width {
		grid-column: 1 / -1;
	}

	.form-field label {
		font-size: 0.875rem;
		font-weight: 500;
		color: #374151;
	}

	.form-field .required {
		color: #ef4444;
	}

	.form-field input {
		padding: 0.5rem;
		border: 1px solid #d1d5db;
		border-radius: 6px;
		font-size: 0.875rem;
		font-family: inherit;
	}

	.form-field input:focus {
		outline: none;
		border-color: #3b82f6;
		box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
	}

	.form-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		margin-top: 1.5rem;
		padding-top: 1rem;
		border-top: 1px solid #e5e7eb;
	}

	.owners-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
	}

	.owner-card {
		background: white;
		border: 1px solid var(--border-color, #e5e7eb);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}

	.owner-name {
		margin: 0 0 0.5rem 0;
		font-size: 1.125rem;
		font-weight: 600;
		color: var(--text-color, #111827);
	}

	.owner-email,
	.owner-phone,
	.owner-address {
		margin: 0;
		color: #6b7280;
		font-size: 0.875rem;
	}

	.owner-actions {
		display: flex;
		gap: 0.5rem;
		margin-top: 1rem;
	}

	.btn {
		padding: 0.5rem 1rem;
		border: none;
		border-radius: 6px;
		font-size: 0.875rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}

	.btn-primary {
		background: #3b82f6;
		color: white;
	}

	.btn-primary:hover {
		background: #2563eb;
	}

	.btn-secondary {
		background: #6b7280;
		color: white;
	}

	.btn-secondary:hover {
		background: #4b5563;
	}

	.btn-danger {
		background: #ef4444;
		color: white;
	}

	.btn-danger:hover {
		background: #dc2626;
	}

	.btn-sm {
		padding: 0.25rem 0.75rem;
		font-size: 0.75rem;
	}

	.empty-message {
		grid-column: 1 / -1;
		text-align: center;
		color: #6b7280;
		padding: 2rem;
	}

	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}
	}
	
	.modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 100;
	}
	
	.modal-content {
		background: white;
		border-radius: 12px;
		width: 90%;
		max-width: 600px;
		max-height: 80vh;
		overflow: hidden;
		display: flex;
		flex-direction: column;
		box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
	}
	
	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 1rem 1.5rem;
		border-bottom: 1px solid #e5e7eb;
	}
	
	.modal-header h2 {
		margin: 0;
		font-size: 1.25rem;
	}
	
	.close-btn {
		background: none;
		border: none;
		font-size: 1.5rem;
		cursor: pointer;
		color: #6b7280;
		padding: 0;
		line-height: 1;
	}
	
	.modal-body {
		padding: 1.5rem;
		overflow-y: auto;
	}
	
	.houses-list {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}
	
	.house-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 1rem;
		background: #f9fafb;
		border-radius: 8px;
		border: 1px solid #e5e7eb;
	}
	
	.house-info h4 {
		margin: 0 0 0.25rem 0;
		font-size: 1rem;
	}
	
	.house-info p {
		margin: 0;
		color: #6b7280;
		font-size: 0.875rem;
	}
	
	.house-status {
		padding: 0.25rem 0.75rem;
		border-radius: 9999px;
		font-size: 0.75rem;
		font-weight: 500;
	}
	
	.house-status.active {
		background: #d1fae5;
		color: #065f46;
	}
	
	.house-status.inactive {
		background: #fee2e2;
		color: #991b1b;
	}
</style>
