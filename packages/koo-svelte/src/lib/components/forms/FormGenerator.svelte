<script>
  import FormField from './FormField.svelte';
  import NumberInput from './NumberInput.svelte';
  import DateInput from './DateInput.svelte';
  
  let { schema, formData = $bindable(), errors = $bindable() } = $props();
  
  function getFieldComponent(field) {
    if (field.type.includes('int32') || field.type.includes('int64') || 
        field.type.includes('float') || field.type.includes('double')) {
      return NumberInput;
    }
    if (field.type.includes('date') || field.type.includes('timestamp')) {
      return DateInput;
    }
    return FormField;
  }
</script>

<form class="koo-form">
  {#each schema.fields as field (field.number)}
    {@const Component = getFieldComponent(field)}
    {@const fieldData = { name: field.name, label: field.name, type: field.type }}
    <Component 
      field={fieldData}
      bind:value={formData[field.name]}
      bind:error={errors[field.name]}
    />
  {/each}
</form>

<style>
  .koo-form {
    display: flex;
    flex-direction: column;
  }
</style>