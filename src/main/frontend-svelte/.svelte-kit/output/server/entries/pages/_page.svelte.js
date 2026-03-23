import "../../chunks/async.js";
import "clsx";
import "@sveltejs/kit/internal";
import "../../chunks/exports.js";
import "../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../chunks/root.js";
import "../../chunks/state.svelte.js";
import { s as session } from "../../chunks/session.svelte.js";
function isAuthenticated() {
  return session.isAuthenticated;
}
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    $$renderer2.push(`<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"><div class="text-center"><h2 class="text-3xl font-bold text-gray-900 mb-4">Welcome to KissOO Svelte 5</h2> <p class="text-lg text-gray-600 mb-8">Modern Svelte 5 frontend with Simple API Modules</p> <div class="mb-4">`);
    {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<span class="text-red-600">Not connected to backend</span>`);
    }
    $$renderer2.push(`<!--]--></div> <div class="space-y-4">`);
    if (isAuthenticated()) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<button class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg">Test Connection</button> <button class="block w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-lg">User Management</button>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<p class="text-gray-600 mb-4">Please login to access the application.</p> <button class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg">Login</button> <a href="/signup" class="block w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-lg">Sign Up</a>`);
    }
    $$renderer2.push(`<!--]--></div></div></main>`);
  });
}
export {
  _page as default
};
