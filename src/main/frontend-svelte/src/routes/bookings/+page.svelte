<script lang="ts">
	import { bookingsAPI, housesAPI, ownersAPI, type Booking, type House, type Owner } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import Table from '$lib/components/Table.svelte';
	import Form from '$lib/components/Form.svelte';

	// Svelte 5: Use $state for reactive variables
	let bookings = $state<Booking[]>([]);
	let houses = $state<House[]>([]);
	let owners = $state<Owner[]>([]);
	let selectedOwnerId = $state<number | null>(null);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingBooking = $state<Booking | null>(null);

	const bookingFields = [
		{
			name: 'owner_id',
			label: 'Owner',
			type: 'select',
			required: true,
			options: []
		},
		{
			name: 'house_id',
			label: 'House',
			type: 'select',
			required: true,
			options: []
		},
		{
			name: 'check_in_date',
			label: 'Check-in Date',
			type: 'date',
			required: true
		},
		{
			name: 'check_out_date',
			label: 'Check-out Date',
			type: 'date',
			required: true
		},
		{
			name: 'check_in_time',
			label: 'Check-in Time (24h)',
			type: 'time',
			required: true,
			step: 900
		},
		{
			name: 'check_out_time',
			label: 'Check-out Time (24h)',
			type: 'time',
			required: true,
			step: 900
		},
		{
			name: 'guest_name',
			label: 'Guest Name',
			type: 'text',
			required: true,
			placeholder: 'Enter guest name'
		},
		{
			name: 'guest_email',
			label: 'Guest Email',
			type: 'email',
			required: true,
			placeholder: 'Enter guest email'
		},
		{
			name: 'guest_phone',
			label: 'Guest Phone',
			type: 'tel',
			required: false,
			placeholder: 'Enter guest phone number'
		},
		{
			name: 'dogs_count',
			label: 'Number of Dogs',
			type: 'number',
			required: false,
			min: 0,
			step: 1,
			placeholder: '0'
		},
		{
			name: 'status',
			label: 'Status',
			type: 'select',
			required: true,
			options: [
				{ value: 'pending', label: 'Pending' },
				{ value: 'confirmed', label: 'Confirmed' },
				{ value: 'checked_in', label: 'Checked In' },
				{ value: 'checked_out', label: 'Checked Out' },
				{ value: 'cancelled', label: 'Cancelled' }
			]
		}
	];

	const tableColumns = [
		{ key: 'house_name', label: 'House' },
		{ key: 'check_in_date', label: 'Check-in' },
		{ key: 'check_out_date', label: 'Check-out' },
		{ key: 'guest_name', label: 'Guest' },
		{ key: 'guest_email', label: 'Email' },
		{
			key: 'status',
			label: 'Status',
			formatter: (value) => value.charAt(0).toUpperCase() + value.slice(1)
		}
	];

	const tableActions = [
		{
			label: 'Edit',
			class: 'edit',
			title: 'Edit booking',
			icon: '✏️'
		},
		{
			label: 'Delete',
			class: 'delete',
			title: 'Delete booking',
			icon: '🗑️'
		}
	];

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
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function handleAction({ action, row }) {
		if (action.label === 'Edit') {
			editingBooking = row;
			showForm = true;
		} else if (action.label === 'Delete') {
			if (confirm('Are you sure you want to delete this booking?')) {
				try {
					await bookingsAPI.delete(row.id);
					await loadData();
				} catch (err) {
					error = err.message;
				}
			}
		}
	}

	async function handleFormSubmit(data) {
		try {
			if (editingBooking) {
				await bookingsAPI.update(editingBooking.id, data);
			} else {
				await bookingsAPI.create(data);
			}

			showForm = false;
			editingBooking = null;
			await loadData();
		} catch (err) {
			error = err.message;
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
		<h1>Bookings</h1>
		<button class="btn btn-primary" onclick={() => (showForm = true)}> Add Booking </button>
	</div>

	{#if showForm}
		<div class="form-section">
			<Form
				fields={bookingFields}
				data={editingBooking || {}}
				{loading}
				title={editingBooking ? 'Edit Booking' : 'Add New Booking'}
				submitLabel={editingBooking ? 'Update Booking' : 'Add Booking'}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
			/>
		</div>
	{/if}

	<div class="table-section">
		<Table
			data={bookings}
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
