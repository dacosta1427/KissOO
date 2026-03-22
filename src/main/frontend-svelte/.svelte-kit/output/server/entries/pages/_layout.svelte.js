import { s as slot } from "../../chunks/async.js";
import "clsx";
import { s as session } from "../../chunks/session.svelte.js";
import "@sveltejs/kit/internal";
import "../../chunks/exports.js";
import "../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../chunks/root.js";
import "../../chunks/state.svelte.js";
function Navbar($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    $$renderer2.push(`<header class="bg-white shadow-sm border-b"><div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"><div class="flex justify-between items-center h-16"><div class="flex items-center"><a href="/" class="text-2xl font-bold text-gray-900 hover:text-gray-700">KissOO Svelte 5</a></div> <nav class="flex items-center space-x-4"><a href="/" class="text-gray-600 hover:text-gray-900 font-medium">Home</a> `);
    if (session.isAuthenticated) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">Users</a> <button class="text-red-600 hover:text-red-800 font-medium">Logout</button> <span class="text-green-600 text-sm">Authenticated</span>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">Login</a> <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">Sign Up</a>`);
    }
    $$renderer2.push(`<!--]--></nav></div></div></header>`);
  });
}
function _layout($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    Navbar($$renderer2);
    $$renderer2.push(`<!----> <!--[-->`);
    slot($$renderer2, $$props, "default", {});
    $$renderer2.push(`<!--]-->`);
  });
}
export {
  _layout as default
};
