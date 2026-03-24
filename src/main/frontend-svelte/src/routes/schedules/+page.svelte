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
	
	let selectedCleanerId = $state<number | null>(null);

	// Form data - using simple state
	let formData = $state<Record<string, string>>({});

	// Svelte 5: Use $derived for form options
	let cleanerOptions = $derived(cleaners.map((c) => ({ value: String(c.id), label: c.name })));
	let bookingOptions = $derived(bookings.map((b) => ({
		value: String(b.id),
		label: `${b.guest_name || 'Guest'} - ${b.check_in_date} to ${b.check_out_date}`,
		checkOutDate: b.check_out_date
	})));
	
	// Simple form fields (text inputs only - dropdowns handled separately)
	let formFields = $derived([
		{
			name: 'date',
			label: 'Work Date',
			type: 'date' as const,
			required: true
		},
		{
			name: 'start_time',
			label: 'Start Time',
			type: 'time' as const,
			required: true
		},
		{
			name: 'end_time',
			label: 'End Time',
			type: 'time' as const,
			required: true
		},
		{
			name: 'status',
			label: 'Status',
			type: 'select' as const,
			required: true,
			options: [
				{ value: 'pending', label: 'Pending' },
				{ value: 'confirmed', label: 'Confirmed' },
				{ value: 'completed', label: 'Completed' },
				{ value: 'cancelled', label: 'Cancelled' }
			]
		}
	]);

	// Computed: get selected cleaner name
	let selectedCleanerName = $derived(
		selectedCleanerId ? cleaners.find(c => c.id === selectedCleanerId)?.name || 'Unknown' : ''
	);

	// Handle booking selection - auto-fill date with check_out_date
	function handleBookingChange(bookingId: string) {
		formData.booking_id = bookingId;
		const booking = bookingOptions.find(b => b.value === bookingId);
		if (booking) {
			formData.date = booking.checkOutDate;
		}
	}

	async function loadData() {
		loading = true;
		error = null;

		try {
			const [schedulesResult, cleanersResult, bookingsResult] = await Promise.all([
				schedulesAPI.getAll({ dateRange }),
				cleanersAPI.getAll(),
				bookingsAPI.getAll()
			]);

			schedules = schedulesResult;
			cleaners = cleanersResult;
			bookings = bookingsResult;

			dataStores.schedules.set(schedules);
			dataStores.cleaners.set(cleaners);
			dataStores.bookings.set(bookings);
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function handleFormSubmit() {
		// Build the schedule data
		const scheduleData = {
			cleaner_id: parseInt(formData.cleaner_id),
			booking_id: parseInt(formData.booking_id),
			date: formData.date,
			start_time: formData.start_time,
			end_time: formData.end_time,
			status: formData.status
		};

		try {
			if (editingSchedule) {
				await schedulesAPI.update(editingSchedule.id, scheduleData);
			} else {
				await schedulesAPI.create(scheduleData);
			}

			showForm = false;
			editingSchedule = null;
			formData = {};
			await loadData();
		} catch (err) {
			error = err.message;
		}
	}

	function handleFormCancel() {
		showForm = false;
		editingSchedule = null;
		formData = {};
	}

	function openAddForm() {
		editingSchedule = null;
		formData = {
			cleaner_id: '',
			booking_id: '',
			date: '',
			start_time: '09:00',
			end_time: '12:00',
			status: 'pending'
		};
		showForm = true;
	}

	function handleScheduleClick(schedule: Schedule) {
		editingSchedule = schedule;
		formData = {
			cleaner_id: String(schedule.cleaner_id),
			booking_id: String(schedule.booking_id),
			date: schedule.date,
			start_time: schedule.start_time || '09:00',
			end_time: schedule.end_time || '12:00',
			status: schedule.status || 'pending'
		};
		showForm = true;
	}
	
	function handleCleanerClick(cleanerId: number) {
		if (selectedCleanerId === cleanerId) {
			selectedCleanerId = null;
		} else {
			selectedCleanerId = cleanerId;
		}
	}

	// Load data on mount
	$effect(() => {
		loadData();
	});
</script>

<div class="schedules-page">
	<div class="page-header">
		<h1>Scheduling Board</h1>
		<div class="header-actions">
			<button class="btn btn-primary" onclick={openAddForm}> Add Schedule </button>
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

	{#if loading}
		<div class="fixed top-4 right-4 z-50 bg-blue-600 text-white px-4 py-2 rounded shadow-lg">
			<span class="inline-block animate-spin mr-2">⏳</span>
			Loading...
		</div>
	{/if}

	{#if error}
		<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
			{error}
		</div>
	{/if}

	{#if showForm}
		<div class="form-section">
			<h3 class="text-lg font-medium mb-4">{editingSchedule ? 'Edit Schedule' : 'Add New Schedule'}</h3>
			
			<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
				<!-- Cleaner Dropdown - Native Select -->
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">
						Cleaner <span class="text-red-500">*</span>
					</label>
					<select
						bind:value={formData.cleaner_id}
						class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						required
					>
						<option value="">-- Select Cleaner --</option>
						{#each cleanerOptions as option}
							<option value={option.value}>{option.label}</option>
						{/each}
					</select>
				</div>

				<!-- Booking Dropdown - Native Select -->
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">
						Booking <span class="text-red-500">*</span>
					</label>
					<select
						value={formData.booking_id}
						onchange={(e) => handleBookingChange((e.target as HTMLSelectElement).value)}
						class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						required
					>
						<option value="">-- Select Booking --</option>
						{#each bookingOptions as option}
							<option value={option.value}>{option.label}</option>
						{/each}
					</select>
				</div>
			</div>

			<!-- Simple fields using Form component -->
			<Form
				fields={formFields}
				bind:data={formData}
				onSubmit={handleFormSubmit}
				onCancel={handleFormCancel}
				submitLabel={editingSchedule ? 'Update Schedule' : 'Add Schedule'}
				cancelLabel="Cancel"
			/>
		</div>
	{/if}

	{#if selectedCleanerName}
		<div class="mb-4 flex items-center gap-4">
			<span class="px-4 py-2 bg-blue-100 text-blue-800 rounded">
				Showing: <strong>{selectedCleanerName}</strong>
			</span>
			<button class="btn btn-secondary" onclick={() => selectedCleanerId = null}>
				Show All Cleaners
			</button>
		</div>
	{/if}

	<ScheduleBoard
		{schedules}
		{cleaners}
		{bookings}
		{dateRange}
		{loading}
		{error}
		onScheduleChange={async (newSchedule) => {
			try {
				if (editingSchedule) {
					await schedulesAPI.update(editingSchedule.id, newSchedule);
				}
				await loadData();
			} catch (err) {
				error = err.message;
			}
		}}
		onScheduleClick={handleScheduleClick}
		onCleanerClick={handleCleanerClick}
		{selectedCleanerId}
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
