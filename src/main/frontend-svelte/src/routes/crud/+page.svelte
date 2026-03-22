<script lang="ts">
  import { onMount } from 'svelte';
  import { getRecords, addRecord, updateRecord, deleteRecord, runReport, runExport } from '$lib/api/Crud';
  import type { PhoneRecord } from '$lib/api/Crud';
  import AgGridWrapper from '$lib/components/AgGridWrapper.svelte';
  import Modal from '$lib/components/Modal.svelte';
  import { Utils } from '$lib/utils/Utils';

  // State
  let records = $state<PhoneRecord[]>([]);
  let loading = $state(false);
  let error = $state('');
  let dataLoading = $state(true);
  let selectedRow = $state<PhoneRecord | null>(null);

  // Modal state
  let editModalOpen = $state(false);
  let editingRecord = $state<PhoneRecord | null>(null);
  let editFirstName = $state('');
  let editLastName = $state('');
  let editPhoneNumber = $state('');
  let editLoading = $state(false);
  let isAddMode = $state(false);

  // Derived validation
  let canSave = $derived(editFirstName.length >= 1 && editLastName.length >= 1);

  // Grid column definitions
  const columnDefs = [
    { headerName: 'Last Name', field: 'lastName', width: 150 },
    { headerName: 'First Name', field: 'firstName', width: 150 },
    { headerName: 'Phone Number', field: 'phoneNumber', width: 150 }
  ];

  // Grid reference
  let gridRef: AgGridWrapper;

  onMount(() => {
    loadRecords();
  });

  async function loadRecords() {
    dataLoading = true;
    error = '';
    try {
      records = await getRecords();
      if (gridRef) {
        gridRef.setRowData(records);
      }
    } catch (e: any) {
      error = 'Failed to load records: ' + (e.message || 'Unknown error');
    } finally {
      dataLoading = false;
    }
  }

  function openAddModal() {
    editingRecord = null;
    editFirstName = '';
    editLastName = '';
    editPhoneNumber = '';
    isAddMode = true;
    editModalOpen = true;
  }

  function openEditModal(record: PhoneRecord) {
    editingRecord = record;
    editFirstName = record.firstName;
    editLastName = record.lastName;
    editPhoneNumber = record.phoneNumber;
    isAddMode = false;
    editModalOpen = true;
  }

  async function handleSave() {
    if (!canSave) return;
    
    editLoading = true;
    error = '';

    try {
      let res;
      if (isAddMode) {
        res = await addRecord(editFirstName, editLastName, editPhoneNumber);
      } else if (editingRecord) {
        res = await updateRecord(editingRecord.id, editFirstName, editLastName, editPhoneNumber);
      }
      
      if (res && res.success) {
        editModalOpen = false;
        await loadRecords();
      } else if (res) {
        error = res.error || 'Failed to save record';
      }
    } catch (e: any) {
      error = 'Failed to save record: ' + (e.message || 'Unknown error');
    } finally {
      editLoading = false;
    }
  }

  async function handleDeleteRecord(id: number) {
    await Utils.yesNo(
      'Confirm',
      'Are you sure you want to delete this record?',
      async () => {
        loading = true;
        error = '';

        try {
          const res = await deleteRecord(id);
          if (res.success) {
            await loadRecords();
          } else {
            error = res.error || 'Failed to delete record';
          }
        } catch (e: any) {
          error = 'Failed to delete record: ' + (e.message || 'Unknown error');
        } finally {
          loading = false;
        }
      }
    );
  }

  function handleSelectionChanged(selectedRows: PhoneRecord[]) {
    selectedRow = selectedRows.length === 1 ? selectedRows[0] : null;
  }

  function handleRowDoubleClicked(record: PhoneRecord) {
    openEditModal(record);
  }

  async function handleReport() {
    const res = await runReport();
    if (res.success && res.reportUrl) {
      Utils.showReport(res.reportUrl);
    } else if (res.error) {
      await Utils.showMessage('Error', res.error);
    }
  }

  async function handleExport() {
    const res = await runExport();
    if (res.success && res.exportUrl) {
      Utils.showReport(res.exportUrl);
    } else if (res.error) {
      await Utils.showMessage('Error', res.error);
    }
  }
</script>

<div class="p-6 max-w-6xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">Phone Book (CRUD)</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  <!-- Action Buttons -->
  <div class="mb-4 flex gap-2">
    <button
      onclick={openAddModal}
      disabled={loading}
      class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
    >
      New
    </button>
    <button
      onclick={() => selectedRow && openEditModal(selectedRow)}
      disabled={!selectedRow || loading}
      class="bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
    >
      Edit
    </button>
    <button
      onclick={() => selectedRow && handleDeleteRecord(selectedRow.id)}
      disabled={!selectedRow || loading}
      class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
    >
      Delete
    </button>
    <button
      onclick={handleReport}
      disabled={loading}
      class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
    >
      Report
    </button>
    <button
      onclick={handleExport}
      disabled={loading}
      class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
    >
      Export
    </button>
  </div>

  <p class="text-gray-600 mb-4">
    (Note that the Report function will not work unless the underlying system has groff installed and the ability to generate PDF files.
    This is generally true of Linux and Mac systems but not true on Windows. A version for Windows must be installed.)
  </p>

  <!-- Data Grid -->
  {#if dataLoading}
    <p class="text-gray-500">Loading records...</p>
  {:else}
    <AgGridWrapper
      bind:this={gridRef}
      {columnDefs}
      rowData={records}
      keyColumn="id"
      onSelectionChanged={handleSelectionChanged}
      onRowDoubleClicked={handleRowDoubleClicked}
      style="height: 400px; width: 100%;"
    />
  {/if}
</div>

<!-- Add/Edit Record Modal -->
<Modal 
  bind:open={editModalOpen} 
  title={isAddMode ? 'Add Record' : 'Edit Record'}
  onClose={() => editModalOpen = false}
>
  <div class="space-y-4">
    <div>
      <label class="block text-sm font-medium text-gray-700">First Name</label>
      <input
        type="text"
        bind:value={editFirstName}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        maxlength="20"
        required
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700">Last Name</label>
      <input
        type="text"
        bind:value={editLastName}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        maxlength="20"
        required
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700">Phone Number</label>
      <input
        type="text"
        bind:value={editPhoneNumber}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        maxlength="25"
      />
    </div>
  </div>
  
  {#snippet footer()}
    <button
      type="button"
      class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
      onclick={handleSave}
      disabled={editLoading || !canSave}
    >
      {editLoading ? 'Saving...' : 'OK'}
    </button>
    <button
      type="button"
      class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
      onclick={() => editModalOpen = false}
    >
      Cancel
    </button>
  {/snippet}
</Modal>