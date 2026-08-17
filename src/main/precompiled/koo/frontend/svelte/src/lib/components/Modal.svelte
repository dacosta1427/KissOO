<script lang="ts">
  interface Props {
    open?: boolean;
    title?: string;
    onClose?: () => void;
    size?: 'sm' | 'md' | 'lg' | 'xl';
    children?: import('svelte').Snippet;
    footer?: import('svelte').Snippet;
  }

  let { open = $bindable(false), title = '', onClose, size = 'md', children, footer }: Props = $props();

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl'
  };

  function handleKeydown(event: KeyboardEvent) {
    if (event.key === 'Escape' && open) {
      close();
    }
  }

  function close() {
    open = false;
    onClose?.();
  }

  function handleOverlayClick(event: MouseEvent) {
    if (event.target === event.currentTarget) {
      close();
    }
  }

  // Focus trap when modal opens
  $effect(() => {
    if (open) {
      document.body.style.overflow = 'hidden';
      // Could add focus trap logic here
    } else {
      document.body.style.overflow = '';
    }
  });
</script>

<svelte:window onkeydown={handleKeydown} />

{#if open}
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div 
    class="fixed inset-0 z-50 overflow-y-auto"
    aria-labelledby="modal-title" 
    role="dialog" 
    aria-modal="true"
  >
    <!-- Background overlay -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div 
      class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
      onclick={handleOverlayClick}
      aria-hidden="true"
    ></div>

    <!-- Modal panel -->
    <div class="flex min-h-full items-center justify-center p-4">
      <div 
        class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all w-full {sizeClasses[size]}"
      >
        <!-- Header -->
        <div class="bg-white px-4 py-3 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-medium leading-6 text-gray-900" id="modal-title">
              {title}
            </h3>
            <button
              type="button"
              class="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              onclick={close}
            >
              <span class="sr-only">Close</span>
              <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="bg-white px-4 py-5 sm:p-6">
          {@render children?.()}
        </div>

        <!-- Footer -->
        {#if footer}
          <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6 border-t border-gray-200">
            {@render footer?.()}
          </div>
        {/if}
      </div>
    </div>
  </div>
{/if}