<script lang="ts">
	import { ownersAPI, housesAPI, bookingsByHouseAPI, schedulesByBookingAPI, type Owner, type House, type Booking, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	
	const tt = (key: string) => t(key, undefined, $currentLocale);

	let owners = $state<Owner[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingOwner = $state<Owner | null>(null);
	
	let ownerHousesWithSchedules = $state<HouseWithSchedules[]>([]);
	let expandedHouseIds = $state<Set<number>>(new Set());
	let loadingHouses = $state(false);

	interface HouseWithSchedules {
		house: House;
		bookings: BookingWithSchedules[];
	}

	interface BookingWithSchedules {
		booking: Booking;
		schedules: Schedule[];
	}

	let formData = $state({
		name: '',
		email: '',
		phone: '',
		address: ''
	});

	async function loadOwners() {
		loading = true;
		error = null;
		try {
			owners = await ownersAPI.getAll();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	async function loadOwnerHousesWithSchedules(ownerId: number) {
		loadingHouses = true;
		ownerHousesWithSchedules = [];
		try {
			const houses = await housesAPI.getByOwner(ownerId);
			for (const house of houses) {
				const bookings = await bookingsByHouseAPI.getByHouse(house.id);
				const bookingsWithSchedules: BookingWithSchedules[] = [];
				for (const booking of bookings) {
					const schedules = await schedulesByBookingAPI.getByBooking(booking.id);
					bookingsWithSchedules.push({ booking, schedules });
				}
				ownerHousesWithSchedules.push({ house, bookings: bookingsWithSchedules });
			}
		} catch (err: any) {
			console.error('Error loading houses:', err);
		} finally {
			loadingHouses = false;
		}
	}

	function openAddForm() {
		editingOwner = null;
		formData = { name: '', email: '', phone: '', address: '' };
		ownerHousesWithSchedules = [];
		showForm = true;
	}

	async function openEditForm(owner: Owner) {
		editingOwner = owner;
		formData = { 
			name: owner.name, 
			email: owner.email || '', 
			phone: owner.phone || '', 
			address: owner.address || '' 
		};
		ownerHousesWithSchedules = [];
		expandedHouseIds = new Set();
		showForm = true;
		await loadOwnerHousesWithSchedules(owner.id);
	}

	function handleFormCancel() {
		showForm = false;
		editingOwner = null;
		ownerHousesWithSchedules = [];
		expandedHouseIds = new Set();
		formData = { name: '', email: '', phone: '', address: '' };
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		try {
			if (editingOwner) {
				await ownersAPI.update(editingOwner.id, formData);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.updated_successfully'));
			} else {
				await ownersAPI.create(formData);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.created_successfully'));
			}
			showForm = false;
			editingOwner = null;
			ownerHousesWithSchedules = [];
			formData = { name: '', email: '', phone: '', address: '' };
			await loadOwners();
		} catch (err: any) {
			notificationActions.error(err.message || t('errors.failed_to_save'));
		}
	}

	async function handleDelete(owner: Owner) {
		if (confirm(t('owners.delete_confirm').replace('"${owner.name}"', `"${owner.name}"`))) {
			try {
				await ownersAPI.delete(owner.id);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.deleted_successfully'));
				await loadOwners();
			} catch (err: any) {
				notificationActions.error(err.message || t('errors.failed_to_delete'));
			}
		}
	}

	function toggleHouse(houseId: number) {
		const newSet = new Set(expandedHouseIds);
		if (newSet.has(houseId)) {
			newSet.delete(houseId);
		} else {
			newSet.add(houseId);
		}
		expandedHouseIds = newSet;
	}

	function formatDate(dateStr: string) {
		if (!dateStr) return '-';
		return dateStr.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3');
	}

	$effect(() => {
		loadOwners();
	});
</script>

<div class="owners-page">
	<div class="page-header">
		<h1>{tt('owners.title')}</h1>
		<button class="btn btn-primary" onclick={openAddForm}>{tt('owners.add_owner')}</button>
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
			<h3 class="form-title">{editingOwner ? t('owners.edit_owner') : t('owners.add_new_owner')}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field">
						<label for="name">{tt('common.name')} <span class="required">*</span></label>
						<input type="text" id="name" bind:value={formData.name} placeholder={tt('owners.enter_owner_name')} required />
					</div>
					<div class="form-field">
						<label for="email">{tt('common.email')}</label>
						<input type="email" id="email" bind:value={formData.email} placeholder={tt('owners.enter_email_address')} />
					</div>
					<div class="form-field">
						<label for="phone">{tt('common.phone')}</label>
						<input type="tel" id="phone" bind:value={formData.phone} placeholder={tt('owners.enter_phone_number')} />
					</div>
					<div class="form-field full-width">
						<label for="address">{tt('common.address')}</label>
						<input type="text" id="address" bind:value={formData.address} placeholder={tt('owners.enter_address')} />
					</div>
				</div>

				{#if editingOwner}
					<div class="houses-schedules-section">
						<h4 class="section-title">{tt('owners.houses')} & {tt('schedules.title')}</h4>
						
						{#if loadingHouses}
							<div class="loading-inline">{tt('common.loading')}</div>
						{:else if ownerHousesWithSchedules.length === 0}
							<div class="no-houses-message">{tt('owners.no_houses')}</div>
						{:else}
							{#each ownerHousesWithSchedules as { house, bookings }}
								<div class="house-card">
									<button type="button" class="house-header" onclick={() => toggleHouse(house.id)}>
										<span class="expand-icon">{expandedHouseIds.has(house.id) ? '▼' : '▶'}</span>
										<span class="house-name">{house.name}</span>
										<span class="house-address">{house.address || '-'}</span>
										<span class="booking-count">{bookings.length} {tt('bookings.title').toLowerCase()}</span>
										<span class="status-badge" class:active={house.active}>{house.active ? tt('common.active') : tt('common.inactive')}</span>
									</button>
									
									{#if expandedHouseIds.has(house.id)}
										<div class="house-details">
											{#if bookings.length === 0}
												<div class="no-data">{tt('bookings.no_bookings')}</div>
											{:else}
												{#each bookings as { booking, schedules }}
													<div class="booking-card">
														<div class="booking-header">
															<span class="guest">{booking.guest_name}</span>
															<span class="dates">{formatDate(booking.check_in_date)} → {formatDate(booking.check_out_date)}</span>
															<span class="status-mini status-{booking.status}">{booking.status}</span>
														</div>
														{#if schedules.length > 0}
															<table class="schedules-table">
																<thead>
																	<tr>
																		<th>{tt('common.date')}</th>
																		<th>{tt('schedules.start_time')}</th>
																		<th>{tt('schedules.end_time')}</th>
																		<th>{tt('schedules.cleaner')}</th>
																		<th>{tt('common.status')}</th>
																	</tr>
																</thead>
																<tbody>
																	{#each schedules as schedule}
																		<tr>
																			<td>{formatDate(schedule.date)}</td>
																			<td>{schedule.start_time}</td>
																			<td>{schedule.end_time}</td>
																			<td>{schedule.cleaner_id || '-'}</td>
																			<td><span class="status-mini status-{schedule.status}">{schedule.status}</span></td>
																		</tr>
																	{/each}
																</tbody>
															</table>
														{:else}
															<div class="no-data">{tt('schedules.no_schedules')}</div>
														{/if}
													</div>
												{/each}
											{/if}
										</div>
									{/if}
								</div>
							{/each}
						{/if}
					</div>
				{/if}

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{editingOwner ? tt('common.update') : tt('common.add')} {tt('owners.title')}
					</button>
				</div>
			</form>
		</div>
	{/if}

	<div class="owners-grid">
		{#if owners.length === 0 && !loading}
			<div class="empty-message">{tt('owners.no_owners')}</div>
		{:else}
			{#each owners as owner}
				<div class="owner-card">
					<h3 class="owner-name">{owner.name}</h3>
					{#if owner.email}<p class="owner-detail">{owner.email}</p>{/if}
					{#if owner.phone}<p class="owner-detail">{owner.phone}</p>{/if}
					{#if owner.address}<p class="owner-detail">{owner.address}</p>{/if}
					<div class="owner-actions">
						<button class="btn btn-secondary btn-sm" onclick={() => openEditForm(owner)}>{tt('common.edit')}</button>
						<button class="btn btn-danger btn-sm" onclick={() => handleDelete(owner)}>{tt('common.delete')}</button>
					</div>
				</div>
			{/each}
		{/if}
	</div>
</div>

<style>
	.owners-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
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
	.owners-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.5rem; }
	.owner-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
	.owner-name { margin: 0 0 0.5rem 0; font-size: 1.125rem; font-weight: 600; color: #111827; }
	.owner-detail { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.owner-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }

	/* Houses & Schedules */
	.houses-schedules-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e5e7eb; }
	.section-title { font-size: 1rem; font-weight: 600; margin: 0 0 1rem 0; color: #374151; }
	.loading-inline { color: #6b7280; padding: 1rem; font-style: italic; }
	.no-houses-message { color: #9ca3af; padding: 1rem; background: #f9fafb; border-radius: 6px; text-align: center; }
	
	.house-card { border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 0.5rem; overflow: hidden; }
	.house-header { 
		width: 100%; display: flex; align-items: center; gap: 0.75rem; padding: 0.75rem 1rem; 
		background: #f9fafb; border: none; cursor: pointer; text-align: left; font-size: 0.875rem;
	}
	.house-header:hover { background: #f3f4f6; }
	.expand-icon { color: #6b7280; font-size: 0.75rem; }
	.house-name { font-weight: 600; color: #111827; }
	.house-address { color: #6b7280; flex: 1; }
	.booking-count { color: #6b7280; font-size: 0.75rem; }
	.status-badge { padding: 0.125rem 0.5rem; border-radius: 9999px; font-size: 0.625rem; font-weight: 500; }
	.status-badge.active { background: #d1fae5; color: #065f46; }
	.status-badge:not(.active) { background: #fee2e2; color: #991b1b; }
	
	.house-details { padding: 0.75rem 1rem 0.75rem 2.5rem; background: white; border-top: 1px solid #e5e7eb; }
	.no-data { color: #9ca3af; font-size: 0.875rem; font-style: italic; padding: 0.5rem; }
	
	.booking-card { border: 1px solid #e5e7eb; border-radius: 6px; margin-bottom: 0.5rem; overflow: hidden; }
	.booking-header { display: flex; align-items: center; gap: 1rem; padding: 0.5rem 0.75rem; background: #f9fafb; border-bottom: 1px solid #e5e7eb; }
	.guest { font-weight: 500; color: #111827; font-size: 0.875rem; }
	.dates { color: #6b7280; font-size: 0.75rem; }
	.status-mini { padding: 0.125rem 0.375rem; border-radius: 4px; font-size: 0.625rem; font-weight: 500; text-transform: capitalize; }
	.status-scheduled { background: #dbeafe; color: #1e40af; }
	.status-completed { background: #d1fae5; color: #065f46; }
	.status-pending { background: #fef3c7; color: #92400e; }
	.status-cancelled { background: #fee2e2; color: #991b1b; }
	.status-confirmed { background: #d1fae5; color: #065f46; }
	
	.schedules-table { width: 100%; border-collapse: collapse; font-size: 0.75rem; }
	.schedules-table th { background: #f3f4f6; padding: 0.5rem; text-align: left; font-weight: 500; color: #374151; }
	.schedules-table td { padding: 0.5rem; border-bottom: 1px solid #f3f4f6; color: #6b7280; }
</style>
