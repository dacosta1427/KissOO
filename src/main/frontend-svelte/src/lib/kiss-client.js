/**
 * Kiss Framework Client Wrapper
 * Provides a clean interface for Server.call() remote function calls
 */

import { Server } from './services/Server.js';

class KissClient {
	constructor(baseUrl = '') {
		this.baseUrl = baseUrl;
	}

	/**
	 * Make a remote function call using KissOO's Server.call() pattern
	 * @param {string} service - The service name (e.g., 'services.Cleaning')
	 * @param {string} method - The method name (e.g., 'getCleaners')
	 * @param {object} args - Arguments to pass to the remote function
	 * @returns {Promise} - Promise that resolves to the remote function result
	 */
	async call(service, method, args = {}) {
		// Use Server.call with className = service, methodName = method
		return await Server.call(service, method, args);
	}

	// Service-specific convenience methods (optional)
	cleaners = {
		getAll: () => this.call('services.Cleaning', 'getCleaners'),
		getById: (id) => this.call('services.Cleaning', 'getCleaner', { id }),
		create: (data) => this.call('services.Cleaning', 'createCleaner', { data }),
		update: (id, data) => this.call('services.Cleaning', 'updateCleaner', { id, data }),
		delete: (id) => this.call('services.Cleaning', 'deleteCleaner', { id })
	};

	bookings = {
		getAll: (filters) => this.call('services.Cleaning', 'getBookings', { filters }),
		getById: (id) => this.call('services.Cleaning', 'getBooking', { id }),
		create: (data) => this.call('services.Cleaning', 'createBooking', { data }),
		update: (id, data) => this.call('services.Cleaning', 'updateBooking', { id, data }),
		delete: (id) => this.call('services.Cleaning', 'deleteBooking', { id }),
		getByHouse: (houseId) => this.call('services.Cleaning', 'getBookingsByHouse', { houseId }),
		getByDateRange: (startDate, endDate) =>
			this.call('services.Cleaning', 'getBookingsByDateRange', { startDate, endDate })
	};

	schedules = {
		getAll: (filters) => this.call('services.Cleaning', 'getSchedules', { filters }),
		getById: (id) => this.call('services.Cleaning', 'getSchedule', { id }),
		create: (data) => this.call('services.Cleaning', 'createSchedule', { data }),
		update: (id, data) => this.call('services.Cleaning', 'updateSchedule', { id, data }),
		delete: (id) => this.call('services.Cleaning', 'deleteSchedule', { id }),
		getByCleaner: (cleanerId) => this.call('services.Cleaning', 'getSchedulesByCleaner', { cleanerId }),
		getByBooking: (bookingId) => this.call('services.Cleaning', 'getSchedulesByBooking', { bookingId }),
		getByDateRange: (startDate, endDate) =>
			this.call('services.Cleaning', 'getSchedulesByDateRange', { startDate, endDate })
	};

	houses = {
		getAll: () => this.call('services.Cleaning', 'getHouses'),
		getById: (id) => this.call('services.Cleaning', 'getHouse', { id }),
		create: (data) => this.call('services.Cleaning', 'createHouse', { data }),
		update: (id, data) => this.call('services.Cleaning', 'updateHouse', { id, data }),
		delete: (id) => this.call('services.Cleaning', 'deleteHouse', { id })
	};
}

export const kissClient = new KissClient();