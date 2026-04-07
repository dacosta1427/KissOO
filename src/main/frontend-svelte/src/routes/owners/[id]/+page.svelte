<script lang="ts">
	import { page } from '$app/stores';
	import { ownersAPI, housesAPI, bookingsByHouseAPI, schedulesByBookingAPI, type Owner, type House, type Booking, type Schedule } from '$lib/api/Cleaning';
	import { t, currentLocale } from '$lib/i18n';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';

	const tt = (key: string) => t(key, undefined, $currentLocale);

	let params = $state<{id?: string}>({});
	
	$effect(() => {
		params = $page.params as {id?: string};
	});
	
	let ownerId = $derived(params.id ? parseInt(params.id) : NaN);

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
		if (isNaN(ownerId)) {
			error = "Invalid owner ID";
			loading = false;
			return;
		}
		try {
			loading = true;
			const allOwners = await ownersAPI.getAll();
			owner = allOwners.find(o => o.id === ownerId) || null;
			if (!owner) {
				error = "Owner not found";
				return;
			}
			
			const allHouses = await housesAPI.getAll();
			houses = allHouses.filter(h => h.owner === ownerId);
			
			const newHouseData = new Map();
			for (const house of houses) {
				const bookings = await bookingsByHouseAPI.getByHouse(house.id);
				const bws: BookingWithSchedules[] = [];
				for (const booking of bookings) {
					const schedules = await schedulesByBookingAPI.getByBooking(booking.id);
					bws.push({ booking, schedules });
				}
				newHouseData.set(house.id, bws);
			}
			houseData = newHouseData;
		} catch (e) {
			error = e instanceof Error ? e.message : 'Unknown error';
		} finally {
			loading = false;
		}
	}

	function toggleHouse(houseId: number) {
		const newSet = new Set(expandedHouseIds);
		if (newSet.has(houseId)) {
			newSet.delete(houseId);
		} else {
			newSet.add(houseId);
		}
		expandedHouseIds = newSet;
	}

	function goBack() {
		goto('/owners');
	}

	onMount(() => {
		loadOwner();
	});
</script>

<div class="max-w-6xl mx-auto px-4 py-6">
	<div class="flex items-center justify-between mb-6 pb-4 border-b border-gray-200">
		<button class="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg font-medium text-gray-700" onclick={goBack}>
			{tt('common.back')}
		</button>
		<h1 class="text-2xl font-semibold text-gray-900">{owner?.name || tt('owners.title')}</h1>
		<div></div>
	</div>

	{#if loading}
		<div class="text-center py-8 text-gray-500">{tt('common.loading')}</div>
	{:else if error}
		<div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">{error}</div>
	{:else if owner}
		<div class="bg-gradient-to-br from-gray-50 to-gray-100 p-6 rounded-lg mb-6 grid grid-cols-1 md:grid-cols-3 gap-4">
			<div>
				<span class="block text-xs uppercase tracking-wide text-gray-500 mb-1">Email</span>
				<span class="text-gray-800">{owner.email || '-'}</span>
			</div>
			<div>
				<span class="block text-xs uppercase tracking-wide text-gray-500 mb-1">Phone</span>
				<span class="text-gray-800">{owner.phone || '-'}</span>
			</div>
			<div>
				<span class="block text-xs uppercase tracking-wide text-gray-500 mb-1">Address</span>
				<span class="text-gray-800">{owner.address || '-'}</span>
			</div>
		</div>

		<h2 class="text-xl font-semibold text-gray-900 mb-4">
			{tt('nav.houses')} ({houses.length})
		</h2>

		{#if houses.length === 0}
			<div class="text-gray-500 text-center py-4">No houses</div>
		{:else}
			{#each houses as house}
				<div class="bg-white border border-gray-200 rounded-lg mb-3 overflow-hidden">
					<button type="button" class="w-full px-4 py-3 text-left hover:bg-gray-50" onclick={() => toggleHouse(house.id)}>
						<div class="flex justify-between items-center">
							<span class="font-medium text-gray-900">{house.name}</span>
							<span class="text-gray-400">{expandedHouseIds.has(house.id) ? '▼' : '▶'}</span>
						</div>
						<div class="text-sm text-gray-500 mt-1">{house.address}</div>
					</button>
					
					{#if expandedHouseIds.has(house.id)}
						{@const bws = houseData.get(house.id) || []}
						<div class="border-t border-gray-100 p-4 bg-gray-50">
							{#if bws.length === 0}
								<div class="text-gray-500 text-sm">No bookings</div>
							{:else}
								{#each bws as bw}
									<div class="mb-3 pb-3 border-b border-gray-200 last:border-0">
										<div class="font-medium text-gray-800">{bw.booking.check_in_date} → {bw.booking.check_out_date}</div>
										<div class="text-sm text-gray-600">Guest: {bw.booking.guest_name}</div>
										<div class="text-xs text-gray-500 mt-1">Status: {bw.booking.status}</div>
										
										{#if bw.schedules.length > 0}
											<div class="mt-2 pl-3 border-l-2 border-blue-400">
												{#each bw.schedules as schedule}
													<div class="text-sm text-gray-600">
														{schedule.date} {schedule.start_time}-{schedule.end_time}
														<span class="ml-2 px-1.5 py-0.5 bg-gray-200 rounded text-xs">{schedule.status}</span>
													</div>
												{/each}
											</div>
										{/if}
									</div>
								{/each}
							{/if}
						</div>
					{/if}
				</div>
			{/each}
		{/if}
	{/if}
</div>