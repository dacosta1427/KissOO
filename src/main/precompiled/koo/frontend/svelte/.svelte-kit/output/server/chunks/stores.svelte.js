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
export {
  notificationActions as a,
  notificationsState as n
};
