/**
 * RestServices.ts - Demo Services API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for REST service demonstrations.
 */

import { Server } from '$lib/services/Server';

export interface AddNumbersResult {
  success: boolean;
  error?: string;
  num3?: number;
}

export interface HasDatabaseResult {
  success: boolean;
  error?: string;
  hasDatabase?: boolean;
}

/**
 * Call Groovy service to add numbers
 * @param num1 - First number
 * @param num2 - Second number
 * @returns Result with sum
 */
export async function addNumbersGroovy(num1: number, num2: number): Promise<AddNumbersResult> {
  const res = await Server.call('services.MyGroovyService', 'addNumbers', { num1, num2 });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    num3: res.num3
  };
}

/**
 * Call Java service to add numbers
 * @param num1 - First number
 * @param num2 - Second number
 * @returns Result with sum
 */
export async function addNumbersJava(num1: number, num2: number): Promise<AddNumbersResult> {
  const res = await Server.call('services.MyJavaService', 'addNumbers', { num1, num2 });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    num3: res.num3
  };
}

/**
 * Call Lisp service to add numbers (if enabled)
 * @param num1 - First number
 * @param num2 - Second number
 * @returns Result with sum
 */
export async function addNumbersLisp(num1: number, num2: number): Promise<AddNumbersResult> {
  const res = await Server.call('services.MyLispService', 'addNumbers', { num1, num2 });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    num3: res.num3
  };
}

/**
 * Check if database is available (via Groovy service)
 * @returns Result with hasDatabase flag
 */
export async function hasDatabase(): Promise<HasDatabaseResult> {
  const res = await Server.call('services.MyGroovyService', 'hasDatabase', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    hasDatabase: res.hasDatabase
  };
}