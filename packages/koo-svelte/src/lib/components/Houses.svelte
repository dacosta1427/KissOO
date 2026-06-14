<script>
  import { DataTable, Modal, openModal } from 'koo-svelte';
  import { HouseProto } from '$lib/generated/proto.js';
  import { Server } from 'kiss/Server.js';
  
  let houses = $state([]);
  let loading = $state(false);
  
  async function loadHouses() {
    loading = true;
    try {
      const response = await Server.binaryCall('HouseService', 'searchHouses', {});
      if (response._Success) {
        houses = response._data.map(d => HouseProto.decode(d));
      }
    } catch (error) {
      console.error('Failed to load houses:', error);
    } finally {
      loading = false;
    }
  }
  
  const columns = [
    { field: 'address', header: 'Address' },
    { field: 'city', header: 'City' },
    { field: 'bedrooms', header: 'Bedrooms' },
    { field: 'bathrooms', header: 'Bathrooms' }
  ];
</script>

<div class="houses-page">
  <h1>Houses</h1>
  <button onclick={loadHouses}>Load Houses</button>
  
  {#if loading}
    <div>Loading houses...</div>
  {:else}
    <DataTable data={houses} columns={columns} />
  {/if}
</div>

<style>
  .houses-page {
    padding: 2rem;
  }
  
  button {
    padding: 0.5rem 1rem;
    background: #1976d2;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
</style>