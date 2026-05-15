/**
 * CleaningService.ts - CleaningService Scheduler API Module
 * 
 * Provides API functions for cleaners, bookings, schedules, houses.
 * Uses KissOO Server.call() for backend communication.
 * Includes loading state and notification handling.
 */

import { Server } from '$lib/services/Server';
import { notificationActions } from '$lib/stores.svelte.js';

// Type definitions
export interface Cleaner {
   oid: number;
   name: string;
   phone?: string;
   email?: string;
   address?: string;
   canLogin?: boolean;
   active?: boolean;
   emailVerified?: boolean;
}

export interface Booking {
  oid: number;
  houseOid: number;
  check_in_date: string;
  check_out_date: string;
  guest_name: string;
  guest_email: string;
  guest_phone?: string;
  notes?: string;
  dogs_count: number; // default 0
  status: 'pending' | 'confirmed' | 'cancelled';
}

export interface Schedule {
  oid: number;
  cleanerOid: number;
  bookingOid: number;
  date: string;
  start_time: string;
  end_time: string;
  notes?: string;
  status: 'scheduled' | 'completed' | 'cancelled' | 'pending';
}

export interface House {
  oid: number;
  name: string;
  address: string;
  description?: string;
  ownerOid?: number;
  ownerName?: string;
  costProfileOid?: number;
  active: boolean;
  check_in_time: string; // 24h format, e.g., "16:00"
  check_out_time: string; // 24h format, e.g., "10:00"
  // Cost calculation fields
  surface_m2?: number;   // Total cleaning surface in m²
  floors?: number;       // Number of floors
  bedrooms?: number;     // Number of bedrooms
  bathrooms?: number;    // Number of bathrooms
  luxury_level?: string; // 'standard', 'premium', 'luxury'
}

interface CleaningResult {
  _Success: boolean;
  _ErrorCode: number;
  _ErrorMessage?: string;
  data: any;
  oid?: number;
}

// Operations that should show toasts
const operationsWithToast = ['create', 'add', 'delete', 'update', 'deleteCleaner', 'deleteBooking', 'deleteHouse', 'deleteOwner'];

// Helper to handle API calls with notifications (only for add, delete, update)
async function callCleaningService(service: string, method: string, args: any = {}, operationName?: string): Promise<CleaningResult> {
  try {
    console.log(`[Cleaning.ts] Calling ${service}.${method} with args:`, args);
    const res = await Server.call(service, method, args) as CleaningResult;
    console.log(`[Cleaning.ts] ${method} response:`, res);
    
    // Only show toasts for create, add, delete, update operations
    const showToast = operationsWithToast.some(op => method.toLowerCase().includes(op.toLowerCase()));
    
    if (showToast && operationName) {
      if (res._Success) {
        notificationActions.success(`${operationName} completed successfully`);
      } else {
        notificationActions.error(`${operationName} failed: ${res._ErrorMessage || 'Unknown error'}`);
      }
    }
    return res;
  } catch (error: any) {
    const errorMessage = error.message || 'Network error';
    console.error(`[Cleaning.ts] ${method} error:`, errorMessage);
    // Only show error toast for operations that normally show toasts
    const showToast = operationsWithToast.some(op => method.toLowerCase().includes(op.toLowerCase()));
    if (showToast && operationName) {
      notificationActions.error(`${operationName} failed: ${errorMessage}`);
    }
    throw error;
  }
}

// Cleaners API
export const cleanersAPI = {
  getAll: async (): Promise<Cleaner[]> => {
    const res = await callCleaningService('services.CleanerService', 'getCleaners', {}, 'Load cleaners');
    return res.data || [];
  },
  
  getByOid: async (oid: number): Promise<Cleaner | null> => {
    const res = await callCleaningService('services.CleanerService', 'getCleaner', { oid }, 'Load cleaner');
    return res.data || null;
  },
  
  create: async (data: Partial<Cleaner>): Promise<Cleaner> => {
    const res = await callCleaningService('services.CleanerService', 'createCleaner', { data }, 'Create cleaner');
    return res.data;
  },
  
  update: async (oid: number, data: Partial<Cleaner>): Promise<Cleaner> => {
    const res = await callCleaningService('services.CleanerService', 'updateCleaner', { oid, data }, 'Update cleaner');
    return res.data;
  },
  
  delete: async (oid: number): Promise<void> => {
    await callCleaningService('services.CleanerService', 'deleteCleaner', { oid }, 'Delete cleaner');
  },
  
  toggleLogin: async (oid: number, canLogin: boolean): Promise<Cleaner> => {
    const res = await callCleaningService('services.CleanerService', 'toggleCleanerLogin', { oid, canLogin }, 'Toggle cleaner login');
    return res.data;
  }
};

