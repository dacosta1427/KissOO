<script lang="ts">
  import { 
    setupTable, 
    bulkInsert, 
    selectAll, 
    countRecords, 
    bulkUpdate, 
    bulkDelete, 
    aggregateSum, 
    aggregateAvg, 
    aggregateGroupBy 
  } from '$lib/api/Benchmark';
  import type { BenchmarkResult } from '$lib/api/Benchmark';

  // State
  let result = $state<BenchmarkResult | null>(null);
  let loading = $state(false);
  let error = $state('');

  async function runOperation(operation: () => Promise<BenchmarkResult>) {
    loading = true;
    error = '';
    result = null;
    try {
      const res = await operation();
      if (res.success) {
        result = res;
      } else {
        error = res.error || 'Operation failed';
      }
    } catch (e: any) {
      error = 'Operation failed: ' + (e.message || 'Unknown error');
    } finally {
      loading = false;
    }
  }

  function formatResult(result: BenchmarkResult): string {
    const parts: string[] = [];
    if (result.count !== undefined) parts.push(`Count: ${result.count}`);
    if (result.elapsed !== undefined) parts.push(`Elapsed: ${result.elapsed} ms`);
    if (result.rate !== undefined) parts.push(`Rate: ${result.rate.toFixed(2)} ops/sec`);
    if (result.sum !== undefined) parts.push(`Sum: ${result.sum}`);
    if (result.avg !== undefined) parts.push(`Average: ${result.avg}`);
    if (result.message) parts.push(`Message: ${result.message}`);
    if (result.results) {
      parts.push('Grouped Results:');
      for (const [key, value] of Object.entries(result.results)) {
        parts.push(`  ${key}: sum=${value.sum}, count=${value.count}`);
      }
    }
    return parts.join('\n');
  }
</script>

<div class="p-6 max-w-4xl mx-auto">
  <h1 class="text-2xl font-bold mb-6">Perst Benchmark</h1>

  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
    </div>
  {/if}

  <div class="space-y-8">
    <!-- Setup Section -->
    <div class="border p-4 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">Setup</h3>
      <button
        onclick={() => runOperation(setupTable)}
        disabled={loading}
        class="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        Clear All Data
      </button>
    </div>

    <!-- Insert Section -->
    <div class="border p-4 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">Insert Operations</h3>
      <div class="flex gap-2">
        <button
          onclick={() => runOperation(() => bulkInsert(100))}
          disabled={loading}
          class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Bulk Insert (100)
        </button>
        <button
          onclick={() => runOperation(() => bulkInsert(1000))}
          disabled={loading}
          class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Bulk Insert (1,000)
        </button>
      </div>
    </div>

    <!-- Query Section -->
    <div class="border p-4 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">Query Operations</h3>
      <div class="flex gap-2">
        <button
          onclick={() => runOperation(selectAll)}
          disabled={loading}
          class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Select All
        </button>
        <button
          onclick={() => runOperation(countRecords)}
          disabled={loading}
          class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Count Records
        </button>
      </div>
    </div>

    <!-- Update Section -->
    <div class="border p-4 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">Update Operations</h3>
      <button
        onclick={() => runOperation(bulkUpdate)}
        disabled={loading}
        class="bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        Bulk Update
      </button>
    </div>

    <!-- Delete Section -->
    <div class="border p-4 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">Delete Operations</h3>
      <button
        onclick={() => runOperation(bulkDelete)}
        disabled={loading}
        class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        Bulk Delete
      </button>
    </div>

    <!-- Aggregation Section -->
    <div class="border p-4 rounded-lg">
      <h3 class="text-lg font-semibold mb-3">Aggregation Operations</h3>
      <div class="flex gap-2">
        <button
          onclick={() => runOperation(aggregateSum)}
          disabled={loading}
          class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Sum
        </button>
        <button
          onclick={() => runOperation(aggregateAvg)}
          disabled={loading}
          class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Average
        </button>
        <button
          onclick={() => runOperation(aggregateGroupBy)}
          disabled={loading}
          class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          Group By
        </button>
      </div>
    </div>

    <!-- Results Section -->
    {#if result}
      <div class="border p-4 rounded-lg bg-gray-50">
        <h3 class="text-lg font-semibold mb-3">Results</h3>
        <pre class="whitespace-pre-wrap font-mono text-sm">{formatResult(result)}</pre>
      </div>
    {/if}
  </div>
</div>