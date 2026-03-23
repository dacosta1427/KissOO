<script>
	import { housesAPI } from '../api/kiss-remote.js';
	import { dataStores } from '../../lib/stores.svelte.js';
	import Table from '$lib/components/Table.svelte';
	import Form from '$lib/components/Form.svelte';

	// Svelte 5: Use $state for reactive variables
	let houses = $state([]);
	let loading = $state(false);
	let error = $state(null);
	let showForm = $state(false);
	let editingHouse = $state(null);

	const houseFields = [
		{
			name: 'name',
			label: 'House Name',
			type: 'text',
			required: true,
			placeholder: 'Enter house name'
		},
		{
			name: 'address',
			label: 'Address',
			type: 'textarea',
			required: true,
			placeholder: 'Enter house address',
			rows: 3
		},
		{
			name: 'capacity',
			label: 'Capacity',
			type: 'number',
			required: true,
			placeholder: 'Enter maximum occupancy'
		},
		{
			name: 'bedrooms',
			label: 'Bedrooms',
			type: 'number',
			required: true,
			placeholder: 'Enter number of bedrooms'
		},
		{
			name: 'bathrooms',
			label: 'Bathrooms',
			type: 'number',
			required: true,
			placeholder: 'Enter number of bathrooms'
		},
		{
			name: 'description',
			label: 'Description',
			type: 'textarea',
			required: false,
			placeholder: 'Enter house description',
			rows: 4
		}
	];

	const tableColumns = [
		{ key: 'name', label: 'Name' },
		{ key: 'address', label: 'Address' },
		{ key: 'capacity', label: 'Capacity' },
		{ key: 'bedrooms', label: 'Bedrooms' },
		{ key: 'bathrooms', label: 'Bathrooms' }
	];

	const tableActions = [
		{
			label: 'Edit',
			class: 'edit',
			title: 'Edit house',
			icon: '✏️'
		},
		{
			label: 'Delete',
			class: 'delete',
			title: 'Delete house',
			icon: '🗑️'
		}
	];

	async function loadHouses() {
		loading = true;
		error = null;

		try {
			const result = await housesAPI.getAll();
			houses = result.data || [];
			dataStores.houses.set(houses);
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function handleAction({ action, row }) {
		if (action.label === 'Edit') {
			editingHouse = row;
			showForm = true;
		} else if (action.label === 'Delete') {
			if (confirm('Are you sure you want to delete this house?')) {
				try {
					await housesAPI.delete(row.id);
					await loadHouses();
				} catch (err) {
					error = err.message;
				}
			}
		}
	}

	async function handleFormSubmit(data) {
		try {
			if (editingHouse) {
				await housesAPI.update(editingHouse.id, data);
			} else {
				await housesAPI.create(data);
			}

			showForm = false;
			editingHouse = null;
			await loadHouses();
		} catch (err) {
			error = err.message;
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingHouse = null;
	}

	// Svelte 5: Use $effect for lifecycle management
	$effect(() => {
		loadHouses();
	});
</script>

<div class="houses-page">
	<div class="page-header">
		<h1>Houses</h1>
		<button class="btn btn-primary" onclick={() => (showForm = true)}> Add House </button>
	</div>

	{#if showForm}
		<div class="form-section">
			<Form
				fields={houseFields}
				data={editingHouse || {}}
				{loading}
				title={editingHouse ? 'Edit House' : 'Add New House'}
				submitLabel={editingHouse ? 'Update House' : 'Add House'}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
			/>
		</div>
	{/if}

	<div class="table-section">
		<Table
			data={houses}
			columns={tableColumns}
			actions={tableActions}
			{loading}
			{error}
			onAction={handleAction}
		/>
	</div>
</div>

<style>
	.houses-page {
		display: flex;
		flex-direction: column;
		gap: 2rem;
	}

	.page-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-bottom: 2px solid var(--border-color);
		padding-bottom: 1rem;
	}

	.page-header h1 {
		margin: 0;
		color: var(--text-color);
	}

	.btn {
		padding: 0.75rem 1.5rem;
		border: none;
		border-radius: 6px;
		font-size: 1rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}

	.btn-primary {
		background: var(--primary-color);
		color: white;
	}

	.btn-primary:hover {
		background: var(--primary-hover);
	}

	.form-section {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		padding: 2rem;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	.table-section {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.form-section {
			padding: 1rem;
		}
	}
</style>
