<script lang="ts">
  import Modal from './Modal.svelte';
  import { modalStore, modal } from '$lib/state/modalStore';
  
  // Subscribe to modal store
  let modalState = $derived($modalStore);
  
  function handleConfirm() {
    modalState.onConfirm?.();
  }
  
  function handleCancel() {
    modalState.onCancel?.();
    modal.close();
  }
</script>

<Modal 
  bind:open={$modalStore.open} 
  title={modalState.title}
  onClose={handleCancel}
>
  <p class="text-gray-700">{modalState.message}</p>
  
  {#snippet footer()}
    <div class="flex gap-2">
      {#if modalState.type === 'confirm'}
        <button
          type="button"
          class="inline-flex w-full justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
          onclick={handleConfirm}
        >
          {modalState.confirmText}
        </button>
        <button
          type="button"
          class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
          onclick={handleCancel}
        >
          {modalState.cancelText}
        </button>
      {:else}
        <button
          type="button"
          class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
          onclick={handleConfirm}
        >
          {modalState.confirmText}
        </button>
      {/if}
    </div>
  {/snippet}
</Modal>