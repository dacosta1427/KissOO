import { c as create_ssr_component, d as add_attribute, e as escape } from "../../../chunks/ssr.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/state.svelte.js";
const Page = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let username = "";
  let password = "";
  return `<div class="min-h-screen bg-gray-50 flex items-center justify-center"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6" data-svelte-h="svelte-pcd23i">KissOO Login</h1> ${``} <form><div class="mb-4"><label for="username" class="block text-gray-700 text-sm font-bold mb-2" data-svelte-h="svelte-ysq2q0">Username</label> <input type="text" id="username" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter username" autocomplete="username"${add_attribute("value", username, 0)}></div> <div class="mb-6"><label for="password" class="block text-gray-700 text-sm font-bold mb-2" data-svelte-h="svelte-1v7xlxw">Password</label> <input type="password" id="password" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="Enter password" autocomplete="current-password"${add_attribute("value", password, 0)}></div> <button type="submit" ${""} class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape("Login")}</button></form> <p class="text-gray-500 text-xs text-center mt-4" data-svelte-h="svelte-vymks2">Don&#39;t have an account? <a href="/signup" class="text-blue-600 hover:text-blue-800">Sign Up</a></p></div></div>`;
});
export {
  Page as default
};
