/**
 * Kiss Framework Client Wrapper
 * Provides a clean interface for Server.call() remote function calls
 */

class KissClient {
	constructor(baseUrl = '') {
		this.baseUrl = baseUrl;
	}

	/**
	 * Make a remote function call using Kiss framework's Server.call() pattern
	 * @param {string} service - The service name (e.g., 'cleaners', 'bookings', 'schedules')
	 * @param {string} method - The method name (e.g., 'getCleaners', 'createBooking')
	 * @param {object} args - Arguments to pass to the remote function
	 * @returns {Promise} - Promise that resolves to the remote function result
	 */
	async call(service, method, args = {}) {
		try {
			// Kiss framework expects POST to /api/kiss with {service, method, args}
			const response = await fetch(`${this.baseUrl}/api/kiss`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ service, method, args })
			});

			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`);
			}

			const result = await response.json();

			// Kiss framework returns { _Success: true/false, ...data }
			if (result._Success) {
				return result;
			} else {
				throw new Error(result._Error || 'Remote function call failed');
			}
		} catch (error) {
			console.error('Kiss remote function call failed:', error);
			throw error;
		}
	}

	// Service-specific convenience methods
	cleaners = {
		getAll: () => this.call('cleaners', 'getCleaners'),
		getById: (id) => this.call('cleaners', 'getCleaner', { id }),
		create: (data) => this.call('cleaners', 'createCleaner', { data }),
		update: (id, data) => this.call('cleaners', 'updateCleaner', { id, data }),
		delete: (id) => this.call('cleaners', 'deleteCleaner', { id })
	};

	bookings = {
		getAll: (filters) => this.call('bookings', 'getBookings', { filters }),
		getById: (id) => this.call('bookings', 'getBooking', { id }),
		create: (data) => this.call('bookings', 'createBooking', { data }),
		update: (id, data) => this.call('bookings', 'updateBooking', { id, data }),
		delete: (id) => this.call('bookings', 'deleteBooking', { id }),
		getByHouse: (houseId) => this.call('bookings', 'getBookingsByHouse', { houseId }),
		getByDateRange: (startDate, endDate) =>
			this.call('bookings', 'getBookingsByDateRange', { startDate, endDate })
	};

	schedules = {
		getAll: (filters) => this.call('schedules', 'getSchedules', { filters }),
		getById: (id) => this.call('schedules', 'getSchedule', { id }),
		create: (data) => this.call('schedules', 'createSchedule', { data }),
		update: (id, data) => this.call('schedules', 'updateSchedule', { id, data }),
		delete: (id) => this.call('schedules', 'deleteSchedule', { id }),
		getByCleaner: (cleanerId) => this.call('schedules', 'getSchedulesByCleaner', { cleanerId }),
		getByBooking: (bookingId) => this.call('schedules', 'getSchedulesByBooking', { bookingId }),
		getByDateRange: (startDate, endDate) =>
			this.call('schedules', 'getSchedulesByDateRange', { startDate, endDate })
	};

	houses = {
		getAll: () => this.call('houses', 'getHouses'),
		getById: (id) => this.call('houses', 'getHouse', { id }),
		create: (data) => this.call('houses', 'createHouse', { data }),
		update: (id, data) => this.call('houses', 'updateHouse', { id, data }),
		delete: (id) => this.call('houses', 'deleteHouse', { id })
	};
}

// Export singleton instance
export const kissClient = new KissClient();
