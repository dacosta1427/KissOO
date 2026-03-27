<script lang="ts">
	import { costProfilesAPI, ownersAPI, type CostProfile, type Owner } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { session } from '$lib/state/session.svelte';
  import { t, currentLocale } from '$lib/i18n';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Check if user is admin
  let isAdmin = $derived(session.username === 'admin' || session.username === 'administrator');

  let costProfiles = $state<CostProfile[]>([]);
	let owners = $state<Owner[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingProfile = $state<CostProfile | null>(null);

	let formData = $state({
		name: '',
		is_standard: false,
		owner: 0,
		base_hourly_rate: 25.0,
		minimum_charge: 75.0,
		rate_per_m2: 0.15,
		rate_per_floor: 15.0,
		rate_per_bedroom: 10.0,
		rate_per_bathroom: 15.0,
		dog_surcharge: 20.0,
		basic_multiplier: 1.0,
		standard_multiplier: 1.0,
		premium_multiplier: 1.25,
		luxury_multiplier: 1.5,
		active: true
	});

	async function loadCostProfiles() {
		loading = true;
		error = null;
		try {
			costProfiles = await costProfilesAPI.getAll();
			owners = await ownersAPI.getAll();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	function openCreateForm() {
		editingProfile = null;
		formData = {
			name: '',
			is_standard: false,
			owner: 0,
			base_hourly_rate: 25.0,
			minimum_charge: 75.0,
			rate_per_m2: 0.15,
			rate_per_floor: 15.0,
			rate_per_bedroom: 10.0,
			rate_per_bathroom: 15.0,
			dog_surcharge: 20.0,
			basic_multiplier: 1.0,
			standard_multiplier: 1.0,
			premium_multiplier: 1.25,
			luxury_multiplier: 1.5,
			active: true
		};
		showForm = true;
	}

	function openEditForm(profile: CostProfile) {
		editingProfile = profile;
		formData = {
			name: profile.name,
			is_standard: profile.is_standard,
			owner: profile.owner || 0,
			base_hourly_rate: profile.base_hourly_rate,
			minimum_charge: profile.minimum_charge,
			rate_per_m2: profile.rate_per_m2,
			rate_per_floor: profile.rate_per_floor,
			rate_per_bedroom: profile.rate_per_bedroom,
			rate_per_bathroom: profile.rate_per_bathroom,
			dog_surcharge: profile.dog_surcharge,
			basic_multiplier: profile.basic_multiplier,
			standard_multiplier: profile.standard_multiplier,
			premium_multiplier: profile.premium_multiplier,
			luxury_multiplier: profile.luxury_multiplier,
			active: profile.active
		};
		showForm = true;
	}

	async function handleSubmit() {
		loading = true;
		try {
			const data = {
				...formData,
				owner: formData.owner || null
			};
			
			if (editingProfile) {
				await costProfilesAPI.update(editingProfile.id, data);
				notificationActions.success('Cost profile updated successfully');
			} else {
				await costProfilesAPI.create(data);
				notificationActions.success('Cost profile created successfully');
			}
			showForm = false;
			await loadCostProfiles();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_save');
			notificationActions.error(error || 'Failed to save cost profile');
		} finally {
			loading = false;
		}
	}

	async function handleDelete(profile: CostProfile) {
		if (!confirm(`Delete cost profile "${profile.name}"?`)) return;
		
		try {
			await costProfilesAPI.delete(profile.id);
			notificationActions.success('Cost profile deleted successfully');
			await loadCostProfiles();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_delete');
			notificationActions.error(error || 'Failed to delete cost profile');
		}
	}

	async function handleCopy(profile: CostProfile) {
		const name = prompt(`Enter name for copied profile:`, `${profile.name} (Copy)`);
		if (!name) return;
		
		try {
			await costProfilesAPI.copy(profile.id, name);
			notificationActions.success('Cost profile copied successfully');
			await loadCostProfiles();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_copy');
			notificationActions.error(error || 'Failed to copy cost profile');
		}
	}

	function getOwnerName(ownerId: number): string {
		if (!ownerId) return 'Global';
		const owner = owners.find(o => o.id === ownerId);
		return owner?.name || 'Unknown';
	}

	$effect(() => {
		loadCostProfiles();
	});
</script>

<div class="cost-profiles-page">
	<div class="page-header">
		<h1>Cost Profiles</h1>
		<button class="btn btn-primary" onclick={openCreateForm} title={tt('hints.add_new')}>
			{tt('cost_profiles.add_new')}
		</button>
	</div>

	{#if error}
		<div class="error-message">{error}</div>
	{/if}

	{#if loading && costProfiles.length === 0}
		<div class="loading">{tt('hints.loading')}</div>
	{:else}
		<div class="profiles-grid">
			{#each costProfiles as profile}
				<div class="profile-card" class:standard={profile.is_standard}>
					<div class="profile-header">
						<h3>{profile.name}</h3>
						{#if profile.is_standard}
							<span class="badge badge-standard">Standard</span>
						{/if}
						{#if !profile.active}
							<span class="badge badge-inactive">Inactive</span>
						{/if}
					</div>
					
					<div class="profile-owner">
						<strong>Owner:</strong> {getOwnerName(profile.owner)}
					</div>
					
					<div class="profile-rates">
						<div class="rate-group">
							<h4>Base Rates</h4>
							<p>Hourly: €{profile.base_hourly_rate.toFixed(2)}</p>
							<p>Minimum: €{profile.minimum_charge.toFixed(2)}</p>
						</div>
						<div class="rate-group">
							<h4>Size Factors</h4>
							<p>Per m²: €{profile.rate_per_m2.toFixed(2)}</p>
							<p>Per floor: €{profile.rate_per_floor.toFixed(2)}</p>
						</div>
						<div class="rate-group">
							<h4>Room Factors</h4>
							<p>Per bedroom: €{profile.rate_per_bedroom.toFixed(2)}</p>
							<p>Per bathroom: €{profile.rate_per_bathroom.toFixed(2)}</p>
						</div>
						<div class="rate-group">
							<h4>Multipliers</h4>
							<p>Premium: ×{profile.premium_multiplier}</p>
							<p>Luxury: ×{profile.luxury_multiplier}</p>
						</div>
					</div>
					
					<div class="profile-surcharge">
						<strong>Dog surcharge:</strong> €{profile.dog_surcharge.toFixed(2)}
					</div>
					
					<div class="profile-actions">
						<button class="btn btn-secondary btn-sm" onclick={() => openEditForm(profile)} title={tt('hints.edit_item')}>
							{tt('common.edit')}
						</button>
						<button class="btn btn-secondary btn-sm" onclick={() => handleCopy(profile)} title="Copy profile">
							Copy
						</button>
						{#if !profile.is_standard}
							<button class="btn btn-danger btn-sm" onclick={() => handleDelete(profile)} title={tt('hints.delete_item')}>
								{tt('common.delete')}
							</button>
						{/if}
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>

<!-- Edit/Create Modal -->
{#if showForm}
	<div class="modal-overlay" role="dialog" aria-modal="true" tabindex="-1" onclick={() => showForm = false} onkeydown={(e) => e.key === 'Escape' && (showForm = false)}>
		<div class="modal-content modal-large" role="document" onclick={(e) => e.stopPropagation()}>
			<h2 class="modal-title">{editingProfile ? 'Edit Cost Profile' : 'Create Cost Profile'}</h2>
			
			<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
				<div class="form-grid">
					<div class="form-field">
						<label for="name">Name *</label>
						<input type="text" id="name" bind:value={formData.name} required placeholder="Profile name" />
					</div>
					
					<div class="form-field">
						<label for="owner">Owner</label>
						<select id="owner" bind:value={formData.owner}>
							<option value={0}>Global (all owners)</option>
							{#each owners as owner}
								<option value={owner.id}>{owner.name}</option>
							{/each}
						</select>
					</div>
					
					<div class="form-field checkbox-field">
						<label>
							<input type="checkbox" bind:checked={formData.is_standard} />
							This is the standard profile
						</label>
					</div>
					
					<div class="form-field checkbox-field">
						<label>
							<input type="checkbox" bind:checked={formData.active} />
							Active
						</label>
					</div>
				</div>
				
				<h3>Base Rates</h3>
				<div class="form-grid">
					<div class="form-field">
						<label for="base_hourly_rate">Base Hourly Rate (€)</label>
						<input type="number" id="base_hourly_rate" bind:value={formData.base_hourly_rate} step="0.01" min="0" />
					</div>
					<div class="form-field">
						<label for="minimum_charge">Minimum Charge (€)</label>
						<input type="number" id="minimum_charge" bind:value={formData.minimum_charge} step="0.01" min="0" />
					</div>
				</div>
				
				<h3>Size Factors</h3>
				<div class="form-grid">
					<div class="form-field">
						<label for="rate_per_m2">Rate per m² (€)</label>
						<input type="number" id="rate_per_m2" bind:value={formData.rate_per_m2} step="0.01" min="0" />
					</div>
					<div class="form-field">
						<label for="rate_per_floor">Rate per Floor (€)</label>
						<input type="number" id="rate_per_floor" bind:value={formData.rate_per_floor} step="0.01" min="0" />
					</div>
				</div>
				
				<h3>Room Factors</h3>
				<div class="form-grid">
					<div class="form-field">
						<label for="rate_per_bedroom">Rate per Bedroom (€)</label>
						<input type="number" id="rate_per_bedroom" bind:value={formData.rate_per_bedroom} step="0.01" min="0" />
					</div>
					<div class="form-field">
						<label for="rate_per_bathroom">Rate per Bathroom (€)</label>
						<input type="number" id="rate_per_bathroom" bind:value={formData.rate_per_bathroom} step="0.01" min="0" />
					</div>
				</div>
				
				<h3>Surcharge</h3>
				<div class="form-grid">
					<div class="form-field">
						<label for="dog_surcharge">Dog Surcharge (€)</label>
						<input type="number" id="dog_surcharge" bind:value={formData.dog_surcharge} step="0.01" min="0" />
					</div>
				</div>
				
				<h3>Luxury Multipliers</h3>
				<div class="form-grid">
					<div class="form-field">
						<label for="basic_multiplier">Basic Multiplier</label>
						<input type="number" id="basic_multiplier" bind:value={formData.basic_multiplier} step="0.05" min="0" />
					</div>
					<div class="form-field">
						<label for="standard_multiplier">Standard Multiplier</label>
						<input type="number" id="standard_multiplier" bind:value={formData.standard_multiplier} step="0.05" min="0" />
					</div>
					<div class="form-field">
						<label for="premium_multiplier">Premium Multiplier</label>
						<input type="number" id="premium_multiplier" bind:value={formData.premium_multiplier} step="0.05" min="0" />
					</div>
					<div class="form-field">
						<label for="luxury_multiplier">Luxury Multiplier</label>
						<input type="number" id="luxury_multiplier" bind:value={formData.luxury_multiplier} step="0.05" min="0" />
					</div>
				</div>
				
				<div class="modal-actions">
					<button type="button" class="btn btn-secondary" onclick={() => showForm = false}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary" disabled={loading}>
						{loading ? tt('hints.saving') : tt('common.save')}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<style>
	.cost-profiles-page {
		padding: 2rem;
		max-width: 1400px;
		margin: 0 auto;
	}
	
	.page-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
	}
	
	.page-header h1 {
		margin: 0;
		font-size: 1.75rem;
		color: #111827;
	}
	
	.profiles-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
		gap: 1.5rem;
	}
	
	.profile-card {
		background: white;
		border-radius: 8px;
		padding: 1.5rem;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
		border: 2px solid transparent;
	}
	
	.profile-card.standard {
		border-color: #3b82f6;
	}
	
	.profile-header {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-bottom: 1rem;
	}
	
	.profile-header h3 {
		margin: 0;
		flex: 1;
	}
	
	.badge {
		padding: 0.25rem 0.5rem;
		border-radius: 4px;
		font-size: 0.75rem;
		font-weight: 600;
	}
	
	.badge-standard {
		background: #dbeafe;
		color: #1d4ed8;
	}
	
	.badge-inactive {
		background: #fee2e2;
		color: #991b1b;
	}
	
	.profile-owner {
		margin-bottom: 1rem;
		color: #6b7280;
	}
	
	.profile-rates {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 0.75rem;
		margin-bottom: 1rem;
	}
	
	.rate-group {
		font-size: 0.875rem;
	}
	
	.rate-group h4 {
		margin: 0 0 0.25rem 0;
		font-size: 0.75rem;
		color: #6b7280;
		text-transform: uppercase;
	}
	
	.rate-group p {
		margin: 0;
	}
	
	.profile-surcharge {
		padding: 0.5rem;
		background: #fef3c7;
		border-radius: 4px;
		margin-bottom: 1rem;
		font-size: 0.875rem;
	}
	
	.profile-actions {
		display: flex;
		gap: 0.5rem;
		padding-top: 1rem;
		border-top: 1px solid #e5e7eb;
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
		max-height: 90vh;
		overflow-y: auto;
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
	}
	
	.modal-large {
		max-width: 700px;
	}
	
	.modal-title {
		margin: 0 0 1.5rem 0;
		font-size: 1.25rem;
		font-weight: 600;
	}
	
	.form-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 1rem;
		margin-bottom: 1rem;
	}
	
	.form-field {
		display: flex;
		flex-direction: column;
	}
	
	.form-field label {
		font-size: 0.875rem;
		font-weight: 500;
		margin-bottom: 0.25rem;
	}
	
	.form-field input,
	.form-field select {
		padding: 0.5rem;
		border: 1px solid #d1d5db;
		border-radius: 4px;
		font-size: 0.875rem;
	}
	
	.form-field input:focus,
	.form-field select:focus {
		outline: none;
		border-color: #3b82f6;
		box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
	}
	
	.checkbox-field {
		flex-direction: row;
		align-items: center;
	}
	
	.checkbox-field label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin: 0;
	}
	
	.checkbox-field input[type="checkbox"] {
		width: 1rem;
		height: 1rem;
	}
	
	.form-grid h3 {
		grid-column: 1 / -1;
		margin: 1rem 0 0.5rem 0;
		font-size: 0.875rem;
		color: #374151;
		border-bottom: 1px solid #e5e7eb;
		padding-bottom: 0.25rem;
	}
	
	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		margin-top: 1.5rem;
		padding-top: 1rem;
		border-top: 1px solid #e5e7eb;
	}
	
	.loading {
		text-align: center;
		padding: 2rem;
		color: #6b7280;
	}
	
	.error-message {
		background: #fee2e2;
		color: #991b1b;
		padding: 1rem;
		border-radius: 4px;
		margin-bottom: 1rem;
	}
</style>
