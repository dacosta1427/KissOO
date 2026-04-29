<script lang="ts">
	import { page } from '$app/stores';
	import { cleanersAPI, schedulesAPI, type Cleaner, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { goto } from '$app/navigation';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let urlCleanerId = $derived($page.params.cleanerId ? parseInt($page.params.cleanerId) : 0);

	let cleaner = $state<Cleaner | null>(null);
	let schedules = $state<Schedule[]>([]);
	let loading = $state(false);
	let showDeleteConfirm = $state(false);

	let formData = $state({
		name: '',
		phone: '',
		email: '',
		address: '',
		canLogin: false
	});

	let saving = $state(false);

	async function loadData() {
		if (!urlCleanerId || urlCleanerId <= 0) return;
		loading = true;
		try {
			cleaner = await cleanersAPI.getById(urlCleanerId);
			if (!cleaner) {
				notificationActions.error(tt('cleaners.cleaner_not_found'));
				goto('/cleaners');
				return;
			}

			formData = {
				name: cleaner.name,
				phone: cleaner.phone || '',
				email: cleaner.email || '',
				address: cleaner.address || '',
				canLogin: cleaner.canLogin || false
			};

			// Load schedules
			schedules = await schedulesAPI.getByCleaner(cleaner.id);
		} catch (err: any) {
			console.error('Error loading cleaner:', err);
			notificationActions.error(err.message || tt('errors.failed_to_load'));
			goto('/cleaners');
		} finally {
			loading = false;
		}
	}

	let hasChanges = $derived(cleaner && (
		formData.name !== cleaner.name ||
		formData.phone !== (cleaner.phone || '') ||
		formData.email !== (cleaner.email || '') ||
		formData.address !== (cleaner.address || '') ||
		formData.canLogin !== (cleaner.canLogin || false)
	));

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		if (!cleaner || saving) return;
		saving = true;
		try {
			const result = await cleanersAPI.update(cleaner.id, formData);
			notificationActions.success(tt('cleaners.updated'));
			cleaner = result;
			await loadData();
		} catch (err: any) {
			notificationActions.error(err.message || tt('errors.failed_to_save'));
		} finally {
			saving = false;
		}
	}

	function handleFormCancel() {
		goto('/cleaners');
	}

	async function handleDelete() {
		if (!cleaner) return;
		if (confirm(tt('cleaners.delete_confirm').replace('${name}', cleaner.name))) {
			try {
				await cleanersAPI.delete(cleaner.id);
				notificationActions.success(tt('cleaners.deleted'));
				goto('/cleaners');
			} catch (err: any) {
				notificationActions.error(err.message || tt('errors.failed_to_delete'));
			}
		}
	}

	function formatDate(dateStr: string) {
		if (!dateStr) return '-';
		const d = new Date(dateStr);
		return d.toLocaleDateString($currentLocale || 'en');
	}

	let loaded = $state(false);
	$effect(() => {
		if (!loaded && urlCleanerId > 0) {
			loaded = true;
			loadData();
		}
	});
</script>

