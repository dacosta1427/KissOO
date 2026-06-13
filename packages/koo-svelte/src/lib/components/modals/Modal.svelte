<script>
  let { open = false, title = '', size = 'md' } = $props();
  let modalRef = $state();
  
  function close() {
    open = false;
  }
</script>

<div 
  class="modal-backdrop" 
  class:active={open}
  onclick={close}
  bind:this={modalRef}
>
  <div class="modal-content {size}" onclick|stopPropagation>
    <div class="modal-header">
      <h2>{title}</h2>
      <button class="modal-close" onclick={close}>×</button>
    </div>
    <div class="modal-body">
      {@render children?.()}
    </div>
  </div>
</div>

<style>
  .modal-backdrop {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1000;
  }
  
  .modal-backdrop.active {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .modal-content {
    background: white;
    border-radius: 8px;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .modal-content.sm { max-width: 400px; }
  .modal-content.md { max-width: 600px; }
  .modal-content.lg { max-width: 800px; }
  .modal-content.xl { max-width: 1200px; }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid #eee;
  }
  
  .modal-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    padding: 0;
    width: 30px;
    height: 30px;
  }
  
  .modal-body {
    padding: 1rem;
  }
</style>