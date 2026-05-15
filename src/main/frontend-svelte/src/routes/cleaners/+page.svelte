<script lang="ts">
	import { cleanersAPI, type Cleaner } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { goto } from '$app/navigation';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let cleaners = $state<Cleaner[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingCleaner = $state<Cleaner | null>(null);
	let viewMode = $state<'card' | 'table'>('card');

	let formData = $state({
		name: '',
		phone: '',
		email: '',
		address: ''
	});

	let togglingCleaners = $state<Set<number>>(new Set());

	async function loadCleaners() {
		loading = true;
		error = null;
		try {
			cleaners = await cleanersAPI.getAll();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	function openAddForm() {
		editingCleaner = null;
		formData = { name: '', phone: '', email: '', address: '' };
		showForm = true;
	}

	async function openEditForm(cleaner: Cleaner) {
		goto('/cleaners/' + cleaner.oid);
	}

	function handleFormCancel() {
		showForm = false;
		editingCleaner = null;
		formData = { name: '', phone: '', email: '', address: '' };
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		try {
			if (editingCleaner) {
				await cleanersAPI.update(editingCleaner.oid, formData);
				notificationActions.success(t('cleaners.updated'));
			} else {
				await cleanersAPI.create(formData);
				notificationActions.success(t('cleaners.created_successfully'));
			}
			showForm = false;
			editingCleaner = null;
			await loadCleaners();
		} catch (err: any) {
			notificationActions.error(err.message || t('errors.failed_to_save'));
		}
	}

	async function handleDelete(cleaner: Cleaner) {
		if (confirm(t('cleaners.delete_confirm').replace('${name}', cleaner.name))) {
			try {
				await cleanersAPI.delete(cleaner.oid);
				notificationActions.success(t('cleaners.deleted'));
				await loadCleaners();
			} catch (err: any) {
				notificationActions.error(err.message || t('errors.failed_to_delete'));
			}
		}
	}

	async function toggleCleanerLoginById(cleanerId: number, canLogin: boolean) {
		const idx = cleaners.findIndex(c => c.oid === cleanerId);
		if (idx >= 0) cleaners[idx] = { ...cleaners[idx], canLogin };
		togglingCleaners = new Set([...togglingCleaners, cleanerId]);

		try {
			const updated = await cleanersAPI.toggleLogin(cleanerId, canLogin);
			if (updated && updated.oid) {
				const updateIdx = cleaners.findIndex(c => c.oid === updated.oid);
				if (updateIdx >= 0) {
					cleaners[updateIdx] = { ...cleaners[updateIdx], canLogin: updated.canLogin };
				}
			}
		} catch (err: any) {
			if (idx >= 0) cleaners[idx] = { ...cleaners[idx], canLogin: !canLogin };
			notificationActions.error(err.message || 'Failed to toggle cleaner login');
		} finally {
			togglingCleaners = new Set([...togglingCleaners].filter(id => id !== cleanerId));
		}
	}

	$effect(() => {
		loadCleaners();
	});
</script>

<div class="cleaners-page">
	<div class="page-header">
		<h1>{tt('cleaners.title')}</h1>
		<button class="btn btn-primary" onclick={openAddForm}>{tt('cleaners.add_cleaner')}</button>
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
			<h3 class="form-title">{editingCleaner ? t('cleaners.edit_cleaner') : t('cleaners.add_new_cleaner')}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field">
						<label for="name">{tt('common.name')} <span class="required">*</span></label>
						<input type="text" id="name" bind:value={formData.name} placeholder={t('cleaners.enter_name')} required />
					</div>
					<div class="form-field">
						<label for="email">{tt('common.email')} <span class="required">*</span></label>
						<input type="email" id="email" bind:value={formData.email} placeholder={t('cleaners.enter_email')} required />
					</div>
					<div class="form-field">
						<label for="phone">{tt('common.phone')}</label>
						<input type="tel" id="phone" bind:value={formData.phone} placeholder={t('cleaners.enter_phone')} />
					</div>
					<div class="form-field full-width">
						<label for="address">{tt('common.address')}</label>
						<input type="text" id="address" bind:value={formData.address} placeholder={t('cleaners.enter_address')} />
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{editingCleaner ? t('common.update') : t('common.add')} {tt('cleaners.title')}
					</button>
				</div>
			</form>
		</div>
	{/if}

	{#if !showForm}
	<!-- View Toggle -->
	<div class="view-toggle">
		<button class="toggle-btn" class:active={viewMode === 'card'} onclick={() => viewMode = 'card'}>
			{tt('houses.card_view')}
		</button>
		<button class="toggle-btn" class:active={viewMode === 'table'} onclick={() => viewMode = 'table'}>
			{tt('houses.table_view')}
		</button>
	</div>

	{#if viewMode === 'card'}
	<div class="cleaners-grid">
		{#if cleaners.length === 0 && !loading}
			<div class="empty-message">{tt('cleaners.no_cleaners')}</div>
		{:else}
			{#each cleaners as cleaner}
				<!-- svelte-ignore a11y_click_events_have_key_events -->
				<!-- svelte-ignore a11y_no_static_element_interactions -->
				<div class="cleaner-card clickable" onclick={() => openEditForm(cleaner)} onkeydown={(e) => e.key === 'Enter' && openEditForm(cleaner)}>
					<div class="card-header">
						<h3 class="cleaner-name">
							{cleaner.name}
							{#if cleaner.emailVerified}
								<span class="ml-1 text-green-600" title="Email verified">
									<svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
										<path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
										<path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
									</svg>
								</span>
							{:else}
								<span class="ml-1 text-red-600" title="Email not verified">
									<svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
										<path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
										<path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
									</svg>
								</span>
							{/if}
						</h3>
						<button
							type="button"
							class="card-toggle"
							class:active={cleaner.canLogin}
							onclick={(e) => { e.stopPropagation(); toggleCleanerLoginById(cleaner.oid, !cleaner.canLogin); }}
							title={cleaner.canLogin ? 'Login enabled' : 'Login disabled'}
						></button>
					</div>
					{#if cleaner.email}<p class="cleaner-detail">{cleaner.email}</p>{/if}
					{#if cleaner.phone}<p class="cleaner-detail">{cleaner.phone}</p>{/if}
					{#if cleaner.address}<p class="cleaner-detail">{cleaner.address}</p>{/if}
					<div class="cleaner-actions">
						<button class="btn btn-secondary btn-sm" onclick={(e) => { e.stopPropagation(); openEditForm(cleaner); }}>{tt('common.edit')}</button>
						<button class="btn btn-danger btn-sm" onclick={(e) => { e.stopPropagation(); handleDelete(cleaner); }}>{tt('common.delete')}</button>
					</div>
				</div>
			{/each}
		{/if}
	</div>
	{:else}
	<!-- Table View -->
	<div class="table-section">
		<table class="data-table">
			<thead>
				<tr>
					<th>{tt('cleaners.name')}</th>
					<th>{tt('cleaners.email')}</th>
					<th>{tt('cleaners.phone')}</th>
					<th>{tt('cleaners.can_login')}</th>
					<th>Email</th>
					<th>{tt('common.actions')}</th>
				</tr>
			</thead>
			<tbody>
				{#each cleaners as cleaner}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<tr class="clickable" onclick={() => openEditForm(cleaner)} onkeydown={(e) => e.key === 'Enter' && openEditForm(cleaner)}>
						<td>{cleaner.name}</td>
						<td>{cleaner.email || '-'}</td>
						<td>{cleaner.phone || '-'}</td>
						<td>
							<button
								type="button"
								class="card-toggle"
								class:active={cleaner.canLogin}
								onclick={(e) => { e.stopPropagation(); toggleCleanerLoginById(cleaner.oid, !cleaner.canLogin); }}
								title={cleaner.canLogin ? 'Login enabled' : 'Login disabled'}
							></button>
						</td>
						<td>
							{#if cleaner.emailVerified}
								<span class="text-green-600" title="Email verified">
									<svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
										<path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
										<path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
									</svg>
								</span>
							{:else}
								<span class="text-red-600" title="Email not verified">
									<svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
										<path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
										<path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
									</svg>
								</span>
							{/if}
						</td>
						<td>
							<button class="btn btn-secondary btn-sm" onclick={(e) => { e.stopPropagation(); openEditForm(cleaner); }}>{tt('common.edit')}</button>
							<button class="btn btn-danger btn-sm" onclick={(e) => { e.stopPropagation(); handleDelete(cleaner); }}>{tt('common.delete')}</button>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
	{/if}
	{/if}
</div>

<style>
	.cleaners-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
	.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; border-bottom: 2px solid #e5e7eb; padding-bottom: 1rem; }
	.page-header h1 { margin: 0; }
	.loading-spinner { position: fixed; top: 1rem; right: 1rem; background: #3b82f6; color: white; padding: 0.5rem 1rem; border-radius: 6px; display: flex; align-items: center; gap: 0.5rem; }
	.spinner { width: 1rem; height: 1rem; border: 2px solid white; border-top-color: transparent; border-radius: 50%; animation: spin 1s linear infinite; }
	@keyframes spin { to { transform: rotate(360deg); } }
	.error-message { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; padding: 1rem; border-radius: 6px; margin-bottom: 1rem; }
	.form-section { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1.5rem; margin-bottom: 2rem; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
	.form-title { margin: 0 0 1.5rem 0; font-size: 1.125rem; font-weight: 600; }
	.form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; }
	.form-field { display: flex; flex-direction: column; gap: 0.25rem; }
	.form-field.full-width { grid-column: 1 / -1; }
	.form-field label { font-size: 0.875rem; font-weight: 500; color: #374151; }
	.form-field .required { color: #ef4444; }
	.form-field input { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.875rem; font-family: inherit; }
	.form-field input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.2); }
	.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e5e7eb; }
	.btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
	.btn-primary { background: #3b82f6; color: white; }
	.btn-primary:hover { background: #2563eb; }
	.btn-secondary { background: #6b7280; color: white; }
	.btn-secondary:hover { background: #4b5563; }
	.btn-danger { background: #ef4444; color: white; }
	.btn-danger:hover { background: #dc2626; }
	.btn-sm { padding: 0.25rem 0.75rem; font-size: 0.75rem; }
	.empty-message { grid-column: 1 / -1; text-align: center; color: #6b7280; padding: 2rem; }
	.cleaners-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.5rem; }
	.cleaner-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); transition: box-shadow 0.2s, border-color 0.2s; }
	.clickable { cursor: pointer; }
	.clickable:hover { border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
	.data-table tr.clickable:hover { background: #eff6ff; }
	.cleaner-name { margin: 0 0 0.5rem 0; font-size: 1.125rem; font-weight: 600; color: #111827; }
	.cleaner-detail { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.cleaner-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
	.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }

	/* Mini toggle switch for cards/tables */
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

	/* View Toggle */
	.view-toggle { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
	.toggle-btn { padding: 0.5rem 1rem; border: 1px solid #e5e7eb; background: white; cursor: pointer; font-size: 0.875rem; }
	.toggle-btn:first-child { border-radius: 6px 0 0 6px; }
	.toggle-btn:last-child { border-radius: 0 6px 6px 0; }
	.toggle-btn.active { background: #3b82f6; color: white; border-color: #3b82f6; }

	/* Table View */
	.table-section { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
	.data-table { width: 100%; border-collapse: collapse; }
	.data-table th, .data-table td { padding: 0.75rem 1rem; text-align: left; border-bottom: 1px solid #e5e7eb; }
	.data-table th { background: #f9fafb; font-weight: 600; color: #374151; }
	.data-table tr:hover { background: #f9fafb; }

	/* Responsive design */
	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}
	}
</style>
