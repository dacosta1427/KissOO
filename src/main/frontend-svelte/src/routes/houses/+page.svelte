<script lang="ts">
	import { housesAPI, ownersAPI, costProfilesAPI, type House, type Owner, type CostProfile } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { session } from '$lib/state/session.svelte';
  import { t, currentLocale } from '$lib/i18n';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Check if user is admin
  let isAdmin = $derived(session.isAdmin === true);
  // Check if user is owner (not admin)
  let isOwner = $derived(!isAdmin && session.ownerOid > 0);

  let houses = $state<House[]>([]);
	let owners = $state<Owner[]>([]);
	let costProfiles = $state<CostProfile[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingHouse = $state<House | null>(null);
	let showNewOwnerModal = $state(false);
	
	// View toggle: 'card' or 'table'
	let viewMode = $state<'card' | 'table'>('card');

	// Form section ref for scroll on small screens
	let formSection = $state<HTMLElement | null>(null);

	// Load houses - backend now filters by actor automatically
	async function loadHouses() {
		loading = true;
		error = null;

		try {
			// Backend filters based on session actor (admin sees all, owner sees theirs)
			houses = await housesAPI.getAll();
			// Load cost profiles for dropdown
			costProfiles = await costProfilesAPI.getAll();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	let formData = $state({
		name: '',
		address: '',
		description: '',
		owner: 0 as number,
		cost_profile: 0 as number,
		check_in_time: '16:00',
		check_out_time: '10:00',
		surface_m2: null as number | null,
		floors: 1,
		bedrooms: 0,
		bathrooms: 0,
		luxury_level: 'standard' as 'basic' | 'standard' | 'premium' | 'luxury'
	});

	let newOwnerData = $state({
		name: '',
		email: '',
		phone: '',
		address: ''
	});

	// Filtered houses - backend already filters, but keep for consistency
	let filteredHouses = $derived(houses);

	async function loadOwners() {
		try {
			owners = await ownersAPI.getAll();
		} catch (err: any) {
			console.error('Failed to load owners:', err);
		}
	}
	
	async function toggleHouseActive(id: number, active: boolean) {
		const idx = houses.findIndex(h => h.id === id);
		if (idx >= 0) {
			houses[idx] = { ...houses[idx], active };
		}
		try {
			const updated = await housesAPI.toggleActive(id, active);
			// Update with server response (authoritative state)
			if (updated && updated.id) {
				const updateIdx = houses.findIndex(h => h.id === updated.id);
				if (updateIdx >= 0) {
					houses[updateIdx] = { ...houses[updateIdx], active: updated.active };
				}
			}
		} catch (err: any) {
			if (idx >= 0) {
				houses[idx] = { ...houses[idx], active: !active };
			}
			notificationActions.error(err.message || 'Failed to toggle house status');
		}
	}

	function openAddForm() {
		editingHouse = null;
		formData = { 
			name: '', 
			address: '', 
			description: '', 
			owner: 0, 
			cost_profile: 0,
			check_in_time: '16:00', 
			check_out_time: '10:00',
			surface_m2: null,
			floors: 1,
			bedrooms: 0,
			bathrooms: 0,
			luxury_level: 'standard'
		};
		showForm = true;
	}

	function openEditForm(house: House) {
		editingHouse = house;
		formData = { 
			name: house.name, 
			address: house.address, 
			description: house.description || '',
			owner: house.owner || 0,
			cost_profile: house.cost_profile || 0,
			check_in_time: house.check_in_time || '16:00',
			check_out_time: house.check_out_time || '10:00',
			surface_m2: house.surface_m2 || null,
			floors: house.floors || 1,
			bedrooms: house.bedrooms || 0,
			bathrooms: house.bathrooms || 0,
			luxury_level: house.luxury_level || 'standard'
		};
		showForm = true;
		scrollToEditForm();
	}

	function handleFormCancel() {
		showForm = false;
		editingHouse = null;
		formData = { name: '', address: '', description: '', owner: 0, cost_profile: 0, check_in_time: '16:00', check_out_time: '10:00', surface_m2: null, floors: 1, bedrooms: 0, bathrooms: 0, luxury_level: 'standard' };
	}

	function handleOwnerChange() {
		if (formData.owner === -1) {
			showNewOwnerModal = true;
			formData.owner = 0;
		}
	}

	async function handleAddNewOwner() {
		if (!newOwnerData.name.trim()) {
			notificationActions.error(t('houses.owner_name') + ' ' + t('errors.required'));
			return;
		}
		
		try {
			const newOwner = await ownersAPI.create(newOwnerData);
			notificationActions.success(t('owners.name') + ' ' + t('notifications.created_successfully'));
			await loadOwners();
			formData.owner = newOwner.id;
			showNewOwnerModal = false;
			newOwnerData = { name: '', email: '', phone: '', address: '' };
		} catch (err: any) {
			notificationActions.error(err.message || t('errors.failed_to_save'));
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
			owner: formData.owner || 0,
			cost_profile: formData.cost_profile || 0,
			check_in_time: formData.check_in_time,
			check_out_time: formData.check_out_time,
			surface_m2: formData.surface_m2,
			floors: formData.floors,
			bedrooms: formData.bedrooms,
			bathrooms: formData.bathrooms,
			luxury_level: formData.luxury_level
		};
		
		try {
			if (editingHouse) {
				await housesAPI.update(editingHouse.id, dataToSend);
				notificationActions.success(t('houses.title') + ' ' + t('notifications.updated_successfully'));
			} else {
				await housesAPI.create(dataToSend);
				notificationActions.success(t('houses.title') + ' ' + t('notifications.created_successfully'));
			}

			showForm = false;
			editingHouse = null;
			formData = { name: '', address: '', description: '', owner: 0, cost_profile: 0, check_in_time: '16:00', check_out_time: '10:00', surface_m2: null, floors: 1, bedrooms: 0, bathrooms: 0, luxury_level: 'standard' };
			await loadHouses();
		} catch (err: any) {
			notificationActions.error(err.message || t('errors.failed_to_save'));
		}
	}

	async function handleDelete(house: House) {
		if (confirm(t('houses.delete_confirm').replace('"${house.name}"', `"${house.name}"`))) {
			try {
				await housesAPI.delete(house.id);
				notificationActions.success(t('houses.title') + ' ' + t('notifications.deleted_successfully'));
				await loadHouses();
			} catch (err: any) {
				notificationActions.error(err.message || t('errors.failed_to_delete'));
			}
		}
	}

	function getOwnerName(ownerId: number): string {
		if (!ownerId || ownerId === 0) return t('houses.no_owner');
		const owner = owners.find(o => o.id === ownerId);
		return owner ? owner.name : t('houses.unknown');
	}

	$effect(() => {
		loadHouses();
		loadOwners();
	});
</script>

<div class="houses-page">
	<div class="page-header">
		<h1>{tt('houses.title')}</h1>
		<button class="btn btn-primary" onclick={openAddForm}>{tt('houses.add_house')}</button>
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
		<div class="form-section" bind:this={formSection}>
			<h3 class="form-title">{editingHouse ? t('houses.edit_house') : t('houses.add_new_house')}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<!-- Name -->
					<div class="form-field">
						<label for="name">{tt('houses.name')} <span class="required">*</span></label>
						<input 
							type="text" 
							id="name" 
							bind:value={formData.name} 
							placeholder={tt('houses.enter_house_name')}
							required 
						/>
					</div>

					<!-- Address -->
					<div class="form-field full-width">
						<label for="address">{tt('houses.address')} <span class="required">*</span></label>
						<input 
							type="text" 
							id="address" 
							bind:value={formData.address} 
							placeholder={tt('houses.enter_house_address')}
							required 
						/>
					</div>

					<!-- Owner -->
					<div class="form-field">
						<label for="owner">{tt('houses.owner')}</label>
						<select id="owner" bind:value={formData.owner} onchange={handleOwnerChange}>
							<option value={0}>-- {tt('houses.no_owner')} --</option>
							{#each owners as owner}
								<option value={owner.id}>{owner.name}</option>
							{/each}
							<option value={-1}>+ {tt('houses.new_owner')}</option>
						</select>
					</div>

					<!-- Cost Profile -->
					<div class="form-field">
						<label for="cost_profile">Cost Profile</label>
						<select id="cost_profile" bind:value={formData.cost_profile}>
							<option value={0}>-- Use Standard --</option>
							{#each costProfiles as profile}
								<option value={profile.id}>{profile.name} {profile.is_standard ? '(Standard)' : ''}</option>
							{/each}
						</select>
					</div>

					<!-- Description -->
					<div class="form-field full-width">
						<label for="description">{tt('common.description')}</label>
						<textarea 
							id="description" 
							bind:value={formData.description} 
							placeholder={tt('houses.enter_house_description')}
							rows="3"
						></textarea>
					</div>

					<!-- Check-in Time -->
					<div class="form-field">
						<label for="check_in_time">{tt('houses.check_in_time')} (24h) <span class="required">*</span></label>
						<input 
							type="time" 
							id="check_in_time" 
							bind:value={formData.check_in_time} 
							step="900"
							required 
						/>
					</div>

					<!-- Check-out Time -->
					<div class="form-field">
						<label for="check_out_time">{tt('houses.check_out_time')} (24h) <span class="required">*</span></label>
						<input 
							type="time" 
							id="check_out_time" 
							bind:value={formData.check_out_time} 
							step="900"
							required 
						/>
					</div>
				</div>

				<!-- Cost Calculation Section -->
				<h3 class="section-title">Cost Calculation</h3>
				<div class="form-grid">
					<div class="form-field">
						<label for="surface_m2">Surface (m²)</label>
						<input 
							type="number" 
							id="surface_m2" 
							bind:value={formData.surface_m2} 
							min="0"
							step="1"
							placeholder="e.g., 120"
						/>
					</div>

					<div class="form-field">
						<label for="floors">Floors</label>
						<input 
							type="number" 
							id="floors" 
							bind:value={formData.floors} 
							min="1"
							step="1"
						/>
					</div>

					<div class="form-field">
						<label for="bedrooms">Bedrooms</label>
						<input 
							type="number" 
							id="bedrooms" 
							bind:value={formData.bedrooms} 
							min="0"
							step="1"
						/>
					</div>

					<div class="form-field">
						<label for="bathrooms">Bathrooms</label>
						<input 
							type="number" 
							id="bathrooms" 
							bind:value={formData.bathrooms} 
							min="0"
							step="1"
						/>
					</div>

					<div class="form-field">
						<label for="luxury_level">Luxury Level</label>
						<select id="luxury_level" bind:value={formData.luxury_level}>
							<option value="basic">Basic</option>
							<option value="standard">Standard</option>
							<option value="premium">Premium</option>
							<option value="luxury">Luxury</option>
						</select>
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{editingHouse ? t('common.update') : t('common.add')} {tt('houses.title')}
					</button>
				</div>
			</form>
		</div>
	{/if}

	{#if showNewOwnerModal}
		<div class="modal-overlay" role="dialog" aria-modal="true" tabindex="-1" onclick={cancelNewOwnerModal} onkeydown={(e) => e.key === 'Escape' && cancelNewOwnerModal()}>
			<div class="modal-content" role="document">
				<h3 class="modal-title">{tt('houses.add_new_owner')}</h3>
				<form onsubmit={(e) => { e.preventDefault(); handleAddNewOwner(); }}>
					<div class="form-grid">
						<div class="form-field">
							<label for="newOwnerName">{tt('common.name')} <span class="required">*</span></label>
							<input 
								type="text" 
								id="newOwnerName" 
								bind:value={newOwnerData.name} 
								placeholder={tt('houses.enter_owner_name')}
								required 
							/>
						</div>
						<div class="form-field">
							<label for="newOwnerEmail">{tt('common.email')}</label>
							<input 
								type="email" 
								id="newOwnerEmail" 
								bind:value={newOwnerData.email} 
								placeholder={tt('owners.enter_email_address')}
							/>
						</div>
						<div class="form-field">
							<label for="newOwnerPhone">{tt('common.phone')}</label>
							<input 
								type="tel" 
								id="newOwnerPhone" 
								bind:value={newOwnerData.phone} 
								placeholder={tt('owners.enter_phone_number')}
							/>
						</div>
						<div class="form-field full-width">
							<label for="newOwnerAddress">{tt('common.address')}</label>
							<input 
								type="text" 
								id="newOwnerAddress" 
								bind:value={newOwnerData.address} 
								placeholder={tt('owners.enter_address')}
							/>
						</div>
					</div>
					<div class="form-actions">
						<button type="button" class="btn btn-secondary" onclick={cancelNewOwnerModal}>
							{tt('common.cancel')}
						</button>
						<button type="submit" class="btn btn-primary">
							{tt('owners.add_owner')}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}

	<!-- View Toggle -->
	<div class="view-toggle">
		<button 
			class="toggle-btn" 
			class:active={viewMode === 'card'}
			onclick={() => viewMode = 'card'}
			title={tt('hints.view_details')}
		>
			{tt('houses.card_view')}
		</button>
		<button 
			class="toggle-btn" 
			class:active={viewMode === 'table'}
			onclick={() => viewMode = 'table'}
			title={tt('hints.view_details')}
		>
			{tt('houses.table_view')}
		</button>
	</div>

	{#if viewMode === 'card'}
		<!-- Card View -->
		<div class="houses-grid">
			{#if filteredHouses.length === 0}
				<div class="empty-message">{isAdmin ? tt('houses.no_houses') : tt('houses.no_houses_owner')}</div>
			{:else}
				{#each filteredHouses as house}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div class="house-card clickable" onclick={() => openEditForm(house)} onkeydown={(e) => e.key === 'Enter' && openEditForm(house)}>
						<div class="card-header">
							<h3 class="house-name">{house.name}</h3>
							<button
								type="button"
								class="card-toggle"
								class:active={house.active}
								onclick={(e) => { e.stopPropagation(); toggleHouseActive(house.id, !house.active); }}
								title={house.active ? 'Deactivate house' : 'Activate house'}
								aria-label={house.active ? 'Deactivate house' : 'Activate house'}
							></button>
						</div>
						<p class="house-address">{house.address}</p>
						{#if house.owner}
							<p class="house-owner">{tt('houses.owner')}: {getOwnerName(house.owner)}</p>
						{/if}
						{#if house.description}
							<p class="house-description">{house.description}</p>
						{/if}
						<div class="house-actions">
							<button class="btn btn-secondary btn-sm" onclick={(e) => { e.stopPropagation(); openEditForm(house); }} title={tt('hints.edit_item')}>
								{tt('common.edit')}
							</button>
							<button class="btn btn-danger btn-sm" onclick={(e) => { e.stopPropagation(); handleDelete(house); }} title={tt('hints.delete_item')}>
								{tt('common.delete')}
							</button>
						</div>
					</div>
				{/each}
			{/if}
		</div>
	{:else}
		<!-- Table View -->
		<div class="table-view">
			<table class="data-table">
				<thead>
					<tr>
						<th>{tt('houses.name')}</th>
						<th>{tt('houses.address')}</th>
						<th>{tt('houses.owner')}</th>
						<th>{tt('houses.check_in_time')}</th>
						<th>{tt('houses.check_out_time')}</th>
						<th>{tt('common.actions')}</th>
					</tr>
				</thead>
				<tbody>
				{#each filteredHouses as house}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<tr class="clickable" onclick={() => openEditForm(house)} onkeydown={(e) => e.key === 'Enter' && openEditForm(house)}>
						<td>{house.name}</td>
						<td>{house.address}</td>
						<td>{getOwnerName(house.owner)}</td>
						<td>{house.check_in_time || '-'}</td>
						<td>{house.check_out_time || '-'}</td>
						<td>
							<button class="btn btn-sm btn-secondary" onclick={(e) => { e.stopPropagation(); openEditForm(house); }} title={tt('hints.edit_item')}>
								{tt('common.edit')}
							</button>
							<button class="btn btn-sm btn-danger" onclick={(e) => { e.stopPropagation(); handleDelete(house); }} title={tt('hints.delete_item')}>
								{tt('common.delete')}
							</button>
						</td>
					</tr>
				{:else}
					<tr>
						<td colspan="6" class="empty-row">{isAdmin ? tt('houses.no_houses') : tt('houses.no_houses_owner')}</td>
					</tr>
				{/each}
				</tbody>
			</table>
		</div>
	{/if}
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

	.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }

	.house-name {
		margin: 0;
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

	/* View Toggle */
	.view-toggle {
		display: flex;
		background: #e5e7eb;
		border-radius: 6px;
		padding: 2px;
		margin-bottom: 1.5rem;
		width: fit-content;
	}

	.toggle-btn {
		padding: 0.375rem 0.75rem;
		font-size: 0.875rem;
		font-weight: 500;
		border: none;
		background: transparent;
		color: #6b7280;
		cursor: pointer;
		border-radius: 4px;
		transition: all 0.2s;
	}

	.toggle-btn.active {
		background: white;
		color: #111827;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
	}

	/* Table View */
	.table-view {
		background: white;
		border-radius: 8px;
		overflow: hidden;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}

	.data-table {
		width: 100%;
		border-collapse: collapse;
	}

	.data-table th,
	.data-table td {
		padding: 0.75rem 1rem;
		text-align: left;
		border-bottom: 1px solid #e5e7eb;
	}

	.data-table th {
		background: #f9fafb;
		font-weight: 600;
		font-size: 0.875rem;
		color: #374151;
	}

	.data-table tbody tr:hover {
		background: #f9fafb;
	}

	.empty-row {
		text-align: center;
		color: #6b7280;
		padding: 2rem;
	}
	
	.section-title {
		margin: 1.5rem 0 1rem 0;
		font-size: 1rem;
		font-weight: 600;
		color: #374151;
		border-bottom: 1px solid #e5e7eb;
		padding-bottom: 0.5rem;
	}

	.clickable { cursor: pointer; }
	.clickable:hover { border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
	.data-table tr.clickable:hover { background: #eff6ff; }
	
	/* Mini toggle switch for active/inactive */
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
