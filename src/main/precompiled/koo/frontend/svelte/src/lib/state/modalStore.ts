/**
 * Modal Store - Manages modal state for the application
 * 
 * Uses Svelte 5 runes via $state in a module context.
 */

import { writable } from 'svelte/store';

export interface ModalState {
  open: boolean;
  title: string;
  message: string;
  type: 'alert' | 'confirm' | 'prompt';
  onConfirm?: () => void | Promise<void>;
  onCancel?: () => void | Promise<void>;
  confirmText?: string;
  cancelText?: string;
}

const initialState: ModalState = {
  open: false,
  title: '',
  message: '',
  type: 'alert',
  confirmText: 'OK',
  cancelText: 'Cancel'
};

// Create a writable store for modal state
export const modalStore = writable<ModalState>(initialState);

// Helper functions to control modals
export const modal = {
  /**
   * Show an alert modal
   */
  alert(title: string, message: string): Promise<void> {
    return new Promise((resolve) => {
      modalStore.set({
        open: true,
        title,
        message,
        type: 'alert',
        onConfirm: () => {
          modalStore.update(state => ({ ...state, open: false }));
          resolve();
        },
        confirmText: 'OK'
      });
    });
  },

  /**
   * Show a confirmation modal
   */
  confirm(
    title: string,
    message: string,
    confirmText?: string,
    cancelText?: string
  ): Promise<boolean> {
    return new Promise((resolve) => {
      modalStore.set({
        open: true,
        title,
        message,
        type: 'confirm',
        onConfirm: () => {
          modalStore.update(state => ({ ...state, open: false }));
          resolve(true);
        },
        onCancel: () => {
          modalStore.update(state => ({ ...state, open: false }));
          resolve(false);
        },
        confirmText: confirmText || 'Yes',
        cancelText: cancelText || 'No'
      });
    });
  },

  /**
   * Close the modal
   */
  close() {
    modalStore.update(state => ({ ...state, open: false }));
  }
};