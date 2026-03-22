/**
 * Crud.ts - Phone Book CRUD API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for phone book operations.
 */

import { Server } from '$lib/services/Server';

export interface PhoneRecord {
  id: number;
  firstName: string;
  lastName: string;
  phoneNumber: string;
}

export interface ApiResult {
  success: boolean;
  error?: string;
  id?: number;
  reportUrl?: string;
  exportUrl?: string;
}

/**
 * Get all phone records from backend
 * @returns Array of phone records
 */
export async function getRecords(): Promise<PhoneRecord[]> {
  const res = await Server.call('services.Crud', 'getRecords', {});
  return res.rows || [];
}

/**
 * Add a new phone record
 * @param firstName - First name
 * @param lastName - Last name
 * @param phoneNumber - Phone number
 * @returns API result
 */
export async function addRecord(
  firstName: string,
  lastName: string,
  phoneNumber: string
): Promise<ApiResult> {
  const res = await Server.call('services.Crud', 'addRecord', {
    firstName,
    lastName,
    phoneNumber
  });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    id: res.id
  };
}

/**
 * Update a phone record
 * @param id - Record ID (oid)
 * @param firstName - New first name
 * @param lastName - New last name
 * @param phoneNumber - New phone number
 * @returns API result
 */
export async function updateRecord(
  id: number,
  firstName: string,
  lastName: string,
  phoneNumber: string
): Promise<ApiResult> {
  const res = await Server.call('services.Crud', 'updateRecord', {
    id,
    firstName,
    lastName,
    phoneNumber
  });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}

/**
 * Delete a phone record
 * @param id - Record ID (oid)
 * @returns API result
 */
export async function deleteRecord(id: number): Promise<ApiResult> {
  const res = await Server.call('services.Crud', 'deleteRecord', { id });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}

/**
 * Run report (if supported)
 * @returns API result with report URL
 */
export async function runReport(): Promise<ApiResult> {
  const res = await Server.call('services.Crud', 'runReport', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    reportUrl: res.reportUrl
  };
}

/**
 * Run export (if supported)
 * @returns API result with export URL
 */
export async function runExport(): Promise<ApiResult> {
  const res = await Server.call('services.Crud', 'runExport', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    exportUrl: res.exportUrl
  };
}