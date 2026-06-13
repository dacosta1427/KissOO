<script>
  import { getModals } from './ModalStore.js';
  import Modal from './Modal.svelte';
  
  let modals = [];
  
  getModals().subscribe($modals => {
    modals = $modals;
  });
</script>

{#each modals as modal (modal.timestamp)}
  <Modal id={modal.id} title={modal.props.title || 'Modal'} size={modal.props.size || 'md'}>
    {#if modal.props.content}
      {@html modal.props.content}
    {/if}
  </Modal>
{/each}