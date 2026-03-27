import "../../../chunks/async.js";
import { u as unsubscribe_stores, s as store_get, d as derived, e as ensure_array_like, b as attr_class } from "../../../chunks/index2.js";
import { S as Server } from "../../../chunks/Auth.js";
import { e as escape_html, c as clsx, a as attr } from "../../../chunks/attributes.js";
import { M as Modal } from "../../../chunks/Modal.js";
import { F as Form } from "../../../chunks/Form.js";
import { a as notificationActions } from "../../../chunks/stores.svelte.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
async function getUsers() {
  console.log("[Users.ts] getUsers called, Server.uuid:", Server.uuid);
  const res = await Server.call("services.Users", "getRecords", {});
  console.log("[Users.ts] getUsers response:", res);
  return res.rows || [];
}
async function addUser(userName, userPassword) {
  console.log("[Users.ts] addUser called:", userName);
  const res = await Server.call("services.Users", "addRecord", {
    userName: userName.toLowerCase(),
    userPassword,
    userActive: "Y"
  });
  console.log("[Users.ts] addUser response:", res);
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    id: res.id
  };
}
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let users = [];
    let loading = false;
    let error = "";
    let dataLoading = true;
    let editModalOpen = false;
    let addFormData = { username: "", password: "" };
    let editFormData = { username: "", password: "", active: "Y" };
    let editLoading = false;
    let canAddUser = derived(() => (addFormData.username?.length ?? 0) >= 3 && (addFormData.password?.length ?? 0) >= 3);
    let addUserFields = derived(() => [
      {
        name: "username",
        label: t("users.enter_username"),
        type: "text",
        required: true,
        placeholder: t("users.enter_username"),
        helpText: t("users.minimum_3_chars")
      },
      {
        name: "password",
        label: t("users.enter_password"),
        type: "password",
        required: true,
        placeholder: t("users.enter_password"),
        helpText: t("users.minimum_3_chars")
      }
    ]);
    let editUserFields = derived(() => [
      {
        name: "username",
        label: t("users.enter_username"),
        type: "text",
        required: true,
        placeholder: t("users.enter_username"),
        helpText: t("users.minimum_3_chars")
      },
      {
        name: "password",
        label: t("users.enter_password"),
        type: "password",
        required: true,
        placeholder: t("users.enter_password"),
        helpText: t("users.minimum_3_chars")
      },
      {
        name: "active",
        label: t("common.active"),
        type: "select",
        required: true,
        options: [
          { value: "Y", label: t("common.active") },
          { value: "N", label: t("common.inactive") }
        ]
      }
    ]);
    async function loadUsers() {
      console.log("[users] loadUsers called");
      dataLoading = true;
      error = "";
      try {
        users = await getUsers();
        console.log("[users] loadUsers success, count:", users.length);
      } catch (e) {
        error = t("errors.failed_to_load") + ": " + (e.message || "Unknown error");
        console.error("[users] loadUsers error:", error);
      } finally {
        dataLoading = false;
      }
    }
    async function handleAddUser(data) {
      console.log("[users] handleAddUser called with:", data);
      if (!canAddUser()) {
        console.log("[users] canAddUser false, aborting");
        return;
      }
      loading = true;
      error = "";
      try {
        const res = await addUser(data.username, data.password);
        console.log("[users] addUser response:", res);
        if (res.success) {
          notificationActions.success(t("users.title") + " " + t("notifications.created_successfully"));
          addFormData = { username: "", password: "" };
          console.log("[users] addUser success, reloading users...");
          await loadUsers();
        } else {
          error = res.error || t("errors.failed_to_save");
          console.error("[users] addUser failed:", error);
          notificationActions.error(error);
        }
      } catch (e) {
        error = t("errors.failed_to_save") + ": " + (e.message || "Unknown error");
        console.error("[users] addUser exception:", error);
        notificationActions.error(error);
      } finally {
        loading = false;
      }
    }
    async function handleEditUser(data) {
      return;
    }
    let $$settled = true;
    let $$inner_renderer;
    function $$render_inner($$renderer3) {
      $$renderer3.push(`<div class="p-6 max-w-4xl mx-auto"><h1 class="text-2xl font-bold mb-6">${escape_html(tt("users.title"))}</h1> `);
      if (error) {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">${escape_html(error)}</div>`);
      } else {
        $$renderer3.push("<!--[-1-->");
      }
      $$renderer3.push(`<!--]--> <div class="mb-6"><h2 class="text-xl font-semibold mb-3">${escape_html(tt("users.add_new_user"))}</h2> `);
      Form($$renderer3, {
        fields: addUserFields(),
        loading,
        submitLabel: tt("users.add_user"),
        onSubmit: handleAddUser,
        get data() {
          return addFormData;
        },
        set data($$value) {
          addFormData = $$value;
          $$settled = false;
        }
      });
      $$renderer3.push(`<!----></div> <div><h2 class="text-xl font-semibold mb-3">${escape_html(tt("users.users_list"))}</h2> `);
      if (dataLoading) {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<p class="text-gray-500">${escape_html(tt("users.loading_users"))}</p>`);
      } else if (users.length === 0) {
        $$renderer3.push("<!--[1-->");
        $$renderer3.push(`<p class="text-gray-500">${escape_html(tt("users.no_users"))}</p>`);
      } else {
        $$renderer3.push("<!--[-1-->");
        $$renderer3.push(`<div class="space-y-3"><!--[-->`);
        const each_array = ensure_array_like(users);
        for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
          let user = each_array[$$index];
          $$renderer3.push(`<div class="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border"><div><p class="font-medium">${escape_html(user.userName)}</p> <p class="text-gray-600 text-sm">${escape_html(tt("common.status"))}: <span${attr_class(clsx(user.userActive === "Y" ? "text-green-600" : "text-red-600"))}>${escape_html(user.userActive === "Y" ? t("common.active") : t("common.inactive"))}</span></p></div> <div class="flex gap-2"><button class="text-blue-600 hover:text-blue-800"${attr("disabled", loading, true)}>${escape_html(tt("common.edit"))}</button> <button class="text-red-600 hover:text-red-800"${attr("disabled", loading, true)}>${escape_html(tt("common.delete"))}</button></div></div>`);
        }
        $$renderer3.push(`<!--]--></div>`);
      }
      $$renderer3.push(`<!--]--></div></div> `);
      Modal($$renderer3, {
        title: tt("users.edit_user"),
        onClose: () => editModalOpen = false,
        get open() {
          return editModalOpen;
        },
        set open($$value) {
          editModalOpen = $$value;
          $$settled = false;
        },
        children: ($$renderer4) => {
          Form($$renderer4, {
            fields: editUserFields(),
            loading: editLoading,
            submitLabel: tt("users.save"),
            cancelLabel: tt("common.cancel"),
            onSubmit: handleEditUser,
            onCancel: () => editModalOpen = false,
            get data() {
              return editFormData;
            },
            set data($$value) {
              editFormData = $$value;
              $$settled = false;
            }
          });
        },
        $$slots: { default: true }
      });
      $$renderer3.push(`<!---->`);
    }
    do {
      $$settled = true;
      $$inner_renderer = $$renderer2.copy();
      $$render_inner($$inner_renderer);
    } while (!$$settled);
    $$renderer2.subsume($$inner_renderer);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
