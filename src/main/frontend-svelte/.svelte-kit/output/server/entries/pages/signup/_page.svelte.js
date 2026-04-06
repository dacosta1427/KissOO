import "../../../chunks/async.js";
import { e as escape_html, a as attr, f as attr_class, u as unsubscribe_stores, s as store_get, d as derived, g as stringify } from "../../../chunks/index2.js";
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
    let confirmPassword = "";
    let email = "";
    let name = "";
    let isValid = derived(() => username.length >= 3 && password.length >= 3 && confirmPassword.length > 0 && password === confirmPassword && email.length > 0 && name.length > 0);
    $$renderer2.push(`<div class="min-h-screen bg-gray-50 flex items-center justify-center"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6">${escape_html(tt("auth.signup_title"))}</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <form><div class="mb-4"><label for="username" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.username"))}</label> <input type="text" id="username"${attr("value", username)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.username"))} autocomplete="username"/></div> <div class="mb-4"><label for="email" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.email"))}</label> <input type="email" id="email"${attr("value", email)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.email"))} autocomplete="email"/></div> <div class="mb-4"><label for="name" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.name"))}</label> <input type="text" id="name"${attr("value", name)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.name"))} autocomplete="name"/></div> <div class="mb-4"><label for="password" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.password"))}</label> <input type="password" id="password"${attr("value", password)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.password"))} autocomplete="new-password"/></div> <div class="mb-6"><label for="confirmPassword" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.confirm_password"))}</label> <input type="password" id="confirmPassword"${attr("value", confirmPassword)}${attr_class(`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${stringify("")}`)}${attr("placeholder", tt("auth.confirm_password"))} autocomplete="new-password"/> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--></div> <button type="submit"${attr("disabled", !isValid(), true)} class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape_html(t("auth.signup_button"))}</button></form> <p class="text-gray-500 text-xs text-center mt-4">${escape_html(tt("auth.have_account"))} <a href="/login" class="text-blue-600 hover:text-blue-800">${escape_html(tt("auth.login_here"))}</a></p></div></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
