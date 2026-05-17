<script lang="ts">
	import { bookingsAPI, housesAPI, ownersAPI, type Booking, type House, type Owner } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { createReactiveTranslator, currentLocale } from '$lib/i18n';
	import { session } from '$lib/state/session.svelte';
	import { toInputDateFormat, toBackendDateFormat, toDisplayDateFormat } from '$lib/utils/Utils';
	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';

	const { tt } = createReactiveTranslator();

	let isAdmin = $derived(session.isAdmin === true);

	let bookings = $state<Booking[]>([]);
	let houses = $state<House[]>([]);
	let owners = $state<Owner[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingBooking = $state<Booking | null>(null);
	let viewMode = $state<'card' | 'table'>('card');
	let locale = $derived(get(currentLocale));

	let userHouseIds = $derived(houses.filter(h => h.ownerOid === session.ownerOid).map(h => h.oid));
	let filteredBookings = $derived(
		isAdmin ? bookings : bookings.filter(b => userHouseIds.includes(b.houseOid))
	);

	// Table sorting
	let sortBy = $state<'guest_name' | 'house' | 'check_in_date' | 'check_out_date' | 'guest_email' | 'status' | ''>('');
	let sortAsc = $state(true);

	let sortedBookings = $derived.by(() => {
		if (!sortBy) return [...filteredBookings];
		
		return [...filteredBookings].sort((a, b) => {
			let valueA: any = a[sortBy];
			let valueB: any = b[sortBy];
			
			// Handle date fields specially
			if (sortBy.endsWith('_date')) {
				valueA = new Date(valueA).getTime();
				valueB = new Date(valueB).getTime();
			}
			
			if (valueA < valueB) return sortAsc ? -1 : 1;
			if (valueA > valueB) return sortAsc ? 1 : -1;
			return 0;
		});
	});

	let formData = $state({
		ownerOid: 0,
		houseOid: 0,
		check_in_date: '',
		check_out_date: '',
		check_in_time: '',
		check_out_time: '',
		guest_name: '',
		guest_email: '',
		guest_phone: '',
		dogs_count: 0,
		status: 'pending'
	});

	async function loadData() {
		loading = true;
		error = null;
		try {
			const [bookingsResult, housesResult, ownersResult] = await Promise.all([
				bookingsAPI.getAll(),
				housesAPI.getAll(),
				ownersAPI.getAll()
			]);
			bookings = bookingsResult;
			houses = housesResult;
			owners = ownersResult;
		} catch (err: any) {
			error = err.message || tt('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	function openAddForm() {
		editingBooking = null;
		formData = {
			ownerOid: 0,
			houseOid: 0,
			check_in_date: '',
			check_out_date: '',
			check_in_time: '',
			check_out_time: '',
			guest_name: '',
			guest_email: '',
			guest_phone: '',
			dogs_count: 0,
			status: 'pending'
		};
		showForm = true;
	}

	function openEditForm(booking: Booking) {
		editingBooking = booking;
		formData = {
			ownerOid: 0,
			houseOid: booking.houseOid,
			check_in_date: toInputDateFormat(booking.check_in_date),
			check_out_date: toInputDateFormat(booking.check_out_date),
			check_in_time: '',
			check_out_time: '',
			guest_name: booking.guest_name,
			guest_email: booking.guest_email,
			guest_phone: booking.guest_phone || '',
			dogs_count: booking.dogs_count,
			status: booking.status
		};
		showForm = true;
	}

	function handleFormCancel() {
		showForm = false;
		editingBooking = null;
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		const dataToSend = {
			houseOid: formData.houseOid,
			check_in_date: toBackendDateFormat(formData.check_in_date),
			check_out_date: toBackendDateFormat(formData.check_out_date),
			guest_name: formData.guest_name,
			guest_email: formData.guest_email,
			guest_phone: formData.guest_phone,
			dogs_count: formData.dogs_count,
			status: formData.status
		};
		try {
			if (editingBooking) {
				await bookingsAPI.update(editingBooking.oid, dataToSend);
				notificationActions.success(tt('bookings.updated'));
			} else {
				await bookingsAPI.create(dataToSend);
				notificationActions.success(tt('bookings.created'));
			}
			showForm = false;
			editingBooking = null;
			await loadData();
		} catch (err: any) {
			notificationActions.error(err.message || tt('errors.failed_to_save'));
		}
	}

	async function handleDelete(booking: Booking) {
		if (confirm(tt('bookings.delete_confirm').replace('${name}', booking.guest_name))) {
			try {
				await bookingsAPI.delete(booking.oid);
				notificationActions.success(tt('bookings.deleted'));
				await loadData();
			} catch (err: any) {
				notificationActions.error(err.message || tt('errors.failed_to_delete'));
			}
		}
	}

	function getHouseName(houseId: number): string {
		if (!houseId || houseId === 0) return tt('houses.unknown');
		const house = houses.find(h => h.oid === houseId);
		return house ? house.name : `#${houseId}`;
	}

	$effect(() => {
		loadData();
	});
</script>

<div class="bookings-page">
	<div class="page-header">
		<h1>{tt('bookings.title')}</h1>
		<button class="btn btn-primary" onclick={openAddForm}>{tt('bookings.add_booking')}</button>
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
			<h3 class="form-title">{editingBooking ? tt('bookings.edit_booking') : tt('bookings.add_new_booking')}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field">
						<label for="guest_name">{tt('bookings.guest_name')} <span class="required">*</span></label>
						<input type="text" id="guest_name" bind:value={formData.guest_name} required />
					</div>
					<div class="form-field">
						<label for="guest_email">{tt('bookings.guest_email')} <span class="required">*</span></label>
						<input type="email" id="guest_email" bind:value={formData.guest_email} required />
					</div>
					<div class="form-field">
						<label for="guest_phone">{tt('common.phone')}</label>
						<input type="tel" id="guest_phone" bind:value={formData.guest_phone} />
					</div>
					<div class="form-field">
						<label for="houseOid">{tt('common.house')} <span class="required">*</span></label>
						<select id="houseOid" bind:value={formData.houseOid} required>
							<option value={0}>-- {tt('houses.select_house')} --</option>
							{#each houses as house}
								<option value={house.oid}>{house.name}</option>
							{/each}
						</select>
					</div>
					<div class="form-field">
						<label for="check_in_date">{tt('bookings.check_in_date')} <span class="required">*</span></label>
						<input type="date" id="check_in_date" bind:value={formData.check_in_date} required />
					</div>
					<div class="form-field">
						<label for="check_out_date">{tt('bookings.check_out_date')} <span class="required">*</span></label>
						<input type="date" id="check_out_date" bind:value={formData.check_out_date} required />
					</div>
					<div class="form-field">
						<label for="dogs_count">{tt('bookings.dogs_count')}</label>
						<input type="number" id="dogs_count" bind:value={formData.dogs_count} min="0" max="10" />
					</div>
					<div class="form-field">
						<label for="status">{tt('common.status')}</label>
						<select id="status" bind:value={formData.status}>
							<option value="pending">{tt('status.pending')}</option>
							<option value="confirmed">{tt('status.confirmed')}</option>
							<option value="cancelled">{tt('status.cancelled')}</option>
						</select>
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{editingBooking ? tt('common.update') : tt('common.add')} {tt('bookings.title')}
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
	<div class="bookings-grid">
		{#if filteredBookings.length === 0 && !loading}
			<div class="empty-message">{tt('bookings.no_bookings')}</div>
		{:else}
			{#each filteredBookings as booking}
				<!-- svelte-ignore a11y_click_events_have_key_events -->
				<!-- svelte-ignore a11y_no_static_element_interactions -->
				<div class="booking-card clickable" onclick={() => goto(`/bookings/${booking.oid}`)} onkeydown={(e) => e.key === 'Enter' && goto(`/bookings/${booking.oid}`)}>
					<div class="card-header">
						<h3 class="booking-guest">{booking.guest_name}</h3>
						<span class="status-badge status-{booking.status}">{booking.status}</span>
					</div>
					<p class="booking-dates">{toDisplayDateFormat(booking.check_in_date, locale)} → {toDisplayDateFormat(booking.check_out_date, locale)}</p>
					<p class="booking-house">{tt('common.house')}: {getHouseName(booking.houseOid)}</p>
					{#if booking.guest_email}<p class="booking-detail">{booking.guest_email}</p>{/if}
					<div class="booking-actions">
						<button class="btn btn-secondary btn-sm" onclick={(e) => { e.stopPropagation(); goto(`/bookings/${booking.oid}`); }}>{tt('common.edit')}</button>
						<button class="btn btn-danger btn-sm" onclick={(e) => { e.stopPropagation(); handleDelete(booking); }}>{tt('common.delete')}</button>
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
					<th 
						onclick={() => {
							if (sortBy === 'guest_name') {
								sortAsc = !sortAsc;
							} else {
								sortBy = 'guest_name';
								sortAsc = true;
							}
						}}
						class:active={sortBy === 'guest_name'}
						class:asc={sortBy === 'guest_name' && sortAsc}
						class:desc={sortBy === 'guest_name' && !sortAsc}
						class:sortable
					>
						{tt('bookings.guest_name')}
						{#if sortBy === 'guest_name'}
							<span class="sort-indicator">{sortAsc ? '↑' : '↓'}</span>
						{/if}
					</th>
					<th 
						onclick={() => {
							if (sortBy === 'house') {
								sortAsc = !sortAsc;
							} else {
								sortBy = 'house';
								sortAsc = true;
							}
						}}
						class:active={sortBy === 'house'}
						class:asc={sortBy === 'house' && sortAsc}
						class:desc={sortBy === 'house' && !sortAsc}
						class:sortable
					>
						{tt('common.house')}
						{#if sortBy === 'house'}
							<span class="sort-indicator">{sortAsc ? '↑' : '↓'}</span>
						{/if}
					</th>
					<th 
						onclick={() => {
							if (sortBy === 'check_in_date') {
								sortAsc = !sortAsc;
							} else {
								sortBy = 'check_in_date';
								sortAsc = true;
							}
						}}
						class:active={sortBy === 'check_in_date'}
						class:asc={sortBy === 'check_in_date' && sortAsc}
						class:desc={sortBy === 'check_in_date' && !sortAsc}
						class:sortable
					>
						{tt('bookings.check_in_date')}
						{#if sortBy === 'check_in_date'}
							<span class="sort-indicator">{sortAsc ? '↑' : '↓'}</span>
						{/if}
					</th>
					<th 
						onclick={() => {
							if (sortBy === 'check_out_date') {
								sortAsc = !sortAsc;
							} else {
								sortBy = 'check_out_date';
								sortAsc = true;
							}
						}}
						class:active={sortBy === 'check_out_date'}
						class:asc={sortBy === 'check_out_date' && sortAsc}
						class:desc={sortBy === 'check_out_date' && !sortAsc}
						class:sortable
					>
						{tt('bookings.check_out_date')}
						{#if sortBy === 'check_out_date'}
							<span class="sort-indicator">{sortAsc ? '↑' : '↓'}</span>
						{/if}
					</th>
					<th 
						onclick={() => {
							if (sortBy === 'guest_email') {
								sortAsc = !sortAsc;
							} else {
								sortBy = 'guest_email';
								sortAsc = true;
							}
						}}
						class:active={sortBy === 'guest_email'}
						class:asc={sortBy === 'guest_email' && sortAsc}
						class:desc={sortBy === 'guest_email' && !sortAsc}
						class:sortable
					>
						{tt('bookings.guest_email')}
						{#if sortBy === 'guest_email'}
							<span class="sort-indicator">{sortAsc ? '↑' : '↓'}</span>
						{/if}
					</th>
					<th 
						onclick={() => {
							if (sortBy === 'status') {
								sortAsc = !sortAsc;
							} else {
								sortBy = 'status';
								sortAsc = true;
							}
						}}
						class:active={sortBy === 'status'}
						class:asc={sortBy === 'status' && sortAsc}
						class:desc={sortBy === 'status' && !sortAsc}
						class:sortable
					>
						{tt('common.status')}
						{#if sortBy === 'status'}
							<span class="sort-indicator">{sortAsc ? '↑' : '↓'}</span>
						{/if}
					</th>
					<th>{tt('common.actions')}</th>
				</tr>
			</thead>
			<tbody>
				{#each sortedBookings as booking}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<tr class="clickable" onclick={() => goto(`/bookings/${booking.oid}`)} onkeydown={(e) => e.key === 'Enter' && goto(`/bookings/${booking.oid}`)}>
						<td>{booking.guest_name}</td>
						<td>{getHouseName(booking.houseOid)}</td>
						<td>{toDisplayDateFormat(booking.check_in_date, $currentLocale)}</td>
						<td>{toDisplayDateFormat(booking.check_out_date, $currentLocale)}</td>
						<td>{booking.guest_email}</td>
						<td><span class="status-badge status-{booking.status}">{booking.status}</span></td>
						<td>
							<button class="btn btn-sm btn-secondary" onclick={(e) => { e.stopPropagation(); goto(`/bookings/${booking.oid}`); }}>{tt('common.edit')}</button>
							<button class="btn btn-sm btn-danger" onclick={(e) => { e.stopPropagation(); handleDelete(booking); }}>{tt('common.delete')}</button>
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
	.bookings-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
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
	.form-field input, .form-field select { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.875rem; font-family: inherit; }
	.form-field input:focus, .form-field select:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.2); }
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
	.bookings-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.5rem; }
	.booking-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); transition: box-shadow 0.2s, border-color 0.2s; }
	.clickable { cursor: pointer; }
	.clickable:hover { border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
	.data-table tr.clickable:hover { background: #eff6ff; }
	.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
	.booking-guest { margin: 0; font-size: 1.125rem; font-weight: 600; color: #111827; }
	.booking-dates { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.booking-house { margin: 0.25rem 0 0 0; color: #3b82f6; font-size: 0.875rem; font-weight: 500; }
	.booking-detail { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.booking-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
	.status-badge { display: inline-block; padding: 0.125rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; text-transform: capitalize; }
	.status-pending { background: #fef3c7; color: #92400e; }
	.status-confirmed { background: #d1fae5; color: #065f46; }
	.status-cancelled { background: #fee2e2; color: #991b1b; }

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
.data-table th {
	background: #f9fafb;
	font-weight: 600;
	color: #374151;
	cursor: pointer;
	user-select: none;
	position: relative;
	padding-right: 1.5rem;
}

.data-table th:hover {
	background: #f3f4f6;
}

.sort-indicator {
	font-size: 0.75rem;
	margin-left: 0.25rem;
	opacity: 0.7;
}

.data-table th.asc .sort-indicator::before {
	content: "▲";
}

.data-table th.desc .sort-indicator::before {
	content: "▼";
}
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
