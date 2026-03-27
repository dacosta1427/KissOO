import "../../../chunks/async.js";
import { b as attr_class, e as ensure_array_like, u as unsubscribe_stores, s as store_get, d as derived } from "../../../chunks/index2.js";
import { s as session } from "../../../chunks/session.svelte.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
import { e as escape_html, a as attr } from "../../../chunks/attributes.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let isAdmin = derived(() => session.username === "admin" || session.username === "administrator");
    let houses = [];
    let owners = [];
    let viewMode = "card";
    let filteredHouses = derived(() => isAdmin() ? houses : houses.filter((h) => h.owner === session.ownerId));
    function getOwnerName(ownerId) {
      if (!ownerId || ownerId === 0) return t("houses.no_owner");
      const owner = owners.find((o) => o.id === ownerId);
      return owner ? owner.name : t("houses.unknown");
    }
    $$renderer2.push(`<div class="houses-page svelte-z3aeqy"><div class="page-header svelte-z3aeqy"><h1 class="svelte-z3aeqy">${escape_html(tt("houses.title"))}</h1> <button class="btn btn-primary svelte-z3aeqy">${escape_html(tt("houses.add_house"))}</button></div> `);
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
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="view-toggle svelte-z3aeqy"><button${attr_class("toggle-btn svelte-z3aeqy", void 0, { "active": viewMode === "card" })}${attr("title", tt("hints.view_details"))}>${escape_html(tt("houses.card_view"))}</button> <button${attr_class("toggle-btn svelte-z3aeqy", void 0, { "active": viewMode === "table" })}${attr("title", tt("hints.view_details"))}>${escape_html(tt("houses.table_view"))}</button></div> `);
    {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="houses-grid svelte-z3aeqy">`);
      if (filteredHouses().length === 0) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<div class="empty-message svelte-z3aeqy">${escape_html(isAdmin() ? tt("houses.no_houses") : tt("houses.no_houses_owner"))}</div>`);
      } else {
        $$renderer2.push("<!--[-1-->");
        $$renderer2.push(`<!--[-->`);
        const each_array_2 = ensure_array_like(filteredHouses());
        for (let $$index_2 = 0, $$length = each_array_2.length; $$index_2 < $$length; $$index_2++) {
          let house = each_array_2[$$index_2];
          $$renderer2.push(`<div class="house-card svelte-z3aeqy"><h3 class="house-name svelte-z3aeqy">${escape_html(house.name)}</h3> <p class="house-address svelte-z3aeqy">${escape_html(house.address)}</p> `);
          if (house.owner) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`<p class="house-owner svelte-z3aeqy">${escape_html(tt("houses.owner"))}: ${escape_html(getOwnerName(house.owner))}</p>`);
          } else {
            $$renderer2.push("<!--[-1-->");
          }
          $$renderer2.push(`<!--]--> `);
          if (house.description) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`<p class="house-description svelte-z3aeqy">${escape_html(house.description)}</p>`);
          } else {
            $$renderer2.push("<!--[-1-->");
          }
          $$renderer2.push(`<!--]--> <div class="house-actions svelte-z3aeqy"><button class="btn btn-secondary btn-sm svelte-z3aeqy"${attr("title", tt("hints.edit_item"))}>${escape_html(tt("common.edit"))}</button> <button class="btn btn-danger btn-sm svelte-z3aeqy"${attr("title", tt("hints.delete_item"))}>${escape_html(tt("common.delete"))}</button></div></div>`);
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