// Bookings API
export const bookingsAPI = {
  getAll: async (): Promise<Booking[]> => {
    const res = await callCleaningService('services.BookingService', 'getBookings', {}, 'Load bookings');
    return res.data || [];
  },
  
  getByOid: async (oid: number): Promise<Booking | null> => {
    const res = await callCleaningService('services.BookingService', 'getBooking', { oid }, 'Load booking');
    return res.data || null;
  },
  
  create: async (data: Partial<Booking>): Promise<Booking> => {
    const res = await callCleaningService('services.BookingService', 'createBooking', { data }, 'Create booking');
    return res.data;
  },
  
  update: async (oid: number, data: Partial<Booking>): Promise<Booking> => {
    const res = await callCleaningService('services.BookingService', 'updateBooking', { oid, data }, 'Update booking');
    return res.data;
  },
  
  delete: async (oid: number): Promise<void> => {
    await callCleaningService('services.BookingService', 'deleteBooking', { oid }, 'Delete booking');
  },
  
  getByHouse: async (houseOid: number): Promise<Booking[]> => {
    const res = await callCleaningService('services.BookingService', 'getBookingsByHouse', { houseOid }, 'Load bookings by house');
    return res.data || [];
  },
  
  getByDateRange: async (startDate: string, endDate: string): Promise<Booking[]> => {
    const res = await callCleaningService('services.BookingService', 'getBookingsByDateRange', { startDate, endDate }, 'Load bookings by date range');
    return res.data || [];
  }
};

// Schedules API
export const schedulesAPI = {
  getAll: async (filters?: any): Promise<Schedule[]> => {
    const res = await callCleaningService('services.ScheduleService', 'getSchedules', { filters }, 'Load schedules');
    return res.data || [];
  },
  
  getByOid: async (oid: number): Promise<Schedule | null> => {
    const res = await callCleaningService('services.ScheduleService', 'getSchedule', { oid }, 'Load schedule');
    return res.data || null;
  },
  
  create: async (data: Partial<Schedule>): Promise<Schedule> => {
    const res = await callCleaningService('services.ScheduleService', 'createSchedule', { data }, 'Create schedule');
    return res.data;
  },
  
  update: async (oid: number, data: Partial<Schedule>): Promise<Schedule> => {
    const res = await callCleaningService('services.ScheduleService', 'updateSchedule', { oid, data }, 'Update schedule');
    return res.data;
  },
  
  delete: async (oid: number): Promise<void> => {
    await callCleaningService('services.ScheduleService', 'deleteSchedule', { oid }, 'Delete schedule');
  },
  
  getByCleaner: async (cleanerOid: number): Promise<Schedule[]> => {
    const res = await callCleaningService('services.ScheduleService', 'getSchedulesByCleaner', { cleanerOid }, 'Load schedules by cleaner');
    return res.data || [];
  },
  
  getByBooking: async (bookingOid: number): Promise<Schedule[]> => {
    const res = await callCleaningService('services.ScheduleService', 'getSchedulesByBooking', { bookingOid }, 'Load schedule by booking');
    return res.data || [];
  },
  
  getByDateRange: async (startDate: string, endDate: string): Promise<Schedule[]> => {
    const res = await callCleaningService('services.ScheduleService', 'getSchedulesByDateRange', { startDate, endDate }, 'Load schedules by date range');
    return res.data || [];
  }
};

