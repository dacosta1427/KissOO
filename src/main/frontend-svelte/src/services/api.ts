/**
 * API Service Layer
 * Provides a clean interface for all API calls using the KissClient
 */

import {
	kissClient,
	type Cleaner,
	type Booking,
	type Schedule,
	type House
} from '$lib/kiss-client';
import { notificationActions } from '$lib/stores.svelte.js';

class ApiService {
	/**
	 * Cleaners API methods
	 */
	async getCleaners(): Promise<Cleaner[]> {
		try {
			const result = await kissClient.cleaners.getAll();
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch cleaners:', error);
			notificationActions.error('Failed to load cleaners');
			throw error;
		}
	}

	async getCleaner(id: string): Promise<Cleaner> {
		try {
			const result = await kissClient.cleaners.getById(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to fetch cleaner');
			}
			return result.data;
		} catch (error) {
			console.error('Failed to fetch cleaner:', error);
			notificationActions.error('Failed to load cleaner');
			throw error;
		}
	}

	async createCleaner(data: Omit<Cleaner, 'id' | 'createdAt' | 'updatedAt'>): Promise<Cleaner> {
		try {
			const result = await kissClient.cleaners.create(data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to create cleaner');
			}
			notificationActions.success('Cleaner created successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to create cleaner:', error);
			notificationActions.error('Failed to create cleaner');
			throw error;
		}
	}

	async updateCleaner(id: string, data: Partial<Cleaner>): Promise<Cleaner> {
		try {
			const result = await kissClient.cleaners.update(id, data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to update cleaner');
			}
			notificationActions.success('Cleaner updated successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to update cleaner:', error);
			notificationActions.error('Failed to update cleaner');
			throw error;
		}
	}

	async deleteCleaner(id: string): Promise<void> {
		try {
			const result = await kissClient.cleaners.delete(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to delete cleaner');
			}
			notificationActions.success('Cleaner deleted successfully');
		} catch (error) {
			console.error('Failed to delete cleaner:', error);
			notificationActions.error('Failed to delete cleaner');
			throw error;
		}
	}

	/**
	 * Bookings API methods
	 */
	async getBookings(filters?: Record<string, any>): Promise<Booking[]> {
		try {
			const result = await kissClient.bookings.getAll(filters);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch bookings:', error);
			notificationActions.error('Failed to load bookings');
			throw error;
		}
	}

	async getBooking(id: string): Promise<Booking> {
		try {
			const result = await kissClient.bookings.getById(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to fetch booking');
			}
			return result.data;
		} catch (error) {
			console.error('Failed to fetch booking:', error);
			notificationActions.error('Failed to load booking');
			throw error;
		}
	}

	async createBooking(data: Omit<Booking, 'id' | 'createdAt' | 'updatedAt'>): Promise<Booking> {
		try {
			const result = await kissClient.bookings.create(data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to create booking');
			}
			notificationActions.success('Booking created successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to create booking:', error);
			notificationActions.error('Failed to create booking');
			throw error;
		}
	}

	async updateBooking(id: string, data: Partial<Booking>): Promise<Booking> {
		try {
			const result = await kissClient.bookings.update(id, data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to update booking');
			}
			notificationActions.success('Booking updated successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to update booking:', error);
			notificationActions.error('Failed to update booking');
			throw error;
		}
	}

	async deleteBooking(id: string): Promise<void> {
		try {
			const result = await kissClient.bookings.delete(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to delete booking');
			}
			notificationActions.success('Booking deleted successfully');
		} catch (error) {
			console.error('Failed to delete booking:', error);
			notificationActions.error('Failed to delete booking');
			throw error;
		}
	}

	async getBookingsByHouse(houseId: string): Promise<Booking[]> {
		try {
			const result = await kissClient.bookings.getByHouse(houseId);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch bookings by house:', error);
			notificationActions.error('Failed to load bookings for house');
			throw error;
		}
	}

	async getBookingsByDateRange(startDate: string, endDate: string): Promise<Booking[]> {
		try {
			const result = await kissClient.bookings.getByDateRange(startDate, endDate);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch bookings by date range:', error);
			notificationActions.error('Failed to load bookings for date range');
			throw error;
		}
	}

