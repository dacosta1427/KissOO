import "../../../chunks/async.js";
import { a as attr } from "../../../chunks/index2.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/root.js";
import "../../../chunks/state.svelte.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let num1 = 0;
    let num2 = 0;
    let loading = false;
    $$renderer2.push(`<div class="p-6 max-w-2xl mx-auto"><h1 class="text-2xl font-bold mb-6">REST Services</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="space-y-6"><div><label class="block text-sm font-medium text-gray-700">Number 1</label> <input type="number"${attr("value", num1)} class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Number 2</label> <input type="number"${attr("value", num2)} class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Result</label> <input type="number"${attr("value", "")} disabled="" class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md bg-gray-100"/></div> <div class="flex gap-2"><button${attr("disabled", loading, true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Call Groovy</button> <button${attr("disabled", loading, true)} class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Call Java</button> <button${attr("disabled", loading, true)} class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Call Lisp</button></div> <p class="text-gray-600 mt-4">Note: If using Lisp (not a requirement), the first time you use it, it has to perform some one-time initialization.
      Thus, the first call takes a long time, but after the initialization is done, all the calls thereafter are fast.</p></div></div>`);
  });
}
export {
  _page as default
};
