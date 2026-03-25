<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	
	export let startDate: Date = new Date();
	export let endDate: Date = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000); // default 7 days
	export let minDate: Date | null = null;
	export let maxDate: Date | null = null;
	
	const dispatch = createEventDispatcher();
	
	let currentMonth = new Date(startDate.getFullYear(), startDate.getMonth(), 1);
	let isDragging = false;
	let dragStartDay: Date | null = null;
	
	$: monthDays = getMonthDays(currentMonth);
	$: weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
	
	function getMonthDays(month: Date) {
		const year = month.getFullYear();
		const monthIndex = month.getMonth();
		const firstDay = new Date(year, monthIndex, 1);
		const lastDay = new Date(year, monthIndex + 1, 0);
		const days = [];
		
		// Add empty slots for days before first day of month
		for (let i = 0; i < firstDay.getDay(); i++) {
			days.push({ date: null, day: null, currentMonth: false });
		}
		
		// Add days of month
		for (let d = 1; d <= lastDay.getDate(); d++) {
			const date = new Date(year, monthIndex, d);
			days.push({ date, day: d, currentMonth: true });
		}
		
		return days;
	}
	
	function prevMonth() {
		currentMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1);
	}
	
	function nextMonth() {
		currentMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1);
	}
	
	function isDateInRange(date: Date) {
		return date >= startDate && date <= endDate;
	}
	
	function isDateSelected(date: Date) {
		return date.getTime() === startDate.getTime() || date.getTime() === endDate.getTime();
	}
	
	function handleDayClick(day: { date: Date | null }) {
		if (!day.date) return;
		
		if (!isDragging) {
			// Start new selection
			startDate = day.date;
			endDate = new Date(day.date.getTime() + 7 * 24 * 60 * 60 * 1000); // default 7 days
			isDragging = true;
			dragStartDay = day.date;
		} else {
			// Complete selection
			if (day.date < dragStartDay) {
				startDate = day.date;
				endDate = dragStartDay;
			} else {
				startDate = dragStartDay;
				endDate = day.date;
			}
			isDragging = false;
			dragStartDay = null;
			dispatch('rangeSelected', { startDate, endDate });
		}
	}
	
	function handleDayMouseEnter(day: { date: Date | null }) {
		if (isDragging && day.date) {
			// Update end date while dragging
			if (day.date < dragStartDay) {
				startDate = day.date;
				endDate = dragStartDay;
			} else {
				startDate = dragStartDay;
				endDate = day.date;
			}
		}
	}
	
	function handleMouseUp() {
		if (isDragging) {
			isDragging = false;
			dragStartDay = null;
			dispatch('rangeSelected', { startDate, endDate });
		}
	}
	
	function formatDate(date: Date) {
		return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	}
</script>

<svelte:window on:mouseup={handleMouseUp} />

<div class="calendar-month">
	<div class="calendar-header">
		<button type="button" class="nav-btn" on:click={prevMonth}>&lt;</button>
		<div class="month-title">
			{currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
		</div>
		<button type="button" class="nav-btn" on:click={nextMonth}>&gt;</button>
	</div>
	
	<div class="calendar-grid">
		{#each weekDays as day}
			<div class="weekday">{day}</div>
		{/each}
		
		{#each monthDays as day}
			<div 
				class="day-cell"
				class:empty={!day.date}
				class:in-range={day.date && isDateInRange(day.date)}
				class:selected={day.date && isDateSelected(day.date)}
				on:click={() => handleDayClick(day)}
				on:mouseenter={() => handleDayMouseEnter(day)}
			>
				{#if day.date}
					<div class="day-number">{day.day}</div>
				{/if}
			</div>
		{/each}
	</div>
	
	<div class="selected-range">
		<span>Selected: {formatDate(startDate)} - {formatDate(endDate)}</span>
	</div>
</div>

<style>
	.calendar-month {
		border: 1px solid var(--border-color, #e5e7eb);
		border-radius: 8px;
		padding: 1rem;
		background: white;
		user-select: none;
	}
	
	.calendar-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 1rem;
	}
	
	.nav-btn {
		background: none;
		border: 1px solid #d1d5db;
		border-radius: 4px;
		padding: 0.25rem 0.5rem;
		cursor: pointer;
	}
	
	.nav-btn:hover {
		background: #f3f4f6;
	}
	
	.month-title {
		font-weight: 600;
		font-size: 1.1rem;
	}
	
	.calendar-grid {
		display: grid;
		grid-template-columns: repeat(7, 1fr);
		gap: 2px;
		margin-bottom: 1rem;
	}
	
	.weekday {
		text-align: center;
		font-weight: 500;
		font-size: 0.75rem;
		color: #6b7280;
		padding: 0.5rem 0;
	}
	
	.day-cell {
		aspect-ratio: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 4px;
		cursor: pointer;
		font-size: 0.875rem;
	}
	
	.day-cell.empty {
		background: transparent;
		cursor: default;
	}
	
	.day-cell:not(.empty):hover {
		background: #e5e7eb;
	}
	
	.day-cell.in-range {
		background: #dbeafe;
	}
	
	.day-cell.selected {
		background: #3b82f6;
		color: white;
	}
	
	.day-number {
		font-weight: 500;
	}
	
	.selected-range {
		text-align: center;
		font-size: 0.875rem;
		color: #374151;
	}
</style>