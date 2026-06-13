<script>
  import { onMount } from 'svelte';
  
  let { row, columns, expanded = $bindable(), onExpand } = $props();
  let detailData = $state(null);
  
  async function toggleExpand() {
    expanded = !expanded;
    if (expanded && onExpand) {
      detailData = await onExpand(row.oid);
    }
  }
</script>

<tr class="expandable-row">
  <td>
    <button class="expand-btn" onclick={toggleExpand}>
      {expanded ? '▼' : '▶'}
    </button>
  </td>
  {#each columns as col}
    <td>{row[col.field]}</td>
  {/each}
</tr>

{#if expanded}
  <tr class="detail-row">
    <td colspan={columns.length + 1}>
      {#if detailData}
        <div class="detail-content">
          {#each Object.entries(detailData) as [key, value]}
            <div class="detail-field">
              <strong>{key}:</strong> {value}
            </div>
          {/each}
        </div>
      {:else}
        <div class="loading-detail">Loading...</div>
      {/if}
    </td>
  </tr>
{/if}

<style>
  .expand-btn {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0;
    font-size: 1rem;
  }
  
  .detail-row td {
    padding: 1rem;
    background: #fafafa;
  }
  
  .detail-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 0.5rem;
  }
  
  .detail-field {
    margin-bottom: 0.25rem;
  }
  
  .loading-detail {
    color: #666;
  }
</style>