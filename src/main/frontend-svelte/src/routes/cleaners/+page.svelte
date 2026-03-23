<script>
	import { cleanersAPI } from '../api/kiss-remote.js';
	import { dataStores, loadingActions, errorActions } from '../../lib/stores.svelte.js';
	import Table from '$lib/components/Table.svelte';
	import Form from '$lib/components/Form.svelte';

	// Svelte 5: Use runes for state management
	let cleaners = $state([]);
	let loading = $state(false);
	let error = $state(null);
	let showForm = $state(false);
	let editingCleaner = $state(null);

	// Load data on mount
	$effect(() => {
		loadCleaners();
	});

	const cleanerFields = [
		{
			name: 'name',
			label: 'Name',
			type: 'text',
			required: true,
			placeholder: 'Enter cleaner name'
		},
		{
			name: 'email',
			label: 'Email',
			type: 'email',
			required: true,
			placeholder: 'Enter cleaner email'
		},
		{
			name: 'phone',
			label: 'Phone',
			type: 'tel',
			required: false,
			placeholder: 'Enter cleaner phone number'
		},
		{
			name: 'address',
			label: 'Address',
			type: 'textarea',
			required: false,
			placeholder: 'Enter cleaner address',
			rows: 3
		},
		{
			name: 'is_active',
			label: 'Active',
			type: 'checkbox',
			required: false
		}
	];

	const tableColumns = [
		{ key: 'name', label: 'Name' },
		{ key: 'email', label: 'Email' },
		{ key: 'phone', label: 'Phone' },
		{
			key: 'is_active',
			label: 'Status',
			formatter: (value) => (value ? 'Active' : 'Inactive')
		}
	];

	const tableActions = [
		{
			label: 'Edit',
			class: 'edit',
			title: 'Edit cleaner',
			icon: '✏️'
		},
		{
			label: 'Delete',
			class: 'delete',
			title: 'Delete cleaner',
			icon: '🗑️'
		}
	];

	async function loadCleaners() {
		loading = true;
		error = null;

		try {
			const result = await cleanersAPI.getAll();
			cleaners = result.data || [];
			dataStores.cleaners.set(cleaners);
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function handleAction({ action, row }) {
		if (action.label === 'Edit') {
			editingCleaner = row;
			showForm = true;
		} else if (action.label === 'Delete') {
			if (confirm('Are you sure you want to delete this cleaner?')) {
				try {
					await cleanersAPI.delete(row.id);
					await loadCleaners();
				} catch (err) {
					error = err.message;
				}
			}
		}
	}

	async function handleFormSubmit(data) {
		try {
			if (editingCleaner) {
				await cleanersAPI.update(editingCleaner.id, data);
			} else {
				await cleanersAPI.create(data);
			}

			showForm = false;
			editingCleaner = null;
			await loadCleaners();
		} catch (err) {
			error = err.message;
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingCleaner = null;
	}
</script>

<div class="cleaners-page">
	<div class="page-header">
		<h1>Cleaners</h1>
		<button class="btn btn-primary" onclick={() => (showForm = true)}> Add Cleaner </button>
	</div>

	{#if showForm}
		<div class="form-section">
			<Form
				fields={cleanerFields}
				data={editingCleaner || {}}
				{loading}
				title={editingCleaner ? 'Edit Cleaner' : 'Add New Cleaner'}
				submitLabel={editingCleaner ? 'Update Cleaner' : 'Add Cleaner'}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
			/>
		</div>
	{/if}

	<div class="table-section">
		<Table
			data={cleaners}
			columns={tableColumns}
			actions={tableActions}
			{loading}
			{error}
			onAction={handleAction}
		/>
	</div>
</div>

<style>
	.cleaners-page {
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
