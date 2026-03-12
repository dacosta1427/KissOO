// Svelte 5 state management - using reactive object pattern
// See: https://joyofcode.xyz/how-to-share-state-in-svelte-5

// Reactive state object - export as object so properties can be mutated
export const userState = $state({
	value: null
});

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
		notificationsState.value = [
			...notificationsState.value,
			{ id: Date.now(), message, type: 'success' }
		];
	},
	error: (message) => {
		notificationsState.value = [
			...notificationsState.value,
			{ id: Date.now(), message, type: 'error' }
		];
	},
	warning: (message) => {
		notificationsState.value = [
			...notificationsState.value,
			{ id: Date.now(), message, type: 'warning' }
		];
	},
	info: (message) => {
		notificationsState.value = [
			...notificationsState.value,
			{ id: Date.now(), message, type: 'info' }
		];
	},
	clear: () => {
		notificationsState.value = [];
	},
	remove: (id) => {
		notificationsState.value = notificationsState.value.filter((n) => n.id !== id);
	}
};

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
