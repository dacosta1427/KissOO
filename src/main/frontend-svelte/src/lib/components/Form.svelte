<script>
  // Svelte 5: Event handling using props
  export let fields = [];
  export let data = {};
  export let errors = {};
  export let loading = false;
  export let title = '';
  export let submitLabel = 'Save';
  export let cancelLabel = 'Cancel';
  
  // Event handlers passed as props
  let onSubmit = $props().onSubmit;
  let onCancel = $props().onCancel;
  let onUpdate = $props().onUpdate;
  
  function handleSubmit() {
    onSubmit?.(data);
  }
  
  function handleCancel() {
    onCancel?.();
  }
  
  function updateField(field, value) {
    data[field.name] = value;
    // Clear field-specific error when user starts typing
    if (errors[field.name]) {
      errors[field.name] = null;
    }
    onUpdate?.({ field: field.name, value });
  }
  
  function getFieldError(field) {
    return errors[field.name] || '';
  }
  
  function isFieldInvalid(field) {
    return !!getFieldError(field);
  }
</script>

<form on:submit|preventDefault={handleSubmit} class="form">
  {#if title}
    <div class="form-header">
      <h3>{title}</h3>
    </div>
  {/if}
  
  <div class="form-body">
    {#each fields as field}
      <div class="form-group {isFieldInvalid(field) ? 'error' : ''}">
        <label for={field.name}>
          {field.label}
          {field.required && <span class="required">*</span>}
        </label>
        
        {#if field.type === 'text' || field.type === 'email' || field.type === 'tel' || field.type === 'number'}
          <input
            type={field.type}
            id={field.name}
            name={field.name}
            bind:value={data[field.name]}
            placeholder={field.placeholder}
            required={field.required}
            disabled={loading}
            class="form-input"
          />
        {:else if field.type === 'select'}
          <select
            id={field.name}
            name={field.name}
            bind:value={data[field.name]}
            required={field.required}
            disabled={loading}
            class="form-select"
          >
            <option value="">-- Select {field.label} --</option>
            {#each field.options as option}
              <option value={option.value}>{option.label}</option>
            {/each}
          </select>
        {:else if field.type === 'textarea'}
          <textarea
            id={field.name}
            name={field.name}
            bind:value={data[field.name]}
            placeholder={field.placeholder}
            required={field.required}
            disabled={loading}
            rows={field.rows || 4}
            class="form-textarea"
          ></textarea>
        {:else if field.type === 'checkbox'}
          <label class="checkbox-label">
            <input
              type="checkbox"
              id={field.name}
              name={field.name}
              bind:checked={data[field.name]}
              disabled={loading}
            />
            {field.label}
          </label>
        {/if}
        
        {#if isFieldInvalid(field)}
          <div class="field-error">{getFieldError(field)}</div>
        {/if}
        
        {#if field.helpText}
          <div class="field-help">{field.helpText}</div>
        {/if}
      </div>
    {/each}
  </div>
  
  <div class="form-actions">
    <button 
      type="button" 
      on:click={handleCancel}
      disabled={loading}
      class="btn btn-secondary"
    >
      {cancelLabel}
    </button>
    <button 
      type="submit" 
      disabled={loading}
      class="btn btn-primary"
    >
      {loading ? 'Saving...' : submitLabel}
    </button>
  </div>
</form>

<style>
  .form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .form-header {
    border-bottom: 1px solid var(--border-color);
    padding-bottom: 15px;
    margin-bottom: 15px;
  }
  
  .form-header h3 {
    margin: 0;
    color: var(--text-color);
  }
  
  .form-body {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
  }
  
  .form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .form-group.error .form-input,
  .form-group.error .form-select,
  .form-group.error .form-textarea {
    border-color: var(--error-color);
    box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1);
  }
  
  label {
    font-weight: 600;
    font-size: 14px;
    color: var(--text-color);
  }
  
  .required {
    color: var(--error-color);
    margin-left: 4px;
  }
  
  .form-input, .form-select, .form-textarea {
    padding: 10px 12px;
    border: 1px solid var(--border-color);
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.2s, box-shadow 0.2s;
    background: white;
  }
  
  .form-input:focus, .form-select:focus, .form-textarea:focus {
    outline: none;
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
  }
  
  .form-textarea {
    resize: vertical;
    min-height: 100px;
  }
  
  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: normal;
    cursor: pointer;
  }
  
  .field-error {
    color: var(--error-color);
    font-size: 12px;
    font-weight: 500;
  }
  
  .field-help {
    color: var(--muted-color);
    font-size: 12px;
  }
  
  .form-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    padding-top: 15px;
    border-top: 1px solid var(--border-color);
  }
  
  .btn {
    padding: 10px 20px;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }
  
  .btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  .btn-primary {
    background: var(--primary-color);
    color: white;
  }
  
  .btn-primary:hover:not(:disabled) {
    background: var(--primary-hover);
  }
  
  .btn-secondary {
    background: var(--secondary-color);
    color: var(--text-color);
  }
  
  .btn-secondary:hover:not(:disabled) {
    background: var(--secondary-hover);
  }
</style>