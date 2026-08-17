<script lang="ts">
	import { schedulesAPI, cleanersAPI, bookingsAPI, housesAPI, type Schedule, type Cleaner, type Booking, type House } from '$lib/api/Cleaning';
	import { dataStores } from '../../lib/stores.svelte.js';
	import { session } from '$lib/state/session.svelte';
	import { notificationActions } from '$lib/stores.svelte.js';
	import ScheduleBoard from '$lib/components/ScheduleBoard.svelte';
  import { t, currentLocale } from '$lib/i18n';
  import { toInputDateFormat, toBackendDateFormat, toDisplayDateFormat } from '$lib/utils/Utils';
  
  // Reactive translation helper
  const tt = (key: string) => t(key, undefined, $currentLocale);

  // Check if user is admin or cleaner
  let isAdmin = $derived(session.username === 'admin' || session.username === 'administrator');
  let isCleaner = $derived(!isAdmin && session.cleanerOid > 0);

  // Svelte 5: Use $state for reactive variables
	let schedules = $state<Schedule[]>([]);
	let cleaners = $state<Cleaner[]>([]);
	let bookings = $state<Booking[]>([]);
	let houses = $state<House[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let showForm = $state(false);
	let editingSchedule = $state<Schedule | null>(null);
	let viewMode = $state<'calendar' | 'table'>('calendar');
	
	// Modal for showing house details to cleaner
	let showHouseModal = $state(false);
	let selectedHouseForModal = $state<House | null>(null);

	// Form section ref for scroll on small screens
	let formSection = $state<HTMLElement | null>(null);

	function scrollToEditForm() {
		// Scroll to edit form on small screens (mobile/tablet)
		if (window.innerWidth < 1024 && formSection) {
			formSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}

	let dateRange = $state({
		start: new Date().toISOString().split('T')[0],
		end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
	});
	
	let selectedCleanerId = $state<number | null>(null);
	
	// For cleaner view: selected schedule to mark complete
	let selectedScheduleForAction = $state<Schedule | null>(null);
	let notes = $state('');

	// Form data
	let formData = $state({
		cleaner_id: '',
		booking_id: '',
		date: '',
		start_time: '09:00',
		end_time: '12:00',
		status: 'scheduled'
	});

	// Svelte 5: Use $derived for form options
	let cleanerOptions = $derived(cleaners.map((c) => ({ value: String(c.id), label: c.name })));
	let bookingOptions = $derived(bookings.map((b) => ({
		value: String(b.id),
		label: `${b.guest_name || t('schedules.guest')} - ${toDisplayDateFormat(b.check_in_date)} to ${toDisplayDateFormat(b.check_out_date)}`,
		checkOutDate: toInputDateFormat(b.check_out_date)
	})));

	// Status options
	let statusOptions = $derived([
		{ value: 'scheduled', label: t('schedules.scheduled') },
		{ value: 'completed', label: t('schedules.completed') },
		{ value: 'cancelled', label: t('schedules.cancelled') },
		{ value: 'pending', label: t('schedules.pending') }
	]);

	// Computed: get selected cleaner name
	let selectedCleanerName = $derived(
		selectedCleanerId ? (cleaners.find(c => c.id === selectedCleanerId)?.name || '') : ''
	);
	
	// Filtered schedules - backend already filters by actor, just use all
	let filteredSchedules = $derived(schedules);

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
			const [schedulesResult, cleanersResult, bookingsResult, housesResult] = await Promise.all([
				schedulesAPI.getAll({ dateRange }),
				cleanersAPI.getAll(),
				bookingsAPI.getAll(),
				housesAPI.getAll()
			]);

			schedules = schedulesResult;
			cleaners = cleanersResult;
			bookings = bookingsResult;
			houses = housesResult;

			dataStores.schedules.set(schedules);
			dataStores.cleaners.set(cleaners);
			dataStores.bookings.set(bookings);
		} catch (err: any) {
			error = err.message || t('errors.failed_to_load');
		} finally {
			loading = false;
		}
	}

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		
		const scheduleData = {
			cleaner_id: parseInt(formData.cleaner_id),
			booking_id: parseInt(formData.booking_id),
			date: toBackendDateFormat(formData.date),
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
		} catch (err: any) {
			error = err.message || t('errors.failed_to_save');
		}
	}

	function resetForm() {
		formData = {
			cleaner_id: '',
			booking_id: '',
			date: '',
			start_time: '09:00',
			end_time: '12:00',
			status: 'scheduled'
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
		scrollToEditForm();
	}

	function handleScheduleClick(schedule: Schedule) {
		editingSchedule = schedule;
		formData = {
			cleaner_id: String(schedule.cleaner_id),
			booking_id: String(schedule.booking_id),
			date: schedule.date,
			start_time: schedule.start_time || '09:00',
			end_time: schedule.end_time || '12:00',
			status: schedule.status || 'scheduled'
		};
		showForm = true;
		scrollToEditForm();
	}

	// For cleaner: show house details in modal
	function showHouseDetailsForCleaner(schedule: Schedule) {
		const booking = bookings.find(b => b.id === schedule.booking_id);
		if (booking && booking.house_id) {
			selectedHouseForModal = houses.find(h => h.id === booking.house_id) || null;
			showHouseModal = true;
		}
	}

	function closeHouseModal() {
		showHouseModal = false;
		selectedHouseForModal = null;
	}
	
	function handleCleanerClick(cleanerId: number) {
		if (selectedCleanerId === cleanerId) {
			selectedCleanerId = null;
		} else {
			selectedCleanerId = cleanerId;
		}
	}

	function handleEmptyCellClick(cleanerId: number, date: Date) {
		editingSchedule = null;
		formData = {
			cleaner_id: String(cleanerId),
			booking_id: '',
			date: date.toISOString().split('T')[0],
			start_time: '09:00',
			end_time: '12:00',
			status: 'scheduled'
		};
		showForm = true;
	}
	
	// Cleaner actions
	async function markComplete(schedule: Schedule) {
		try {
			await schedulesAPI.update(schedule.id, {
				...schedule,
				status: 'completed',
				notes: notes || schedule.notes
			});
			notificationActions.success(tt('schedules.marked_complete'));
			await loadData();
			selectedScheduleForAction = null;
			notes = '';
		} catch (err: any) {
			error = err.message || t('errors.failed_to_save');
		}
	}
	
	async function startCleaning(schedule: Schedule) {
		try {
			await schedulesAPI.update(schedule.id, {
				...schedule,
				status: 'pending'  // pending means "in progress"
			});
			notificationActions.success(tt('schedules.started'));
			await loadData();
		} catch (err: any) {
			error = err.message || t('errors.failed_to_save');
		}
	}
	
	function openCompleteForm(schedule: Schedule) {
		selectedScheduleForAction = schedule;
		notes = schedule.notes || '';
	}

	// Load data on mount
	$effect(() => {
		loadData();
	});
</script>

<div class="schedules-page">
	<div class="page-header">
		<h1>{tt('schedules.title')}</h1>
		<div class="header-actions">
			<div class="view-toggle">
				<button 
					class="toggle-btn" 
					class:active={viewMode === 'calendar'}
					onclick={() => viewMode = 'calendar'}
				>
					{tt('schedules.calendar')}
				</button>
				<button 
					class="toggle-btn" 
					class:active={viewMode === 'table'}
					onclick={() => viewMode = 'table'}
				>
					{tt('schedules.table')}
				</button>
			</div>
			<button class="btn btn-primary" onclick={openAddForm}>{tt('schedules.add_schedule')}</button>
		</div>
	</div>

	<div class="date-range-controls">
		<div class="form-group">
			<label for="startDate">{tt('common.start_date')}</label>
			<input type="date" id="startDate" bind:value={dateRange.start} onchange={loadData} />
		</div>
		<div class="form-group">
			<label for="endDate">{tt('common.end_date')}</label>
			<input type="date" id="endDate" bind:value={dateRange.end} onchange={loadData} />
		</div>
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
		<div class="form-section" bind:this={formSection}>
			<h3 class="form-title">{editingSchedule ? t('schedules.edit_schedule') : t('schedules.add_new_schedule')}</h3>
			
			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<!-- Cleaner -->
					<div class="form-field">
						<label for="cleaner_id">{tt('schedules.cleaner')} <span class="required">*</span></label>
						<select id="cleaner_id" bind:value={formData.cleaner_id} required>
							<option value="">-- {tt('schedules.select_cleaner')} --</option>
							{#each cleanerOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>

					<!-- Booking -->
					<div class="form-field">
						<label for="booking_id">{tt('schedules.booking')} <span class="required">*</span></label>
						<select 
							id="booking_id" 
							value={formData.booking_id} 
							onchange={(e) => handleBookingChange((e.target as HTMLSelectElement).value)}
							required
						>
							<option value="">-- {tt('schedules.select_booking')} --</option>
							{#each bookingOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>

					<!-- Date -->
					<div class="form-field">
						<label for="date">{tt('schedules.work_date')} <span class="required">*</span></label>
						<input type="date" id="date" bind:value={formData.date} required />
					</div>

					<!-- Start Time -->
					<div class="form-field">
						<label for="start_time">{tt('schedules.start_time')} <span class="required">*</span></label>
						<input type="time" id="start_time" bind:value={formData.start_time} step="3600" required />
					</div>

					<!-- End Time -->
					<div class="form-field">
						<label for="end_time">{tt('schedules.end_time')} <span class="required">*</span></label>
						<input type="time" id="end_time" bind:value={formData.end_time} step="3600" required />
					</div>

					<!-- Status -->
					<div class="form-field">
						<label for="status">{tt('schedules.status')} <span class="required">*</span></label>
						<select id="status" bind:value={formData.status} required>
							{#each statusOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>
				</div>

				<div class="form-actions">
					<button type="button" class="btn btn-secondary" onclick={handleFormCancel}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-primary">
						{editingSchedule ? t('common.update') : t('common.add')} {tt('schedules.title')}
					</button>
				</div>
			</form>
		</div>
	{/if}

	{#if selectedCleanerName}
		<div class="filter-banner">
			<span>{tt('schedules.showing')}: <strong>{selectedCleanerName}</strong></span>
			<button class="btn btn-secondary btn-sm" onclick={() => selectedCleanerId = null}>
				{tt('schedules.show_all')}
			</button>
		</div>
	{/if}

	{#if viewMode === 'calendar'}
		<ScheduleBoard
			schedules={filteredSchedules}
			{cleaners}
			{bookings}
			{dateRange}
			{loading}
			{error}
			onScheduleChange={async (newSchedule: any) => {
				try {
					if (editingSchedule) {
						await schedulesAPI.update(editingSchedule.id, newSchedule);
					}
					await loadData();
				} catch (err: any) {
					error = err.message || t('errors.failed_to_save');
				}
			}}
			onScheduleClick={handleScheduleClick}
			onCleanerClick={handleCleanerClick}
			onEmptyCellClick={handleEmptyCellClick}
			{selectedCleanerId}
		/>
	{:else}
		<div class="table-view">
			<table class="schedule-table">
				<thead>
					<tr>
						<th>{tt('schedules.cleaner')}</th>
						<th>{tt('schedules.guest')}</th>
						<th>{tt('schedules.date')}</th>
						<th>{tt('schedules.time')}</th>
						<th>{tt('schedules.status')}</th>
						<th>{tt('schedules.actions')}</th>
					</tr>
				</thead>
				<tbody>
					{#each filteredSchedules as schedule}
						{@const cleaner = cleaners.find(c => c.id === schedule.cleaner_id)}
						{@const booking = bookings.find(b => b.id === schedule.booking_id)}
						<!-- svelte-ignore a11y_click_events_have_key_events -->
						<!-- svelte-ignore a11y_no_static_element_interactions -->
						<tr class={isAdmin ? "clickable" : ""} onclick={isAdmin ? () => handleScheduleClick(schedule) : undefined} onkeydown={isAdmin ? (e) => e.key === 'Enter' && handleScheduleClick(schedule) : undefined}>
							<td>{cleaner?.name || t('houses.unknown')}</td>
							<td>{booking?.guest_name || t('schedules.guest')}</td>
							<td>{schedule.date}</td>
							<td>{schedule.start_time || ''} - {schedule.end_time || ''}</td>
							<td>
								<span class="status-badge status-{schedule.status}">{schedule.status}</span>
							</td>
							<td>
								{#if isAdmin}
									<button class="btn btn-sm btn-secondary" onclick={(e) => { e.stopPropagation(); handleScheduleClick(schedule); }} title={tt('hints.edit_item')}>
										{tt('common.edit')}
									</button>
								{:else if isCleaner}
									<button class="btn btn-sm btn-info" onclick={(e) => { e.stopPropagation(); showHouseDetailsForCleaner(schedule); }} title={tt('houses.view_house')}>
										{tt('houses.view_house')}
									</button>
									{#if schedule.status !== 'completed'}
										{#if schedule.status === 'scheduled'}
											<button class="btn btn-sm btn-primary" onclick={(e) => { e.stopPropagation(); startCleaning(schedule); }} title={tt('schedules.start_cleaning')}>
												{tt('schedules.start')}
											</button>
										{/if}
										<button class="btn btn-sm btn-success" onclick={(e) => { e.stopPropagation(); openCompleteForm(schedule); }} title={tt('schedules.mark_complete')}>
											{tt('schedules.complete')}
										</button>
									{/if}
								{/if}
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="6" class="empty-row">
								{isAdmin ? tt('schedules.no_schedules') : tt('schedules.no_schedules_cleaner')}
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	{/if}
</div>

<!-- Complete CleaningService Modal -->
{#if selectedScheduleForAction}
	<div class="modal-overlay" role="dialog" aria-modal="true" tabindex="-1" onclick={() => selectedScheduleForAction = null} onkeydown={(e) => e.key === 'Escape' && (selectedScheduleForAction = null)}>
		<div class="modal-content" role="document" onclick={(e) => e.stopPropagation()}>
			<h3 class="modal-title">{tt('schedules.complete_cleaning')}</h3>
			<p class="modal-description">{tt('schedules.add_notes')}</p>
			<form onsubmit={(e) => { e.preventDefault(); markComplete(selectedScheduleForAction!); }}>
				<div class="form-field">
					<label for="completion-notes">{tt('schedules.notes')}</label>
					<textarea 
						id="completion-notes" 
						bind:value={notes}
						rows="4"
						placeholder={tt('schedules.notes_placeholder')}
					></textarea>
				</div>
				<div class="modal-actions">
					<button type="button" class="btn btn-secondary" onclick={() => selectedScheduleForAction = null}>
						{tt('common.cancel')}
					</button>
					<button type="submit" class="btn btn-success">
						{tt('schedules.mark_complete')}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- House Details Modal for Cleaner -->
{#if showHouseModal && selectedHouseForModal}
	<div class="modal-overlay" role="dialog" aria-modal="true" tabindex="-1" onclick={closeHouseModal} onkeydown={(e) => e.key === 'Escape' && closeHouseModal()}>
		<div class="modal-content" role="document" onclick={(e) => e.stopPropagation()}>
			<h3 class="modal-title">{tt('houses.house_details')}</h3>
			<div class="house-details">
				<p><strong>{tt('houses.name')}:</strong> {selectedHouseForModal.name}</p>
				<p><strong>{tt('houses.address')}:</strong> {selectedHouseForModal.address}</p>
				<p><strong>{tt('houses.check_in_time')}:</strong> {selectedHouseForModal.check_in_time || '-'}</p>
				<p><strong>{tt('houses.check_out_time')}:</strong> {selectedHouseForModal.check_out_time || '-'}</p>
				<p><strong>{tt('houses.surface_m2')}:</strong> {selectedHouseForModal.surface_m2 || '-'}</p>
				<p><strong>{tt('houses.floors')}:</strong> {selectedHouseForModal.floors || '-'}</p>
				<p><strong>{tt('houses.bedrooms')}:</strong> {selectedHouseForModal.bedrooms || '-'}</p>
				<p><strong>{tt('houses.bathrooms')}:</strong> {selectedHouseForModal.bathrooms || '-'}</p>
				{#if selectedHouseForModal.description}
					<p><strong>{tt('houses.description')}:</strong> {selectedHouseForModal.description}</p>
				{/if}
			</div>
			<div class="modal-actions">
				<button type="button" class="btn btn-secondary" onclick={closeHouseModal}>
					{tt('common.close')}
				</button>
			</div>
		</div>
	</div>
{/if}

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

	.status-scheduled {
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
	
	/* Success button for complete action */
	.btn-success {
		background: #10b981;
		color: white;
	}
	
	.btn-success:hover {
		background: #059669;
	}
	
	/* Modal styles */
	.modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 100;
	}
	
	.modal-content {
		background: white;
		border-radius: 8px;
		padding: 1.5rem;
		width: 90%;
		max-width: 500px;
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
	}
	
	.modal-title {
		margin: 0 0 0.5rem 0;
		font-size: 1.25rem;
		font-weight: 600;
		color: #111827;
	}
	
	.modal-description {
		margin: 0 0 1.5rem 0;
		color: #6b7280;
		font-size: 0.875rem;
	}
	
	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		margin-top: 1.5rem;
	}

	.clickable { cursor: pointer; }
	.clickable:hover { border-color: #3b82f6; box-shadow: 0 2px 8px rgba(59,130,246,0.2); }
	.schedule-table tr.clickable:hover { background: #eff6ff; }
</style>
