/**
 * Ollama.ts - AI/LLM API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for Ollama AI interactions.
 */

import { Server } from '$lib/services/Server';

export interface OllamaResult {
  success: boolean;
  error?: string;
  isOllamaUp?: boolean;
  models?: string[];
  textResponse?: string;
  htmlResponse?: string;
}

/**
 * Check if Ollama server is running
 * @returns Result with isOllamaUp flag
 */
export async function isOllamaUp(): Promise<OllamaResult> {
  const res = await Server.call('services.OllamaQuery', 'isOllamaUp', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    isOllamaUp: res.isOllamaUp
  };
}

/**
 * List available Ollama models
 * @returns Result with models array
 */
export async function listModels(): Promise<OllamaResult> {
  const res = await Server.call('services.OllamaQuery', 'listModels', {});
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    models: res.models
  };
}

/**
 * Send a prompt to Ollama
 * @param model - Model name to use
 * @param prompt - Prompt text
 * @returns Result with text and HTML responses
 */
export async function ask(model: string, prompt: string): Promise<OllamaResult> {
  const res = await Server.call('services.OllamaQuery', 'ask', {
    model,
    prompt
  });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    textResponse: res.textResponse,
    htmlResponse: res.htmlResponse
  };
}