// Houses API
export const housesAPI = {
  getAll: async (): Promise<House[]> => {
    const res = await callCleaningService('services.HouseService', 'getHouses', {}, 'Load houses');
    return res.data || [];
  },
  
  getByOid: async (oid: number): Promise<House | null> => {
    const res = await callCleaningService('services.HouseService', 'getHouse', { oid }, 'Load house');
    return res.data || null;
  },
  
  create: async (data: Partial<House>): Promise<House> => {
    const res = await callCleaningService('services.HouseService', 'createHouse', { data }, 'Create house');
    return res.data;
  },
  
  update: async (oid: number, data: Partial<House>): Promise<House> => {
    const res = await callCleaningService('services.HouseService', 'updateHouse', { oid, data }, 'Update house');
    return res.data;
  },
  
  delete: async (oid: number): Promise<void> => {
    await callCleaningService('services.HouseService', 'deleteHouse', { oid }, 'Delete house');
  },
  
  getByOwner: async (ownerOid: number): Promise<House[]> => {
    const res = await callCleaningService('services.HouseService', 'getOwnerHouses', { ownerOid }, 'Load houses by owner');
    return res.data || [];
  },
  
  toggleActive: async (oid: number, active: boolean): Promise<House> => {
    const res = await callCleaningService('services.HouseService', 'updateHouse', { oid, data: { active } }, active ? 'Activate house' : 'Deactivate house');
    return res.data;
  }
};

// Bookings API additions for house schedules
export const bookingsByHouseAPI = {
  getByHouse: async (houseOid: number): Promise<Booking[]> => {
    const res = await callCleaningService('services.BookingService', 'getBookingsByHouse', { houseOid }, 'Load bookings by house');
    return res.data || [];
  }
};

export const schedulesByBookingAPI = {
  getByBooking: async (bookingOid: number): Promise<Schedule[]> => {
    const res = await callCleaningService('services.ScheduleService', 'getSchedulesByBooking', { bookingOid }, 'Load schedules by booking');
    return res.data || [];
  }
};

// Owners API
export const ownersAPI = {
  getAll: async (): Promise<any[]> => {
    const res = await callCleaningService('services.OwnerService', 'getOwners', {}, 'Load owners');
    return res.data || [];
  },
  
  getByOid: async (oid: number): Promise<any | null> => {
    const res = await callCleaningService('services.OwnerService', 'getOwner', { oid }, 'Load owner');
    return res.data || null;
  },
  
  create: async (data: any): Promise<any> => {
    const res = await callCleaningService('services.OwnerService', 'createOwner', { data }, 'Create owner');
    return res.data;
  },
  
  update: async (oid: number, data: any): Promise<any> => {
    const res = await callCleaningService('services.OwnerService', 'updateOwner', { oid, data }, 'Update owner');
    return res.data;
  },
  
  delete: async (oid: number): Promise<void> => {
    await callCleaningService('services.OwnerService', 'deleteOwner', { oid }, 'Delete owner');
  },
  
  toggleLogin: async (oid: number, canLogin: boolean): Promise<any> => {
    const res = await callCleaningService('services.OwnerService', 'toggleOwnerLogin', { oid, canLogin }, 'Toggle owner login');
    return res.data;
  }
};

// CostProfile types
export interface CostProfile {
   oid: number;
   name: string;
   is_standard: boolean;
   ownerOid: number;
   base_hourly_rate: number;
  minimum_charge: number;
  rate_per_m2: number;
  rate_per_floor: number;
  rate_per_bedroom: number;
  rate_per_bathroom: number;
  dog_surcharge: number;
  basic_multiplier: number;
  standard_multiplier: number;
  premium_multiplier: number;
  luxury_multiplier: number;
  active: boolean;
}

// CostProfiles API
export const costProfilesAPI = {
  getAll: async (): Promise<CostProfile[]> => {
    const res = await callCleaningService('services.CostProfileService', 'getCostProfiles', {}, 'Load cost profiles');
    return res.data || [];
  },

  getByOid: async (oid: number): Promise<CostProfile | null> => {
    const res = await callCleaningService('services.CostProfileService', 'getCostProfile', { oid }, 'Load cost profile');
    return res.data || null;
  },

  create: async (data: Partial<CostProfile>): Promise<CostProfile> => {
    const res = await callCleaningService('services.CostProfileService', 'createCostProfile', { data }, 'Create cost profile');
    return res.data;
  },

  update: async (oid: number, data: Partial<CostProfile>): Promise<CostProfile> => {
    const res = await callCleaningService('services.CostProfileService', 'updateCostProfile', { oid, data }, 'Update cost profile');
    return res.data;
  },

  delete: async (oid: number): Promise<void> => {
    await callCleaningService('services.CostProfileService', 'deleteCostProfile', { oid }, 'Delete cost profile');
  },

  copy: async (oid: number, name: string): Promise<CostProfile> => {
    const res = await callCleaningService('services.CostProfileService', 'copyCostProfile', { sourceOid: oid, name }, 'Copy cost profile');
    return res.data;
  }
};
