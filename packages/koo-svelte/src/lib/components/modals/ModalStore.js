import { writable } from 'svelte/store';

const modals = writable([]);

export function openModal(id, props = {}) {
  const modal = {
    id,
    props,
    timestamp: Date.now()
  };
  modals.update($modals => [...$modals, modal]);
  return modal.timestamp;
}

export function closeModal(timestamp) {
  modals.update($modals => 
    $modals.filter(m => m.timestamp !== timestamp)
  );
}

export function closeAllModals() {
  modals.set([]);
}

export function getModals() {
  return modals;
}