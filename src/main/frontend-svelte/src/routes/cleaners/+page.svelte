<script lang="ts">
	import { cleanersAPI, type Cleaner } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import Table from '$lib/components/Table.svelte';
	import Form from '$lib/components/Form.svelte';
  import { t, currentLocale } from '$lib/i18n';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Svelte 5: Use runes for state management
	let cleaners = $state<Cleaner[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingCleaner = $state<Cleaner | null>(null);

	// Load data on mount
	$effect(() => {
		loadCleaners();
	});

	// Reactive form fields using $derived
	let cleanerFields = $derived([
		{
			name: 'name',
			label: t('cleaners.name'),
			type: 'text' as const,
			required: true,
			placeholder: t('cleaners.enter_cleaner_name')
		},
		{
			name: 'email',
			label: t('cleaners.email'),
			type: 'email' as const,
			required: true,
			placeholder: t('cleaners.enter_cleaner_email')
		},
		{
			name: 'phone',
			label: t('cleaners.phone'),
			type: 'tel' as const,
			required: false,
			placeholder: t('cleaners.enter_cleaner_phone')
		},
		{
			name: 'address',
			label: t('cleaners.address'),
			type: 'textarea' as const,
			required: false,
			placeholder: t('cleaners.enter_cleaner_address'),
			rows: 3
		},
		{
			name: 'active',
			label: t('common.active'),
			type: 'checkbox' as const,
			required: false
		}
	]);

	let tableColumns = $derived([
		{ key: 'name', label: t('cleaners.name') },
		{ key: 'email', label: t('cleaners.email') },
		{ key: 'phone', label: t('cleaners.phone') },
		{ key: 'address', label: t('cleaners.address') },
		{
			key: 'active',
			label: t('common.status'),
			formatter: (value: boolean) => (value ? t('common.active') : t('common.inactive'))
		}
	]);

	let tableActions = $derived([
		{
			label: t('common.edit'),
			class: 'edit',
			title: t('cleaners.edit_cleaner'),
			icon: '✏️'
		},
		{
			label: t('common.delete'),
			class: 'delete',
			title: t('cleaners.delete_cleaner'),
			icon: '🗑️'
		}
	]);

	async function loadCleaners() {
		loading = true;
		error = null;

		try {
			cleaners = await cleanersAPI.getAll();
			dataStores.cleaners.set(cleaners);
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	async function handleAction({ action, row }: { action: any; row: any }) {
		if (action.label === t('common.edit')) {
			editingCleaner = row;
			showForm = true;
		} else if (action.label === t('common.delete')) {
			if (confirm(t('cleaners.delete_confirm'))) {
				try {
					await cleanersAPI.delete(row.id);
					await loadCleaners();
				} catch (err: any) {
					error = err.message || t('errors.failed_to_delete');
				}
			}
		}
	}

	async function handleFormSubmit(data: any) {
		try {
			if (editingCleaner) {
				await cleanersAPI.update(editingCleaner.id, data);
			} else {
				await cleanersAPI.create(data);
			}

			showForm = false;
			editingCleaner = null;
			await loadCleaners();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_save');
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingCleaner = null;
	}
</script>

<div class="cleaners-page">
	<div class="page-header">
		<h1>{tt('cleaners.title')}</h1>
		<button class="btn btn-primary" onclick={() => (showForm = true)}> {tt('cleaners.add_cleaner')} </button>
	</div>

	{#if showForm}
		<div class="form-section">
			<Form
				fields={cleanerFields}
				data={editingCleaner || {}}
				{loading}
				title={editingCleaner ? t('cleaners.edit_cleaner') : t('cleaners.add_new_cleaner')}
				submitLabel={editingCleaner ? t('cleaners.edit_cleaner') : t('cleaners.add_cleaner')}
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
