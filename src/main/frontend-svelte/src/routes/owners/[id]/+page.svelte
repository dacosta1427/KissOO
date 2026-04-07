<script lang="ts">
	import { page } from '$app/stores';
	import { ownersAPI, housesAPI, bookingsByHouseAPI, schedulesByBookingAPI, type Owner, type House, type Booking, type Schedule } from '$lib/api/Cleaning';
	import { notificationActions } from '$lib/stores.svelte.js';
	import { t, currentLocale } from '$lib/i18n';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let ownerId = $derived.by(() => {
		const p = $page;
		const id = p.params.id;
		console.log('[owners/[id]] $page.params.id:', id);
		return id ? parseInt(id) : NaN;
	});
	let owner = $state<Owner | null>(null);
	let houses = $state<House[]>([]);
	let loading = $state(true);
	let error = $state<string | null>(null);
	let expandedHouseIds = $state<Set<number>>(new Set());

	interface BookingWithSchedules {
		booking: Booking;
		schedules: Schedule[];
	}

	let houseData = $state<Map<number, BookingWithSchedules[]>>(new Map());

	async function loadOwner() {
		try {
			loading = true;
			const owners = await ownersAPI.getAll();
			owner = owners.find(o => o.id === ownerId) || null;
			if (!owner) {
				error = "Owner not found";
				return;
			}
			
			const allHouses = await housesAPI.getAll();
			console.log('[owners/[id]] allHouses:', allHouses.map(h => ({id: h.id, owner: h.owner})));
			houses = allHouses.filter(h => h.owner === ownerId);
			
			for (const house of houses) {
				const bookings = await bookingsByHouseAPI.getByHouse(house.id);
				const bws: BookingWithSchedules[] = [];
				for (const booking of bookings) {
					const schedules = await schedulesByBookingAPI.getByBooking(booking.id);
					bws.push({ booking, schedules });
				}
				houseData.set(house.id, bws);
			}
			houseData = houseData;
		} catch (e) {
			error = e instanceof Error ? e.message : 'Unknown error';
		} finally {
			loading = false;
		}
	}

	function toggleHouse(houseId: number) {
		if (expandedHouseIds.has(houseId)) {
			expandedHouseIds.delete(houseId);
		} else {
			expandedHouseIds.add(houseId);
		}
		expandedHouseIds = expandedHouseIds;
	}

	function goBack() {
		goto('/owners');
	}

	onMount(() => {
		loadOwner();
	});
</script>

<div class="owner-detail-page">
	<div class="page-header">
		<button class="btn btn-secondary" onclick={goBack}>{tt('common.back')}</button>
		<h1>{owner?.name || tt('owners.title')}</h1>
	</div>

	{#if loading}
		<div class="loading-spinner">{tt('common.loading')}</div>
	{:else if error}
		<div class="error">{error}</div>
	{:else if owner}
		<div class="owner-info">
			<p><strong>Email:</strong> {owner.email}</p>
			<p><strong>Phone:</strong> {owner.phone}</p>
			<p><strong>Address:</strong> {owner.address}</p>
		</div>

		<h2>{tt('nav.houses')} ({houses.length})</h2>
		{#each houses as house}
			<div class="house-card">
				<h3 onclick={() => toggleHouse(house.id)}>{house.name}</h3>
				<p>{house.address}</p>
				
				{#if expandedHouseIds.has(house.id)}
					{@const bws = houseData.get(house.id) || []}
					{#each bws as bw}
						<div class="booking-item">
							<p><strong>Booking:</strong> {bw.booking.check_in_date} - {bw.booking.check_out_date}</p>
							<p>Guest: {bw.booking.guest_name}</p>
							<p>Status: {bw.booking.status}</p>
							
							{#if bw.schedules.length > 0}
								<h4>Schedules:</h4>
								{#each bw.schedules as schedule}
									<p>{schedule.date} {schedule.start_time}-{schedule.end_time} - {schedule.status}</p>
								{/each}
							{/if}
						</div>
					{/each}
				{/if}
			</div>
		{/each}
	{/if}
</div>

<style>
	.page-header {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-bottom: 1rem;
	}
	.owner-info {
		background: #f5f5f5;
		padding: 1rem;
		border-radius: 4px;
		margin-bottom: 1rem;
	}
	.house-card {
		border: 1px solid #ddd;
		padding: 1rem;
		margin-bottom: 0.5rem;
		border-radius: 4px;
	}
	.booking-item {
		margin-left: 1rem;
		padding: 0.5rem;
		background: #fafafa;
	}
	.error {
		color: red;
	}
	.loading-spinner {
		color: #666;
	}
</style>