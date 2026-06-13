<script>
  let { field, value = $bindable(), error = $bindable() } = $props();
</script>

<div class="form-field">
  <label for={field.name}>{field.label || field.name}</label>
  <input
    id={field.name}
    type="date"
    {value}
    oninput={(e) => value = e.target.value}
    aria-invalid={error ? 'true' : 'false'}
    class:error
  />
  {#if error}
    <span class="error-message">{error}</span>
  {/if}
</div>

<style>
  .form-field {
    margin-bottom: 1rem;
  }
  
  label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
  }
  
  input {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  input.error {
    border-color: #d32f2f;
  }
  
  .error-message {
    color: #d32f2f;
    font-size: 0.875rem;
    margin-top: 0.25rem;
    display: block;
  }
</style>