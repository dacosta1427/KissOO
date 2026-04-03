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

	// View toggle: 'card' or 'table'
	let viewMode = $state<'card' | 'table'>('card');

	// Form section ref for scroll on small screens
	let formSection = $state<HTMLElement | null>(null);

	function scrollToEditForm() {
		// Scroll to edit form on small screens (mobile/tablet)
		if (window.innerWidth < 1024 && formSection) {
			formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}

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
	
	async function toggleCleanerActive(id: number, active: boolean) {
		const idx = cleaners.findIndex(c => c.id === id);
		if (idx >= 0) {
			cleaners[idx] = { ...cleaners[idx], active };
		}
		try {
			await cleanersAPI.toggleActive(id, active);
		} catch (err: any) {
			if (idx >= 0) {
				cleaners[idx] = { ...cleaners[idx], active: !active };
			}
			notificationActions.error(err.message || 'Failed to toggle cleaner status');
		}
	}

	async function handleAction({ action, row }: { action: any; row: any }) {
		if (action.label === t('common.edit')) {
			editingCleaner = row;
			showForm = true;
			scrollToEditForm();
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
		<div class="form-section" bind:this={formSection}>
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

	<!-- View Toggle -->
	<div class="view-toggle">
		<button
			class="toggle-btn"
			class:active={viewMode === 'card'}
			onclick={() => viewMode = 'card'}
		>
			{tt('houses.card_view')}
		</button>
		<button
			class="toggle-btn"
			class:active={viewMode === 'table'}
			onclick={() => viewMode = 'table'}
		>
			{tt('houses.table_view')}
		</button>
	</div>

	{#if viewMode === 'card'}
		<!-- Card View -->
		<div class="cleaners-grid">
			{#if cleaners.length === 0 && !loading}
				<div class="empty-message">{tt('cleaners.no_cleaners')}</div>
			{:else}
				{#each cleaners as cleaner}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div class="cleaner-card clickable" onclick={() => { editingCleaner = cleaner; showForm = true; scrollToEditForm(); }} onkeydown={(e) => e.key === 'Enter' && (editingCleaner = cleaner, showForm = true, scrollToEditForm())}>
						<div class="card-header">
							<h3 class="cleaner-name">{cleaner.name}</h3>
							<button
								type="button"
								class="card-toggle"
								class:active={cleaner.active}
								onclick={(e) => { e.stopPropagation(); toggleCleanerActive(cleaner.id, !cleaner.active); }}
								title={cleaner.active ? 'Deactivate cleaner' : 'Activate cleaner'}
								aria-label={cleaner.active ? 'Deactivate cleaner' : 'Activate cleaner'}
							></button>
						</div>
						{#if cleaner.email}<p class="cleaner-detail">{cleaner.email}</p>{/if}
						{#if cleaner.phone}<p class="cleaner-detail">{cleaner.phone}</p>{/if}
						{#if cleaner.address}<p class="cleaner-detail">{cleaner.address}</p>{/if}
						<div class="cleaner-actions">
							<button class="btn btn-secondary btn-sm" onclick={(e) => { e.stopPropagation(); editingCleaner = cleaner; showForm = true; scrollToEditForm(); }}>
								{tt('common.edit')}
							</button>
							<button class="btn btn-danger btn-sm" onclick={(e) => { e.stopPropagation(); if (confirm(t('cleaners.delete_confirm'))) { cleanersAPI.delete(cleaner.id).then(() => loadCleaners()); } }}>
								{tt('common.delete')}
							</button>
						</div>
					</div>
				{/each}
			{/if}
		</div>
	{:else}
		<!-- Table View -->
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
	{/if}
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

	.btn-secondary {
		background: #6b7280;
		color: white;
	}

	.btn-danger {
		background: #ef4444;
		color: white;
	}

	.btn-sm {
		padding: 0.25rem 0.75rem;
		font-size: 0.75rem;
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

	/* View Toggle */
	.view-toggle {
		display: flex;
		gap: 0.5rem;
	}
	.toggle-btn {
		padding: 0.5rem 1rem;
		border: 1px solid var(--border-color);
		background: white;
		cursor: pointer;
		font-size: 0.875rem;
	}
	.toggle-btn:first-child { border-radius: 6px 0 0 6px; }
	.toggle-btn:last-child { border-radius: 0 6px 6px 0; }
	.toggle-btn.active { background: var(--primary-color); color: white; border-color: var(--primary-color); }

	/* Card View */
	.cleaners-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
	}
	.cleaner-card {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}
	.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
	.cleaner-name { margin: 0; font-size: 1.125rem; font-weight: 600; }
	.cleaner-detail { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.cleaner-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
	
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
	.empty-message { text-align: center; color: #6b7280; padding: 2rem; grid-column: 1 / -1; }

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

	.clickable { cursor: pointer; }
	.clickable:hover { border-color: var(--primary-color); box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
</style>
