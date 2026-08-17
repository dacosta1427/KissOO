<script lang="ts">
  import { uploadFile } from '$lib/api/FileUpload';

  // State
  let selectedFile = $state<File | null>(null);
  let loading = $state(false);
  let error = $state('');
  let success = $state('');

  function handleFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      selectedFile = input.files[0] ?? null;
      error = '';
      success = '';
    }
  }

  async function handleUpload() {
    if (!selectedFile) {
      error = 'Please select a file first';
      return;
    }

    loading = true;
    error = '';
    success = '';

    try {
      // Example additional data
      const additionalData = {
        var1: 22,
        var2: 33
      };
      
      const result = await uploadFile(selectedFile, additionalData);
      
      if (result.success) {
        success = `File "${result.fileName}" uploaded successfully (${result.fileSize} bytes)`;
        selectedFile = null;
        // Reset file input
        const fileInput = document.getElementById('file-input') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
      } else {
        error = result.error || 'Upload failed';
      }
    } catch (e: any) {
      error = 'Upload failed: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }
</script>

<div class="p-6 max-w-2xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">File Upload</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  {#if success}
    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
      {success}
    </div>
  {/if}

  <div class="space-y-6">
    <div>
      <label class="block text-sm font-medium text-gray-700">Select File</label>
      <input
        id="file-input"
        type="file"
        onchange={handleFileSelect}
        class="mt-1 block w-full text-sm text-gray-500
          file:mr-4 file:py-2 file:px-4
          file:rounded-md file:border-0
          file:text-sm file:font-semibold
          file:bg-blue-50 file:text-blue-700
          hover:file:bg-blue-100"
      />
    </div>

    {#if selectedFile}
      <div class="text-sm text-gray-600">
        Selected: {selectedFile.name} ({selectedFile.size} bytes)
      </div>
    {/if}

    <button
      onclick={handleUpload}
      disabled={!selectedFile || loading}
      class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
    >
      {loading ? 'Uploading...' : 'Upload'}
    </button>
  </div>
</div>