import "../../../../chunks/async.js";
import { e as escape_html, u as unsubscribe_stores, s as store_get } from "../../../../chunks/index2.js";
import "@sveltejs/kit/internal";
import "../../../../chunks/exports.js";
import "../../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../../chunks/root.js";
import "../../../../chunks/state.svelte.js";
import { t, c as currentLocale } from "../../../../chunks/index3.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    $$renderer2.push(`<div class="owners-page svelte-1laq395"><div class="page-header svelte-1laq395"><button class="btn btn-secondary svelte-1laq395">${escape_html(tt("common.back"))}</button> <h1 class="svelte-1laq395">${escape_html(tt("owners.edit_owner"))}</h1> <button class="btn btn-danger btn-sm svelte-1laq395">${escape_html(tt("common.delete"))}</button></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]-->`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
