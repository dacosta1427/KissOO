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
  id: number;
  name: string;
  phone?: string;
  email?: string;
  address?: string;
  canLogin: boolean;
  emailVerified?: boolean;
}

export interface Booking {
  id: number;
  house_id: number;
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
  id: number;
  cleaner_id: number;
  booking_id: number;
  date: string;
  start_time: string;
  end_time: string;
  notes?: string;
  status: 'scheduled' | 'completed' | 'cancelled' | 'pending';
}

export interface House {
  id: number;
  name: string;
  address: string;
  description?: string;
  owner?: number;  // Owner OID (was owner_id)
  ownerName?: string;  // Owner name for display
  cost_profile?: number;  // CostProfile OID
  active: boolean;
  check_in_time: string; // 24h format, e.g., "16:00"
  check_out_time: string; // 24h format, e.g., "10:00"
  // Cost calculation fields
  surface_m2?: number;   // Total cleaning surface in m²
  floors?: number;       // Number of floors
  bedrooms?: number;     // Number of bedrooms
  bathrooms?: number;    // Number of bathrooms
  luxury_level?: 'basic' | 'standard' | 'premium' | 'luxury';
}

export interface Owner {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  active: boolean;
  canLogin?: boolean;  // Whether owner has login capability
  emailVerified?: boolean;  // Email verification status
}