	/**
	 * Schedules API methods
	 */
	async getSchedules(filters?: Record<string, any>): Promise<Schedule[]> {
		try {
			const result = await kissClient.schedules.getAll(filters);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch schedules:', error);
			notificationActions.error('Failed to load schedules');
			throw error;
		}
	}

	async getSchedule(id: string): Promise<Schedule> {
		try {
			const result = await kissClient.schedules.getById(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to fetch schedule');
			}
			return result.data;
		} catch (error) {
			console.error('Failed to fetch schedule:', error);
			notificationActions.error('Failed to load schedule');
			throw error;
		}
	}

	async createSchedule(data: Omit<Schedule, 'id' | 'createdAt' | 'updatedAt'>): Promise<Schedule> {
		try {
			const result = await kissClient.schedules.create(data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to create schedule');
			}
			notificationActions.success('Schedule created successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to create schedule:', error);
			notificationActions.error('Failed to create schedule');
			throw error;
		}
	}

	async updateSchedule(id: string, data: Partial<Schedule>): Promise<Schedule> {
		try {
			const result = await kissClient.schedules.update(id, data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to update schedule');
			}
			notificationActions.success('Schedule updated successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to update schedule:', error);
			notificationActions.error('Failed to update schedule');
			throw error;
		}
	}

	async deleteSchedule(id: string): Promise<void> {
		try {
			const result = await kissClient.schedules.delete(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to delete schedule');
			}
			notificationActions.success('Schedule deleted successfully');
		} catch (error) {
			console.error('Failed to delete schedule:', error);
			notificationActions.error('Failed to delete schedule');
			throw error;
		}
	}

	async getSchedulesByCleaner(cleanerId: string): Promise<Schedule[]> {
		try {
			const result = await kissClient.schedules.getByCleaner(cleanerId);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch schedules by cleaner:', error);
			notificationActions.error('Failed to load schedules for cleaner');
			throw error;
		}
	}

	async getSchedulesByBooking(bookingId: string): Promise<Schedule[]> {
		try {
			const result = await kissClient.schedules.getByBooking(bookingId);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch schedules by booking:', error);
			notificationActions.error('Failed to load schedules for booking');
			throw error;
		}
	}

	async getSchedulesByDateRange(startDate: string, endDate: string): Promise<Schedule[]> {
		try {
			const result = await kissClient.schedules.getByDateRange(startDate, endDate);
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch schedules by date range:', error);
			notificationActions.error('Failed to load schedules for date range');
			throw error;
		}
	}

	/**
	 * Houses API methods
	 */
	async getHouses(): Promise<House[]> {
		try {
			const result = await kissClient.houses.getAll();
			return result._Success ? result.data : [];
		} catch (error) {
			console.error('Failed to fetch houses:', error);
			notificationActions.error('Failed to load houses');
			throw error;
		}
	}

	async getHouse(id: string): Promise<House> {
		try {
			const result = await kissClient.houses.getById(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to fetch house');
			}
			return result.data;
		} catch (error) {
			console.error('Failed to fetch house:', error);
			notificationActions.error('Failed to load house');
			throw error;
		}
	}

	async createHouse(data: Omit<House, 'id' | 'createdAt' | 'updatedAt'>): Promise<House> {
		try {
			const result = await kissClient.houses.create(data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to create house');
			}
			notificationActions.success('House created successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to create house:', error);
			notificationActions.error('Failed to create house');
			throw error;
		}
	}

	async updateHouse(id: string, data: Partial<House>): Promise<House> {
		try {
			const result = await kissClient.houses.update(id, data);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to update house');
			}
			notificationActions.success('House updated successfully');
			return result.data;
		} catch (error) {
			console.error('Failed to update house:', error);
			notificationActions.error('Failed to update house');
			throw error;
		}
	}

	async deleteHouse(id: string): Promise<void> {
		try {
			const result = await kissClient.houses.delete(id);
			if (!result._Success) {
				throw new Error(result._Error || 'Failed to delete house');
			}
			notificationActions.success('House deleted successfully');
		} catch (error) {
			console.error('Failed to delete house:', error);
			notificationActions.error('Failed to delete house');
			throw error;
		}
	}
}

// Export singleton instance
export const apiService = new ApiService();
