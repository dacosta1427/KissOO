<script lang="ts">
	import { page } from '$app/stores';
	import { housesAPI, bookingsAPI, cleanersAPI, schedulesAPI, type House, type Booking, type Cleaner, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { goto } from '$app/navigation';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let urlBookingId = $derived($page.params.bookingId ? parseInt($page.params.bookingId) : 0);

	let booking = $state<Booking | null>(null);
	let house = $state<House | null>(null);
	let schedules = $state<Schedule[]>([]);
	let cleaners = $state<Cleaner[]>([]);
	let loading = $state(false);
	let showDeleteConfirm = $state(false);

	let formData = $state({
		house_id: 0,
		check_in_date: '',
		check_out_date: '',
		guest_name: '',
		guest_email: '',
		guest_phone: '',
		notes: '',
		dogs_count: 0,
		status: 'pending' as 'pending' | 'confirmed' | 'cancelled'
	});

	let saving = $state(false);
	let showSchedules = $state(true);

	async function loadData() {
		if (!urlBookingId || urlBookingId <= 0) return;
		loading = true;
		try {
			booking = await bookingsAPI.getById(urlBookingId);
			if (!booking) {
				notificationActions.error(tt('bookings.booking_not_found'));
				goto('/bookings');
				return;
			}

			formData = {
				house_id: booking.house_id,
				check_in_date: booking.check_in_date,
				check_out_date: booking.check_out_date,
				guest_name: booking.guest_name,
				guest_email: booking.guest_email,
				guest_phone: booking.guest_phone || '',
				notes: booking.notes || '',
				dogs_count: booking.dogs_count,
				status: booking.status
			};

			// Load house
			house = await housesAPI.getById(booking.house_id) || null;

			// Load schedules
			schedules = await schedulesAPI.getByBooking(booking.id);

			// Load cleaners referenced in schedules
			const cleanerIds = [...new Set(schedules.map(s => s.cleaner_id))];
			for (const cid of cleanerIds) {
				const c = await cleanersAPI.getById(cid);
				if (c) cleaners.push(c);
			}
		} catch (err: any) {
			console.error('Error loading booking:', err);
			notificationActions.error(err.message || tt('errors.failed_to_load'));
			goto('/bookings');
		} finally {
			loading = false;
		}
	}

	let hasChanges = $derived(booking && (
		formData.house_id !== booking.house_id ||
		formData.check_in_date !== booking.check_in_date ||
		formData.check_out_date !== booking.check_out_date ||
		formData.guest_name !== booking.guest_name ||
		formData.guest_email !== booking.guest_email ||
		formData.guest_phone !== (booking.guest_phone || '') ||
		formData.notes !== (booking.notes || '') ||
		formData.dogs_count !== booking.dogs_count ||
		formData.status !== booking.status
	));

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		if (!booking || saving) return;
		saving = true;
		try {
			const result = await bookingsAPI.update(booking.id, formData);
			notificationActions.success(tt('bookings.updated'));
			booking = result;
			await loadData();
		} catch (err: any) {
			notificationActions.error(err.message || tt('errors.failed_to_save'));
		} finally {
			saving = false;
		}
	}

	function handleFormCancel() {
		goto('/bookings');
	}

	async function handleDelete() {
		if (!booking) return;
		if (confirm(tt('bookings.delete_confirm').replace('${name}', booking.guest_name))) {
			try {
				await bookingsAPI.delete(booking.id);
				notificationActions.success(tt('bookings.deleted'));
				goto('/bookings');
			} catch (err: any) {
				notificationActions.error(err.message || tt('errors.failed_to_delete'));
			}
		}
	}

	function formatDate(dateStr: string) {
		if (!dateStr) return '-';
		const d = new Date(dateStr);
		return d.toLocaleDateString($currentLocale || 'en');
	}

	let loaded = $state(false);
	$effect(() => {
		if (!loaded && urlBookingId > 0) {
			loaded = true;
			loadData();
		}
	});
</script>

