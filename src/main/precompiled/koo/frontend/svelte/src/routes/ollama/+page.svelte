<script lang="ts">
  import { onMount } from 'svelte';
  import { isOllamaUp, listModels, ask } from '$lib/api/Ollama';

  // State
  let ollamaUp = $state(false);
  let models = $state<string[]>([]);
  let selectedModel = $state('');
  let prompt = $state('');
  let response = $state('');
  let htmlResponse = $state('');
  let loading = $state(false);
  let error = $state('');
  let context = $state('');
  let initializing = $state(true);

  onMount(async () => {
    try {
      const upResult = await isOllamaUp();
      if (upResult.success && upResult.isOllamaUp) {
        ollamaUp = true;
        const modelsResult = await listModels();
        if (modelsResult.success && modelsResult.models) {
          models = modelsResult.models;
          if (models.length > 0) {
            selectedModel = models[0] ?? '';
          }
        } else {
          error = modelsResult.error || 'Failed to load models';
        }
      } else {
        error = 'Ollama server is not running on your machine.';
      }
    } catch (e: any) {
      error = 'Failed to connect to Ollama: ' + (e.message || 'Unknown error');
    } finally {
      initializing = false;
    }
  });

  async function handleSend() {
    if (!selectedModel || !prompt.trim()) {
      error = 'Please select a model and enter a prompt';
      return;
    }

    loading = true;
    error = '';
    response = '';
    htmlResponse = '';

    try {
      const fullPrompt = context 
        ? context + "My new question is: " + prompt 
        : prompt;
      
      const result = await ask(selectedModel, fullPrompt);
      
      if (result.success) {
        response = result.textResponse || '';
        htmlResponse = result.htmlResponse || '';
        
        if (!context) {
          context = "Our prior conversations are as follows:\n";
        }
        context += prompt + '\n' + response + '\n';
      } else {
        error = result.error || 'Failed to get response';
      }
    } catch (e: any) {
      error = 'Failed to get response: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }

  function handleNewContext() {
    context = '';
    prompt = '';
    response = '';
    htmlResponse = '';
  }
</script>

<div class="p-6 max-w-4xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">AI / LLM / Ollama Interface</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  {#if initializing}
    <p class="text-gray-500">Connecting to Ollama...</p>
  {:else if !ollamaUp}
    <div class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded mb-4">
      Ollama server is not running. Please start Ollama and refresh this page.
    </div>
  {:else}
    <div class="space-y-6">
      <div>
        <label class="block text-sm font-medium text-gray-700">Select a model:</label>
        <select
          bind:value={selectedModel}
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          required
        >
          <option value="">(select a model)</option>
          {#each models as model}
            <option value={model}>{model}</option>
          {/each}
        </select>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700">Prompt:</label>
        <textarea
          bind:value={prompt}
          placeholder="Enter your prompt..."
          rows="4"
          minlength="10"
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        ></textarea>
      </div>

      {#if htmlResponse}
        <div>
          <label class="block text-sm font-medium text-gray-700">Response:</label>
          <div class="mt-1 p-3 border border-gray-300 rounded-md bg-gray-50 prose max-w-none">
            {@html htmlResponse}
          </div>
        </div>
      {/if}

      <div class="flex gap-2">
        <button
          onclick={handleSend}
          disabled={loading || !selectedModel || !prompt.trim()}
          class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          {loading ? 'Thinking...' : 'Send'}
        </button>
        <button
          onclick={handleNewContext}
          disabled={loading}
          class="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          New Context
        </button>
      </div>

      <p class="text-gray-600 mt-4">
        Note: This depends on a locally running Ollama server with downloaded models. See <a href="https://ollama.com" class="text-blue-600 hover:underline">https://ollama.com</a>
      </p>
    </div>
  {/if}
</div>