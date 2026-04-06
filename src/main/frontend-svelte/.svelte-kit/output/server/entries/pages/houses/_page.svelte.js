import "../../../chunks/async.js";
import { e as escape_html, f as attr_class, a as attr, c as ensure_array_like, u as unsubscribe_stores, s as store_get, d as derived } from "../../../chunks/index2.js";
import { s as session } from "../../../chunks/session.svelte.js";
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
    let isAdmin = derived(() => session.isAdmin === true);
    let houses = [];
    let owners = [];
    let bookings = [];
    let viewMode = "card";
    let filteredHouses = derived(() => houses);
    function getOwnerName(ownerId) {
      if (!ownerId || ownerId === 0) return t("houses.no_owner");
      const owner = owners.find((o) => o.id === ownerId);
      return owner ? owner.name : t("houses.unknown");
    }
    function getBookingCount(houseId) {
      return bookings.filter((b) => b.house_id === houseId).length;
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
          $$renderer2.push(`<div class="house-card clickable svelte-z3aeqy"><div class="card-header svelte-z3aeqy"><h3 class="house-name svelte-z3aeqy">${escape_html(house.name)}</h3> <button type="button"${attr_class("card-toggle svelte-z3aeqy", void 0, { "active": house.active })}${attr("title", house.active ? "Deactivate house" : "Activate house")}${attr("aria-label", house.active ? "Deactivate house" : "Activate house")}></button></div> <p class="house-address svelte-z3aeqy">${escape_html(house.address)}</p> `);
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
          $$renderer2.push(`<!--]--> <p class="house-bookings">${escape_html(tt("bookings.title"))}: ${escape_html(getBookingCount(house.id))}</p> <div class="house-actions svelte-z3aeqy"><button class="btn btn-secondary btn-sm svelte-z3aeqy"${attr("title", tt("hints.edit_item"))}>${escape_html(tt("common.edit"))}</button> <button class="btn btn-primary btn-sm svelte-z3aeqy"${attr("title", tt("bookings.add_booking"))}>${escape_html(tt("bookings.add_booking"))}</button> <button class="btn btn-danger btn-sm svelte-z3aeqy"${attr("title", tt("hints.delete_item"))}>${escape_html(tt("common.delete"))}</button></div></div>`);
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
