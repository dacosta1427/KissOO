/**
 * Kiss Remote Functions API Service
 * Provides a clean interface for all remote function calls
 */

import { kissClient } from '$lib/kiss-client.js';
import { loadingActions, errorActions, notificationActions } from '$lib/stores.svelte.js';

/**
 * Execute a remote function with loading states and error handling
 */
async function executeRemoteFunction(service, method, args = {}, operationName = '') {
	const operationKey = `${service}-${method}`;

	loadingActions.start(operationKey);
	errorActions.clear(operationKey);

	try {
		const result = await kissClient.call(service, method, args);
		if (operationName) {
			notificationActions.success(`${operationName} completed successfully`);
		}
		return result;
	} catch (error) {
		const errorMessage = operationName
			? `${operationName} failed: ${error.message}`
			: error.message;
		errorActions.set(operationKey, errorMessage);
		notificationActions.error(errorMessage);
		throw error;
	} finally {
		loadingActions.stop(operationKey);
	}
}

// Cleaners API
export const cleanersAPI = {
	getAll: () => executeRemoteFunction('cleaners', 'getCleaners', {}, 'Load cleaners'),
	getById: (id) => executeRemoteFunction('cleaners', 'getCleaner', { id }, 'Load cleaner'),
	create: (data) => executeRemoteFunction('cleaners', 'createCleaner', { data }, 'Create cleaner'),
	update: (id, data) =>
		executeRemoteFunction('cleaners', 'updateCleaner', { id, data }, 'Update cleaner'),
	delete: (id) => executeRemoteFunction('cleaners', 'deleteCleaner', { id }, 'Delete cleaner')
};

// Bookings API
export const bookingsAPI = {
	getAll: (filters) =>
		executeRemoteFunction('bookings', 'getBookings', { filters }, 'Load bookings'),
	getById: (id) => executeRemoteFunction('bookings', 'getBooking', { id }, 'Load booking'),
	create: (data) => executeRemoteFunction('bookings', 'createBooking', { data }, 'Create booking'),
	update: (id, data) =>
		executeRemoteFunction('bookings', 'updateBooking', { id, data }, 'Update booking'),
	delete: (id) => executeRemoteFunction('bookings', 'deleteBooking', { id }, 'Delete booking'),
	getByHouse: (houseId) =>
		executeRemoteFunction('bookings', 'getBookingsByHouse', { houseId }, 'Load bookings by house'),
	getByDateRange: (startDate, endDate) =>
		executeRemoteFunction(
			'bookings',
			'getBookingsByDateRange',
			{ startDate, endDate },
			'Load bookings by date range'
		)
};

// Schedules API
export const schedulesAPI = {
	getAll: (filters) =>
		executeRemoteFunction('schedules', 'getSchedules', { filters }, 'Load schedules'),
	getById: (id) => executeRemoteFunction('schedules', 'getSchedule', { id }, 'Load schedule'),
	create: (data) =>
		executeRemoteFunction('schedules', 'createSchedule', { data }, 'Create schedule'),
	update: (id, data) =>
		executeRemoteFunction('schedules', 'updateSchedule', { id, data }, 'Update schedule'),
	delete: (id) => executeRemoteFunction('schedules', 'deleteSchedule', { id }, 'Delete schedule'),
	getByCleaner: (cleanerId) =>
		executeRemoteFunction(
			'schedules',
			'getSchedulesByCleaner',
			{ cleanerId },
			'Load schedules by cleaner'
		),
	getByBooking: (bookingId) =>
		executeRemoteFunction(
			'schedules',
			'getSchedulesByBooking',
			{ bookingId },
			'Load schedule by booking'
		),
	getByDateRange: (startDate, endDate) =>
		executeRemoteFunction(
			'schedules',
			'getSchedulesByDateRange',
			{ startDate, endDate },
			'Load schedules by date range'
		)
};

// Houses API
export const housesAPI = {
	getAll: () => executeRemoteFunction('houses', 'getHouses', {}, 'Load houses'),
	getById: (id) => executeRemoteFunction('houses', 'getHouse', { id }, 'Load house'),
	create: (data) => executeRemoteFunction('houses', 'createHouse', { data }, 'Create house'),
	update: (id, data) =>
		executeRemoteFunction('houses', 'updateHouse', { id, data }, 'Update house'),
	delete: (id) => executeRemoteFunction('houses', 'deleteHouse', { id }, 'Delete house')
};
