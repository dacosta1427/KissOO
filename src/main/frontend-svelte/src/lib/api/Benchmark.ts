/**
 * Benchmark.ts - Performance Testing API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for benchmark operations.
 */

import { Server } from '$lib/services/Server';

export interface BenchmarkResult {
  success: boolean;
  error?: string;
  count?: number;
  elapsed?: number;
  rate?: number;
  sum?: number;
  avg?: number;
  results?: Record<string, { category: string; sum: number; count: number }>;
  message?: string;
}

/**
 * Clear all benchmark data
 * @returns Benchmark result
 */
export async function setupTable(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'setupTable', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    message: res.message
  };
}

/**
 * Insert bulk benchmark data
 * @param count - Number of records to insert (default 100)
 * @returns Benchmark result with timing
 */
export async function bulkInsert(count: number = 100): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'bulkInsert', { count });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    count: res.count,
    elapsed: res.elapsed,
    rate: res.rate
  };
}

/**
 * Select all benchmark records
 * @returns Benchmark result with timing
 */
export async function selectAll(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'selectAll', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    count: res.count,
    elapsed: res.elapsed
  };
}

/**
 * Count benchmark records
 * @returns Benchmark result with count and timing
 */
export async function countRecords(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'countRecords', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    count: res.count,
    elapsed: res.elapsed
  };
}

/**
 * Update all benchmark records
 * @returns Benchmark result with timing
 */
export async function bulkUpdate(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'bulkUpdate', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    count: res.count,
    elapsed: res.elapsed
  };
}

/**
 * Delete all benchmark records
 * @returns Benchmark result with timing
 */
export async function bulkDelete(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'bulkDelete', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    count: res.count,
    elapsed: res.elapsed
  };
}

/**
 * Sum all values
 * @returns Benchmark result with sum and timing
 */
export async function aggregateSum(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'aggregateSum', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    sum: res.sum,
    elapsed: res.elapsed
  };
}

/**
 * Average all values
 * @returns Benchmark result with average and timing
 */
export async function aggregateAvg(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'aggregateAvg', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    avg: res.avg,
    elapsed: res.elapsed
  };
}

/**
 * Group by category
 * @returns Benchmark result with grouped data and timing
 */
export async function aggregateGroupBy(): Promise<BenchmarkResult> {
  const res = await Server.call('services.Benchmark', 'aggregateGroupBy', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    results: res.results,
    elapsed: res.elapsed
  };
}