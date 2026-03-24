<script lang="ts">
	import { schedulesAPI, cleanersAPI, bookingsAPI, type Schedule, type Cleaner, type Booking } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import ScheduleBoard from '$lib/components/ScheduleBoard.svelte';

	// Svelte 5: Use $state for reactive variables
	let schedules = $state<Schedule[]>([]);
	let cleaners = $state<Cleaner[]>([]);
	let bookings = $state<Booking[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingSchedule = $state<Schedule | null>(null);
	let viewMode = $state<'calendar' | 'table'>('calendar');
	let dateRange = $state({
		start: new Date().toISOString().split('T')[0],
		end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
	});
	
	let selectedCleanerId = $state<number | null>(null);

	// Form data
	let formData = $state({
		cleaner_id: '',
		booking_id: '',
		date: '',
		start_time: '09:00',
		end_time: '12:00',
		status: 'pending'
	});

	// Svelte 5: Use $derived for form options
	let cleanerOptions = $derived.by(() => {
		const opts = cleaners.map((c) => ({ value: String(c.id), label: c.name }));
		console.log('[DEBUG] cleanerOptions:', opts.length, opts);
		return opts;
	});
	let bookingOptions = $derived.by(() => {
		const opts = bookings.map((b) => ({
			value: String(b.id),
			label: `${b.guest_name || 'Guest'} - ${b.check_in_date} to ${b.check_out_date}`,
			checkOutDate: b.check_out_date
		}));
		console.log('[DEBUG] bookingOptions:', opts.length, opts);
		return opts;
	});

	// Status options
	const statusOptions = [
		{ value: 'pending', label: 'Pending' },
		{ value: 'confirmed', label: 'Confirmed' },
		{ value: 'completed', label: 'Completed' },
		{ value: 'cancelled', label: 'Cancelled' }
	];

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

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		
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
			resetForm();
			await loadData();
		} catch (err) {
			error = err.message;
		}
	}

	function resetForm() {
		formData = {
			cleaner_id: '',
			booking_id: '',
			date: '',
			start_time: '09:00',
			end_time: '12:00',
			status: 'pending'
		};
	}

	function handleFormCancel() {
		showForm = false;
		editingSchedule = null;
		resetForm();
	}

	function openAddForm() {
		editingSchedule = null;
		resetForm();
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
			<div class="view-toggle">
				<button 
					class="toggle-btn" 
					class:active={viewMode === 'calendar'}
					onclick={() => viewMode = 'calendar'}
				>
					Calendar
				</button>
				<button 
					class="toggle-btn" 
					class:active={viewMode === 'table'}
					onclick={() => viewMode = 'table'}
				>
					Table
				</button>
			</div>
			<button class="btn btn-primary" onclick={openAddForm}>Add Schedule</button>
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
		<div class="loading-spinner">
			<span class="spinner"></span>
			Loading...
		</div>
	{/if}

	{#if error}
		<div class="error-message">{error}</div>
	{/if}

	{#if showForm}
		<div class="form-section">
			<h3 class="form-title">{editingSchedule ? 'Edit Schedule' : 'Add New Schedule'}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<!-- Cleaner -->
					<div class="form-field">
						<label for="cleaner_id">Cleaner <span class="required">*</span></label>
						<select id="cleaner_id" bind:value={formData.cleaner_id} required>
							<option value="">-- Select Cleaner --</option>
							{#each cleanerOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>

					<!-- Booking -->
					<div class="form-field">
						<label for="booking_id">Booking <span class="required">*</span></label>
						<select 
							id="booking_id" 
							value={formData.booking_id} 
							onchange={(e) => handleBookingChange((e.target as HTMLSelectElement).value)}
							required
						>
							<option value="">-- Select Booking --</option>
							{#each bookingOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>

					<!-- Date -->
					<div class="form-field">
						<label for="date">Work Date <span class="required">*</span></label>
						<input type="date" id="date" bind:value={formData.date} required />
					</div>

					<!-- Start Time -->
					<div class="form-field">
						<label for="start_time">Start Time <span class="required">*</span></label>
						<input type="time" id="start_time" bind:value={formData.start_time} required />
					</div>

					<!-- End Time -->
					<div class="form-field">
						<label for="end_time">End Time <span class="required">*</span></label>
						<input type="time" id="end_time" bind:value={formData.end_time} required />
					</div>

					<!-- Status -->
					<div class="form-field">
						<label for="status">Status <span class="required">*</span></label>
						<select id="status" bind:value={formData.status} required>
							{#each statusOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						Cancel
					</button>
					<button type="submit" class="btn btn-primary">
						{editingSchedule ? 'Update' : 'Add'} Schedule
					</button>
				</div>
			</form>
		</div>
	{/if}

	{#if selectedCleanerName}
		<div class="filter-banner">
			<span>Showing: <strong>{selectedCleanerName}</strong></span>
			<button class="btn btn-secondary btn-sm" onclick={() => selectedCleanerId = null}>
				Show All
			</button>
		</div>
	{/if}

	{#if viewMode === 'calendar'}
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
	{:else}
		<div class="table-view">
			<table class="schedule-table">
				<thead>
					<tr>
						<th>Cleaner</th>
						<th>Guest</th>
						<th>Date</th>
						<th>Time</th>
						<th>Status</th>
						<th>Actions</th>
					</tr>
				</thead>
				<tbody>
					{#each schedules as schedule}
						{@const cleaner = cleaners.find(c => c.id === schedule.cleaner_id)}
						{@const booking = bookings.find(b => b.id === schedule.booking_id)}
						<tr>
							<td>{cleaner?.name || 'Unknown'}</td>
							<td>{booking?.guest_name || 'Unknown'}</td>
							<td>{schedule.date}</td>
							<td>{schedule.start_time || ''} - {schedule.end_time || ''}</td>
							<td>
								<span class="status-badge status-{schedule.status}">{schedule.status}</span>
							</td>
							<td>
								<button class="btn btn-sm btn-secondary" onclick={() => handleScheduleClick(schedule)}>
									Edit
								</button>
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="6" class="empty-row">No schedules found</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	{/if}
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
		align-items: center;
	}

	.view-toggle {
		display: flex;
		background: #e5e7eb;
		border-radius: 6px;
		padding: 2px;
	}

	.toggle-btn {
		padding: 0.375rem 0.75rem;
		font-size: 0.875rem;
		font-weight: 500;
		border: none;
		background: transparent;
		color: #6b7280;
		cursor: pointer;
		border-radius: 4px;
		transition: all 0.2s;
	}

	.toggle-btn.active {
		background: white;
		color: #111827;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
	}

	.date-range-controls {
		display: flex;
		gap: 2rem;
		margin-bottom: 2rem;
		padding: 1.5rem;
		background: var(--card-bg, #f9fafb);
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
		border: 1px solid var(--border-color, #d1d5db);
		border-radius: 6px;
		font-size: 1rem;
	}

	.loading-spinner {
		position: fixed;
		top: 1rem;
		right: 1rem;
		z-index: 50;
		background: #3b82f6;
		color: white;
		padding: 0.5rem 1rem;
		border-radius: 6px;
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.spinner {
		display: inline-block;
		width: 1rem;
		height: 1rem;
		border: 2px solid white;
		border-top-color: transparent;
		border-radius: 50%;
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		to { transform: rotate(360deg); }
	}

	.error-message {
		background: #fee2e2;
		border: 1px solid #fca5a5;
		color: #991b1b;
		padding: 1rem;
		border-radius: 6px;
		margin-bottom: 1rem;
	}

	.form-section {
		margin-bottom: 2rem;
		background: white;
		border-radius: 8px;
		padding: 1.5rem;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	.form-title {
		margin: 0 0 1.5rem 0;
		font-size: 1.125rem;
		font-weight: 600;
	}

	.form-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
	}

	.form-field {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.form-field label {
		font-size: 0.875rem;
		font-weight: 500;
		color: #374151;
	}

	.form-field .required {
		color: #ef4444;
	}

	.form-field input,
	.form-field select {
		padding: 0.5rem;
		border: 1px solid #d1d5db;
		border-radius: 6px;
		font-size: 0.875rem;
		background: white;
	}

	.form-field input:focus,
	.form-field select:focus {
		outline: none;
		border-color: #3b82f6;
		box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
	}

	.form-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		margin-top: 1.5rem;
		padding-top: 1rem;
		border-top: 1px solid #e5e7eb;
	}

	.btn {
		padding: 0.5rem 1rem;
		border: none;
		border-radius: 6px;
		font-size: 0.875rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}

	.btn-primary {
		background: #3b82f6;
		color: white;
	}

	.btn-primary:hover {
		background: #2563eb;
	}

	.btn-secondary {
		background: #6b7280;
		color: white;
	}

	.btn-secondary:hover {
		background: #4b5563;
	}

	.btn-sm {
		padding: 0.25rem 0.75rem;
		font-size: 0.75rem;
	}

	.filter-banner {
		display: flex;
		align-items: center;
		justify-content: space-between;
		background: #dbeafe;
		color: #1e40af;
		padding: 0.75rem 1rem;
		border-radius: 6px;
		margin-bottom: 1rem;
	}

	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.header-actions {
			flex-direction: column;
		}

		.date-range-controls {
			flex-direction: column;
		}

		.form-grid {
			grid-template-columns: 1fr;
		}
	}

	/* Table View */
	.table-view {
		background: white;
		border-radius: 8px;
		overflow: hidden;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}

	.schedule-table {
		width: 100%;
		border-collapse: collapse;
	}

	.schedule-table th,
	.schedule-table td {
		padding: 0.75rem 1rem;
		text-align: left;
		border-bottom: 1px solid #e5e7eb;
	}

	.schedule-table th {
		background: #f9fafb;
		font-weight: 600;
		font-size: 0.875rem;
		color: #374151;
	}

	.schedule-table tbody tr:hover {
		background: #f9fafb;
	}

	.empty-row {
		text-align: center;
		color: #6b7280;
		padding: 2rem;
	}

	.status-badge {
		display: inline-block;
		padding: 0.25rem 0.5rem;
		font-size: 0.75rem;
		font-weight: 500;
		border-radius: 4px;
		text-transform: capitalize;
	}

	.status-pending {
		background: #fef3c7;
		color: #92400e;
	}

	.status-confirmed {
		background: #dbeafe;
		color: #1e40af;
	}

	.status-completed {
		background: #d1fae5;
		color: #065f46;
	}

	.status-cancelled {
		background: #fee2e2;
		color: #991b1b;
	}
</style>
