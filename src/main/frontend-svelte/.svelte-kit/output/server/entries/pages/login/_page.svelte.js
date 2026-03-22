import { a as attr, e as escape_html, d as derived } from "../../../chunks/async.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/root.js";
import "../../../chunks/state.svelte.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let username = "";
    let password = "";
    let isValid = derived(() => username.length > 0 && password.length > 0);
    $$renderer2.push(`<div class="min-h-screen bg-gray-50 flex items-center justify-center"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6">KissOO Login</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <form><div class="mb-4"><label for="username" class="block text-gray-700 text-sm font-bold mb-2">Username</label> <input type="text" id="username"${attr("value", username)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter username" autocomplete="username"/></div> <div class="mb-6"><label for="password" class="block text-gray-700 text-sm font-bold mb-2">Password</label> <input type="password" id="password"${attr("value", password)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter password" autocomplete="current-password"/></div> <button type="submit"${attr("disabled", !isValid(), true)} class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape_html("Login")}</button></form> <p class="text-gray-500 text-xs text-center mt-4">Don't have an account? <a href="/signup" class="text-blue-600 hover:text-blue-800">Sign Up</a></p></div></div>`);
  });
}
export {
  _page as default
};
