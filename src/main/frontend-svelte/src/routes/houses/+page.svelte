<script lang="ts">
	import { housesAPI, type House } from '$lib/api/Cleaning';
	import { Utils } from '$lib/utils/Utils';
	import { notificationActions } from '$lib/stores.svelte.js';

	// Svelte 5: Use $state for reactive variables
	let houses = $state<House[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingHouse = $state<House | null>(null);

	const houseFields = [
		{
			name: 'name',
			label: 'House Name',
			type: 'text' as const,
			required: true,
			placeholder: 'Enter house name'
		},
		{
			name: 'address',
			label: 'Address',
			type: 'text' as const,
			required: true,
			placeholder: 'Enter house address'
		},
		{
			name: 'description',
			label: 'Description',
			type: 'text' as const,
			required: false,
			placeholder: 'Enter house description'
		}
	];

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

	async function handleDelete(house: House) {
		await Utils.yesNo(
			'Confirm Delete',
			`Are you sure you want to delete "${house.name}"?`,
			async () => {
				try {
					await housesAPI.delete(house.id);
					notificationActions.success('House deleted successfully');
					await loadHouses();
				} catch (err: any) {
					notificationActions.error(err.message || 'Failed to delete house');
				}
			}
		);
	}

	async function handleFormSubmit(data: Record<string, any>) {
		try {
			if (editingHouse) {
				await housesAPI.update(editingHouse.id, data);
				notificationActions.success('House updated successfully');
			} else {
				await housesAPI.create(data);
				notificationActions.success('House created successfully');
			}

			showForm = false;
			editingHouse = null;
			await loadHouses();
		} catch (err: any) {
			notificationActions.error(err.message || 'Failed to save house');
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingHouse = null;
	}

	function openAddForm() {
		editingHouse = null;
		showForm = true;
	}

	function openEditForm(house: House) {
		editingHouse = house;
		showForm = true;
	}

	// Load data on mount
	$effect(() => {
		loadHouses();
	});
</script>

<div class="houses-page">
	<div class="page-header">
		<h1>Houses</h1>
		<button class="btn btn-primary" onclick={openAddForm}> Add House </button>
	</div>

	{#if loading}
		<div class="fixed top-4 right-4 z-50 bg-blue-600 text-white px-4 py-2 rounded shadow-lg">
			<span class="inline-block animate-spin mr-2">⏳</span>
			Loading...
		</div>
	{/if}

	{#if error}
		<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
			{error}
		</div>
	{/if}

	{#if showForm}
		<div class="form-section">
			<h3 class="text-lg font-medium mb-4">{editingHouse ? 'Edit House' : 'Add New House'}</h3>
			{#if houseFields.length === 0}
				<div class="text-gray-500">No fields defined</div>
			{:else}
				<form onsubmit={(e) => { e.preventDefault(); handleFormSubmit({}); }}>
					{#each houseFields as field}
						<div class="form-group">
							<label for={field.name} class="block text-sm font-medium text-gray-700">
								{field.label}
								{#if field.required}<span class="text-red-500">*</span>{/if}
							</label>
							{#if field.type === 'textarea'}
								<textarea
									id={field.name}
									name={field.name}
									placeholder={field.placeholder}
									required={field.required}
									class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
									rows="3"
								></textarea>
							{:else}
								<input
									type={field.type}
									id={field.name}
									name={field.name}
									placeholder={field.placeholder}
									required={field.required}
									class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
								/>
							{/if}
						</div>
					{/each}
					<div class="flex justify-end gap-3 mt-4">
						<button type="button" onclick={handleFormCancel} class="btn btn-secondary">
							Cancel
						</button>
						<button type="submit" class="btn btn-primary">
							{editingHouse ? 'Update' : 'Add'} House
						</button>
					</div>
				</form>
			{/if}
		</div>
	{/if}

	<div class="houses-grid">
		{#if houses.length === 0}
			<div class="text-gray-500 text-center py-8">No houses found. Add one to get started.</div>
		{:else}
			{#each houses as house}
				<div class="house-card">
					<h3 class="font-semibold text-lg">{house.name}</h3>
					<p class="text-gray-600 text-sm">{house.address}</p>
					{#if house.description}
						<p class="text-gray-500 text-sm mt-2">{house.description}</p>
					{/if}
					<div class="house-actions">
						<button class="btn btn-sm btn-secondary" onclick={() => openEditForm(house)}>
							Edit
						</button>
						<button class="btn btn-sm btn-danger" onclick={() => handleDelete(house)}>
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
		border-bottom: 2px solid var(--border-color);
		padding-bottom: 1rem;
	}

	.page-header h1 {
		margin: 0;
		color: var(--text-color);
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
		background: var(--primary-color, #3b82f6);
		color: white;
	}

	.btn-primary:hover {
		background: var(--primary-hover, #2563eb);
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

	.form-section {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		padding: 1.5rem;
		margin-bottom: 2rem;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	.form-group {
		margin-bottom: 1rem;
	}

	.houses-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
	}

	.house-card {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}

	.house-card h3 {
		margin: 0 0 0.5rem 0;
		color: var(--text-color);
	}

	.house-actions {
		display: flex;
		gap: 0.5rem;
		margin-top: 1rem;
	}

	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}
	}
</style>
