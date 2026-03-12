<script>
  import { notifications } from '../stores/index.js';
  
  // Svelte 5: Use $derived for reactive store access
  let notificationList = $derived($notifications);
  
  function getNotificationClass(type) {
    switch (type) {
      case 'success': return 'success';
      case 'error': return 'error';
      case 'warning': return 'warning';
      case 'info': return 'info';
      default: return 'info';
    }
  }
  
  function dismissNotification(id) {
    // This would call the store action, but for now we'll just filter locally
    notificationList = notificationList.filter(n => n.id !== id);
  }
</script>

{#if notificationList.length > 0}
  <div class="notification-container">
    {#each notificationList as notification (notification.id)}
      <div class="notification {getNotificationClass(notification.type)}">
        <div class="notification-content">
          <span class="notification-message">{notification.message}</span>
        </div>
        <button class="notification-close" on:click={() => dismissNotification(notification.id)}>
          ✕
        </button>
      </div>
    {/each}
  </div>
{/if}

<style>
  .notification-container {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    gap: 10px;
    max-width: 300px;
  }
  
  .notification {
    background: white;
    border-left: 4px solid #3498db;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    padding: 15px;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    animation: slideIn 0.3s ease-out;
    min-width: 250px;
  }
  
  .notification.success {
    border-left-color: #27ae60;
  }
  
  .notification.error {
    border-left-color: #e74c3c;
  }
  
  .notification.warning {
    border-left-color: #f39c12;
  }
  
  .notification.info {
    border-left-color: #3498db;
  }
  
  .notification-content {
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
  
  .notification-message {
    font-weight: 500;
    color: #333;
  }
  
  .notification-close {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 18px;
    color: #666;
    padding: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s;
  }
  
  .notification-close:hover {
    background-color: #f5f5f5;
  }
  
  @keyframes slideIn {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
  
  @media (max-width: 600px) {
    .notification-container {
      right: 10px;
      left: 10px;
      top: 10px;
    }
    
    .notification {
      min-width: auto;
      max-width: none;
    }
  }
</style>