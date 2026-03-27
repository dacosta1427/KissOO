<script lang="ts">
	import { bookingsAPI, housesAPI, ownersAPI, type Booking, type House, type Owner } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import { session } from '$lib/state/session.svelte';
	import Table from '$lib/components/Table.svelte';
	import Form from '$lib/components/Form.svelte';
  import { t, currentLocale } from '$lib/i18n';
  
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
	
	// View toggle: 'card' or 'table'
	let viewMode = $state<'card' | 'table'>('table');
	
	// Get user's house IDs for filtering
	let userHouseIds = $derived(houses.filter(h => h.owner_id === session.ownerId).map(h => h.id));
	
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
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingBooking = null;
	}

	// Svelte 5: Use $effect for lifecycle management
	$effect(() => {
		loadData();
	});
</script>

<div class="bookings-page">
	<div class="page-header">
		<h1>{tt('bookings.title')}</h1>
		<button class="btn btn-primary" onclick={() => (showForm = true)}> {tt('bookings.add_booking')} </button>
	</div>

	{#if showForm}
		<div class="form-section">
			<Form
				fields={bookingFields}
				data={editingBooking || {}}
				{loading}
				title={editingBooking ? t('bookings.edit_booking') : t('bookings.add_new_booking')}
				submitLabel={editingBooking ? t('bookings.edit_booking') : t('bookings.add_booking')}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
			/>
		</div>
	{/if}

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
