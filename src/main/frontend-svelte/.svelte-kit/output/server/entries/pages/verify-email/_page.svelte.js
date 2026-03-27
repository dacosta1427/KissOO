import "../../../chunks/async.js";
import { u as unsubscribe_stores, s as store_get } from "../../../chunks/index2.js";
import { e as escape_html } from "../../../chunks/attributes.js";
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
    $$renderer2.push(`<div class="min-h-screen bg-gray-50 flex items-center justify-center"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">`);
    {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div> <h1 class="text-xl font-semibold text-gray-900">${escape_html(tt("verify.loading"))}</h1> <p class="text-gray-600 mt-2">${escape_html(tt("verify.please_wait"))}</p>`);
    }
    $$renderer2.push(`<!--]--></div></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
