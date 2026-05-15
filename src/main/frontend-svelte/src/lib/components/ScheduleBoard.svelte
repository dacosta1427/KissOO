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
		selectedCleanerId = null,
		visibleStartIndex = $bindable(0),
		visibleCount = 7
	} = $props();

	// Svelte 5: Use $state for reactive variables
	let dragData = $state(null);
	let dragOverDate = $state(null);
	let dragOverCleanerId = $state(null);

	// Svelte 5: Use $derived for reactive computed values
	let dates = $derived(generateDateRange(dateRange.start, dateRange.end));
	let scheduleMatrix = $derived(buildScheduleMatrix(schedules, cleaners, dates));
	let filteredCleaners = $derived(selectedCleanerId ? cleaners.filter(c => c.oid === selectedCleanerId) : cleaners);
	let dateCount = $derived(dates.length);
	let visibleDates = $derived(dates.slice(visibleStartIndex, visibleStartIndex + visibleCount));
	let maxStartIndex = $derived(Math.max(0, dates.length - visibleCount));

	let scrollContainer;

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
			matrix[cleaner.oid] = {};
			dates.forEach((date) => {
				const dateString = date.toISOString().split('T')[0];
				matrix[cleaner.oid][dateString] = null;
			});
		});

		schedules.forEach((schedule) => {
			// Convert YYYYMMDD to YYYY-MM-DD for matching
			const scheduleDate = schedule.date;
			let dateString;
			if (scheduleDate && scheduleDate.length === 8) {
				dateString = scheduleDate.substring(0,4) + '-' + scheduleDate.substring(4,6) + '-' + scheduleDate.substring(6,8);
			} else {
				dateString = scheduleDate;
			}
			cleaners.forEach((cleaner) => {
				if (cleaner.oid === schedule.cleanerOid && matrix[cleaner.oid][dateString] !== undefined) {
					matrix[cleaner.oid][dateString] = schedule;
				}
			});
		});

		return matrix;
	}

	function scrollLeft() {
		if (visibleStartIndex > 0) {
			visibleStartIndex = Math.max(0, visibleStartIndex - visibleCount);
		}
	}

	function scrollRight() {
		if (visibleStartIndex < maxStartIndex) {
			visibleStartIndex = Math.min(maxStartIndex, visibleStartIndex + visibleCount);
		}
	}

	function goToToday() {
		const today = new Date().toISOString().split('T')[0];
		const todayIndex = dates.findIndex(d => d.toISOString().split('T')[0] === today);
		if (todayIndex >= 0) {
			visibleStartIndex = Math.max(0, Math.min(todayIndex - Math.floor(visibleCount / 2), maxStartIndex));
		}
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
				cleanerOid: cleanerId,
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
		return bookings.find((b) => b.oid === bookingId);
	}

	function formatDate(date) {
		return date.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric'
		});
	}

	function formatDateShort(date) {
		return date.toLocaleDateString('en-US', {
			month: 'short',
			day: 'numeric'
		});
	}

	function isWeekend(date) {
		const day = date.getDay();
		return day === 0 || day === 6; // Sunday = 0, Saturday = 6
	}

	function isToday(date) {
		const today = new Date();
		return date.getDate() === today.getDate() &&
		       date.getMonth() === today.getMonth() &&
		       date.getFullYear() === today.getFullYear();
	}
</script>