<div class="booking-page">
	<div class="page-header">
		<button class="btn btn-secondary" onclick={() => goto('/bookings')}>{tt('common.back')}</button>
		<h1>{tt("bookings.edit_booking")}</h1>
		<button class="btn btn-danger" onclick={() => showDeleteConfirm = true}>{tt('common.delete')}</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			{tt('common.loading')}
		</div>
	{/if}

	{#if booking}
		<div class="form-section" >
			<h3 class="form-title">{tt('bookings.edit_booking')}</h3>

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
						<label for="house_id">{tt('common.house')}</label>
						<select id="house_id" bind:value={formData.house_id} disabled>
							<option value={formData.house_id}>{house?.name || 'Loading...'}</option>
						</select>
					</div>
					<div class="form-field">
						<label for="check_in_date">{tt('common.check_in')} <span class="required">*</span></label>
						<input type="date" id="check_in_date" bind:value={formData.check_in_date} required />
					</div>
					<div class="form-field">
						<label for="check_out_date">{tt('common.check_out')} <span class="required">*</span></label>
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
					<div class="form-field full-width">
						<label for="notes">{tt('common.notes')}</label>
						<textarea id="notes" bind:value={formData.notes} rows={4}></textarea>
					</div>
				</div>

				<div class="form-actions">
					<Button type="button" class="btn-secondary" onclick={handleFormCancel} disabled={saving}>
						{tt('common.cancel')}
					</Button>
					<Button type="submit" class={hasChanges ? 'btn-primary' : 'btn-disabled'} loading={saving} disabled={!hasChanges}>
						{tt('common.save')}
					</Button>
				</div>
			</form>

			{#if showSchedules && schedules.length > 0}
				<div class="schedules-section">
					<h3>{tt('schedules.title')} ({schedules.length})</h3>
					<table class="data-table">
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
								{@const cleaner = cleaners.find(c => c.id === schedule.cleaner_id)}
								<tr onclick={() => goto(`/schedules/${schedule.id}`)}>
									<td>{formatDate(schedule.date)}</td>
									<td>{schedule.start_time}</td>
									<td>{schedule.end_time}</td>
									<td>{cleaner?.name || `#${schedule.cleaner_id}`}</td>
									<td><span class="status status-{schedule.status}">{schedule.status}</span></td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{/if}
		</div>
	{/if}
</div>

{#if showDeleteConfirm}
<div class="modal-overlay" onclick={() => showDeleteConfirm = false}>
	<div class="modal-content" onclick={(e) => e.stopPropagation()}>
		<h3>{tt('common.confirm')}</h3>
		<p>{tt('bookings.delete_confirm').replace('${name}', booking?.guest_name || '')}</p>
		<div class="modal-actions">
			<Button type="button" class="btn-secondary" onclick={() => showDeleteConfirm = false}>
				{tt('common.cancel')}
			</Button>
			<Button type="button" class="btn-danger" onclick={handleDelete}>
				{tt('common.delete')}
			</Button>
		</div>
	</div>
</div>

	{/if}

<style>
	.booking-page { padding: 2rem; max-width: 900px; margin: 0 auto; }
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
	.form-field input, .form-field select, .form-field textarea { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.875rem; font-family: inherit; }
	.form-field textarea { resize: vertical; }
	.form-field input:focus, .form-field select:focus, .form-field textarea:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.2); }
	.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e5e7eb; }
	.btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
	.btn-primary { background: #3b82f6; color: white; }
	.btn-primary:hover { background: #2563eb; }
	.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }
	.btn-secondary { background: #6b7280; color: white; }
	.btn-secondary:hover { background: #4b5563; }
	.btn-danger { background: #ef4444; color: white; }
	.btn-danger:hover { background: #dc2626; }
	.schedules-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e5e7eb; }
	.schedules-section h3 { font-size: 1rem; font-weight: 600; margin: 0 0 1rem 0; color: #374151; }
	.data-table { width: 100%; border-collapse: collapse; }
	.data-table th { background: #f3f4f6; padding: 0.75rem; text-align: left; font-weight: 600; color: #374151; border-bottom: 2px solid #e5e7eb; }
	.data-table td { padding: 0.75rem; border-bottom: 1px solid #e5e7eb; cursor: pointer; }
	.data-table tr:hover { background: #f9fafb; }
	.status { padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 500; text-transform: capitalize; }
	.status-pending { background: #fef3c7; color: #92400e; }
	.status-confirmed { background: #d1fae5; color: #065f46; }
	.status-cancelled { background: #fee2e2; color: #991b1b; }
	.status-scheduled { background: #dbeafe; color: #1e40af; }
	.status-completed { background: #d1fae5; color: #065f46; }
	.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
	.modal-content { background: white; border-radius: 8px; padding: 1.5rem; max-width: 400px; width: 90%; }
	.modal-content h3 { margin: 0 0 1rem 0; font-size: 1.125rem; font-weight: 600; }
	.modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
</style>
