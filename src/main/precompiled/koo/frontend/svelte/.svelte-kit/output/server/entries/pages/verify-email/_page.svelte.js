import "../../../chunks/async.js";
import { e as escape_html, a as attr, u as unsubscribe_stores, s as store_get } from "../../../chunks/index2.js";
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
    let password = "";
    let confirmPassword = "";
    let submitting = false;
    $$renderer2.push(`<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"><div class="flex items-center justify-center min-h-[60vh]"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">`);
    {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="text-center mb-6">`);
      {
        $$renderer2.push("<!--[-1-->");
        $$renderer2.push(`<h1 class="text-2xl font-bold text-gray-900">${escape_html(tt("verify.verify_email_title") || "Verify Your Email")}</h1>`);
      }
      $$renderer2.push(`<!--]--> <p class="text-gray-600 mt-2">${escape_html(tt("verify.verify_email_desc") || "Set your password to activate your account")}</p> `);
      {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></div> <form><div class="mb-4"><label for="password" class="block text-sm font-medium text-gray-700 mb-1">${escape_html(tt("common.password") || "Password")}</label> <input type="password" id="password"${attr("value", password)} class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"${attr("placeholder", tt("common.password_placeholder") || "Enter password")} required="" minlength="6"/></div> <div class="mb-4"><label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">${escape_html(tt("common.confirm_password") || "Confirm Password")}</label> <input type="password" id="confirmPassword"${attr("value", confirmPassword)} class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"${attr("placeholder", tt("common.confirm_password_placeholder") || "Confirm password")} required=""/></div> `);
      {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--> <button type="submit"${attr("disabled", submitting, true)} class="w-full py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">`);
      {
        $$renderer2.push("<!--[-1-->");
        $$renderer2.push(`${escape_html(tt("verify.verify_and_set_password") || "Verify & Set Password")}`);
      }
      $$renderer2.push(`<!--]--></button></form>`);
    }
    $$renderer2.push(`<!--]--></div></div></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
