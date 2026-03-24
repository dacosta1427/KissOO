<script lang="ts">
	import { schedulesAPI, cleanersAPI, bookingsAPI, type Schedule, type Cleaner, type Booking } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import ScheduleBoard from '$lib/components/ScheduleBoard.svelte';
	import Form from '$lib/components/Form.svelte';

	// Svelte 5: Use $state for reactive variables
	let schedules = $state<Schedule[]>([]);
	let cleaners = $state<Cleaner[]>([]);
	let bookings = $state<Booking[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingSchedule = $state<Schedule | null>(null);
	let dateRange = $state({
		start: new Date().toISOString().split('T')[0],
		end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
	});

	let scheduleFields = $state([
		{
			name: 'cleaner_id',
			label: 'Cleaner',
			type: 'select',
			required: true,
			options: []
		},
		{
			name: 'booking_id',
			label: 'Booking',
			type: 'select',
			required: true,
			options: []
		},
		{
			name: 'assigned_date',
			label: 'Assigned Date',
			type: 'date',
			required: true
		},
		{
			name: 'status',
			label: 'Status',
			type: 'select',
			required: true,
			options: [
				{ value: 'pending', label: 'Pending' },
				{ value: 'confirmed', label: 'Confirmed' },
				{ value: 'completed', label: 'Completed' },
				{ value: 'cancelled', label: 'Cancelled' }
			]
		}
	]);

	async function loadData() {
		loading = true;
		error = null;

		try {
			// Load all data in parallel
			const [schedulesResult, cleanersResult, bookingsResult] = await Promise.all([
				schedulesAPI.getAll({ dateRange }),
				cleanersAPI.getAll(),
				bookingsAPI.getAll()
			]);

			schedules = schedulesResult;
			cleaners = cleanersResult;
			bookings = bookingsResult;

			console.log('[schedules] cleaners loaded:', cleaners.length, cleaners);
			console.log('[schedules] bookings loaded:', bookings.length);

			// Update form options
			scheduleFields[0].options = cleaners.map((c) => ({ value: c.id, label: c.name }));
			scheduleFields[1].options = bookings.map((b) => ({
				value: b.id,
				label: `House: ${b.house_name || 'Unknown'} - ${b.check_in_date} to ${b.check_out_date}`
			}));
			console.log('[scheduleFields] cleaner options:', scheduleFields[0].options);
			// Trigger reactivity by reassigning array
			scheduleFields = [...scheduleFields];

			dataStores.schedules.set(schedules);
			dataStores.cleaners.set(cleaners);
			dataStores.bookings.set(bookings);
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function handleScheduleChange(newSchedule) {
		try {
			if (editingSchedule) {
				await schedulesAPI.update(editingSchedule.id, newSchedule);
			} else {
				await schedulesAPI.create(newSchedule);
			}

			await loadData();
		} catch (err) {
			error = err.message;
		}
	}

	async function handleFormSubmit(data) {
		try {
			if (editingSchedule) {
				await schedulesAPI.update(editingSchedule.id, data);
			} else {
				await schedulesAPI.create(data);
			}

			showForm = false;
			editingSchedule = null;
			await loadData();
		} catch (err) {
			error = err.message;
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingSchedule = null;
	}

	function handleScheduleClick(schedule) {
		editingSchedule = schedule;
		showForm = true;
	}

	// Svelte 5: Use $effect for lifecycle management
	$effect(() => {
		loadData();
	});
</script>

<div class="schedules-page">
	<div class="page-header">
		<h1>Scheduling Board</h1>
		<div class="header-actions">
			<button class="btn btn-primary" onclick={() => (showForm = true)}> Add Schedule </button>
		</div>
	</div>

	<div class="date-range-controls">
		<div class="form-group">
			<label for="startDate">Start Date</label>
			<input type="date" id="startDate" bind:value={dateRange.start} onchange={loadData} />
		</div>
		<div class="form-group">
			<label for="endDate">End Date</label>
			<input type="date" id="endDate" bind:value={dateRange.end} onchange={loadData} />
		</div>
	</div>

	{#if cleaners.length === 0 && !loading}
		<div class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded mb-4">
			No cleaners found. Please add cleaners before creating schedules.
		</div>
	{/if}
	{#if error}
		<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
			{error}
		</div>
	{/if}

	{#if showForm}
		<div class="form-section">
			<Form
				fields={scheduleFields}
				data={editingSchedule || {}}
				{loading}
				title={editingSchedule ? 'Edit Schedule' : 'Add New Schedule'}
				submitLabel={editingSchedule ? 'Update Schedule' : 'Add Schedule'}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
			/>
		</div>
	{/if}

	<ScheduleBoard
		{schedules}
		{cleaners}
		{bookings}
		{dateRange}
		{loading}
		{error}
		onScheduleChange={handleScheduleChange}
		onScheduleClick={handleScheduleClick}
	/>
</div>

<style>
	.schedules-page {
		padding: 2rem;
		max-width: 1400px;
		margin: 0 auto;
	}

	.page-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
		border-bottom: 1px solid var(--border-color);
		padding-bottom: 1rem;
	}

	.page-header h1 {
		margin: 0;
		color: var(--text-color);
	}

	.header-actions {
		display: flex;
		gap: 1rem;
	}

	.date-range-controls {
		display: flex;
		gap: 2rem;
		margin-bottom: 2rem;
		padding: 1.5rem;
		background: var(--card-bg);
		border-radius: 8px;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.form-group label {
		font-weight: 600;
		color: var(--text-color);
		font-size: 0.9rem;
	}

	.form-group input {
		padding: 0.5rem;
		border: 1px solid var(--border-color);
		border-radius: 6px;
		font-size: 1rem;
	}

	.form-section {
		margin-bottom: 2rem;
		background: white;
		border-radius: 8px;
		padding: 1.5rem;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.header-actions {
			flex-direction: column;
			align-items: stretch;
		}

		.date-range-controls {
			flex-direction: column;
			align-items: stretch;
		}

		.form-section {
			padding: 1rem;
		}
	}
</style>
