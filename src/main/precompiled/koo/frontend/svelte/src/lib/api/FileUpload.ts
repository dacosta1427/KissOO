/**
 * FileUpload.ts - File Upload API Module
 * 
 * Simple functions that call Server.fileUploadSend() directly.
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
  try {
    const formData = new FormData();
    formData.append('_file-0', file);
    
    const res = await Server.fileUploadSend(
      'services.FileUpload',
      'upload',
      formData,
      additionalData,
      'Uploading file...',
      'File uploaded successfully'
    );
    
    return {
      success: res._Success ?? res.success ?? false,
      error: res._ErrorMessage || res.error,
      fileName: file.name,
      fileSize: file.size
    };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Upload failed'
    };
  }
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
  try {
    const formData = new FormData();
    files.forEach((file, index) => {
      formData.append(`_file-${index}`, file);
    });
    
    const res = await Server.fileUploadSend(
      'services.FileUpload',
      'upload',
      formData,
      additionalData,
      'Uploading files...',
      'Files uploaded successfully'
    );
    
    return {
      success: res._Success ?? res.success ?? false,
      error: res._ErrorMessage || res.error
    };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Upload failed'
    };
  }
}