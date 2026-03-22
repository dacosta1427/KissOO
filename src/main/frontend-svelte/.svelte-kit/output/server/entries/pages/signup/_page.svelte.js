import { a as attr, b as attr_class, e as escape_html, d as derived, c as stringify } from "../../../chunks/async.js";
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
    let confirmPassword = "";
    let isValid = derived(() => username.length >= 3 && password.length >= 3 && confirmPassword.length > 0 && password === confirmPassword);
    $$renderer2.push(`<div class="min-h-screen bg-gray-50 flex items-center justify-center"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6">Sign Up</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <form><div class="mb-4"><label for="username" class="block text-gray-700 text-sm font-bold mb-2">Username</label> <input type="text" id="username"${attr("value", username)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter username" autocomplete="username"/></div> <div class="mb-4"><label for="password" class="block text-gray-700 text-sm font-bold mb-2">Password</label> <input type="password" id="password"${attr("value", password)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter password" autocomplete="new-password"/></div> <div class="mb-6"><label for="confirmPassword" class="block text-gray-700 text-sm font-bold mb-2">Confirm Password</label> <input type="password" id="confirmPassword"${attr("value", confirmPassword)}${attr_class(`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${stringify("")}`)} placeholder="Confirm password" autocomplete="new-password"/> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--></div> <button type="submit"${attr("disabled", !isValid(), true)} class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape_html("Sign Up")}</button></form> <p class="text-gray-500 text-xs text-center mt-4">Already have an account? <a href="/login" class="text-blue-600 hover:text-blue-800">Login</a></p></div></div>`);
  });
}
export {
  _page as default
};
