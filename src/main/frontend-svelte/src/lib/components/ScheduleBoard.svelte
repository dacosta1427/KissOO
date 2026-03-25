<script>
	// Svelte 5: Use $props() for props in runes mode
	let {
		schedules = [],
		cleaners = [],
		bookings = [],
		dateRange = { start: null, end: null },
		loading = false,
		error = null,
		onScheduleChange,
		onScheduleClick,
		onCleanerClick,
		onEmptyCellClick,
		selectedCleanerId = null
	} = $props();

	// Svelte 5: Use $state for reactive variables
	let dragData = $state(null);
	let dragOverDate = $state(null);
	let dragOverCleanerId = $state(null);

	// Svelte 5: Use $derived for reactive computed values
	let dates = $derived(generateDateRange(dateRange.start, dateRange.end));
	let scheduleMatrix = $derived(buildScheduleMatrix(schedules, cleaners, dates));
	let filteredCleaners = $derived(selectedCleanerId ? cleaners.filter(c => c.id === selectedCleanerId) : cleaners);

	function generateDateRange(start, end) {
		if (!start || !end) return [];

		const dates = [];
		const current = new Date(start);
		const endDate = new Date(end);

		while (current <= endDate) {
			dates.push(new Date(current));
			current.setDate(current.getDate() + 1);
		}

		return dates;
	}

	function buildScheduleMatrix(schedules, cleaners, dates) {
		const matrix = {};

		cleaners.forEach((cleaner) => {
			matrix[cleaner.id] = {};
			dates.forEach((date) => {
				const dateString = date.toISOString().split('T')[0];
				matrix[cleaner.id][dateString] = null;
			});
		});

		schedules.forEach((schedule) => {
			const dateString = schedule.date;
			cleaners.forEach((cleaner) => {
				if (cleaner.id === schedule.cleaner_id && matrix[cleaner.id][dateString]) {
					matrix[cleaner.id][dateString] = schedule;
				}
			});
		});

		return matrix;
	}

	function handleDragStart(e, schedule) {
		dragData = schedule;
		e.dataTransfer.effectAllowed = 'move';
		e.dataTransfer.setData('text/plain', JSON.stringify(schedule));
	}

	function handleDragOver(e) {
		e.preventDefault();
		e.dataTransfer.dropEffect = 'move';
	}

	function handleDragEnter(e, cleanerId, date) {
		dragOverCleanerId = cleanerId;
		dragOverDate = date;
	}

	function handleDragLeave(e, cleanerId, date) {
		if (dragOverCleanerId === cleanerId && dragOverDate === date) {
			dragOverCleanerId = null;
			dragOverDate = null;
		}
	}

	function handleDrop(e, cleanerId, date) {
		e.preventDefault();

		if (dragData) {
			const newSchedule = {
				...dragData,
				cleaner_id: cleanerId,
				date: date.toISOString().split('T')[0]
			};

			onScheduleChange?.(newSchedule);
			dragData = null;
			dragOverCleanerId = null;
			dragOverDate = null;
		}
	}

	function handleScheduleClick(schedule) {
		onScheduleClick?.(schedule);
	}

	function handleEmptyCellClick(cleanerId, date) {
		onEmptyCellClick?.(cleanerId, date);
	}

	function getBookingInfo(bookingId) {
		return bookings.find((b) => b.id === bookingId);
	}

	function formatDate(date) {
		return date.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric'
		});
	}

	function isWeekend(date) {
		const day = date.getDay();
		return day === 0 || day === 6; // Sunday = 0, Saturday = 6
	}
</script>

