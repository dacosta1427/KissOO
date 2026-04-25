import "../../../chunks/async.js";
import { e as escape_html, f as attr_class, c as ensure_array_like, a as attr, u as unsubscribe_stores, s as store_get } from "../../../chunks/index2.js";
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
    let owners = [];
    let viewMode = "card";
    $$renderer2.push(`<div class="owners-page svelte-1c47b5t"><div class="page-header svelte-1c47b5t"><h1 class="svelte-1c47b5t">${escape_html(tt("owners.title"))}</h1> <button class="btn btn-primary svelte-1c47b5t">${escape_html(tt("owners.add_owner"))}</button></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="view-toggle svelte-1c47b5t"><button${attr_class("toggle-btn svelte-1c47b5t", void 0, { "active": viewMode === "card" })}>${escape_html(tt("houses.card_view"))}</button> <button${attr_class("toggle-btn svelte-1c47b5t", void 0, { "active": viewMode === "table" })}>${escape_html(tt("houses.table_view"))}</button></div> `);
      {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<div class="owners-grid svelte-1c47b5t">`);
        if (owners.length === 0 && true) {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<div class="empty-message svelte-1c47b5t">${escape_html(tt("owners.no_owners"))}</div>`);
        } else {
          $$renderer2.push("<!--[-1-->");
          $$renderer2.push(`<!--[-->`);
          const each_array_3 = ensure_array_like(owners);
          for (let $$index_3 = 0, $$length = each_array_3.length; $$index_3 < $$length; $$index_3++) {
            let owner = each_array_3[$$index_3];
            $$renderer2.push(`<div class="owner-card clickable svelte-1c47b5t"><div class="card-header svelte-1c47b5t"><h3 class="owner-name svelte-1c47b5t">${escape_html(owner.name)} `);
            if (owner.emailVerified) {
              $$renderer2.push("<!--[0-->");
              $$renderer2.push(`<span class="ml-1 text-green-600" title="Email verified"><svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg></span>`);
            } else {
              $$renderer2.push("<!--[-1-->");
              $$renderer2.push(`<span class="ml-1 text-red-600" title="Email not verified"><svg class="inline w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg></span>`);
            }
            $$renderer2.push(`<!--]--></h3> <button type="button"${attr_class("card-toggle svelte-1c47b5t", void 0, { "active": owner.canLogin })}${attr("title", owner.canLogin ? "Login enabled" : "Login disabled")}></button></div> `);
            if (owner.email) {
              $$renderer2.push("<!--[0-->");
              $$renderer2.push(`<p class="owner-detail svelte-1c47b5t">${escape_html(owner.email)}</p>`);
            } else {
              $$renderer2.push("<!--[-1-->");
            }
            $$renderer2.push(`<!--]--> `);
            if (owner.phone) {
              $$renderer2.push("<!--[0-->");
              $$renderer2.push(`<p class="owner-detail svelte-1c47b5t">${escape_html(owner.phone)}</p>`);
            } else {
              $$renderer2.push("<!--[-1-->");
            }
            $$renderer2.push(`<!--]--> `);
            if (owner.address) {
              $$renderer2.push("<!--[0-->");
              $$renderer2.push(`<p class="owner-detail svelte-1c47b5t">${escape_html(owner.address)}</p>`);
            } else {
              $$renderer2.push("<!--[-1-->");
            }
            $$renderer2.push(`<!--]--> <div class="owner-actions svelte-1c47b5t"><button class="btn btn-secondary btn-sm svelte-1c47b5t">${escape_html(tt("common.edit"))}</button> <button class="btn btn-danger btn-sm svelte-1c47b5t">${escape_html(tt("common.delete"))}</button></div></div>`);
          }
          $$renderer2.push(`<!--]-->`);
        }
        $$renderer2.push(`<!--]--></div>`);
      }
      $$renderer2.push(`<!--]-->`);
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
