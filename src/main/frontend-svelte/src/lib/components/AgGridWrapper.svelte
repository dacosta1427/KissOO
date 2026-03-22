<script lang="ts">
  import { onMount, onDestroy } from 'svelte';
  import { createGrid, type GridApi, type GridOptions } from 'ag-grid-community';
  import 'ag-grid-community/styles/ag-grid.css';
  import 'ag-grid-community/styles/ag-theme-quartz.css';

  interface Props {
    columnDefs: any[];
    rowData?: any[];
    keyColumn?: string;
    onSelectionChanged?: (selectedRows: any[]) => void;
    onRowDoubleClicked?: (row: any) => void;
    singleSelection?: boolean;
    suppressHorizontalScroll?: boolean;
    style?: string;
    class?: string;
  }

  let {
    columnDefs,
    rowData = [],
    keyColumn,
    onSelectionChanged,
    onRowDoubleClicked,
    singleSelection = true,
    suppressHorizontalScroll = true,
    style = 'height: 400px; width: 100%;',
    class: className = ''
  }: Props = $props();

  let gridDiv: HTMLDivElement;
  let api: GridApi | null = $state(null);
  let gridOptions: GridOptions;

  onMount(() => {
    gridOptions = {
      columnDefs,
      rowData,
      rowSelection: {
        mode: singleSelection ? 'singleRow' : 'multiRow',
        enableDeselection: singleSelection,
        enableClickSelection: true,
        checkboxes: false,
        headerCheckbox: false
      },
      suppressHorizontalScroll,
      suppressRowHoverHighlight: false,
      suppressCellFocus: true,
      defaultColDef: {
        resizable: true
      },
      onGridReady: (params) => {
        api = params.api;
        if (suppressHorizontalScroll) {
          params.api.sizeColumnsToFit();
          window.addEventListener('resize', () => {
            setTimeout(() => {
              if (gridDiv.offsetParent != null) {
                params.api.sizeColumnsToFit();
              }
            }, 100);
          });
        }
      },
      onSelectionChanged: () => {
        if (api && onSelectionChanged) {
          const selectedRows = api.getSelectedRows();
          onSelectionChanged(selectedRows);
        }
      },
      onRowDoubleClicked: (event) => {
        if (onRowDoubleClicked && event.data) {
          onRowDoubleClicked(event.data);
        }
      }
    };

    if (keyColumn) {
      gridOptions.getRowId = (params) => String(params.data[keyColumn]);
    }

    // Add theme class
    gridDiv.classList.add('ag-theme-quartz');
    
    // Create grid
    createGrid(gridDiv, gridOptions);
  });

  onDestroy(() => {
    if (api) {
      api.destroy();
    }
  });

  // Expose methods via imperative handle
  export function setRowData(data: any[]) {
    if (api) {
      api.setGridOption('rowData', data);
    }
  }

  export function addRecords(records: any[]) {
    if (api) {
      api.applyTransaction({ add: records });
    }
  }

  export function clear() {
    if (api) {
      api.setGridOption('rowData', []);
    }
  }

  export function getSelectedRows(): any[] {
    return api ? api.getSelectedRows() : [];
  }

  export function getSelectedRow(): any | null {
    const rows = getSelectedRows();
    return rows.length === 1 ? rows[0] : null;
  }

  export function deleteRow(id: string | number) {
    if (api && keyColumn) {
      const node = api.getRowNode(String(id));
      if (node && node.data) {
        api.applyTransaction({ remove: [node.data] });
      }
    }
  }

  export function updateRow(row: any) {
    if (api) {
      api.applyTransaction({ update: [row] });
    }
  }

  export function deselectAll() {
    if (api) {
      api.deselectAll();
    }
  }

  export function sizeColumnsToFit() {
    if (api) {
      api.sizeColumnsToFit();
    }
  }

  // Reactive updates for props
  $effect(() => {
    if (api && columnDefs) {
      api.setGridOption('columnDefs', columnDefs);
    }
  });

  $effect(() => {
    if (api && rowData) {
      api.setGridOption('rowData', rowData);
    }
  });

  $effect(() => {
    if (api) {
      api.setGridOption('rowSelection', {
        mode: singleSelection ? 'singleRow' : 'multiRow',
        enableDeselection: singleSelection,
        enableClickSelection: true,
        checkboxes: false,
        headerCheckbox: false
      });
    }
  });
</script>

<div 
  bind:this={gridDiv} 
  class="{className}"
  style="{style}"
></div>