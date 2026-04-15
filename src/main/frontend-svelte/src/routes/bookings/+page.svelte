<script lang="ts">
	import { bookingsAPI, housesAPI, ownersAPI, type Booking, type House, type Owner } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import { session } from '$lib/state/session.svelte';
	import Table from '$lib/components/Table.svelte';
	import Form from '$lib/components/Form.svelte';
	import Button from '$lib/components/Button.svelte';
  import { t, currentLocale } from '$lib/i18n';
  import { toInputDateFormat, toBackendDateFormat, toDisplayDateFormat } from '$lib/utils/Utils';
  import { page } from '$app/stores';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Check if user is admin
  let isAdmin = $derived(session.username === 'admin' || session.username === 'administrator');

  // Svelte 5: Use $state for reactive variables
	let bookings = $state<Booking[]>([]);
	let houses = $state<House[]>([]);
	let owners = $state<Owner[]>([]);
	let selectedOwnerId = $state<number | null>(null);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingBooking = $state<Booking | null>(null);
	let saving = $state(false);
	
	// View toggle: 'card' or 'table'
	let viewMode = $state<'card' | 'table'>('table');

	// Form section ref for scroll on small screens
	let formSection = $state<HTMLElement | null>(null);

	function scrollToEditForm() {
		// Scroll to edit form on small screens (mobile/tablet)
		if (window.innerWidth < 1024 && formSection) {
			formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}
	
	// Get user's house IDs for filtering
	let userHouseIds = $derived(houses.filter(h => h.owner_id === session.ownerOid).map(h => h.id));
	
	// Filtered bookings based on user role
	let filteredBookings = $derived(
		isAdmin ? bookings : bookings.filter(b => userHouseIds.includes(b.house_id))
	);

	// Reactive form fields using $derived
	let bookingFields = $derived([
		{
			name: 'owner_id',
			label: t('houses.owner'),
			type: 'select',
			required: true,
			options: []
		},
		{
			name: 'house_id',
			label: t('bookings.house'),
			type: 'select',
			required: true,
			options: []
		},
		{
			name: 'check_in_date',
			label: t('bookings.check_in_date'),
			type: 'date',
			required: true
		},
		{
			name: 'check_out_date',
			label: t('bookings.check_out_date'),
			type: 'date',
			required: true
		},
		{
			name: 'check_in_time',
			label: t('bookings.check_in_time') + ' (24h)',
			type: 'time',
			required: true,
			step: 900
		},
		{
			name: 'check_out_time',
			label: t('bookings.check_out_time') + ' (24h)',
			type: 'time',
			required: true,
			step: 900
		},
		{
			name: 'guest_name',
			label: t('bookings.guest_name'),
			type: 'text',
			required: true,
			placeholder: t('bookings.enter_guest_name')
		},
		{
			name: 'guest_email',
			label: t('bookings.guest_email'),
			type: 'email',
			required: true,
			placeholder: t('bookings.enter_guest_email')
		},
		{
			name: 'guest_phone',
			label: t('bookings.guest_phone'),
			type: 'tel',
			required: false,
			placeholder: t('bookings.enter_guest_phone')
		},
		{
			name: 'dogs_count',
			label: t('bookings.number_of_dogs'),
			type: 'number',
			required: false,
			min: 0,
			step: 1,
			placeholder: '0'
		},
		{
			name: 'status',
			label: t('common.status'),
			type: 'select',
			required: true,
			options: [
				{ value: 'pending', label: t('bookings.pending') },
				{ value: 'confirmed', label: t('bookings.confirmed') },
				{ value: 'checked_in', label: t('bookings.checked_in') },
				{ value: 'checked_out', label: t('bookings.checked_out') },
				{ value: 'cancelled', label: t('bookings.cancelled') }
			]
		}
	]);

	let tableColumns = $derived([
		{ key: 'house_name', label: t('bookings.house') },
		{ key: 'check_in_date', label: t('bookings.check_in_date') },
		{ key: 'check_out_date', label: t('bookings.check_out_date') },
		{ key: 'guest_name', label: t('bookings.guest_name') },
		{ key: 'guest_email', label: t('common.email') },
		{
			key: 'status',
			label: t('common.status'),
			formatter: (value: string) => value.charAt(0).toUpperCase() + value.slice(1)
		}
	]);

	let tableActions = $derived([
		{
			label: t('common.edit'),
			class: 'edit',
			title: t('bookings.edit_booking'),
			icon: '✏️'
		},
		{
			label: t('common.delete'),
			class: 'delete',
			title: t('bookings.delete_booking'),
			icon: '🗑️'
		}
	]);

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

			// Update form options
			bookingFields[0].options = owners.map((o) => ({ value: o.id, label: o.name }));
			// House options will be updated reactively based on selected owner

			dataStores.bookings.set(bookings);
			dataStores.houses.set(houses);
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	async function handleAction({ action, row }: { action: any; row: any }) {
		if (action.label === t('common.edit')) {
			editingBooking = row;
			showForm = true;
			scrollToEditForm();
		} else if (action.label === t('common.delete')) {
			if (confirm(t('bookings.delete_confirm'))) {
				try {
					await bookingsAPI.delete(row.id);
					await loadData();
				} catch (err: any) {
					error = err.message || t('errors.failed_to_delete');
				}
			}
		}
	}

	async function handleFormSubmit(data: any) {
		if (saving) return;
		saving = true;
		try {
			if (editingBooking) {
				await bookingsAPI.update(editingBooking.id, data);
			} else {
				await bookingsAPI.create(data);
			}

			showForm = false;
			editingBooking = null;
			await loadData();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_save');
		} finally {
			saving = false;
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingBooking = null;
	}

	// Svelte 5: Use $effect for lifecycle management
	$effect(() => {
		loadData();
		
		// Check for newBooking URL param - pre-select house and open form
		const urlParams = new URLSearchParams(window.location.search);
		const newBooking = urlParams.get('newBooking');
		const houseIdParam = urlParams.get('houseId');
		
		if (newBooking === 'true' && houseIdParam) {
			const houseId = parseInt(houseIdParam);
			// Find the house and pre-select it
			const house = houses.find(h => h.id === houseId);
			if (house) {
				// Find owner of this house and select it
				const ownerId = house.owner;
				if (ownerId) {
					selectedOwnerId = ownerId;
					// Update house options for this owner
					const ownerHouses = houses.filter(h => h.owner === ownerId);
					bookingFields[1].options = ownerHouses.map(h => ({ value: h.id, label: h.name }));
					// Pre-fill form with house
					setTimeout(() => {
						showForm = true;
					}, 100);
				}
			}
		}
	});
</script>

<div class="bookings-page">
	<div class="page-header">
		<h1>{tt('bookings.title')}</h1>
		<button class="btn btn-primary" onclick={() => (showForm = true)}> {tt('bookings.add_booking')} </button>
	</div>

	{#if showForm}
		<div class="form-section" bind:this={formSection}>
			<Form
				fields={bookingFields}
				data={editingBooking || {}}
				loading={saving}
				title={editingBooking ? t('bookings.edit_booking') : t('bookings.add_new_booking')}
				submitLabel={editingBooking ? t('bookings.edit_booking') : t('bookings.add_booking')}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
			/>
		</div>
	{/if}

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
		<!-- Card View -->
		<div class="bookings-grid">
			{#if filteredBookings.length === 0 && !loading}
				<div class="empty-message">{tt('bookings.no_bookings')}</div>
			{:else}
				{#each filteredBookings as booking}
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div class="booking-card clickable" onclick={() => { editingBooking = booking; showForm = true; scrollToEditForm(); }} onkeydown={(e) => e.key === 'Enter' && (editingBooking = booking, showForm = true, scrollToEditForm())}>
						<h3 class="booking-guest">{booking.guest_name}</h3>
						<p class="booking-dates">{toDisplayDateFormat(booking.check_in_date)} → {toDisplayDateFormat(booking.check_out_date)}</p>
						<p class="booking-house">House: {houses.find(h => h.id === booking.house_id)?.name || booking.house_id}</p>
						{#if booking.guest_email}<p class="booking-detail">{booking.guest_email}</p>{/if}
						<span class="status-badge status-{booking.status}">{booking.status}</span>
						<div class="booking-actions">
							<button class="btn btn-secondary btn-sm" onclick={(e) => { e.stopPropagation(); editingBooking = booking; showForm = true; scrollToEditForm(); }}>
								{tt('common.edit')}
							</button>
							<button class="btn btn-danger btn-sm" onclick={(e) => { e.stopPropagation(); if (confirm(t('bookings.delete_confirm'))) { bookingsAPI.delete(booking.id).then(() => loadData()); } }}>
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
				data={filteredBookings}
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
	.bookings-page {
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

	/* View Toggle */
	.view-toggle { display: flex; gap: 0.5rem; }
	.toggle-btn { padding: 0.5rem 1rem; border: 1px solid #e5e7eb; background: white; cursor: pointer; font-size: 0.875rem; }
	.toggle-btn:first-child { border-radius: 6px 0 0 6px; }
	.toggle-btn:last-child { border-radius: 0 6px 6px 0; }
	.toggle-btn.active { background: #3b82f6; color: white; border-color: #3b82f6; }

	/* Card View */
	.bookings-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; }
	.booking-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
	.booking-guest { margin: 0 0 0.5rem 0; font-size: 1.125rem; font-weight: 600; }
	.booking-dates { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.booking-house { margin: 0.25rem 0; color: #374151; font-size: 0.875rem; }
	.booking-detail { margin: 0; color: #6b7280; font-size: 0.875rem; }
	.booking-actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
	.status-badge { display: inline-block; padding: 0.125rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; margin-top: 0.5rem; text-transform: capitalize; }
	.status-pending { background: #fef3c7; color: #92400e; }
	.status-confirmed { background: #d1fae5; color: #065f46; }
	.status-cancelled { background: #fee2e2; color: #991b1b; }
	.empty-message { text-align: center; color: #6b7280; padding: 2rem; grid-column: 1 / -1; }
	.btn-secondary { background: #6b7280; color: white; }
	.btn-danger { background: #ef4444; color: white; }
	.btn-sm { padding: 0.25rem 0.75rem; font-size: 0.75rem; }

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
	.clickable:hover { border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
</style>
