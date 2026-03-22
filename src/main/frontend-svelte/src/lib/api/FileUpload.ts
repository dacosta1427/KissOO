/**
 * FileUpload.ts - File Upload API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for file upload operations.
 */

import { Server } from '$lib/services/Server';

export interface UploadResult {
  success: boolean;
  error?: string;
  fileName?: string;
  fileSize?: number;
}

/**
 * Upload a file to the server
 * @param file - File object to upload
 * @param additionalData - Optional additional data to send with the file
 * @returns Upload result
 */
export async function uploadFile(
  file: File,
  additionalData?: Record<string, any>
): Promise<UploadResult> {
  // This will be implemented after adding fileUploadSend to Server.ts
  // For now, return a placeholder
  console.warn('File upload not yet implemented in Server.ts');
  return {
    success: false,
    error: 'File upload not implemented'
  };
}

/**
 * Upload multiple files to the server
 * @param files - Array of File objects to upload
 * @param additionalData - Optional additional data to send with the files
 * @returns Upload result
 */
export async function uploadFiles(
  files: File[],
  additionalData?: Record<string, any>
): Promise<UploadResult> {
  // This will be implemented after adding fileUploadSend to Server.ts
  console.warn('File upload not yet implemented in Server.ts');
  return {
    success: false,
    error: 'File upload not implemented'
  };
}