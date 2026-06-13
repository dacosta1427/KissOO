<script>
  import { onMount } from 'svelte';
  
  let { data = [], columns = [], loading = false } = $props();
  let sortedData = $state([]);
  let sortColumn = $state('');
  let sortDirection = $state('asc');
  
  onMount(() => {
    sortedData = [...data];
  });
  
  function handleSort(column) {
    if (sortColumn === column) {
      sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      sortColumn = column;
      sortDirection = 'asc';
    }
    
    sortedData = data.slice().sort((a, b) => {
      const aVal = a[column];
      const bVal = b[column];
      if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }
</script>

<div class="data-table-container">
  {#if loading}
    <div class="loading">Loading...</div>
  {:else}
    <table class="data-table">
      <thead>
        <tr>
          {#each columns as col}
            <th onclick={() => handleSort(col.field)}>
              {col.header}
              {#if sortColumn === col.field}
                <span class="sort-indicator">{sortDirection === 'asc' ? '↑' : '↓'}</span>
              {/if}
            </th>
          {/each}
        </tr>
      </thead>
      <tbody>
        {#each sortedData as row (row.oid)}
          <tr>
            {#each columns as col}
              <td>{row[col.field]}</td>
            {/each}
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</div>

<style>
  .data-table-container {
    overflow-x: auto;
  }
  
  .data-table {
    width: 100%;
    border-collapse: collapse;
  }
  
  .data-table th,
  .data-table td {
    padding: 0.75rem;
    text-align: left;
    border-bottom: 1px solid #eee;
  }
  
  .data-table th {
    background: #f5f5f5;
    cursor: pointer;
    user-select: none;
  }
  
  .data-table th:hover {
    background: #e0e0e0;
  }
  
  .sort-indicator {
    margin-left: 0.25rem;
  }
  
  .loading {
    padding: 2rem;
    text-align: center;
  }
</style>