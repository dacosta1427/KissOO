<script lang="ts">
  import { addNumbersGroovy, addNumbersJava, addNumbersLisp } from '$lib/api/RestServices';

  // State
  let num1 = $state(0);
  let num2 = $state(0);
  let result = $state<number | null>(null);
  let loading = $state(false);
  let error = $state('');

  async function callGroovy() {
    loading = true;
    error = '';
    result = null;
    try {
      const res = await addNumbersGroovy(num1, num2);
      if (res.success) {
        result = res.num3 ?? 0;
      } else {
        error = res.error || 'Failed to call Groovy service';
      }
    } catch (e: any) {
      error = 'Failed to call Groovy service: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }

  async function callJava() {
    loading = true;
    error = '';
    result = null;
    try {
      const res = await addNumbersJava(num1, num2);
      if (res.success) {
        result = res.num3 ?? 0;
      } else {
        error = res.error || 'Failed to call Java service';
      }
    } catch (e: any) {
      error = 'Failed to call Java service: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }

  async function callLisp() {
    loading = true;
    error = '';
    result = null;
    try {
      const res = await addNumbersLisp(num1, num2);
      if (res.success) {
        result = res.num3 ?? 0;
      } else {
        error = res.error || 'Failed to call Lisp service';
      }
    } catch (e: any) {
      error = 'Failed to call Lisp service: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }
</script>

<div class="p-6 max-w-2xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">REST Services</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  <div class="space-y-6">
    <div>
      <label class="block text-sm font-medium text-gray-700">Number 1</label>
      <input
        type="number"
        bind:value={num1}
        class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <div>
      <label class="block text-sm font-medium text-gray-700">Number 2</label>
      <input
        type="number"
        bind:value={num2}
        class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <div>
      <label class="block text-sm font-medium text-gray-700">Result</label>
      <input
        type="number"
        value={result ?? ''}
        disabled
        class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md bg-gray-100"
      />
    </div>

    <div class="flex gap-2">
      <button
        onclick={callGroovy}
        disabled={loading}
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        Call Groovy
      </button>
      <button
        onclick={callJava}
        disabled={loading}
        class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        Call Java
      </button>
      <button
        onclick={callLisp}
        disabled={loading}
        class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        Call Lisp
      </button>
    </div>

    <p class="text-gray-600 mt-4">
      Note: If using Lisp (not a requirement), the first time you use it, it has to perform some one-time initialization.
      Thus, the first call takes a long time, but after the initialization is done, all the calls thereafter are fast.
    </p>
  </div>
</div>