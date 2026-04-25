import "../../../chunks/async.js";
import { e as escape_html, f as attr_class, c as ensure_array_like, a as attr, u as unsubscribe_stores, s as store_get } from "../../../chunks/index2.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/root.js";
import "../../../chunks/state.svelte.js";
/* empty css                                                  */
import { t, c as currentLocale } from "../../../chunks/index3.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let cleaners = [];
    let viewMode = "card";
    $$renderer2.push(`<div class="cleaners-page svelte-ivoje8"><div class="page-header svelte-ivoje8"><h1 class="svelte-ivoje8">${escape_html(tt("cleaners.title"))}</h1> <button class="btn btn-primary svelte-ivoje8">${escape_html(tt("cleaners.add_cleaner"))}</button></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="view-toggle svelte-ivoje8"><button${attr_class("toggle-btn svelte-ivoje8", void 0, { "active": viewMode === "card" })}>${escape_html(tt("houses.card_view"))}</button> <button${attr_class("toggle-btn svelte-ivoje8", void 0, { "active": viewMode === "table" })}>${escape_html(tt("houses.table_view"))}</button></div> `);
    {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="cleaners-grid svelte-ivoje8">`);
      if (cleaners.length === 0 && true) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<div class="empty-message svelte-ivoje8">${escape_html(tt("cleaners.no_cleaners"))}</div>`);
      } else {
        $$renderer2.push("<!--[-1-->");
        $$renderer2.push(`<!--[-->`);
        const each_array = ensure_array_like(cleaners);
        for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
          let cleaner = each_array[$$index];
          $$renderer2.push(`<div class="cleaner-card clickable svelte-ivoje8"><div class="card-header svelte-ivoje8"><h3 class="cleaner-name svelte-ivoje8">${escape_html(cleaner.name)} `);
          if (cleaner.emailVerified) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`<span class="ml-1 text-green-600" title="Email verified"><svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg></span>`);
          } else {
            $$renderer2.push("<!--[-1-->");
            $$renderer2.push(`<span class="ml-1 text-red-600" title="Email not verified"><svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg></span>`);
          }
          $$renderer2.push(`<!--]--></h3> <button type="button"${attr_class("card-toggle svelte-ivoje8", void 0, { "active": cleaner.canLogin })}${attr("title", cleaner.canLogin ? "Disable login" : "Enable login")}${attr("aria-label", cleaner.canLogin ? "Disable login" : "Enable login")}></button></div> `);
          if (cleaner.email) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`<p class="cleaner-detail svelte-ivoje8">${escape_html(cleaner.email)}</p>`);
          } else {
            $$renderer2.push("<!--[-1-->");
          }
          $$renderer2.push(`<!--]--> `);
          if (cleaner.phone) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`<p class="cleaner-detail svelte-ivoje8">${escape_html(cleaner.phone)}</p>`);
          } else {
            $$renderer2.push("<!--[-1-->");
          }
          $$renderer2.push(`<!--]--> `);
          if (cleaner.address) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`<p class="cleaner-detail svelte-ivoje8">${escape_html(cleaner.address)}</p>`);
          } else {
            $$renderer2.push("<!--[-1-->");
          }
          $$renderer2.push(`<!--]--> <div class="cleaner-actions svelte-ivoje8"><button class="btn btn-secondary btn-sm svelte-ivoje8">${escape_html(tt("common.edit"))}</button> <button class="btn btn-danger btn-sm svelte-ivoje8">${escape_html(tt("common.delete"))}</button></div></div>`);
        }
        $$renderer2.push(`<!--]-->`);
      }
      $$renderer2.push(`<!--]--></div>`);
    }
    $$renderer2.push(`<!--]--></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
