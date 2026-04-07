<script lang="ts">
	import { page } from '$app/stores';
	import { ownersAPI, housesAPI, cleanersAPI, bookingsByHouseAPI, schedulesByBookingAPI, type Owner, type House, type Cleaner, type Booking, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { Server } from '$lib/services/Server';
	import { toDisplayDateFormat } from '$lib/utils/Utils';
	import { goto } from '$app/navigation';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	// Get ownerId from URL
	let params = $state<{id?: string}>({});
	$effect(() => {
		params = $page.params as {id?: string};
	});
	
	let urlOwnerId = $derived(params.id ? parseInt(params.id) : 0);

	// State: load single owner by URL param
	let editingOwner = $state<Owner | null>(null);
	let showForm = $state(true);  // Always show form in detail view
	let ownerHousesWithSchedules = $state<HouseWithSchedules[]>([]);
	let expandedHouseIds = $state<Set<number>>(new Set());
	let loading = $state(false);
	let cleaners = $state<Cleaner[]>([]);

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

	// Add house modal state
	let showAddHouseModal = $state(false);
	let houseFormData = $state({
		name: '',
		address: '',
		description: '',
		check_in_time: '16:00',
		check_out_time: '10:00',
		surface_m2: null as number | null,
		floors: 1,
		bedrooms: 0,
		bathrooms: 0,
		luxury_level: 'standard' as 'basic' | 'standard' | 'premium' | 'luxury'
	});

	// Owner login state
	let ownerCanLogin = $state(false);
	let loginToggleLoading = $state(false);
	let loginInfo = $state<{username?: string; tempPassword?: string} | null>(null);

	// Form section ref
	let formSection = $state<HTMLElement | null>(null);

	function scrollToEditForm() {
		if (window.innerWidth < 1024 && formSection) {
			formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}

	async function toggleOwnerLogin() {
		if (!editingOwner) return;
		loginToggleLoading = true;
		loginInfo = null;
		try {
			const res = await Server.call('services.Cleaning', 'toggleOwnerLogin', {
				id: editingOwner.id,
				canLogin: !ownerCanLogin
			});
			if (res._Success || res.success) {
				ownerCanLogin = !ownerCanLogin;
				if (res.temporaryPassword) {
					loginInfo = { username: res.username, tempPassword: res.temporaryPassword };
				}
				notificationActions.success(res.message || 'Login status updated');
				// Reload full data to ensure fresh Perst references
				await loadData();
			} else {
				notificationActions.error(res._ErrorMessage || res.error || 'Failed to toggle login');
			}
		} catch (err: any) {
			notificationActions.error(err.message || 'Failed to toggle login');
		} finally {
			loginToggleLoading = false;
		}
	}

	async function toggleOwnerLoginById(ownerId: number, canLogin: boolean) {
		try {
			const res = await Server.call('services.Cleaning', 'toggleOwnerLogin', {
				id: ownerId,
				canLogin
			});
			if (res._Success || res.success) {
				if (res.temporaryPassword) {
					notificationActions.success(`Temp password: ${res.temporaryPassword}`);
				}
			} else {
				notificationActions.error(res._ErrorMessage || res.error || 'Failed to toggle login');
			}
		} catch (err: any) {
			notificationActions.error(err.message || 'Failed to toggle login');
		}
	}

	function openAddHouseModal() {
		houseFormData = {
			name: '',
			address: '',
			description: '',
			check_in_time: '16:00',
			check_out_time: '10:00',
			surface_m2: null,
			floors: 1,
			bedrooms: 0,
			bathrooms: 0,
			luxury_level: 'standard'
		};
		showAddHouseModal = true;
	}

	async function handleAddHouseSubmit(e: Event) {
		e.preventDefault();
		if (!editingOwner) return;
		try {
			await housesAPI.create({
				...houseFormData,
				owner: editingOwner.id,
				active: true
			});
			notificationActions.success('House created successfully');
			showAddHouseModal = false;
			await loadOwnerHousesWithSchedules(editingOwner.id);
		} catch (err: any) {
			notificationActions.error(err.message || 'Failed to create house');
		}
	}

	function getCleanerName(cleanerId: number): string {
		const cleaner = cleaners.find(c => c.id === cleanerId);
		return cleaner ? cleaner.name : `#${cleanerId}`;
	}

	async function loadOwnerHousesWithSchedules(ownerId: number) {
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
		}
	}

	async function loadData() {
		if (!urlOwnerId) return;
		loading = true;
		try {
			// Load owner by ID
			const allOwners = await ownersAPI.getAll();
			editingOwner = allOwners.find(o => o.id === urlOwnerId) || null;
			if (!editingOwner) {
				loading = false;
				return;
			}
			
			// Load form data
			formData = {
				name: editingOwner.name,
				email: editingOwner.email || '',
				phone: editingOwner.phone || '',
				address: editingOwner.address || ''
			};
			ownerCanLogin = editingOwner.canLogin || false;
			loginInfo = null;
			
			// Load cleaners
			cleaners = await cleanersAPI.getAll();
			
			// Load houses with schedules
			ownerHousesWithSchedules = [];
			expandedHouseIds = new Set();
			await loadOwnerHousesWithSchedules(urlOwnerId);
		} catch (err: any) {
			console.error('Error loading owner:', err);
		} finally {
			loading = false;
		}
	}

	function handleFormCancel() {
		goto('/owners');  // Go back to list
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		try {
			if (editingOwner) {
				await ownersAPI.update(editingOwner.id, formData);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.updated_successfully'));
			}
		} catch (err: any) {
			notificationActions.error(err.message || t('errors.failed_to_save'));
		}
	}

	async function handleDelete() {
		if (!editingOwner) return;
		if (confirm(t('owners.delete_confirm').replace('"${owner.name}"', `"${editingOwner.name}"`))) {
			try {
				await ownersAPI.delete(editingOwner.id);
				notificationActions.success(t('owners.title') + ' ' + t('notifications.deleted_successfully'));
				goto('/owners');
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
		return toDisplayDateFormat(dateStr, $currentLocale);
	}

	let loaded = $state(false);
	$effect(() => {
		if (!loaded && urlOwnerId > 0) {
			loaded = true;
			loadData();
		}
	});
</script>

<div class="owners-page">
	<div class="page-header">
		<button class="btn btn-secondary" onclick={() => goto('/owners')}>{tt('common.back')}</button>
		<h1>{editingOwner?.name || tt('owners.edit_owner')}</h1>
		<button class="btn btn-danger btn-sm" onclick={handleDelete}>{tt('common.delete')}</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			{tt('common.loading')}
		</div>
	{/if}

	{#if showForm && editingOwner}
		<div class="form-section" bind:this={formSection}>
			<h3 class="form-title">{tt('owners.edit_owner')}</h3>
			
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

				<!-- Can Login Toggle -->
				<div class="login-toggle-section">
					<div class="toggle-row">
						<label class="toggle-label" for="login-toggle">
							<span>{tt('owners.can_login') || 'Can log in'}</span>
							<span class="toggle-hint">{tt('owners.login_hint') || 'Allow owner to login with email'}</span>
						</label>
						<button
							id="login-toggle"
							type="button"
							class="toggle-switch"
							class:active={ownerCanLogin}
							onclick={toggleOwnerLogin}
							disabled={loginToggleLoading}
							title={ownerCanLogin ? (tt('owners.disable_login') || 'Disable login') : (tt('owners.enable_login') || 'Enable login')}
							aria-label="Toggle owner login"
						></button>
					</div>
					{#if loginInfo}
						<div class="login-info">
							<strong>{tt('owners.login_credentials') || 'Login credentials:'}</strong>
							<p>Email: {loginInfo.username}</p>
							<p>{tt('owners.temp_password') || 'Temporary password:'} <code>{loginInfo.tempPassword}</code></p>
							<p class="info-note">{tt('owners.password_reset_note') || 'Owner must change password on first login'}</p>
						</div>
					{/if}
				</div>

				<div class="houses-schedules-section">
					<div class="section-header">
						<h4 class="section-title">{tt('owners.houses')} & {tt('schedules.title')}</h4>
						<button type="button" class="btn btn-primary btn-sm" onclick={openAddHouseModal}>
							{tt('houses.add_house') || 'Add House'}
						</button>
					</div>
					
					{#if ownerHousesWithSchedules.length === 0}
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
																		<td>{getCleanerName(schedule.cleaner_id)}</td>
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

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{tt('common.update')} Owner
					</button>
				</div>
			</form>
		</div>
	{/if}
</div>

{#if showAddHouseModal}
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="modal-overlay" onclick={() => showAddHouseModal = false}>
	<!-- svelte-ignore a11y_interactive_supports_focus -->
	<div class="modal-content" role="dialog" aria-modal="true" tabindex="-1" onclick={(e) => e.stopPropagation()}>
		<h3>{tt('houses.add_house') || 'Add House'}</h3>
		<form onsubmit={handleAddHouseSubmit}>
			<div class="form-grid">
				<div class="form-field full-width">
					<label for="house-name">{tt('common.name')} <span class="required">*</span></label>
					<input type="text" id="house-name" bind:value={houseFormData.name} required />
				</div>
				<div class="form-field full-width">
					<label for="house-address">{tt('common.address')}</label>
					<input type="text" id="house-address" bind:value={houseFormData.address} />
				</div>
				<div class="form-field full-width">
					<label for="house-desc">{tt('common.description')}</label>
					<input type="text" id="house-desc" bind:value={houseFormData.description} />
				</div>
				<div class="form-field">
					<label for="house-checkin">{tt('houses.check_in_time') || 'Check-in'}</label>
					<input type="time" id="house-checkin" bind:value={houseFormData.check_in_time} />
				</div>
				<div class="form-field">
					<label for="house-checkout">{tt('houses.check_out_time') || 'Check-out'}</label>
					<input type="time" id="house-checkout" bind:value={houseFormData.check_out_time} />
				</div>
				<div class="form-field">
					<label for="house-rooms">{tt('houses.bedrooms') || 'Bedrooms'}</label>
					<input type="number" id="house-rooms" bind:value={houseFormData.bedrooms} min="0" />
				</div>
				<div class="form-field">
					<label for="house-baths">{tt('houses.bathrooms') || 'Bathrooms'}</label>
					<input type="number" id="house-baths" bind:value={houseFormData.bathrooms} min="0" />
				</div>
			</div>
			<div class="form-actions">
				<button type="button" class="btn btn-secondary" onclick={() => showAddHouseModal = false}>{tt('common.cancel')}</button>
				<button type="submit" class="btn btn-primary">{tt('common.add')}</button>
			</div>
		</form>
	</div>
</div>
{/if}

<style>
	.owners-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
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

	/* Login Toggle */
	.login-toggle-section { margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e5e7eb; }
	.toggle-row { display: flex; justify-content: space-between; align-items: center; }
	.toggle-label { display: flex; flex-direction: column; }
	.toggle-label span:first-child { font-weight: 600; color: #111827; }
	.toggle-hint { font-size: 0.75rem; color: #6b7280; }
	.toggle-switch {
		position: relative; width: 48px; height: 24px; border-radius: 12px; border: none; cursor: pointer;
		background: #fca5a5; transition: background 0.2s ease; padding: 0;
	}
	.toggle-switch::after {
		content: ''; position: absolute; top: 2px; left: 2px; width: 20px; height: 20px;
		border-radius: 50%; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.2); transition: transform 0.2s ease;
	}
	.toggle-switch.active { background: #10b981; }
	.toggle-switch.active::after { transform: translateX(24px); }
	.toggle-switch:disabled { opacity: 0.5; cursor: not-allowed; }
	.login-info { margin-top: 1rem; padding: 1rem; background: #f0fdf4; border: 1px solid #86efac; border-radius: 6px; }
	.login-info code { background: #fef3c7; padding: 0.125rem 0.375rem; border-radius: 4px; font-family: monospace; }
	.info-note { font-size: 0.75rem; color: #6b7280; margin-top: 0.5rem; }

	/* Houses & Schedules */
	.houses-schedules-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e5e7eb; }
	.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
	.section-title { font-size: 1rem; font-weight: 600; margin: 0; color: #374151; }

	/* Modal */
	.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
	.modal-content { background: white; border-radius: 8px; padding: 1.5rem; max-width: 500px; width: 90%; max-height: 90vh; overflow-y: auto; }
	.modal-content h3 { margin: 0 0 1rem 0; font-size: 1.125rem; font-weight: 600; }

	/* House Card */
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
	.no-houses-message { color: #9ca3af; padding: 1rem; background: #f9fafb; border-radius: 6px; text-align: center; }

	/* Booking Card */
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