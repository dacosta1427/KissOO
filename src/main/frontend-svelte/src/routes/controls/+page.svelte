<script lang="ts">
  import Modal from '$lib/components/Modal.svelte';

  // State for various controls
  let textValue = $state('');
  let numericValue = $state(0);
  let dateValue = $state('');
  let timeValue = $state('');
  let checkboxChecked = $state(false);
  let radioValue = $state('Yes');
  let dropdownValue = $state('');
  let listboxValue = $state('');
  let textareaValue = $state('');

  // Modal state
  let modalOpen = $state(false);

  function handleSubmit() {
    alert('Form submitted!\n' + 
          `Text: ${textValue}\n` +
          `Numeric: ${numericValue}\n` +
          `Date: ${dateValue}\n` +
          `Time: ${timeValue}\n` +
          `Checkbox: ${checkboxChecked}\n` +
          `Radio: ${radioValue}\n` +
          `Dropdown: ${dropdownValue}\n` +
          `Listbox: ${listboxValue}\n` +
          `Textarea: ${textareaValue}`);
  }
</script>

<div class="p-6 max-w-4xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">Controls</h1>

  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
    <!-- Text Input -->
    <div>
      <label class="block text-sm font-medium text-gray-700">Text Input</label>
      <input
        type="text"
        bind:value={textValue}
        maxlength="40"
        required
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <!-- Numeric Input -->
    <div>
      <label class="block text-sm font-medium text-gray-700">Numeric Input</label>
      <input
        type="number"
        bind:value={numericValue}
        min="10"
        max="100"
        step="0.01"
        class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <!-- Date Input -->
    <div>
      <label class="block text-sm font-medium text-gray-700">Date Input</label>
      <input
        type="date"
        bind:value={dateValue}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <!-- Time Input -->
    <div>
      <label class="block text-sm font-medium text-gray-700">Time Input</label>
      <input
        type="time"
        bind:value={timeValue}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <!-- Checkbox -->
    <div>
      <label class="flex items-center">
        <input
          type="checkbox"
          bind:checked={checkboxChecked}
          class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
        />
        <span class="ml-2 text-sm text-gray-700">My checkbox</span>
      </label>
    </div>

    <!-- Radio Buttons -->
    <div>
      <label class="block text-sm font-medium text-gray-700">Radio Buttons</label>
      <div class="mt-1 space-x-4">
        <label class="inline-flex items-center">
          <input
            type="radio"
            bind:group={radioValue}
            value="Yes"
            class="form-radio text-blue-600"
          />
          <span class="ml-2">Yes</span>
        </label>
        <label class="inline-flex items-center">
          <input
            type="radio"
            bind:group={radioValue}
            value="No"
            class="form-radio text-blue-600"
          />
          <span class="ml-2">No</span>
        </label>
      </div>
    </div>

    <!-- Dropdown -->
    <div>
      <label class="block text-sm font-medium text-gray-700">Dropdown List</label>
      <select
        bind:value={dropdownValue}
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <option value="">Select an option</option>
        <option value="1">Option 1</option>
        <option value="2">Option 2</option>
        <option value="3">Option 3</option>
      </select>
    </div>

    <!-- Listbox -->
    <div>
      <label class="block text-sm font-medium text-gray-700">List Box</label>
      <select
        bind:value={listboxValue}
        size="3"
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <option value="1">Option 1</option>
        <option value="2">Option 2</option>
        <option value="3">Option 3</option>
      </select>
    </div>

    <!-- Textarea -->
    <div class="md:col-span-2">
      <label class="block text-sm font-medium text-gray-700">Text Area</label>
      <textarea
        bind:value={textareaValue}
        rows="3"
        class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      ></textarea>
    </div>
  </div>

  <div class="mt-6 flex gap-2">
    <button
      onclick={() => modalOpen = true}
      class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded"
    >
      Popup
    </button>
    <button
      onclick={handleSubmit}
      class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
    >
      Ok
    </button>
  </div>
</div>

<!-- Popup Modal -->
<Modal 
  bind:open={modalOpen} 
  title="The Header"
  onClose={() => modalOpen = false}
>
  <p>The content</p>
  <br><br><br>
  
  {#snippet footer()}
    <button
      type="button"
      class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
      onclick={() => modalOpen = false}
    >
      Ok
    </button>
  {/snippet}
</Modal>