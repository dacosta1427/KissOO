<script lang="ts">
	import { page } from '$app/stores';
	import { housesAPI, bookingsAPI, cleanersAPI, schedulesAPI, type House, type Booking, type Cleaner, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { goto } from '$app/navigation';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let urlScheduleId = $derived($page.params.scheduleId ? parseInt($page.params.scheduleId) : 0);

	let schedule = $state<Schedule | null>(null);
	let booking = $state<Booking | null>(null);
	let house = $state<House | null>(null);
	let cleaner = $state<Cleaner | null>(null);
	let loading = $state(false);
	let showDeleteConfirm = $state(false);

	let formData = $state({
		cleaner_id: 0,
		booking_id: 0,
		date: '',
		start_time: '09:00',
		end_time: '10:00',
		notes: '',
		status: 'scheduled' as 'scheduled' | 'completed' | 'cancelled' | 'pending'
	});

	let saving = $state(false);

	async function loadData() {
		if (!urlScheduleId || urlScheduleId <= 0) return;
		loading = true;
		try {
			schedule = await schedulesAPI.getById(urlScheduleId);
			if (!schedule) {
				notificationActions.error(tt('schedules.schedule_not_found'));
				goto('/schedules');
				return;
			}

			formData = {
				cleaner_id: schedule.cleaner_id,
				booking_id: schedule.booking_id,
				date: schedule.date,
				start_time: schedule.start_time,
				end_time: schedule.end_time,
				notes: schedule.notes || '',
				status: schedule.status
			};

			// Load related data
			if (schedule.booking_id) {
				booking = await bookingsAPI.getById(schedule.booking_id) || null;
				if (booking) {
					house = await housesAPI.getById(booking.house_id) || null;
				}
			}
			if (schedule.cleaner_id) {
				cleaner = await cleanersAPI.getById(schedule.cleaner_id) || null;
			}
		} catch (err: any) {
			console.error('Error loading schedule:', err);
			notificationActions.error(err.message || tt('errors.failed_to_load'));
			goto('/schedules');
		} finally {
			loading = false;
		}
	}

	let hasChanges = $derived(schedule && (
		formData.cleaner_id !== schedule.cleaner_id ||
		formData.booking_id !== schedule.booking_id ||
		formData.date !== schedule.date ||
		formData.start_time !== schedule.start_time ||
		formData.end_time !== schedule.end_time ||
		formData.notes !== (schedule.notes || '') ||
		formData.status !== schedule.status
	));

	async function handleFormSubmit(e: Event) {
		e.preventDefault();
		if (!schedule || saving) return;
		saving = true;
		try {
			const result = await schedulesAPI.update(schedule.id, formData);
			notificationActions.success(tt('schedules.updated'));
			schedule = result;
			await loadData();
		} catch (err: any) {
			notificationActions.error(err.message || tt('errors.failed_to_save'));
		} finally {
			saving = false;
		}
	}

	function handleFormCancel() {
		goto('/schedules');
	}

	async function handleDelete() {
		if (!schedule) return;
		if (confirm(tt('schedules.delete_confirm').replace('${id}', String(schedule.id)))) {
			try {
				await schedulesAPI.delete(schedule.id);
				notificationActions.success(tt('schedules.deleted'));
				goto('/schedules');
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
		if (!loaded && urlScheduleId > 0) {
			loaded = true;
			loadData();
		}
	});
</script>

<div class="schedule-page">
	<div class="page-header">
		<button class="btn btn-secondary" onclick={() => goto('/schedules')}>{tt('common.back')}</button>
		<h1>{tt('schedules.edit_schedule')}</h1>
		<button class="btn btn-danger" onclick={() => showDeleteConfirm = true}>{tt('common.delete')}</button>
	</div>

	{#if loading}
		<div class="loading-spinner">
			<span class="spinner"></span>
			{tt('common.loading')}
		</div>
	{/if}

	{#if schedule}
		<div class="form-section">
			<h3 class="form-title">{tt('schedules.edit_schedule')}</h3>

			<form onsubmit={handleFormSubmit}>
				<div class="form-grid">
					<div class="form-field">
						<label for="booking_id">{tt('bookings.booking')} <span class="required">*</span></label>
						<select id="booking_id" bind:value={formData.booking_id} disabled>
							<option value={formData.booking_id}>{booking?.guest_name || 'Loading...'}</option>
						</select>
					</div>
					<div class="form-field">
						<label for="cleaner_id">{tt('schedules.cleaner')} <span class="required">*</span></label>
						<select id="cleaner_id" bind:value={formData.cleaner_id}>
							<option value={formData.cleaner_id}>{cleaner?.name || 'Loading...'}</option>
						</select>
					</div>
					<div class="form-field">
						<label for="date">{tt('common.date')} <span class="required">*</span></label>
						<input type="date" id="date" bind:value={formData.date} required />
					</div>
					<div class="form-field">
						<label for="start_time">{tt('schedules.start_time')} <span class="required">*</span></label>
						<input type="time" id="start_time" bind:value={formData.start_time} required />
					</div>
					<div class="form-field">
						<label for="end_time">{tt('schedules.end_time')} <span class="required">*</span></label>
						<input type="time" id="end_time" bind:value={formData.end_time} required />
					</div>
					<div class="form-field">
						<label for="status">{tt('common.status')}</label>
						<select id="status" bind:value={formData.status}>
							<option value="scheduled">{tt('status.scheduled')}</option>
							<option value="completed">{tt('status.completed')}</option>
							<option value="cancelled">{tt('status.cancelled')}</option>
							<option value="pending">{tt('status.pending')}</option>
						</select>
					</div>
					<div class="form-field full-width">
						<label for="notes">{tt('common.notes')}</label>
						<textarea id="notes" bind:value={formData.notes} rows={3}></textarea>
					</div>
				</div>

				<div class="related-info">
					{#if house}
						<div class="info-card">
							<h4>{tt('common.house')}</h4>
							<p><strong>{house.name}</strong></p>
							{#if house.address}<p>{house.address}</p>{/if}
						</div>
					{/if}
					{#if booking}
						<div class="info-card">
							<h4>{tt('bookings.booking')}</h4>
							<p><strong>{booking.guest_name}</strong></p>
							<p>{formatDate(booking.check_in_date)} → {formatDate(booking.check_out_date)}</p>
						</div>
					{/if}
					{#if cleaner}
						<div class="info-card">
							<h4>{tt('schedules.cleaner')}</h4>
							<p><strong>{cleaner.name}</strong></p>
							{#if cleaner.phone}<p>{cleaner.phone}</p>{/if}
						</div>
					{/if}
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
		</div>
	{/if}
</div>

{#if showDeleteConfirm}
<div class="modal-overlay" onclick={() => showDeleteConfirm = false}>
	<div class="modal-content" onclick={(e) => e.stopPropagation()}>
		<h3>{tt('common.confirm')}</h3>
		<p>{tt('schedules.delete_confirm').replace('${id}', schedule?.id || '')}</p>
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
	.schedule-page { padding: 2rem; max-width: 900px; margin: 0 auto; }
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
	.related-info { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e5e7eb; }
	.info-card { margin-bottom: 1rem; padding: 1rem; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; }
	.info-card h4 { margin: 0 0 0.5rem 0; font-size: 0.875rem; font-weight: 600; color: #374151; }
	.info-card p { margin: 0.25rem 0; color: #6b7280; }
	.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e5e7eb; }
	.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
	.modal-content { background: white; border-radius: 8px; padding: 1.5rem; max-width: 400px; width: 90%; }
	.modal-content h3 { margin: 0 0 1rem 0; font-size: 1.125rem; font-weight: 600; }
	.modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
</style>
