<script>
  // Svelte 5: Event handling using props
  export let data = [];
  export let columns = [];
  export let actions = [];
  export let loading = false;
  export let error = null;
  export let selectedRow = null;
  
  // Event handlers passed as props
  let onAction = $props().onAction;
  let onRowClick = $props().onRowClick;
  
  function handleAction(action, row) {
    onAction?.({ action, row });
  }
  
  function handleRowClick(row) {
    onRowClick?.(row);
  }
</script>

<div class="table-container">
  {#if error}
    <div class="error-message">{error}</div>
  {:else if loading}
    <div class="loading-message">Loading...</div>
  {:else if data.length === 0}
    <div class="empty-message">No data available</div>
  {:else}
    <table class="data-table">
      <thead>
        <tr>
          {#each columns as column}
            <th>{column.label}</th>
          {/each}
          {#if actions.length > 0}
            <th>Actions</th>
          {/if}
        </tr>
      </thead>
      <tbody>
        {#each data as row, index}
          <tr 
            class:selected={selectedRow === row.id}
            on:click={() => handleRowClick(row)}
          >
            {#each columns as column}
              <td>
                {#if column.formatter}
                  {column.formatter(row[column.key], row)}
                {:else}
                  {row[column.key]}
                {/if}
              </td>
            {/each}
            {#if actions.length > 0}
              <td class="actions-cell">
                {#each actions as action}
                  <button 
                    class="action-btn"
                    on:click|stopPropagation={() => handleAction(action, row)}
                    title={action.title}
                  >
                    {action.icon && <span class="action-icon">{action.icon}</span>}
                    {action.label}
                  </button>
                {/each}
              </td>
            {/if}
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</div>

<style>
  .table-container {
    overflow-x: auto;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    background: white;
  }
  
  .data-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
  }
  
  .data-table th {
    background: var(--table-header-bg);
    color: var(--table-header-text);
    padding: 12px;
    text-align: left;
    font-weight: 600;
    border-bottom: 2px solid var(--border-color);
    position: sticky;
    top: 0;
    z-index: 1;
  }
  
  .data-table td {
    padding: 12px;
    border-bottom: 1px solid var(--border-color);
    vertical-align: middle;
  }
  
  .data-table tr:hover {
    background-color: var(--row-hover-bg);
  }
  
  .data-table tr.selected {
    background-color: var(--row-selected-bg);
    outline: 2px solid var(--primary-color);
  }
  
  .actions-cell {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
  
  .action-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    border: 1px solid var(--border-color);
    border-radius: 6px;
    background: white;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.2s;
  }
  
  .action-btn:hover {
    background: var(--primary-color);
    color: white;
    border-color: var(--primary-color);
  }
  
  .action-btn.edit:hover { background: var(--edit-color); border-color: var(--edit-color); }
  .action-btn.delete:hover { background: var(--delete-color); border-color: var(--delete-color); }
  .action-btn.view:hover { background: var(--view-color); border-color: var(--view-color); }
  
  .loading-message, .error-message, .empty-message {
    padding: 20px;
    text-align: center;
    color: var(--text-color);
  }
  
  .error-message { color: var(--error-color); }
  .empty-message { color: var(--muted-color); }
</style>