export interface CostProfile {
  id: number;
  name: string;
  is_standard: boolean;
  owner?: number;  // Owner OID, null for global
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

export interface CostEstimate {
  base_cost: number;
  size_cost: number;
  room_cost: number;
  luxury_multiplier: number;
  dog_surcharge: number;
  total: number;
  breakdown: string[];
}

export interface CleaningResult {
  _Success: boolean;
  _ErrorMessage?: string;
  _ErrorCode?: number;
  data?: any;
  estimated_hours?: number;
}

export interface ApiResult {
  success: boolean;
  error?: string;
  id?: number;
}

// Operations that should show toasts
const operationsWithToast = ['create', 'add', 'delete', 'update', 'deleteCleaner', 'deleteBooking', 'deleteHouse', 'deleteOwner'];

// Helper to handle API calls with notifications (only for add, delete, update)
async function callCleaningService(method: string, args: any = {}, operationName?: string): Promise<CleaningResult> {
  try {
    console.log(`[Cleaning.ts] Calling ${method} with args:`, args);
    const res = await Server.call('services.CleaningService', method, args) as CleaningResult;
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
    const res = await callCleaningService('getCleaners', {}, 'Load cleaners');
    return res.data || [];
  },
  
  getById: async (id: number): Promise<Cleaner | null> => {
    const res = await callCleaningService('getCleaner', { id }, 'Load cleaner');
    return res.data || null;
  },
  
  create: async (data: Partial<Cleaner>): Promise<Cleaner> => {
    const res = await callCleaningService('createCleaner', { data }, 'Create cleaner');
    return res.data;
  },
  
  update: async (id: number, data: Partial<Cleaner>): Promise<Cleaner> => {
    const res = await callCleaningService('updateCleaner', { id, data }, 'Update cleaner');
    return res.data;
  },
  
  delete: async (id: number): Promise<void> => {
    await callCleaningService('deleteCleaner', { id }, 'Delete cleaner');
  },
  
  toggleLogin: async (id: number, canLogin: boolean): Promise<Cleaner> => {
    const res = await callCleaningService('toggleCleanerLogin', { id, canLogin }, canLogin ? 'Enable cleaner login' : 'Disable cleaner login');
    return res;
  }
};

// Bookings API
export const bookingsAPI = {
  getAll: async (filters?: any): Promise<Booking[]> => {
    const res = await callCleaningService('getBookings', { filters }, 'Load bookings');
    return res.data || [];
  },
  
  getById: async (id: number): Promise<Booking | null> => {
    const res = await callCleaningService('getBooking', { id }, 'Load booking');
    return res.data || null;
  },
  
  create: async (data: Partial<Booking>): Promise<Booking> => {
    const res = await callCleaningService('createBooking', { data }, 'Create booking');
    return res.data;
  },
  
  update: async (id: number, data: Partial<Booking>): Promise<Booking> => {
    const res = await callCleaningService('updateBooking', { id, data }, 'Update booking');
    return res.data;
  },
  
  delete: async (id: number): Promise<void> => {
    await callCleaningService('deleteBooking', { id }, 'Delete booking');
  },
  
  getByHouse: async (houseId: number): Promise<Booking[]> => {
    const res = await callCleaningService('getBookingsByHouse', { houseId }, 'Load bookings by house');
    return res.data || [];
  },
  
  getByDateRange: async (startDate: string, endDate: string): Promise<Booking[]> => {
    const res = await callCleaningService('getBookingsByDateRange', { startDate, endDate }, 'Load bookings by date range');
    return res.data || [];
  }
};

// Schedules API
export const schedulesAPI = {
  getAll: async (filters?: any): Promise<Schedule[]> => {
    const res = await callCleaningService('getSchedules', { filters }, 'Load schedules');
    return res.data || [];
  },
  
  getById: async (id: number): Promise<Schedule | null> => {
    const res = await callCleaningService('getSchedule', { id }, 'Load schedule');
    return res.data || null;
  },
  
  create: async (data: Partial<Schedule>): Promise<Schedule> => {
    const res = await callCleaningService('createSchedule', { data }, 'Create schedule');
    return res.data;
  },
  
  update: async (id: number, data: Partial<Schedule>): Promise<Schedule> => {
    const res = await callCleaningService('updateSchedule', { id, data }, 'Update schedule');
    return res.data;
  },
  
  delete: async (id: number): Promise<void> => {
    await callCleaningService('deleteSchedule', { id }, 'Delete schedule');
  },
  
  getByCleaner: async (cleanerId: number): Promise<Schedule[]> => {
    const res = await callCleaningService('getSchedulesByCleaner', { cleanerId }, 'Load schedules by cleaner');
    return res.data || [];
  },
  
  getByBooking: async (bookingId: number): Promise<Schedule[]> => {
    const res = await callCleaningService('getSchedulesByBooking', { bookingId }, 'Load schedule by booking');
    return res.data || [];
  },
  
  getByDateRange: async (startDate: string, endDate: string): Promise<Schedule[]> => {
    const res = await callCleaningService('getSchedulesByDateRange', { startDate, endDate }, 'Load schedules by date range');
    return res.data || [];
  }
};

// Houses API
export const housesAPI = {
  getAll: async (): Promise<House[]> => {
    const res = await callCleaningService('getHouses', {}, 'Load houses');
    return res.data || [];
  },
  
  getById: async (id: number): Promise<House | null> => {
    const res = await callCleaningService('getHouse', { id }, 'Load house');
    return res.data || null;
  },
  
  create: async (data: Partial<House>): Promise<House> => {
    const res = await callCleaningService('createHouse', { data }, 'Create house');
    return res.data;
  },
  
  update: async (id: number, data: Partial<House>): Promise<House> => {
    const res = await callCleaningService('updateHouse', { id, data }, 'Update house');
    return res.data;
  },
  
  delete: async (id: number): Promise<void> => {
    await callCleaningService('deleteHouse', { id }, 'Delete house');
  },
  
  getByOwner: async (ownerId: number): Promise<House[]> => {
    const res = await callCleaningService('getOwnerHouses', { owner_id: ownerId }, 'Load houses by owner');
    return res.data || [];
  },
  
  toggleActive: async (id: number, active: boolean): Promise<House> => {
    const res = await callCleaningService('updateHouse', { id, data: { active } }, active ? 'Activate house' : 'Deactivate house');
    return res.data;
  }
};

// Bookings API additions for house schedules
export const bookingsByHouseAPI = {
  getByHouse: async (houseId: number): Promise<Booking[]> => {
    const res = await callCleaningService('getBookingsByHouse', { houseId }, 'Load bookings by house');
    return res.data || [];
  }
};

export const schedulesByBookingAPI = {
  getByBooking: async (bookingId: number): Promise<Schedule[]> => {
    const res = await callCleaningService('getSchedulesByBooking', { bookingId }, 'Load schedules by booking');
    return res.data || [];
  }
};

// Owners API
export const ownersAPI = {
  getAll: async (): Promise<Owner[]> => {
    const res = await callCleaningService('getOwners', {}, 'Load owners');
    return res.data || [];
  },
  
  getById: async (id: number): Promise<Owner | null> => {
    const res = await callCleaningService('getOwner', { id }, 'Load owner');
    return res.data || null;
  },
  
  create: async (data: Partial<Owner>): Promise<Owner> => {
    const res = await callCleaningService('createOwner', { data }, 'Create owner');
    return res.data;
  },
  
  update: async (id: number, data: Partial<Owner>): Promise<Owner> => {
    const res = await callCleaningService('updateOwner', { id, data }, 'Update owner');
    return res.data;
  },
  
  delete: async (id: number): Promise<void> => {
    await callCleaningService('deleteOwner', { id }, 'Delete owner');
  }
};

// Cost Profiles API - methods are in services.CleaningService.groovy
export const costProfilesAPI = {
  getAll: async (): Promise<CostProfile[]> => {
    const res = await callCleaningService('getCostProfiles', {}, 'Load cost profiles');
    return res.data || [];
  },
  
  getById: async (id: number): Promise<CostProfile | null> => {
    const res = await callCleaningService('getCostProfile', { id }, 'Load cost profile');
    return res.data || null;
  },
  
  getStandard: async (): Promise<CostProfile | null> => {
    const res = await callCleaningService('getStandardCostProfile', {}, 'Load standard cost profile');
    return res.data || null;
  },
  
  create: async (data: Partial<CostProfile>): Promise<CostProfile> => {
    const res = await callCleaningService('createCostProfile', { data }, 'Create cost profile');
    return res.data;
  },
  
  update: async (id: number, data: Partial<CostProfile>): Promise<CostProfile> => {
    const res = await callCleaningService('updateCostProfile', { id, data }, 'Update cost profile');
    return res.data;
  },
  
  delete: async (id: number): Promise<void> => {
    await callCleaningService('deleteCostProfile', { id }, 'Delete cost profile');
  },
  
  copy: async (sourceId: number, name: string, ownerId?: number): Promise<CostProfile> => {
    const res = await callCleaningService('copyCostProfile', { source_id: sourceId, name, owner: ownerId }, 'Copy cost profile');
    return res.data;
  }
};

// Cost Calculation API
export const costAPI = {
  calculate: async (houseId: number, bookingId?: number, profileId?: number): Promise<CostEstimate | null> => {
    const args: any = { house_id: houseId };
    if (bookingId) args.booking_id = bookingId;
    if (profileId) args.cost_profile_id = profileId;
    const res = await callCleaningService('calculateCost', args, 'Calculate cost');
    return res.data || null;
  },
  
  estimateHours: async (houseId: number): Promise<number | null> => {
    const res = await callCleaningService('estimateHours', { house_id: houseId }, 'Estimate hours');
    return res.estimated_hours || null;
  }
};
