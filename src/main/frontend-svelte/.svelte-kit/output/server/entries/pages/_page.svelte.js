import { c as create_ssr_component, d as add_attribute, e as escape } from "../../chunks/ssr.js";
import { S as Server } from "../../chunks/Server.js";
import "@sveltejs/kit/internal";
import "../../chunks/exports.js";
import "../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../chunks/state.svelte.js";
function goto(url, opts = {}) {
  {
    throw new Error("Cannot call goto(...) on the server");
  }
}
const Page = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let backendUrl = "";
  let isConnected = false;
  async function testConnection() {
    try {
      const result = await Server.call("Test", "ping", {});
      isConnected = result._Success;
      backendUrl = Server.getURL();
    } catch {
      isConnected = false;
      backendUrl = Server.getURL();
    }
  }
  function navigateToUsers() {
    goto();
  }
  return `<div class="min-h-screen bg-gray-50"><header class="bg-white shadow-sm border-b"><div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"><div class="flex justify-between items-center h-16"><div class="flex items-center" data-svelte-h="svelte-1u9kmhi"><h1 class="text-2xl font-bold text-gray-900">KissOO Svelte 5</h1></div> <div class="flex items-center space-x-4"><div class="text-sm text-gray-500">${isConnected ? `<span class="text-green-600">Connected to ${escape(backendUrl)}</span>` : `<span class="text-red-600" data-svelte-h="svelte-1lvnmsp">Not connected</span>`}</div></div></div></div></header> <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"><div class="text-center"><h2 class="text-3xl font-bold text-gray-900 mb-4" data-svelte-h="svelte-13sh5xn">Welcome to KissOO Svelte 5</h2> <p class="text-lg text-gray-600 mb-8" data-svelte-h="svelte-9dwir5">Modern Svelte 5 frontend for the KissOO framework</p> <div class="space-y-4"><button class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg"${add_attribute("onclick", testConnection, 0)}>Test Connection</button> <button class="block w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-lg"${add_attribute("onclick", navigateToUsers, 0)}>User Management</button></div></div></main></div>`;
});
export {
  Page as default
};
