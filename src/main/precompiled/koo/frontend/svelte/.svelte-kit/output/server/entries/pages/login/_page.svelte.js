import "../../../chunks/async.js";
import { e as escape_html, a as attr, u as unsubscribe_stores, s as store_get, d as derived } from "../../../chunks/index2.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/root.js";
import "../../../chunks/state.svelte.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let username = "";
    let password = "";
    let rememberMe = false;
    let isValid = derived(() => username.length > 0 && password.length > 0);
    $$renderer2.push(`<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"><div class="flex items-center justify-center min-h-[60vh]"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6">${escape_html(tt("auth.login_title"))}</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <form><div class="mb-4"><label for="username" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.username"))}</label> <input type="text" id="username"${attr("value", username)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.username"))} autocomplete="username"/></div> <div class="mb-4"><label for="password" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.password"))}</label> <input type="password" id="password"${attr("value", password)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.password"))} autocomplete="current-password"/></div> <div class="mb-4 flex items-center"><input type="checkbox" id="rememberMe"${attr("checked", rememberMe, true)} class="mr-2 leading-tight"/> <label for="rememberMe" class="text-sm text-gray-700">${escape_html(tt("auth.remember_me"))}</label></div> <button type="submit"${attr("disabled", !isValid(), true)} class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape_html(t("auth.login_button"))}</button></form> <p class="text-gray-500 text-xs text-center mt-4">${escape_html(tt("auth.no_account"))} <a href="/signup" class="text-blue-600 hover:text-blue-800">${escape_html(tt("auth.sign_up_here"))}</a></p></div></div></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
