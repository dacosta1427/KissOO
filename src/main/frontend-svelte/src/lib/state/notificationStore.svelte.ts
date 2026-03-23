/**
 * notificationStore.ts - Reactive notification state management using Svelte 5 runes.
 * Provides toast notifications with success, error, warning, and info types.
 */

// Notification interface
export interface Notification {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

// Reactive state for notifications
let notificationsState = $state<Notification[]>([]);

// Actions for managing notifications
export const notificationActions = {
  /**
   * Add a success notification
   */
  success(message: string): void {
    addNotification(message, 'success');
  },

  /**
   * Add an error notification
   */
  error(message: string): void {
    addNotification(message, 'error');
  },

  /**
   * Add a warning notification
   */
  warning(message: string): void {
    addNotification(message, 'warning');
  },

  /**
   * Add an info notification
   */
  info(message: string): void {
    addNotification(message, 'info');
  },

  /**
   * Clear all notifications
   */
  clear(): void {
    notificationsState = [];
  },

  /**
   * Remove a specific notification by id
   */
  remove(id: number): void {
    notificationsState = notificationsState.filter(n => n.id !== id);
  }
};

// Helper function to add notification
function addNotification(message: string, type: Notification['type']): void {
  const notification: Notification = {
    id: Date.now() + Math.random(), // Ensure unique id
    message,
    type
  };
  notificationsState = [...notificationsState, notification];
  
  // Auto-remove after 5 seconds for success/info, 8 seconds for warning/error
  const timeout = (type === 'success' || type === 'info') ? 5000 : 8000;
  setTimeout(() => {
    notificationActions.remove(notification.id);
  }, timeout);
}

// Export reactive state for derived usage
export const notifications = {
  get value(): Notification[] {
    return notificationsState;
  },
  subscribe(fn: (value: Notification[]) => void): () => void {
    fn(notificationsState);
    // In Svelte 5 runes, we could use $effect but this is a simple subscription
    return () => {};
  }
};