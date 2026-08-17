import "../../../chunks/async.js";
import { e as escape_html, c as ensure_array_like, f as attr_class, a as attr, u as unsubscribe_stores, s as store_get, d as derived } from "../../../chunks/index2.js";
import { s as session } from "../../../chunks/session.svelte.js";
import "@sveltejs/kit/internal";
import "../../../chunks/exports.js";
import "../../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../../chunks/root.js";
import "../../../chunks/state.svelte.js";
/* empty css                                                  */
import { t, c as currentLocale } from "../../../chunks/index3.js";
function Table($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let {
      data = [],
      columns = [],
      actions = [],
      loading = false,
      error = null,
      selectedRow = null
    } = $$props;
    $$renderer2.push(`<div class="table-container svelte-1iq5b9c">`);
    if (error) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="error-message svelte-1iq5b9c">${escape_html(error)}</div>`);
    } else if (loading) {
      $$renderer2.push("<!--[1-->");
      $$renderer2.push(`<div class="loading-message svelte-1iq5b9c">Loading...</div>`);
    } else if (data.length === 0) {
      $$renderer2.push("<!--[2-->");
      $$renderer2.push(`<div class="empty-message svelte-1iq5b9c">No data available</div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<table class="data-table svelte-1iq5b9c"><thead><tr class="svelte-1iq5b9c"><!--[-->`);
      const each_array = ensure_array_like(columns);
      for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
        let column = each_array[$$index];
        $$renderer2.push(`<th class="svelte-1iq5b9c">${escape_html(column.label)}</th>`);
      }
      $$renderer2.push(`<!--]-->`);
      if (actions.length > 0) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<th class="svelte-1iq5b9c">Actions</th>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></tr></thead><tbody><!--[-->`);
      const each_array_1 = ensure_array_like(data);
      for (let index = 0, $$length = each_array_1.length; index < $$length; index++) {
        let row = each_array_1[index];
        $$renderer2.push(`<tr${attr_class("svelte-1iq5b9c", void 0, { "selected": selectedRow === row.id })}><!--[-->`);
        const each_array_2 = ensure_array_like(columns);
        for (let $$index_1 = 0, $$length2 = each_array_2.length; $$index_1 < $$length2; $$index_1++) {
          let column = each_array_2[$$index_1];
          $$renderer2.push(`<td class="svelte-1iq5b9c">`);
          if (column.formatter) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`${escape_html(column.formatter(row[column.key], row))}`);
          } else {
            $$renderer2.push("<!--[-1-->");
            $$renderer2.push(`${escape_html(row[column.key])}`);
          }
          $$renderer2.push(`<!--]--></td>`);
        }
        $$renderer2.push(`<!--]-->`);
        if (actions.length > 0) {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<td class="actions-cell svelte-1iq5b9c"><!--[-->`);
          const each_array_3 = ensure_array_like(actions);
          for (let $$index_2 = 0, $$length2 = each_array_3.length; $$index_2 < $$length2; $$index_2++) {
            let action = each_array_3[$$index_2];
            $$renderer2.push(`<button class="action-btn svelte-1iq5b9c"${attr("title", action.title)}>`);
            if (action.icon) {
              $$renderer2.push("<!--[0-->");
              $$renderer2.push(`<span class="action-icon">${escape_html(action.icon)}</span>`);
            } else {
              $$renderer2.push("<!--[-1-->");
            }
            $$renderer2.push(`<!--]--> ${escape_html(action.label)}</button>`);
          }
          $$renderer2.push(`<!--]--></td>`);
        } else {
          $$renderer2.push("<!--[-1-->");
        }
        $$renderer2.push(`<!--]--></tr>`);
      }
      $$renderer2.push(`<!--]--></tbody></table>`);
    }
    $$renderer2.push(`<!--]--></div>`);
  });
}
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let isAdmin = derived(() => session.username === "admin" || session.username === "administrator");
    let bookings = [];
    let houses = [];
    let loading = false;
    let error = null;
    let viewMode = "table";
    let userHouseIds = derived(() => houses.filter((h) => h.owner_id === session.ownerOid).map((h) => h.id));
    let filteredBookings = derived(() => isAdmin() ? bookings : bookings.filter((b) => userHouseIds().includes(b.house_id)));
    let tableColumns = derived(() => [
      { key: "house_name", label: t("bookings.house") },
      { key: "check_in_date", label: t("bookings.check_in_date") },
      { key: "check_out_date", label: t("bookings.check_out_date") },
      { key: "guest_name", label: t("bookings.guest_name") },
      { key: "guest_email", label: t("common.email") },
      {
        key: "status",
        label: t("common.status"),
        formatter: (value) => value.charAt(0).toUpperCase() + value.slice(1)
      }
    ]);
    let tableActions = derived(() => [
      {
        label: t("common.edit"),
        class: "edit",
        title: t("bookings.edit_booking"),
        icon: "✏️"
      },
      {
        label: t("common.delete"),
        class: "delete",
        title: t("bookings.delete_booking"),
        icon: "🗑️"
      }
    ]);
    $$renderer2.push(`<div class="bookings-page svelte-uq5w8t"><div class="page-header svelte-uq5w8t"><h1 class="svelte-uq5w8t">${escape_html(
      // Svelte 5: Use $effect for lifecycle management
      // Check for newBooking URL param - pre-select house and open form
      // Find the house and pre-select it
      // Find owner of this house and select it
      // Update house options for this owner
      // Pre-fill form with house
      tt("bookings.title")
    )}</h1> <button class="btn btn-primary svelte-uq5w8t">${escape_html(tt("bookings.add_booking"))}</button></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="view-toggle svelte-uq5w8t"><button${attr_class("toggle-btn svelte-uq5w8t", void 0, { "active": viewMode === "card" })}>${escape_html(tt("houses.card_view"))}</button> <button${attr_class("toggle-btn svelte-uq5w8t", void 0, { "active": viewMode === "table" })}>${escape_html(tt("houses.table_view"))}</button></div> `);
    {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<div class="table-section svelte-uq5w8t">`);
      Table($$renderer2, {
        data: filteredBookings(),
        columns: tableColumns(),
        actions: tableActions(),
        loading,
        error
      });
      $$renderer2.push(`<!----></div>`);
    }
    $$renderer2.push(`<!--]--></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
