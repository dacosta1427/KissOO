// Svelte 5 state management - using reactive object pattern
// See: https://joyofcode.xyz/how-to-share-state-in-svelte-5

// Reactive state object - export as object so properties can be mutated
/** @type {{value: any}} */
export const userState = $state({
	value: null
});

/** @type {{value: any[]}} */
export const notificationsState = $state({
	value: []
});

// User actions
export const userActions = {
	login: (userData) => {
		userState.value = userData;
	},
	logout: () => {
		userState.value = null;
	}
};

// Notifications actions
export const notificationActions = {
	success: (message) => {
		addNotification(message, 'success');
	},
	error: (message) => {
		addNotification(message, 'error');
	},
	warning: (message) => {
		addNotification(message, 'warning');
	},
	info: (message) => {
		addNotification(message, 'info');
	},
	clear: () => {
		notificationsState.value = [];
	},
	remove: (id) => {
		notificationsState.value = notificationsState.value.filter((n) => n.id !== id);
	}
};

// Helper to add notification with auto-dismiss
function addNotification(message, type) {
	const id = Date.now() + Math.random();
	notificationsState.value = [
		...notificationsState.value,
		{ id, message, type }
	];
	// Auto-remove after 5 seconds for success/info, 8 seconds for warning/error
	const timeout = (type === 'success' || type === 'info') ? 5000 : 8000;
	setTimeout(() => {
		notificationActions.remove(id);
	}, timeout);
}

// Data stores for sharing data across components
/** @param {any[]} [initialValue] */
const createStore = (initialValue = []) => {
	/** @type {any} */
	let value = $state(initialValue);
	return {
		/** @param {function} fn */
		subscribe: (fn) => {
			fn(value);
			return () => {};
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

export const dataStores = {
	cleaners: createStore([]),
	bookings: createStore([]),
	schedules: createStore([]),
	houses: createStore([])
};

// Loading actions
/** @type {{start: (key: string) => void, stop: (key: string) => void}} */
export const loadingActions = {
	start: (_key) => {},
	stop: (_key) => {}
};

// Error actions
/** @type {{set: (key: string, message: string) => void, clear: (key: string) => void}} */
export const errorActions = {
	set: (_key, _message) => {},
	clear: (_key) => {}
};

// Legacy store exports for backward compatibility
export const user = {
	subscribe: (fn) => {
		fn(userState.value);
		return () => {};
	},
	set: userActions.login,
	update: (fn) => {
		userState.value = fn(userState.value);
	}
};

export const notifications = {
	subscribe: (fn) => {
		fn(notificationsState.value);
		return () => {};
	},
	set: (value) => {
		notificationsState.value = value;
	},
	update: (fn) => {
		notificationsState.value = fn(notificationsState.value);
	}
};
