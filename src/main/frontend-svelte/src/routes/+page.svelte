<script>
  import { apiService } from '../services/api.js';
  import { loadingActions, errorActions, notificationActions } from '$lib/stores.js';

  // Svelte 5: Use $state for reactive variables
  let cleaners = $state([]);
  let bookings = $state([]);
  let schedules = $state([]);
  let loading = $state(false);
  let error = $state(null);

  async function loadDashboardData() {
    loadingActions.start('dashboard');
    errorActions.clear('dashboard');

    try {
      // Load all data in parallel
      const [cleanersData, bookingsData, schedulesData] = await Promise.all([
        apiService.getCleaners(),
        apiService.getBookings(),
        apiService.getSchedules()
      ]);

      cleaners = cleanersData;
      bookings = bookingsData;
      schedules = schedulesData;

      notificationActions.success(`Loaded ${cleaners.length} cleaners, ${bookings.length} bookings, ${schedules.length} schedules`);
    } catch (err) {
      const errorMessage = 'Failed to load dashboard data';
      errorActions.set('dashboard', errorMessage);
      notificationActions.error(errorMessage);
      console.error('Error loading dashboard:', err);
    } finally {
      loadingActions.stop('dashboard');
    }
  }

  // Svelte 5: Use $effect for lifecycle management
  $effect(() => {
    loadDashboardData();
  });
</script>

<div class="dashboard">
  <header class="dashboard-header">
    <h1>CleaningScheduler Dashboard</h1>
    <p>Your vacation rental cleaning management system</p>
  </header>

  <div class="stats-grid">
    <div class="stat-card">
      <div class="stat-number">{cleaners.length}</div>
      <div class="stat-label">Cleaners</div>
    </div>
    <div class="stat-card">
      <div class="stat-number">{bookings.length}</div>
      <div class="stat-label">Bookings</div>
    </div>
    <div class="stat-card">
      <div class="stat-number">{schedules.length}</div>
      <div class="stat-label">Schedules</div>
    </div>
  </div>

  <div class="actions-grid">
    <a href="/cleaners" class="action-btn">Manage Cleaners</a>
    <a href="/bookings" class="action-btn">View Bookings</a>
    <a href="/schedules" class="action-btn">Schedule Cleanings</a>
  </div>
</div>

<style>
  .dashboard {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  .dashboard-header {
    text-align: center;
    margin-bottom: 3rem;
  }

  .dashboard-header h1 {
    color: #2c3e50;
    margin-bottom: 0.5rem;
  }

  .dashboard-header p {
    color: #7f8c8d;
    font-size: 1.1rem;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 2rem;
    margin-bottom: 3rem;
  }

  .stat-card {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    text-align: center;
    border: 1px solid #e0e0e0;
  }

  .stat-number {
    font-size: 2.5rem;
    font-weight: bold;
    color: #3498db;
    margin-bottom: 0.5rem;
  }

  .stat-label {
    font-size: 1.1rem;
    color: #7f8c8d;
    text-transform: uppercase;
    letter-spacing: 1px;
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
  }

  .action-btn {
    display: block;
    padding: 1.5rem;
    background: #3498db;
    color: white;
    text-decoration: none;
    border-radius: 8px;
    text-align: center;
    font-weight: 600;
    transition: all 0.2s;
    border: 1px solid #2980b9;
  }

  .action-btn:hover {
    background: #2980b9;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  }

  @media (max-width: 768px) {
    .dashboard {
      padding: 1rem;
    }

    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