<div class="schedule-board" style="--date-count: {visibleDates.length}">
	<!-- Date Navigation Slider Row -->
	<div class="date-navigator">
		<button class="nav-btn nav-left" onclick={scrollLeft} disabled={visibleStartIndex === 0} aria-label="Scroll left">
			‹
		</button>
		<div class="nav-dates">
			{#each visibleDates as date, i}
				<button
					class="nav-date-btn {isWeekend(date) ? 'weekend' : ''} {isToday(date) ? 'today' : ''}"
					onclick={() => {
						// Center the clicked date in view
						const globalIdx = dates.indexOf(date);
						visibleStartIndex = Math.max(0, Math.min(globalIdx - Math.floor(visibleCount / 2), maxStartIndex));
					}}
				>
					<div class="nav-date-label">{formatDateShort(date)}</div>
					{#if isToday(date)}
						<span class="today-dot"></span>
					{/if}
				</button>
			{/each}
		</div>
		<button class="nav-btn nav-right" onclick={scrollRight} disabled={visibleStartIndex >= maxStartIndex} aria-label="Scroll right">
			›
		</button>
		<div class="nav-actions">
			<button class="nav-today-btn" onclick={goToToday}>Today</button>
		</div>
	</div>

	<!-- Progress/Loading Bar -->
	{#if loading}
	<div class="loading-bar">
		<div class="loading-bar-fill"></div>
	</div>
	{/if}

	<!-- Legend -->
	<div class="status-legend">
		<span class="legend-item"><span class="legend-color status-scheduled"></span> Scheduled</span>
		<span class="legend-item"><span class="legend-color status-completed"></span> Completed</span>
		<span class="legend-item"><span class="legend-color status-cancelled"></span> Cancelled</span>
		<span class="legend-item"><span class="legend-color status-pending"></span> Pending</span>
	</div>

	<!-- Column Headers -->
	<div class="board-header">
		<div class="cleaner-header">Cleaners</div>
		{#each visibleDates as date}
			<div class="date-header {isWeekend(date) ? 'weekend' : ''} {isToday(date) ? 'today-col' : ''}">
				<div class="date-label">{formatDateShort(date)}</div>
				<div class="date-day">{date.getDate()}</div>
			</div>
		{/each}
	</div>

	{#if error}
		<div class="error-message">{error}</div>
	{:else if loading}
		<div class="loading-message">Loading schedule...</div>
	{:else}
		<!-- Schedule Rows -->
		{#each filteredCleaners as cleaner (cleaner.oid)}
			<div class="board-row" role="row">
				<div 
					class="cleaner-cell {selectedCleanerId === cleaner.oid ? 'selected' : ''}"
					onclick={() => onCleanerClick?.(cleaner.oid)}
					onkeydown={(e) => {
						if (e.key === 'Enter' || e.key === ' ') {
							onCleanerClick?.(cleaner.oid);
						}
					}}
					role="button"
					tabindex="0"
				>
					<div class="cleaner-name">{cleaner.name}</div>
					<div class="cleaner-info">{cleaner.email}</div>
				</div>

				{#each visibleDates as date}
					<div
						class="schedule-cell {isWeekend(date) ? 'weekend' : ''} {dragOverCleanerId ===
							cleaner.oid && dragOverDate === date
							? 'drag-over'
							: ''}"
						role="gridcell"
						tabindex="-1"
						ondragover={handleDragOver}
						ondragenter={(e) => handleDragEnter(e, cleaner.oid, date)}
						ondragleave={(e) => handleDragLeave(e, cleaner.oid, date)}
						ondrop={(e) => handleDrop(e, cleaner.oid, date)}
					>
						{#if scheduleMatrix[cleaner.oid][date.toISOString().split('T')[0]]}
							{@const item = scheduleMatrix[cleaner.oid][date.toISOString().split('T')[0]]}
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
								{getBookingInfo(item.bookingOid)?.guest_name || 'Unknown Guest'}
								</div>
								<div class="schedule-status">
									{item.status}
								</div>
							</div>
						{:else}
							<button
								class="add-schedule-btn"
								onclick={() => handleEmptyCellClick(cleaner.oid, date)}
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
	/* -- Loading Bar -- */
	.loading-bar {
		height: 3px;
		background: rgba(52, 152, 219, 0.15);
		border-radius: 2px;
		overflow: hidden;
		margin-bottom: 0.5rem;
	}

	.loading-bar-fill {
		height: 100%;
		background: var(--primary-color);
		border-radius: 2px;
		animation: loading-bar-anim 1.2s ease-in-out infinite;
	}

	@keyframes loading-bar-anim {
		0% { width: 0%; margin-left: 0; }
		50% { width: 70%; margin-left: 15%; }
		100% { width: 0%; margin-left: 100%; }
	}

	/* -- Date Navigator -- */
	.date-navigator {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 0.75rem 1rem;
		border-bottom: 1px solid var(--border-color);
		background: var(--card-bg);
	}

	.nav-btn {
		width: 32px;
		height: 32px;
		border: 1px solid var(--border-color);
		border-radius: 6px;
		background: white;
		color: var(--text-color);
		font-size: 1.2rem;
		cursor: pointer;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: all 0.2s;
		flex-shrink: 0;
	}

	.nav-btn:hover:not(:disabled) {
		background: var(--primary-color);
		color: white;
		border-color: var(--primary-color);
	}

	.nav-btn:disabled {
		opacity: 0.3;
		cursor: not-allowed;
	}

	.nav-dates {
		display: flex;
		gap: 0.25rem;
		flex: 1;
		overflow-x: auto;
		padding-bottom: 2px;
	}

	.nav-date-btn {
		background: none;
		border: 1px solid transparent;
		border-radius: 6px;
		padding: 6px 8px;
		cursor: pointer;
		text-align: center;
		min-width: 70px;
		transition: all 0.2s;
		white-space: nowrap;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 2px;
	}

	.nav-date-btn:hover {
		background: var(--hover-bg);
		border-color: var(--border-color);
	}

	.nav-date-btn.weekend {
		color: #8e44ad;
	}

	.nav-date-btn.today {
		border-color: var(--primary-color);
		background: rgba(52, 152, 219, 0.08);
	}

	.nav-date-label {
		font-size: 11px;
		font-weight: 600;
		color: var(--text-color);
	}

	.nav-date-btn .today-dot {
		width: 6px;
		height: 6px;
		background: var(--primary-color);
		border-radius: 50%;
	}

	.nav-actions {
		flex-shrink: 0;
	}

	.nav-today-btn {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 6px;
		padding: 6px 14px;
		font-size: 12px;
		font-weight: 600;
		color: var(--primary-color);
		cursor: pointer;
		transition: all 0.2s;
	}

	.nav-today-btn:hover {
		background: var(--primary-color);
		color: white;
	}

	/* -- Enhanced Scrollbar for Schedule Board -- */
	.schedule-board {
		width: 100%;
		overflow-x: auto;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		background: white;
		scrollbar-width: thin;
		scrollbar-color: var(--primary-color) rgba(52, 152, 219, 0.15);
	}

	.schedule-board::-webkit-scrollbar {
		height: 8px;
	}

	.schedule-board::-webkit-scrollbar-track {
		background: rgba(52, 152, 219, 0.08);
		border-radius: 4px;
	}

	.schedule-board::-webkit-scrollbar-thumb {
		background: var(--primary-color);
		border-radius: 4px;
		border: 2px solid white;
	}

	.schedule-board::-webkit-scrollbar-thumb:hover {
		background: #2563eb;
	}

	/* -- Status Legend -- */
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
		width: 12px;
		height: 12px;
		border-radius: 3px;
	}

	.legend-color.status-scheduled { background: #3b82f6; }
	.legend-color.status-completed { background: #10b981; }
	.legend-color.status-cancelled { background: #ef4444; }
	.legend-color.status-pending  { background: #f59e0b; }

	/* -- Board Header -- */
	.board-header {
		display: grid;
		grid-template-columns: 200px repeat(var(--date-count, 1), 150px);
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
		padding: 15px 10px;
		font-weight: 700;
		border-right: 1px solid var(--border-color);
		text-align: center;
		position: relative;
	}

	.date-header.weekend { background-color: rgba(241, 196, 15, 0.08); }
	.date-header.today-col { background-color: rgba(52, 152, 219, 0.06); border-bottom: 2px solid var(--primary-color); }

	.date-label {
		font-size: 11px;
		font-weight: 600;
		text-transform: uppercase;
		letter-spacing: 0.5px;
		color: var(--muted-color);
	}

	.date-day {
		font-size: 16px;
		font-weight: 700;
		color: var(--text-color);
	}

	/* -- Board Rows -- */
	.board-row {
		display: grid;
		grid-template-columns: 200px repeat(var(--date-count, 1), 150px);
		border-bottom: 1px solid var(--border-color);
	}

	/* -- Cleaner Cell -- */
	.cleaner-cell {
		padding: 12px;
		border-right: 1px solid var(--border-color);
		background: var(--row-bg);
		cursor: pointer;
		transition: all 0.2s;
		min-height: 80px;
	}

	.cleaner-cell.selected {
		background: var(--primary-color);
		color: white;
	}

	.cleaner-cell.selected .cleaner-name,
	.cleaner-cell.selected .cleaner-info {
		color: white;
	}

	.cleaner-name { font-weight: 600; color: var(--text-color); margin-bottom: 2px; }
	.cleaner-info { font-size: 11px; color: var(--muted-color); }

	/* -- Schedule Cells -- */
	.schedule-cell {
		border-right: 1px solid var(--border-color);
		min-height: 100px;
		position: relative;
		transition: background-color 0.2s;
		padding: 4px;
	}

	.schedule-cell.weekend { background-color: rgba(241, 196, 15, 0.04); }
	.schedule-cell.drag-over { background-color: rgba(52, 152, 219, 0.2); box-shadow: inset 0 0 12px rgba(52, 152, 219, 0.3); }
	.schedule-cell:not(:has(.schedule-item)):hover { background-color: rgba(52, 152, 219, 0.08); cursor: pointer; }

	/* -- Schedule Items -- */
	.schedule-item {
		padding: 8px;
		border-radius: 6px;
		cursor: grab;
		transition: all 0.2s;
		border: 1px solid rgba(255, 255, 255, 0.25);
		min-height: 70px;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.schedule-item.status-scheduled { background: #3b82f6; color: white; }
	.schedule-item.status-completed { background: #10b981; color: white; }
	.schedule-item.status-cancelled  { background: #ef4444; color: white; }
	.schedule-item.status-pending   { background: #f59e0b; color: white; }

	.schedule-item:hover { transform: translateY(-2px); box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15); }
	.schedule-item:active { cursor: grabbing; }

	.schedule-time { font-weight: 700; font-size: 12px; background: rgba(255, 255, 255, 0.2); padding: 2px 5px; border-radius: 3px; text-align: center; }
	.schedule-house { font-weight: 600; font-size: 12px; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

	.schedule-status {
		font-size: 9px;
		text-transform: uppercase;
		font-weight: 700;
		letter-spacing: 0.4px;
		background: rgba(255, 255, 255, 0.18);
		padding: 2px 5px;
		border-radius: 3px;
		text-align: center;
	}

	/* -- Add Button -- */
	.add-schedule-btn {
		width: 100%;
		height: 100%;
		min-height: 80px;
		background: transparent;
		border: 2px dashed rgba(0, 0, 0, 0.08);
		border-radius: 6px;
		cursor: pointer;
		font-size: 22px;
		color: rgba(0, 0, 0, 0.15);
		transition: all 0.2s;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.add-schedule-btn:hover {
		border-color: var(--primary-color);
		color: var(--primary-color);
		background: rgba(52, 152, 219, 0.04);
	}

	/* -- Messages -- */
	.loading-message { padding: 20px; text-align: center; color: var(--muted-color); }
	.error-message   { padding: 20px; text-align: center; color: var(--error-color); }

	/* -- Responsive -- */
	@media (max-width: 768px) {
		.board-header,
		.board-row { grid-template-columns: 120px repeat(7, 110px); }
		.cleaner-header, .cleaner-cell { padding: 8px; }
		.schedule-cell  { min-height: 70px; }
		.schedule-item  { padding: 6px; }
		.nav-date-btn   { min-width: 56px; }
	}
</style>