<div class="cleaner-page">
	<div class="page-header">
		<button class="btn btn-secondary" onclick={() => goto('/cleaners')}>{tt('common.back')}</button>
		<h1>{tt("cleaners.edit_cleaner")}</h1>
		<button class="btn btn-danger" onclick={() => showDeleteConfirm = true}>{tt('common.delete')}</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			{tt('common.loading')}
		</div>
	{/if}

	{#if cleaner}
		<div class="form-section">
			<h3 class="form-title">{tt('cleaners.edit_cleaner')}</h3>

			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field full-width">
						<label for="name">{tt('common.name')} <span class="required">*</span></label>
						<input type="text" id="name" bind:value={formData.name} required />
					</div>
					<div class="form-field">
						<label for="email">{tt('common.email')}</label>
						<input type="email" id="email" bind:value={formData.email} />
					</div>
					<div class="form-field">
						<label for="phone">{tt('common.phone')}</label>
						<input type="tel" id="phone" bind:value={formData.phone} />
					</div>
					<div class="form-field full-width">
						<label for="address">{tt('common.address')}</label>
						<input type="text" id="address" bind:value={formData.address} />
					</div>
					<div class="form-field">
						<label class="toggle-label">
							<span>{tt('cleaners.can_login') || 'Can log in'}</span>
						</label>
						<input type="checkbox" id="canLogin" bind:checked={formData.canLogin} />
					</div>
				</div>

				<div class="form-actions">
					<Button type="button" class="btn-secondary" onclick={handleFormCancel} disabled={saving}>
						{tt('common.cancel')}
					</Button>
					<Button type="submit" class={hasChanges ? 'btn-primary' : 'btn-disabled'} loading={saving} disabled={!hasChanges}>
						{tt('common.save')}
					</Button>
				</div>
			</form>

			{#if schedules.length > 0}
				<div class="schedules-section">
					<h3>{tt('schedules.title')} ({schedules.length})</h3>
					<table class="data-table">
						<thead>
							<tr>
								<th>{tt('common.date')}</th>
								<th>{tt('schedules.start_time')}</th>
								<th>{tt('schedules.end_time')}</th>
								<th>{tt('bookings.guest_name')}</th>
								<th>{tt('common.house')}</th>
								<th>{tt('common.status')}</th>
							</tr>
						</thead>
						<tbody>
							{#each schedules as schedule}
								<tr onclick={() => goto(`/schedules/${schedule.id}`)}>
									<td>{formatDate(schedule.date)}</td>
									<td>{schedule.start_time}</td>
									<td>{schedule.end_time}</td>
									<td>{schedule.booking?.guest_name || '-'}</td>
									<td>{schedule.booking?.house?.name || '-'}</td>
									<td><span class="status status-{schedule.status}">{schedule.status}</span></td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{/if}
		</div>
	{/if}
</div>

{#if showDeleteConfirm}
<div class="modal-overlay" onclick={() => showDeleteConfirm = false}>
	<div class="modal-content" onclick={(e) => e.stopPropagation()}>
		<h3>{tt('common.confirm')}</h3>
		<p>{tt('cleaners.delete_confirm').replace('${name}', cleaner?.name || '')}</p>
		<div class="modal-actions">
			<Button type="button" class="btn-secondary" onclick={() => showDeleteConfirm = false}>
				{tt('common.cancel')}
			</Button>
			<Button type="button" class="btn-danger" onclick={handleDelete}>
				{tt('common.delete')}
			</Button>
		</div>
	</div>
</div>

	{/if}

<style>
	.cleaner-page { padding: 2rem; max-width: 900px; margin: 0 auto; }
	.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; border-bottom: 2px solid #e5e7eb; padding-bottom: 1rem; }
	.page-header h1 { margin: 0; }
	.loading-spinner { position: fixed; top: 1rem; right: 1rem; background: #3b82f6; color: white; padding: 0.5rem 1rem; border-radius: 6px; display: flex; align-items: center; gap: 0.5rem; }
	.spinner { width: 1rem; height: 1rem; border: 2px solid white; border-top-color: transparent; border-radius: 50%; animation: spin 1s linear infinite; }
	@keyframes spin { to { transform: rotate(360deg); } }
	.form-section { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1.5rem; margin-bottom: 2rem; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
	.form-title { margin: 0 0 1.5rem 0; font-size: 1.125rem; font-weight: 600; }
	.form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; }
	.form-field { display: flex; flex-direction: column; gap: 0.25rem; }
	.form-field.full-width { grid-column: 1 / -1; }
	.form-field label { font-size: 0.875rem; font-weight: 500; color: #374151; }
	.form-field .required { color: #ef4444; }
	.form-field input, .form-field select, .form-field textarea { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.875rem; font-family: inherit; }
	.form-field input[type="checkbox"] { width: 1.25rem; height: 1.25rem; }
	.form-field textarea { resize: vertical; }
	.form-field input:focus, .form-field select:focus, .form-field textarea:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.2); }
	.toggle-label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; }
	.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e5e7eb; }
	.btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
	.btn-primary { background: #3b82f6; color: white; }
	.btn-primary:hover { background: #2563eb; }
	.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }
	.btn-secondary { background: #6b7280; color: white; }
	.btn-secondary:hover { background: #4b5563; }
	.btn-danger { background: #ef4444; color: white; }
	.btn-danger:hover { background: #dc2626; }
	.schedules-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e5e7eb; }
	.schedules-section h3 { font-size: 1rem; font-weight: 600; margin: 0 0 1rem 0; color: #374151; }
	.data-table { width: 100%; border-collapse: collapse; }
	.data-table th { background: #f3f4f6; padding: 0.75rem; text-align: left; font-weight: 600; color: #374151; border-bottom: 2px solid #e5e7eb; }
	.data-table td { padding: 0.75rem; border-bottom: 1px solid #e5e7eb; cursor: pointer; }
	.data-table tr:hover { background: #f9fafb; }
	.status { padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 500; text-transform: capitalize; }
	.status-pending { background: #fef3c7; color: #92400e; }
	.status-confirmed { background: #d1fae5; color: #065f46; }
	.status-cancelled { background: #fee2e2; color: #991b1b; }
	.status-scheduled { background: #dbeafe; color: #1e40af; }
	.status-completed { background: #d1fae5; color: #065f46; }
	.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
	.modal-content { background: white; border-radius: 8px; padding: 1.5rem; max-width: 400px; width: 90%; }
	.modal-content h3 { margin: 0 0 1rem 0; font-size: 1.125rem; font-weight: 600; }
	.modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
</style>