<div class="schedule-board">
	<div class="status-legend">
		<span class="legend-item"><span class="legend-color status-scheduled"></span> Scheduled</span>
		<span class="legend-item"><span class="legend-color status-completed"></span> Completed</span>
		<span class="legend-item"><span class="legend-color status-cancelled"></span> Cancelled</span>
		<span class="legend-item"><span class="legend-color status-pending"></span> Pending</span>
	</div>
	<div class="board-header">
		<div class="cleaner-header">Cleaners</div>
		{#each dates as date}
			<div class="date-header {isWeekend(date) ? 'weekend' : ''}">
				<div class="date-label">{formatDate(date)}</div>
			</div>
		{/each}
	</div>

	{#if error}
		<div class="error-message">{error}</div>
	{:else if loading}
		<div class="loading-message">Loading schedule...</div>
	{:else}
		{#each filteredCleaners as cleaner}
			<div class="board-row" key={cleaner.id} role="row">
				<div 
					class="cleaner-cell {selectedCleanerId === cleaner.id ? 'selected' : ''}"
					onclick={() => onCleanerClick?.(cleaner.id)}
					onkeydown={(e) => {
						if (e.key === 'Enter' || e.key === ' ') {
							onCleanerClick?.(cleaner.id);
						}
					}}
					role="button"
					tabindex="0"
				>
					<div class="cleaner-name">{cleaner.name}</div>
					<div class="cleaner-info">{cleaner.email}</div>
				</div>

				{#each dates as date}
					<div
						class="schedule-cell {isWeekend(date) ? 'weekend' : ''} {dragOverCleanerId ===
							cleaner.id && dragOverDate === date
							? 'drag-over'
							: ''}"
						role="gridcell"
						tabindex="-1"
						ondragover={handleDragOver}
						ondragenter={(e) => handleDragEnter(e, cleaner.id, date)}
						ondragleave={(e) => handleDragLeave(e, cleaner.id, date)}
						ondrop={(e) => handleDrop(e, cleaner.id, date)}
					>
						{#if scheduleMatrix[cleaner.id][date.toISOString().split('T')[0]]}
							{@const item = scheduleMatrix[cleaner.id][date.toISOString().split('T')[0]]}
							<div
								class="schedule-item status-{item.status}"
								draggable="true"
								role="button"
								tabindex="0"
								ondragstart={(e) => handleDragStart(e, item)}
								onclick={() => handleScheduleClick(item)}
								onkeydown={(e) => {
									if (e.key === 'Enter' || e.key === ' ') {
										handleScheduleClick(item);
									}
								}}
							>
								<div class="schedule-time">
									{item.start_time || ''} - {item.end_time || ''}
								</div>
								<div class="schedule-house">
								{getBookingInfo(item.booking_id)?.guest_name || 'Unknown Guest'}
								</div>
								<div class="schedule-status">
									{item.status}
								</div>
							</div>
						{:else}
							<button
								class="add-schedule-btn"
								onclick={() => handleEmptyCellClick(cleaner.id, date)}
								title="Add schedule"
							>
								+
							</button>
						{/if}
					</div>
				{/each}
			</div>
		{/each}
	{/if}
</div>

<style>
	.schedule-board {
		width: 100%;
		overflow-x: auto;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		background: white;
	}

	.status-legend {
		display: flex;
		gap: 1.5rem;
		padding: 0.75rem 1rem;
		background: #f9fafb;
		border-bottom: 1px solid var(--border-color);
		font-size: 0.85rem;
	}

	.legend-item {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.legend-color {
		width: 16px;
		height: 16px;
		border-radius: 4px;
	}

	.legend-color.status-scheduled {
		background: #3b82f6;
	}

	.legend-color.status-completed {
		background: #10b981;
	}

	.legend-color.status-cancelled {
		background: #ef4444;
	}

	.legend-color.status-pending {
		background: #f59e0b;
	}

	.board-header {
		display: grid;
		grid-template-columns: 200px repeat(7, 150px);
		background: var(--table-header-bg);
		color: var(--table-header-text);
		position: sticky;
		top: 0;
		z-index: 2;
	}

	.cleaner-header {
		padding: 15px;
		font-weight: 700;
		border-right: 1px solid var(--border-color);
	}

	.date-header {
		padding: 15px;
		font-weight: 700;
		border-right: 1px solid var(--border-color);
		text-align: center;
		position: relative;
	}

	.date-header.weekend {
		background-color: rgba(241, 196, 15, 0.1);
		color: #8e44ad;
	}

	.date-label {
		font-size: 12px;
		font-weight: 600;
		text-transform: uppercase;
		letter-spacing: 0.5px;
	}

	.board-row {
		display: grid;
		grid-template-columns: 200px repeat(7, 150px);
		border-bottom: 1px solid var(--border-color);
	}

	.cleaner-cell {
		padding: 15px;
		border-right: 1px solid var(--border-color);
		background: var(--row-bg);
		cursor: pointer;
		transition: background-color 0.2s;
	}
	
	.cleaner-cell.selected {
		background: var(--primary-color);
		color: white;
	}
	
	.cleaner-cell.selected .cleaner-name,
	.cleaner-cell.selected .cleaner-info {
		color: white;
	}

	.cleaner-name {
		font-weight: 600;
		color: var(--text-color);
		margin-bottom: 4px;
	}

	.cleaner-info {
		font-size: 12px;
		color: var(--muted-color);
	}

	.schedule-cell {
		border-right: 1px solid var(--border-color);
		min-height: 100px;
		position: relative;
		transition: background-color 0.2s;
		padding: 4px;
	}

	.schedule-cell.weekend {
		background-color: rgba(241, 196, 15, 0.05);
	}

	.schedule-cell.drag-over {
		background-color: rgba(52, 152, 219, 0.3);
		box-shadow: inset 0 0 10px rgba(52, 152, 219, 0.5);
	}

	.schedule-cell:not(:has(.schedule-item)):hover {
		background-color: rgba(52, 152, 219, 0.1);
		cursor: pointer;
	}

	.schedule-item {
		padding: 8px;
		border-radius: 6px;
		cursor: grab;
		transition: all 0.2s;
		border: 1px solid rgba(255, 255, 255, 0.3);
		min-height: 80px;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.schedule-item.status-scheduled {
		background: #3b82f6;
		color: white;
	}

	.schedule-item.status-completed {
		background: #10b981;
		color: white;
	}

	.schedule-item.status-cancelled {
		background: #ef4444;
		color: white;
	}

	.schedule-item.status-pending {
		background: #f59e0b;
		color: white;
	}

	.schedule-item:hover {
		transform: translateY(-2px);
		box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
	}

	.schedule-item:active {
		cursor: grabbing;
	}

	.schedule-time {
		font-weight: 700;
		font-size: 13px;
		background: rgba(255, 255, 255, 0.25);
		padding: 2px 6px;
		border-radius: 4px;
		text-align: center;
	}

	.schedule-house {
		font-weight: 600;
		font-size: 13px;
		text-align: center;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.schedule-status {
		font-size: 10px;
		text-transform: uppercase;
		font-weight: 700;
		letter-spacing: 0.5px;
		background: rgba(255, 255, 255, 0.2);
		padding: 2px 6px;
		border-radius: 4px;
		text-align: center;
	}

	.add-schedule-btn {
		width: 100%;
		height: 100%;
		min-height: 80px;
		background: transparent;
		border: 2px dashed rgba(0, 0, 0, 0.1);
		border-radius: 6px;
		cursor: pointer;
		font-size: 24px;
		color: rgba(0, 0, 0, 0.2);
		transition: all 0.2s;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.add-schedule-btn:hover {
		border-color: var(--primary-color);
		color: var(--primary-color);
		background: rgba(52, 152, 219, 0.05);
	}

	.loading-message,
	.error-message {
		padding: 20px;
		text-align: center;
		color: var(--text-color);
	}

	.error-message {
		color: var(--error-color);
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.board-header,
		.board-row {
			grid-template-columns: 150px repeat(7, 120px);
		}

		.cleaner-header,
		.cleaner-cell {
			padding: 10px;
		}

		.schedule-cell {
			min-height: 80px;
		}

		.schedule-item {
			padding: 8px;
		}
	}
</style>
