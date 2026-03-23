import "../../../chunks/async.js";
import { a as attr } from "../../../chunks/attributes.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let loading = false;
    $$renderer2.push(`<div class="p-6 max-w-4xl mx-auto"><h1 class="text-2xl font-bold mb-6">Perst Benchmark</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="space-y-8"><div class="border p-4 rounded-lg"><h3 class="text-lg font-semibold mb-3">Setup</h3> <button${attr("disabled", loading, true)} class="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Clear All Data</button></div> <div class="border p-4 rounded-lg"><h3 class="text-lg font-semibold mb-3">Insert Operations</h3> <div class="flex gap-2"><button${attr("disabled", loading, true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Bulk Insert (100)</button> <button${attr("disabled", loading, true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Bulk Insert (1,000)</button></div></div> <div class="border p-4 rounded-lg"><h3 class="text-lg font-semibold mb-3">Query Operations</h3> <div class="flex gap-2"><button${attr("disabled", loading, true)} class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Select All</button> <button${attr("disabled", loading, true)} class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Count Records</button></div></div> <div class="border p-4 rounded-lg"><h3 class="text-lg font-semibold mb-3">Update Operations</h3> <button${attr("disabled", loading, true)} class="bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Bulk Update</button></div> <div class="border p-4 rounded-lg"><h3 class="text-lg font-semibold mb-3">Delete Operations</h3> <button${attr("disabled", loading, true)} class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Bulk Delete</button></div> <div class="border p-4 rounded-lg"><h3 class="text-lg font-semibold mb-3">Aggregation Operations</h3> <div class="flex gap-2"><button${attr("disabled", loading, true)} class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Sum</button> <button${attr("disabled", loading, true)} class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Average</button> <button${attr("disabled", loading, true)} class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Group By</button></div></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--></div></div>`);
  });
}
export {
  _page as default
};
