import { c as create_ssr_component, d as add_attribute, e as escape } from "../../../chunks/ssr.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/state.svelte.js";
const Page = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let username = "";
  let password = "";
  let confirmPassword = "";
  return `<div class="min-h-screen bg-gray-50 flex items-center justify-center"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6" data-svelte-h="svelte-1ipmtur">Sign Up</h1> ${``} <form><div class="mb-4"><label for="username" class="block text-gray-700 text-sm font-bold mb-2" data-svelte-h="svelte-ysq2q0">Username</label> <input type="text" id="username" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter username" autocomplete="username"${add_attribute("value", username, 0)}></div> <div class="mb-4"><label for="password" class="block text-gray-700 text-sm font-bold mb-2" data-svelte-h="svelte-1v7xlxw">Password</label> <input type="password" id="password" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter password" autocomplete="new-password"${add_attribute("value", password, 0)}></div> <div class="mb-6"><label for="confirmPassword" class="block text-gray-700 text-sm font-bold mb-2" data-svelte-h="svelte-s7a7c0">Confirm Password</label> <input type="password" id="confirmPassword" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Confirm password" autocomplete="new-password"${add_attribute("value", confirmPassword, 0)}></div> <button type="submit" ${""} class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape("Sign Up")}</button></form> <p class="text-gray-500 text-xs text-center mt-4" data-svelte-h="svelte-18zq0yi">Already have an account? <a href="/login" class="text-blue-600 hover:text-blue-800">Login</a></p></div></div>`;
});
export {
  Page as default
};
