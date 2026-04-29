<script lang="ts">
	import { page } from '$app/stores';
	import { housesAPI, ownersAPI, cleanersAPI, bookingsAPI, schedulesAPI, type House, type Owner, type Cleaner, type Booking, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { Server } from '$lib/services/Server';
	import { toDisplayDateFormat } from '$lib/utils/Utils';
	import { goto } from '$app/navigation';
	import Button from '$lib/components/Button.svelte';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let urlHouseId = $derived($page.params.houseId ? parseInt($page.params.houseId) : 0);

	let editingHouse = $state<House | null>(null);
	let showForm = $state(true);
	let loading = $state(false);
	let owner = $state<Owner | null>(null);
	let bookings = $state<Booking[]>([]);
	let cleaners = $state<Cleaner[]>([]);
	let schedules = $state<Schedule[]>([]);

	let formData = $state({
		name: '',
		address: '',
		description: '',
		owner: 0,
		cost_profile: 0,
		check_in_time: '16:00',
		check_out_time: '10:00',
		surface_m2: null as number | null,
		floors: 1,
		bedrooms: 0,
		bathrooms: 0,
		luxury_level: 'standard' as 'basic' | 'standard' | 'premium' | 'luxury'
	});

	let saving = $state(false);
	let showDeleteConfirm = $state(false);

	function scrollToEditForm() {
		if (window.innerWidth < 1024 && formSection) {
			formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}

	let formSection = $state<HTMLElement | null>(null);

	async function loadData() {
		if (!urlHouseId || urlHouseId <= 0) return;
		loading = true;
		try {
			editingHouse = await housesAPI.getById(urlHouseId);
			if (!editingHouse) {
				notificationActions.error(tt('houses.house_not_found'));
				goto('/houses');
				return;
			}

			formData = {
				name: editingHouse.name,
				address: editingHouse.address || '',
				description: editingHouse.description || '',
				owner: editingHouse.owner || 0,
				cost_profile: editingHouse.cost_profile || 0,
				check_in_time: editingHouse.check_in_time || '16:00',
				check_out_time: editingHouse.check_out_time || '10:00',
				surface_m2: editingHouse.surface_m2 || null,
				floors: editingHouse.floors || 1,
				bedrooms: editingHouse.bedrooms || 0,
				bathrooms: editingHouse.bathrooms || 0,
				luxury_level: editingHouse.luxury_level || 'standard'
			};

			// Load related data
			if (editingHouse.owner) {
				owner = await ownersAPI.getById(editingHouse.owner) || null;
			}
			bookings = await bookingsAPI.getByHouse(editingHouse.id);
			for (const booking of bookings) {
				const s = await schedulesAPI.getByBooking(booking.id);
				schedules.push(...s);
			}
		} catch (err: any) {
			console.error('Error loading house:', err);
			notificationActions.error(err.message || tt('errors.failed_to_load'));
			goto('/houses');
		} finally {
			loading = false;
		}
	}

	let hasChanges = $derived(editingHouse && (
		formData.name !== editingHouse.name ||
		formData.address !== (editingHouse.address || '') ||
		formData.description !== (editingHouse.description || '') ||
		formData.owner !== (editingHouse.owner || 0) ||
		formData.cost_profile !== (editingHouse.cost_profile || 0) ||
		formData.check_in_time !== (editingHouse.check_in_time || '16:00') ||
		formData.check_out_time !== (editingHouse.check_out_time || '10:00') ||
		formData.surface_m2 !== (editingHouse.surface_m2 || null) ||
		formData.floors !== (editingHouse.floors || 1) ||
		formData.bedrooms !== (editingHouse.bedrooms || 0) ||
		formData.bathrooms !== (editingHouse.bathrooms || 0) ||
		formData.luxury_level !== (editingHouse.luxury_level || 'standard')
	));

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		if (!editingHouse || saving) return;
		saving = true;
		try {
			const result = await housesAPI.update(editingHouse.id, formData);
			notificationActions.success(tt('houses.updated'));
			editingHouse = result;
			await loadData();
		} catch (err: any) {
			notificationActions.error(err.message || tt('errors.failed_to_save'));
		} finally {
			saving = false;
		}
	}

	function handleFormCancel() {
		goto('/houses');
	}

	async function handleDelete() {
		if (!editingHouse) return;
		if (confirm(tt('houses.delete_confirm').replace('"${house.name}"', `"${editingHouse.name}"`))) {
			try {
				await housesAPI.delete(editingHouse.id);
				notificationActions.success(tt('houses.deleted'));
				goto('/houses');
			} catch (err: any) {
				notificationActions.error(err.message || tt('errors.failed_to_delete'));
			}
		}
	}

	function formatDate(dateStr: string) {
		if (!dateStr) return '-';
		return toDisplayDateFormat(dateStr, $currentLocale);
	}

	let loaded = $state(false);
	$effect(() => {
		if (!loaded && urlHouseId > 0) {
			loaded = true;
			loadData();
		}
	});
</script>

<div class="house-page">
	<div class="page-header">
		<button class="btn btn-secondary" onclick={() => goto('/houses')}>{tt('common.back')}</button>
		<h1>{editingHouse?.name || tt('houses.edit_house')}</h1>
		<button class="btn btn-danger" onclick={() => showDeleteConfirm = true}>{tt('common.delete')}</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			{tt('common.loading')}
		</div>
	{/if}

	{#if showForm && editingHouse}
		<div class="form-section" bind:this={formSection}>
			<h3 class="form-title">{tt('houses.edit_house')}</h3>

			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field full-width">
						<label for="name">{tt('common.name')} <span class="required">*</span></label>
						<input type="text" id="name" bind:value={formData.name} placeholder={tt('houses.enter_name')} required />
					</div>
					<div class="form-field">
						<label for="owner">{tt('common.owner')}</label>
						<select id="owner" bind:value={formData.owner}>
							<option value={0}>{tt('common.select_owner')}</option>
							{#each owners as owner}
								<option value={owner.id}>{owner.name}</option>
							{/each}
						</select>
					</div>
					<div class="form-field">
						<label for="cost_profile">{tt('common.cost_profile')}</label>
						<select id="cost_profile" bind:value={formData.cost_profile}>
							<option value={0}>{tt('common.select_profile')}</option>
						</select>
					</div>
					<div class="form-field">
						<label for="address">{tt('common.address')}</label>
						<input type="text" id="address" bind:value={formData.address} placeholder={tt('houses.enter_address')} />
					</div>
					<div class="form-field">
						<label for="description">{tt('common.description')}</label>
						<textarea id="description" bind:value={formData.description} rows={3}></textarea>
					</div>
					<div class="form-field">
						<label for="luxury_level">{tt('common.luxury_level')}</label>
						<select id="luxury_level" bind:value={formData.luxury_level}>
							<option value="basic">Basic</option>
							<option value="standard">Standard</option>
							<option value="premium">Premium</option>
							<option value="luxury">Luxury</option>
						</select>
					</div>
					<div class="form-field">
						<label for="surface_m2">{tt('houses.surface_m2')}</label>
						<input type="number" id="surface_m2" bind:value={formData.surface_m2} placeholder="e.g., 120" min="0" />
					</div>
					<div class="form-field">
						<label for="floors">{tt('common.floors')}</label>
						<input type="number" id="floors" bind:value={formData.floors} min="1" max="10" />
					</div>
					<div class="form-field">
						<label for="bedrooms">{tt('common.bedrooms')}</label>
						<input type="number" id="bedrooms" bind:value={formData.bedrooms} min="0" max="20" />
					</div>
					<div class="form-field">
						<label for="bathrooms">{tt('common.bathrooms')}</label>
						<input type="number" id="bathrooms" bind:value={formData.bathrooms} min="0" max="10" />
					</div>
					<div class="form-field">
						<label for="check_in">{tt('houses.check_in_time')}</label>
						<input type="time" id="check_in" bind:value={formData.check_in_time} />
					</div>
					<div class="form-field">
						<label for="check_out">{tt('houses.check_out_time')}</label>
						<input type="time" id="check_out" bind:value={formData.check_out_time} />
					</div>
					<div class="form-actions">
						<Button type="button" class="btn-secondary" onclick={handleFormCancel} disabled={saving}>
							{tt('common.cancel')}
						</Button>
						<Button type="submit" class={hasChanges ? 'btn-primary' : 'btn-disabled'} loading={saving} disabled={!hasChanges}>
							{tt('common.save')}
						</Button>
					</div>
				</div>
			</form>

			{#if owner}
				<div class="owner-info">
					<h3>{tt('common.owner')}</h3>
					<p><strong>{owner.name}</strong></p>
					{#if owner.email}<p>{owner.email}</p>{/if}
					{#if owner.phone}<p>{owner.phone}</p>{/if}
					{#if owner.address}<p>{owner.address}</p>{/if}
				</div>
			{/if}

			{#if bookings.length > 0}
				<div class="bookings-section">
					<h3>{tt('bookings.title')} ({bookings.length})</h3>
					<table class="data-table">
						<thead>
							<tr>
								<th>{tt('bookings.guest_name')}</th>
								<th>{tt('common.check_in')}</th>
								<th>{tt('common.check_out')}</th>
								<th>{tt('common.status')}</th>
							</tr>
						</thead>
						<tbody>
							{#each bookings as booking}
								<tr onclick={() => goto(`/bookings/${booking.id}`)}>
									<td>{booking.guest_name}</td>
									<td>{formatDate(booking.check_in_date)}</td>
									<td>{formatDate(booking.check_out_date)}</td>
									<td><span class="status status-{booking.status}">{booking.status}</span></td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{/if}

			{#if schedules.length > 0}
				<div class="schedules-section">
					<h3>{tt('schedules.title')} ({schedules.length})</h3>
					<table class="data-table">
						<thead>
							<tr>
								<th>{tt('common.date')}</th>
								<th>{tt('schedules.start_time')}</th>
								<th>{tt('schedules.end_time')}</th>
								<th>{tt('common.status')}</th>
							</tr>
						</thead>
						<tbody>
							{#each schedules as schedule}
								<tr onclick={() => goto(`/schedules/${schedule.id}`)}>
									<td>{formatDate(schedule.date)}</td>
									<td>{schedule.start_time}</td>
									<td>{schedule.end_time}</td>
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
		<p>{tt('houses.delete_confirm').replace('"${house.name}"', `"${editingHouse?.name}"`)}</p>
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
	.house-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
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
	.form-field textarea { resize: vertical; }
	.form-field input:focus, .form-field select:focus, .form-field textarea:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.2); }
	.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e5e7eb; }
	.btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
	.btn-primary { background: #3b82f6; color: white; }
	.btn-primary:hover { background: #2563eb; }
	.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }
	.btn-secondary { background: #6b7280; color: white; }
	.btn-secondary:hover { background: #4b5563; }
	.btn-danger { background: #ef4444; color: white; }
	.btn-danger:hover { background: #dc2626; }
	.owner-info, .bookings-section, .schedules-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e5e7eb; }
	.owner-info h3, .bookings-section h3, .schedules-section h3 { font-size: 1rem; font-weight: 600; margin: 0 0 1rem 0; color: #374151; }
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
