import "clsx";
const notificationsState = { value: [] };
const notificationActions = {
  success: (message) => {
    addNotification(message, "success");
  },
  error: (message) => {
    addNotification(message, "error");
  },
  warning: (message) => {
    addNotification(message, "warning");
  },
  info: (message) => {
    addNotification(message, "info");
  },
  clear: () => {
    notificationsState.value = [];
  },
  remove: (id) => {
    notificationsState.value = notificationsState.value.filter((n) => n.id !== id);
  }
};
function addNotification(message, type) {
  const id = Date.now() + Math.random();
  notificationsState.value = [...notificationsState.value, { id, message, type }];
  const timeout = type === "success" || type === "info" ? 5e3 : 8e3;
  setTimeout(
    () => {
      notificationActions.remove(id);
    },
    timeout
  );
}
const createStore = (initialValue = []) => {
  let value = initialValue;
  return {
    /** @param {function} fn */
    subscribe: (fn) => {
      fn(value);
      return () => {
      };
    },
    /** @param {any} newValue */
    set: (newValue) => {
      value = newValue;
    },
    /** @param {function} fn */
    update: (fn) => {
      value = fn(value);
    }
  };
};
const dataStores = {
  cleaners: createStore([]),
  bookings: createStore([]),
  schedules: createStore([]),
  houses: createStore([])
};
export {
  notificationActions as a,
  dataStores as d,
  notificationsState as n
};
