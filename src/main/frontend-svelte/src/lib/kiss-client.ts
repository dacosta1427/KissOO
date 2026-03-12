/**
 * Kiss Framework Client Wrapper
 * Provides a clean interface for Server.call() remote function calls
 */

// Type definitions for Kiss framework response
interface KissResponse<T = any> {
	_Success: boolean;
	_Error?: string;
	[key: string]: any;
}

interface KissCallOptions {
	service: string;
	method: string;
	args?: Record<string, any>;
}

// Service-specific types
interface Cleaner {
	id: string;
	name: string;
	email: string;
	phone?: string;
	active: boolean;
	createdAt: string;
	updatedAt: string;
}

interface Booking {
	id: string;
	houseId: string;
	startDate: string;
	endDate: string;
	guestName: string;
	guestEmail: string;
	status: 'pending' | 'confirmed' | 'cancelled';
	createdAt: string;
	updatedAt: string;
}

interface Schedule {
	id: string;
	cleanerId: string;
	bookingId: string;
	houseId: string;
	date: string;
	startTime: string;
	endTime: string;
	status: 'pending' | 'in_progress' | 'completed' | 'cancelled';
	notes?: string;
	createdAt: string;
	updatedAt: string;
}

interface House {
	id: string;
	name: string;
	address: string;
	bedrooms: number;
	bathrooms: number;
	maxGuests: number;
	amenities: string[];
	active: boolean;
	createdAt: string;
	updatedAt: string;
}

class KissClient {
	private baseUrl: string;

	constructor(baseUrl: string = '') {
		this.baseUrl = baseUrl;
	}

	/**
	 * Make a remote function call using Kiss framework's Server.call() pattern
	 * @param service - The service name (e.g., 'cleaners', 'bookings', 'schedules')
	 * @param method - The method name (e.g., 'getCleaners', 'createBooking')
	 * @param args - Arguments to pass to the remote function
	 * @returns Promise that resolves to the remote function result
	 */
	async call<T = any>(
		service: string,
		method: string,
		args: Record<string, any> = {}
	): Promise<KissResponse<T>> {
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

			const result: KissResponse<T> = await response.json();

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

	// Service-specific convenience methods with proper typing
	cleaners = {
		getAll: () => this.call<Cleaner[]>('cleaners', 'getCleaners'),
		getById: (id: string) => this.call<Cleaner>('cleaners', 'getCleaner', { id }),
		create: (data: Omit<Cleaner, 'id' | 'createdAt' | 'updatedAt'>) =>
			this.call<Cleaner>('cleaners', 'createCleaner', { data }),
		update: (id: string, data: Partial<Cleaner>) =>
			this.call<Cleaner>('cleaners', 'updateCleaner', { id, data }),
		delete: (id: string) => this.call('cleaners', 'deleteCleaner', { id })
	};

	bookings = {
		getAll: (filters?: Record<string, any>) =>
			this.call<Booking[]>('bookings', 'getBookings', { filters }),
		getById: (id: string) => this.call<Booking>('bookings', 'getBooking', { id }),
		create: (data: Omit<Booking, 'id' | 'createdAt' | 'updatedAt'>) =>
			this.call<Booking>('bookings', 'createBooking', { data }),
		update: (id: string, data: Partial<Booking>) =>
			this.call<Booking>('bookings', 'updateBooking', { id, data }),
		delete: (id: string) => this.call('bookings', 'deleteBooking', { id }),
		getByHouse: (houseId: string) =>
			this.call<Booking[]>('bookings', 'getBookingsByHouse', { houseId }),
		getByDateRange: (startDate: string, endDate: string) =>
			this.call<Booking[]>('bookings', 'getBookingsByDateRange', { startDate, endDate })
	};

	schedules = {
		getAll: (filters?: Record<string, any>) =>
			this.call<Schedule[]>('schedules', 'getSchedules', { filters }),
		getById: (id: string) => this.call<Schedule>('schedules', 'getSchedule', { id }),
		create: (data: Omit<Schedule, 'id' | 'createdAt' | 'updatedAt'>) =>
			this.call<Schedule>('schedules', 'createSchedule', { data }),
		update: (id: string, data: Partial<Schedule>) =>
			this.call<Schedule>('schedules', 'updateSchedule', { id, data }),
		delete: (id: string) => this.call('schedules', 'deleteSchedule', { id }),
		getByCleaner: (cleanerId: string) =>
			this.call<Schedule[]>('schedules', 'getSchedulesByCleaner', { cleanerId }),
		getByBooking: (bookingId: string) =>
			this.call<Schedule[]>('schedules', 'getSchedulesByBooking', { bookingId }),
		getByDateRange: (startDate: string, endDate: string) =>
			this.call<Schedule[]>('schedules', 'getSchedulesByDateRange', { startDate, endDate })
	};

	houses = {
		getAll: () => this.call<House[]>('houses', 'getHouses'),
		getById: (id: string) => this.call<House>('houses', 'getHouse', { id }),
		create: (data: Omit<House, 'id' | 'createdAt' | 'updatedAt'>) =>
			this.call<House>('houses', 'createHouse', { data }),
		update: (id: string, data: Partial<House>) =>
			this.call<House>('houses', 'updateHouse', { id, data }),
		delete: (id: string) => this.call('houses', 'deleteHouse', { id })
	};
}

// Export singleton instance
export const kissClient = new KissClient();

// Export types for use in other components
export type { KissResponse, KissCallOptions, Cleaner, Booking, Schedule, House };
