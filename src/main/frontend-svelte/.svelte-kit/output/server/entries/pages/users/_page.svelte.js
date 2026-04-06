import "../../../chunks/async.js";
import { u as unsubscribe_stores, e as escape_html, f as attr_class, s as store_get, d as derived, c as ensure_array_like, m as clsx, a as attr } from "../../../chunks/index2.js";
import { S as Server } from "../../../chunks/Auth.js";
import { F as Form } from "../../../chunks/Form.js";
import { a as notificationActions } from "../../../chunks/stores.svelte.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
async function getUsers() {
  console.log("[Users.ts] getUsers called, Server.uuid:", Server.uuid);
  const res = await Server.call("services.Users", "getUsers", {});
  console.log("[Users.ts] getUsers response:", res);
  return res.rows || [];
}
async function addUser(userName, userPassword) {
  console.log("[Users.ts] addUser called:", userName);
  const res = await Server.call("services.Users", "createUser", {
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
    let allUsers = [];
    let loading = false;
    let error = "";
    let dataLoading = true;
    let addFormData = { username: "", password: "" };
    let canAddUser = derived(() => (addFormData.username?.length ?? 0) >= 3 && (addFormData.password?.length ?? 0) >= 3);
    let filterMode = "all";
    let filteredUsers = derived(
      () => allUsers
    );
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
    async function loadUsers() {
      console.log("[users] loadUsers called");
      dataLoading = true;
      error = "";
      try {
        allUsers = await getUsers();
        console.log("[users] loadUsers success, count:", allUsers.length);
      } catch (e) {
        error = t("errors.failed_to_load") + ": " + (e.message || "Unknown error");
        console.error("[users] loadUsers error:", error);
      } finally {
        dataLoading = false;
      }
    }
    async function handleAddUser(data) {
      if (!canAddUser()) return;
      loading = true;
      error = "";
      try {
        const res = await addUser(data.username, data.password);
        if (res.success) {
          notificationActions.success(t("users.title") + " " + t("notifications.created_successfully"));
          addFormData = { username: "", password: "" };
          await loadUsers();
        } else {
          error = res.error || t("errors.failed_to_save");
          notificationActions.error(error);
        }
      } catch (e) {
        error = t("errors.failed_to_save") + ": " + (e.message || "Unknown error");
        notificationActions.error(error);
      } finally {
        loading = false;
      }
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
      $$renderer3.push(`<!----></div> <div class="flex gap-2 mb-4"><button${attr_class("filter-btn svelte-9fk07v", void 0, { "active": filterMode === "all" })}>All</button> <button${attr_class("filter-btn svelte-9fk07v", void 0, { "active": filterMode === "Owner" })}>Owners</button> <button${attr_class("filter-btn svelte-9fk07v", void 0, { "active": filterMode === "Cleaner" })}>Cleaners</button></div> <div><h2 class="text-xl font-semibold mb-3">${escape_html(tt("users.users_list"))}</h2> `);
      if (dataLoading) {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<p class="text-gray-500">${escape_html(tt("users.loading_users"))}</p>`);
      } else if (filteredUsers().length === 0) {
        $$renderer3.push("<!--[1-->");
        $$renderer3.push(`<p class="text-gray-500">${escape_html(tt("users.no_users"))}</p>`);
      } else {
        $$renderer3.push("<!--[-1-->");
        $$renderer3.push(`<div class="space-y-3"><!--[-->`);
        const each_array = ensure_array_like(filteredUsers());
        for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
          let user = each_array[$$index];
          $$renderer3.push(`<div class="user-card svelte-9fk07v"><div class="user-info svelte-9fk07v"><div class="user-main svelte-9fk07v"><span class="font-medium">${escape_html(user.userName)}</span> `);
          if (user.actorType) {
            $$renderer3.push("<!--[0-->");
            $$renderer3.push(`<span${attr_class("actor-badge svelte-9fk07v", void 0, {
              "owner": user.actorType === "Owner",
              "cleaner": user.actorType === "Cleaner"
            })}>${escape_html(user.actorType)}</span>`);
          } else {
            $$renderer3.push("<!--[-1-->");
          }
          $$renderer3.push(`<!--]--></div> <div class="user-details svelte-9fk07v"><span class="text-gray-600 text-sm">${escape_html(tt("common.can_login"))}: <span${attr_class(clsx(user.canLogin ? "text-green-600" : "text-red-600"))}>${escape_html(user.canLogin ? t("common.yes") : t("common.no"))}</span></span> <span class="ml-2"${attr("title", user.emailVerified ? "Email verified" : "Email not verified")}>`);
          if (user.emailVerified) {
            $$renderer3.push("<!--[0-->");
            $$renderer3.push(`<svg class="inline w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg>`);
          } else {
            $$renderer3.push("<!--[-1-->");
            $$renderer3.push(`<svg class="inline w-4 h-4 text-red-600" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg>`);
          }
          $$renderer3.push(`<!--]--></span> `);
          if (user.email) {
            $$renderer3.push("<!--[0-->");
            $$renderer3.push(`<span class="text-gray-400 ml-1">(${escape_html(user.email)})</span>`);
          } else {
            $$renderer3.push("<!--[-1-->");
          }
          $$renderer3.push(`<!--]--></div></div> <div class="user-actions svelte-9fk07v"><button type="button"${attr_class("card-toggle svelte-9fk07v", void 0, { "active": user.canLogin })}${attr("title", user.canLogin ? "Disable login" : "Enable login")}></button> <button class="text-blue-600 hover:text-blue-800"${attr("disabled", loading, true)}>${escape_html(tt("common.edit"))}</button> <button class="text-red-600 hover:text-red-800"${attr("disabled", loading, true)}>${escape_html(tt("common.delete"))}</button></div></div>`);
        }
        $$renderer3.push(`<!--]--></div>`);
      }
      $$renderer3.push(`<!--]--></div></div>`);
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
