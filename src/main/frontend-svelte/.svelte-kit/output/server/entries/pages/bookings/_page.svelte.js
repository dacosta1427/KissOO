import "../../../chunks/async.js";
import { u as unsubscribe_stores, s as store_get, d as derived } from "../../../chunks/index2.js";
import { T as Table, b as bookingsAPI, h as housesAPI, o as ownersAPI } from "../../../chunks/Table.js";
import { d as dataStores } from "../../../chunks/stores.svelte.js";
import { s as session } from "../../../chunks/session.svelte.js";
import { F as Form } from "../../../chunks/Form.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
import { e as escape_html } from "../../../chunks/attributes.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let isAdmin = derived(() => session.username === "admin" || session.username === "administrator");
    let bookings = [];
    let houses = [];
    let owners = [];
    let loading = false;
    let error = null;
    let showForm = false;
    let editingBooking = null;
    let userHouseIds = derived(() => houses.filter((h) => h.owner_id === session.ownerId).map((h) => h.id));
    let filteredBookings = derived(() => isAdmin() ? bookings : bookings.filter((b) => userHouseIds().includes(b.house_id)));
    let bookingFields = derived(() => [
      {
        name: "owner_id",
        label: t("houses.owner"),
        type: "select",
        required: true,
        options: []
      },
      {
        name: "house_id",
        label: t("bookings.house"),
        type: "select",
        required: true,
        options: []
      },
      {
        name: "check_in_date",
        label: t("bookings.check_in_date"),
        type: "date",
        required: true
      },
      {
        name: "check_out_date",
        label: t("bookings.check_out_date"),
        type: "date",
        required: true
      },
      {
        name: "check_in_time",
        label: t("bookings.check_in_time") + " (24h)",
        type: "time",
        required: true,
        step: 900
      },
      {
        name: "check_out_time",
        label: t("bookings.check_out_time") + " (24h)",
        type: "time",
        required: true,
        step: 900
      },
      {
        name: "guest_name",
        label: t("bookings.guest_name"),
        type: "text",
        required: true,
        placeholder: t("bookings.enter_guest_name")
      },
      {
        name: "guest_email",
        label: t("bookings.guest_email"),
        type: "email",
        required: true,
        placeholder: t("bookings.enter_guest_email")
      },
      {
        name: "guest_phone",
        label: t("bookings.guest_phone"),
        type: "tel",
        required: false,
        placeholder: t("bookings.enter_guest_phone")
      },
      {
        name: "dogs_count",
        label: t("bookings.number_of_dogs"),
        type: "number",
        required: false,
        min: 0,
        step: 1,
        placeholder: "0"
      },
      {
        name: "status",
        label: t("common.status"),
        type: "select",
        required: true,
        options: [
          { value: "pending", label: t("bookings.pending") },
          { value: "confirmed", label: t("bookings.confirmed") },
          { value: "checked_in", label: t("bookings.checked_in") },
          { value: "checked_out", label: t("bookings.checked_out") },
          { value: "cancelled", label: t("bookings.cancelled") }
        ]
      }
    ]);
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
    async function loadData() {
      loading = true;
      error = null;
      try {
        const [bookingsResult, housesResult, ownersResult] = await Promise.all([bookingsAPI.getAll(), housesAPI.getAll(), ownersAPI.getAll()]);
        bookings = bookingsResult;
        houses = housesResult;
        owners = ownersResult;
        bookingFields()[0].options = owners.map((o) => ({ value: o.id, label: o.name }));
        dataStores.bookings.set(bookings);
        dataStores.houses.set(houses);
      } catch (err) {
        error = err.message || t("errors.failed_to_load");
      } finally {
        loading = false;
      }
    }
    async function handleAction({ action, row }) {
      if (action.label === t("common.edit")) {
        editingBooking = row;
        showForm = true;
      } else if (action.label === t("common.delete")) {
        if (confirm(t("bookings.delete_confirm"))) {
          try {
            await bookingsAPI.delete(row.id);
            await loadData();
          } catch (err) {
            error = err.message || t("errors.failed_to_delete");
          }
        }
      }
    }
    async function handleFormSubmit(data) {
      try {
        if (editingBooking) {
          await bookingsAPI.update(editingBooking.id, data);
        } else {
          await bookingsAPI.create(data);
        }
        showForm = false;
        editingBooking = null;
        await loadData();
      } catch (err) {
        error = err.message || t("errors.failed_to_save");
      }
    }
    function handleFormCancel() {
      showForm = false;
      editingBooking = null;
    }
    $$renderer2.push(`<div class="bookings-page svelte-uq5w8t"><div class="page-header svelte-uq5w8t"><h1 class="svelte-uq5w8t">${escape_html(
      // Svelte 5: Use $effect for lifecycle management
      tt("bookings.title")
    )}</h1> <button class="btn btn-primary svelte-uq5w8t">${escape_html(tt("bookings.add_booking"))}</button></div> `);
    if (showForm) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="form-section svelte-uq5w8t">`);
      Form($$renderer2, {
        fields: bookingFields(),
        data: editingBooking || {},
        loading,
        title: editingBooking ? t("bookings.edit_booking") : t("bookings.add_new_booking"),
        submitLabel: editingBooking ? t("bookings.edit_booking") : t("bookings.add_booking"),
        onSubmit: handleFormSubmit,
        onCancel: handleFormCancel
      });
      $$renderer2.push(`<!----></div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="table-section svelte-uq5w8t">`);
    Table($$renderer2, {
      data: filteredBookings(),
      columns: tableColumns(),
      actions: tableActions(),
      loading,
      error,
      onAction: handleAction
    });
    $$renderer2.push(`<!----></div></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
