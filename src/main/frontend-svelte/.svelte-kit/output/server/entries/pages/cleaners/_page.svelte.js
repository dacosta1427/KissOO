import "../../../chunks/async.js";
import { u as unsubscribe_stores, s as store_get, d as derived } from "../../../chunks/index2.js";
import { T as Table, c as cleanersAPI } from "../../../chunks/Table.js";
import { d as dataStores } from "../../../chunks/stores.svelte.js";
import { F as Form } from "../../../chunks/Form.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
import { e as escape_html } from "../../../chunks/attributes.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let cleaners = [];
    let loading = false;
    let error = null;
    let showForm = false;
    let editingCleaner = null;
    let cleanerFields = derived(() => [
      {
        name: "name",
        label: t("cleaners.name"),
        type: "text",
        required: true,
        placeholder: t("cleaners.enter_cleaner_name")
      },
      {
        name: "email",
        label: t("cleaners.email"),
        type: "email",
        required: true,
        placeholder: t("cleaners.enter_cleaner_email")
      },
      {
        name: "phone",
        label: t("cleaners.phone"),
        type: "tel",
        required: false,
        placeholder: t("cleaners.enter_cleaner_phone")
      },
      {
        name: "address",
        label: t("cleaners.address"),
        type: "textarea",
        required: false,
        placeholder: t("cleaners.enter_cleaner_address"),
        rows: 3
      },
      {
        name: "active",
        label: t("common.active"),
        type: "checkbox",
        required: false
      }
    ]);
    let tableColumns = derived(() => [
      { key: "name", label: t("cleaners.name") },
      { key: "email", label: t("cleaners.email") },
      { key: "phone", label: t("cleaners.phone") },
      { key: "address", label: t("cleaners.address") },
      {
        key: "active",
        label: t("common.status"),
        formatter: (value) => value ? t("common.active") : t("common.inactive")
      }
    ]);
    let tableActions = derived(() => [
      {
        label: t("common.edit"),
        class: "edit",
        title: t("cleaners.edit_cleaner"),
        icon: "✏️"
      },
      {
        label: t("common.delete"),
        class: "delete",
        title: t("cleaners.delete_cleaner"),
        icon: "🗑️"
      }
    ]);
    async function loadCleaners() {
      loading = true;
      error = null;
      try {
        cleaners = await cleanersAPI.getAll();
        dataStores.cleaners.set(cleaners);
      } catch (err) {
        error = err.message || t("errors.failed_to_load");
      } finally {
        loading = false;
      }
    }
    async function handleAction({ action, row }) {
      if (action.label === t("common.edit")) {
        editingCleaner = row;
        showForm = true;
      } else if (action.label === t("common.delete")) {
        if (confirm(t("cleaners.delete_confirm"))) {
          try {
            await cleanersAPI.delete(row.id);
            await loadCleaners();
          } catch (err) {
            error = err.message || t("errors.failed_to_delete");
          }
        }
      }
    }
    async function handleFormSubmit(data) {
      try {
        if (editingCleaner) {
          await cleanersAPI.update(editingCleaner.id, data);
        } else {
          await cleanersAPI.create(data);
        }
        showForm = false;
        editingCleaner = null;
        await loadCleaners();
      } catch (err) {
        error = err.message || t("errors.failed_to_save");
      }
    }
    function handleFormCancel() {
      showForm = false;
      editingCleaner = null;
    }
    $$renderer2.push(`<div class="cleaners-page svelte-ivoje8"><div class="page-header svelte-ivoje8"><h1 class="svelte-ivoje8">${escape_html(tt("cleaners.title"))}</h1> <button class="btn btn-primary svelte-ivoje8">${escape_html(tt("cleaners.add_cleaner"))}</button></div> `);
    if (showForm) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="form-section svelte-ivoje8">`);
      Form($$renderer2, {
        fields: cleanerFields(),
        data: editingCleaner || {},
        loading,
        title: editingCleaner ? t("cleaners.edit_cleaner") : t("cleaners.add_new_cleaner"),
        submitLabel: editingCleaner ? t("cleaners.edit_cleaner") : t("cleaners.add_cleaner"),
        onSubmit: handleFormSubmit,
        onCancel: handleFormCancel
      });
      $$renderer2.push(`<!----></div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="table-section svelte-ivoje8">`);
    Table($$renderer2, {
      data: cleaners,
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
