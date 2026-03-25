<script lang="ts">
	import { housesAPI, ownersAPI, type House, type Owner } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';

	let houses = $state<House[]>([]);
	let owners = $state<Owner[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingHouse = $state<House | null>(null);
	let showNewOwnerModal = $state(false);

	let formData = $state({
		name: '',
		address: '',
		description: '',
		owner_id: 0 as number
	});

	let newOwnerData = $state({
		name: '',
		email: '',
		phone: '',
		address: ''
	});

	async function loadHouses() {
		loading = true;
		error = null;

		try {
			houses = await housesAPI.getAll();
		} catch (err: any) {
			error = err.message || 'Failed to load houses';
		} finally {
			loading = false;
		}
	}

	async function loadOwners() {
		try {
			owners = await ownersAPI.getAll();
		} catch (err: any) {
			console.error('Failed to load owners:', err);
		}
	}

	function openAddForm() {
		editingHouse = null;
		formData = { name: '', address: '', description: '', owner_id: 0 };
		showForm = true;
	}

	function openEditForm(house: House) {
		editingHouse = house;
		formData = { 
			name: house.name, 
			address: house.address, 
			description: house.description || '',
			owner_id: house.owner_id || 0
		};
		showForm = true;
	}

	function handleFormCancel() {
		showForm = false;
		editingHouse = null;
		formData = { name: '', address: '', description: '', owner_id: 0 };
	}

	function handleOwnerChange() {
		if (formData.owner_id === -1) {
			showNewOwnerModal = true;
			formData.owner_id = 0;
		}
	}

	async function handleAddNewOwner() {
		if (!newOwnerData.name.trim()) {
			notificationActions.error('Owner name is required');
			return;
		}
		
		try {
			const newOwner = await ownersAPI.create(newOwnerData);
			notificationActions.success('Owner created successfully');
			await loadOwners();
			formData.owner_id = newOwner.id;
			showNewOwnerModal = false;
			newOwnerData = { name: '', email: '', phone: '', address: '' };
		} catch (err: any) {
			notificationActions.error(err.message || 'Failed to create owner');
		}
	}

	function cancelNewOwnerModal() {
		showNewOwnerModal = false;
		newOwnerData = { name: '', email: '', phone: '', address: '' };
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		
		const dataToSend = {
			name: formData.name,
			address: formData.address,
			description: formData.description,
			owner_id: formData.owner_id || 0
		};
		
		try {
			if (editingHouse) {
				await housesAPI.update(editingHouse.id, dataToSend);
				notificationActions.success('House updated successfully');
			} else {
				await housesAPI.create(dataToSend);
				notificationActions.success('House created successfully');
			}

			showForm = false;
			editingHouse = null;
			formData = { name: '', address: '', description: '', owner_id: 0 };
			await loadHouses();
		} catch (err: any) {
			notificationActions.error(err.message || 'Failed to save house');
		}
	}

	async function handleDelete(house: House) {
		if (confirm(`Are you sure you want to delete "${house.name}"?`)) {
			try {
				await housesAPI.delete(house.id);
				notificationActions.success('House deleted successfully');
				await loadHouses();
			} catch (err: any) {
				notificationActions.error(err.message || 'Failed to delete house');
			}
		}
	}

	function getOwnerName(ownerId: number): string {
		if (!ownerId || ownerId === 0) return 'No owner';
		const owner = owners.find(o => o.id === ownerId);
		return owner ? owner.name : 'Unknown';
	}

	$effect(() => {
		loadHouses();
		loadOwners();
	});
</script>

<div class="houses-page">
	<div class="page-header">
		<h1>Houses</h1>
		<button class="btn btn-primary" onclick={openAddForm}>Add House</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			Loading...
		</div>
	{/if}

	{#if error}
		<div class="error-message">{error}</div>
	{/if}

	{#if showForm}
		<div class="form-section">
			<h3 class="form-title">{editingHouse ? 'Edit House' : 'Add New House'}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<!-- Name -->
					<div class="form-field">
						<label for="name">Name <span class="required">*</span></label>
						<input 
							type="text" 
							id="name" 
							bind:value={formData.name} 
							placeholder="Enter house name"
							required 
						/>
					</div>

					<!-- Address -->
					<div class="form-field full-width">
						<label for="address">Address <span class="required">*</span></label>
						<input 
							type="text" 
							id="address" 
							bind:value={formData.address} 
							placeholder="Enter house address"
							required 
						/>
					</div>

					<!-- Owner -->
					<div class="form-field">
						<label for="owner">Owner</label>
						<select id="owner" bind:value={formData.owner_id} onchange={handleOwnerChange}>
							<option value={0}>-- No Owner --</option>
							{#each owners as owner}
								<option value={owner.id}>{owner.name}</option>
							{/each}
							<option value={-1}>+ New owner</option>
						</select>
					</div>

					<!-- Description -->
					<div class="form-field full-width">
						<label for="description">Description</label>
						<textarea 
							id="description" 
							bind:value={formData.description} 
							placeholder="Enter house description"
							rows="3"
						></textarea>
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						Cancel
					</button>
					<button type="submit" class="btn btn-primary">
						{editingHouse ? 'Update' : 'Add'} House
					</button>
				</div>
			</form>
		</div>
	{/if}

	{#if showNewOwnerModal}
		<div class="modal-overlay" role="dialog" aria-modal="true" onclick={cancelNewOwnerModal} onkeydown={(e) => e.key === 'Escape' && cancelNewOwnerModal()}>
			<div class="modal-content" role="document" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()}>
				<h3 class="modal-title">Add New Owner</h3>
				<form onsubmit={(e) => { e.preventDefault(); handleAddNewOwner(); }}>
					<div class="form-grid">
						<div class="form-field">
							<label for="newOwnerName">Name <span class="required">*</span></label>
							<input 
								type="text" 
								id="newOwnerName" 
								bind:value={newOwnerData.name} 
								placeholder="Enter owner name"
								required 
							/>
						</div>
						<div class="form-field">
							<label for="newOwnerEmail">Email</label>
							<input 
								type="email" 
								id="newOwnerEmail" 
								bind:value={newOwnerData.email} 
								placeholder="Enter email"
							/>
						</div>
						<div class="form-field">
							<label for="newOwnerPhone">Phone</label>
							<input 
								type="tel" 
								id="newOwnerPhone" 
								bind:value={newOwnerData.phone} 
								placeholder="Enter phone"
							/>
						</div>
						<div class="form-field full-width">
							<label for="newOwnerAddress">Address</label>
							<input 
								type="text" 
								id="newOwnerAddress" 
								bind:value={newOwnerData.address} 
								placeholder="Enter address"
							/>
						</div>
					</div>
					<div class="form-actions">
						<button type="button" class="btn btn-secondary" onclick={cancelNewOwnerModal}>
							Cancel
						</button>
						<button type="submit" class="btn btn-primary">
							Add Owner
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}

	<div class="houses-grid">
		{#if houses.length === 0}
			<div class="empty-message">No houses found. Add one to get started.</div>
		{:else}
			{#each houses as house}
				<div class="house-card">
					<h3 class="house-name">{house.name}</h3>
					<p class="house-address">{house.address}</p>
					{#if house.owner_id}
						<p class="house-owner">Owner: {getOwnerName(house.owner_id)}</p>
					{/if}
					{#if house.description}
						<p class="house-description">{house.description}</p>
					{/if}
					<div class="house-actions">
						<button class="btn btn-secondary btn-sm" onclick={() => openEditForm(house)}>
							Edit
						</button>
						<button class="btn btn-danger btn-sm" onclick={() => handleDelete(house)}>
							Delete
						</button>
					</div>
				</div>
			{/each}
		{/if}
	</div>
</div>

<style>
	.houses-page {
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

	.form-field input,
	.form-field textarea,
	.form-field select {
		padding: 0.5rem;
		border: 1px solid #d1d5db;
		border-radius: 6px;
		font-size: 0.875rem;
		font-family: inherit;
	}

	.form-field input:focus,
	.form-field textarea:focus,
	.form-field select:focus {
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

	.houses-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
	}

	.house-card {
		background: white;
		border: 1px solid var(--border-color, #e5e7eb);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}

	.house-name {
		margin: 0 0 0.5rem 0;
		font-size: 1.125rem;
		font-weight: 600;
		color: var(--text-color, #111827);
	}

	.house-address {
		margin: 0;
		color: #6b7280;
		font-size: 0.875rem;
	}

	.house-owner {
		margin: 0.25rem 0 0 0;
		color: #3b82f6;
		font-size: 0.875rem;
		font-weight: 500;
	}

	.house-description {
		margin: 0.5rem 0 0 0;
		color: #9ca3af;
		font-size: 0.75rem;
	}

	.house-actions {
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
		border-radius: 8px;
		padding: 1.5rem;
		width: 90%;
		max-width: 500px;
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
	}

	.modal-title {
		margin: 0 0 1.5rem 0;
		font-size: 1.25rem;
		font-weight: 600;
		color: #111827;
	}
</style>
