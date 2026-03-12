<script>
  import { schedulesAPI, cleanersAPI, bookingsAPI } from '../api/kiss-remote.js';
  import { dataStores } from '../../lib/stores.js';
  import ScheduleBoard from '$lib/components/ScheduleBoard.svelte';
  import Form from '$lib/components/Form.svelte';
  
  // Svelte 5: Use $state for reactive variables
  let schedules = $state([]);
  let cleaners = $state([]);
  let bookings = $state([]);
  let loading = $state(false);
  let error = $state(null);
  let showForm = $state(false);
  let editingSchedule = $state(null);
  let dateRange = $state({
    start: new Date().toISOString().split('T')[0],
    end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  });
  
  const scheduleFields = [
    {
      name: 'cleaner_id',
      label: 'Cleaner',
      type: 'select',
      required: true,
      options: []
    },
    {
      name: 'booking_id',
      label: 'Booking',
      type: 'select',
      required: true,
      options: []
    },
    {
      name: 'assigned_date',
      label: 'Assigned Date',
      type: 'date',
      required: true
    },
    {
      name: 'status',
      label: 'Status',
      type: 'select',
      required: true,
      options: [
        { value: 'pending', label: 'Pending' },
        { value: 'confirmed', label: 'Confirmed' },
        { value: 'completed', label: 'Completed' },
        { value: 'cancelled', label: 'Cancelled' }
      ]
    }
  ];
  
  async function loadData() {
    loading = true;
    error = null;
    
    try {
      // Load all data in parallel
      const [schedulesResult, cleanersResult, bookingsResult] = await Promise.all([
        schedulesAPI.getAll({ dateRange }),
        cleanersAPI.getAll(),
        bookingsAPI.getAll()
      ]);
      
      schedules = schedulesResult.data || [];
      cleaners = cleanersResult.data || [];
      bookings = bookingsResult.data || [];
      
      // Update form options
      scheduleFields[0].options = cleaners.map(c => ({ value: c.id, label: c.name }));
      scheduleFields[1].options = bookings.map(b => ({ 
        value: b.id, 
        label: `House: ${b.house_name || 'Unknown'} - ${b.check_in_date} to ${b.check_out_date}` 
      }));
      
      dataStores.schedules.set(schedules);
      dataStores.cleaners.set(cleaners);
      dataStores.bookings.set(bookings);
    } catch (err) {
      error = err.message;
    } finally {
      loading = false;
    }
  }
  
  async function handleScheduleChange(newSchedule) {
    try {
      if (editingSchedule) {
        await schedulesAPI.update(editingSchedule.id, newSchedule);
      } else {
        await schedulesAPI.create(newSchedule);
      }
      
      await loadData();
    } catch (err) {
      error = err.message;
    }
  }
  
  async function handleFormSubmit(data) {
    try {
      if (editingSchedule) {
        await schedulesAPI.update(editingSchedule.id, data);
      } else {
        await schedulesAPI.create(data);
      }
      
      showForm = false;
      editingSchedule = null;
      await loadData();
    } catch (err) {
      error = err.message;
    }
  }
  
  function handleFormCancel() {
    showForm = false;
    editingSchedule = null;
  }
  
  function handleScheduleClick(schedule) {
    editingSchedule = schedule;
    showForm = true;
  }
  
  // Svelte 5: Use $effect for lifecycle management
  $effect(() => {
    loadData();
  });
</script>

<div class="schedules-page">
  <div class="page-header">
    <h1>Scheduling Board</h1>
    <div class="header-actions">
      <button class="btn btn-primary" on:click={() => showForm = true}>
        Add Schedule
      </button>
      <div class="date-range-controls">
        <label>
          Start Date:
          <input 
            type="date" 
            bind:value={dateRange.start}
            on:change={loadData}
          />
        </label>
        <label>
          End Date:
          <input 
            type="date" 
            bind:value={dateRange.end}
            on:change={loadData}
          />
        </label>
      </div>
    </div>
  </div>
  
  {#if showForm}
    <div class="form-section">
      <Form
        fields={scheduleFields}
        data={editingSchedule || {}}
        loading={loading}
        title={editingSchedule ? 'Edit Schedule' : 'Add New Schedule'}
        submitLabel={editingSchedule ? 'Update Schedule' : 'Add Schedule'}
        on:submit={handleFormSubmit}
        on:cancel={handleFormCancel}
      />
    </div>
  {/if}
  
  <div class="board-section">
    <ScheduleBoard
      schedules={schedules}
      cleaners={cleaners}
      bookings={bookings}
      dateRange={dateRange}
      loading={loading}
      error={error}
      on:scheduleChange={handleScheduleChange}
      on:scheduleClick={handleScheduleClick}
    />
  </div>
</div>

<style>
  .schedules-page {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px solid var(--border-color);
    padding-bottom: 1rem;
  }
  
  .page-header h1 {
    margin: 0;
    color: var(--text-color);
  }
  
  .header-actions {
    display: flex;
    gap: 1rem;
    align-items: center;
  }
  
  .btn {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .btn-primary {
    background: var(--primary-color);
    color: white;
  }
  
  .btn-primary:hover {
    background: var(--primary-hover);
  }
  
  .date-range-controls {
    display: flex;
    gap: 1rem;
    align-items: center;
    background: white;
    padding: 0.5rem;
    border-radius: 6px;
    border: 1px solid var(--border-color);
  }
  
  .date-range-controls label {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-color);
  }
  
  .date-range-controls input {
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: 4px;
    font-size: 0.9rem;
  }
  
  .form-section {
    background: white;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 2rem;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }
  
  .board-section {
    background: white;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 1rem;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }
  
  /* Responsive design */
  @media (max-width: 768px) {
    .page-header {
      flex-direction: column;
      gap: 1rem;
      align-items: stretch;
    }
    
    .header-actions {
      flex-direction: column;
      align-items: stretch;
    }
    
    .date-range-controls {
      flex-direction: column;
      align-items: stretch;
    }
    
    .form-section {
      padding: 1rem;
    }
  }
</style>