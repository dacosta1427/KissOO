import "../../../chunks/async.js";
import { d as derived } from "../../../chunks/index2.js";
import { a as attr, e as escape_html } from "../../../chunks/attributes.js";
import { M as Modal } from "../../../chunks/Modal.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let newUserName = "";
    let newUserPassword = "";
    let editModalOpen = false;
    let editUserName = "";
    let editUserPassword = "";
    let editUserActive = "Y";
    let canAddUser = derived(() => newUserName.length >= 3 && newUserPassword.length >= 3);
    let canEditUser = derived(() => editUserName.length >= 3 && editUserPassword.length >= 3);
    let $$settled = true;
    let $$inner_renderer;
    function $$render_inner($$renderer3) {
      $$renderer3.push(`<div class="p-6 max-w-4xl mx-auto"><h1 class="text-2xl font-bold mb-6">User Management</h1> `);
      {
        $$renderer3.push("<!--[-1-->");
      }
      $$renderer3.push(`<!--]--> <div class="mb-6"><h2 class="text-xl font-semibold mb-3">Add New User</h2> <div class="space-y-4"><div class="grid grid-cols-2 gap-4"><input type="text" placeholder="Username"${attr("value", newUserName)} class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/> <input type="password" placeholder="Password"${attr("value", newUserPassword)} class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <button${attr("disabled", !canAddUser(), true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">${escape_html("Add User")}</button></div></div> <div><h2 class="text-xl font-semibold mb-3">Users</h2> `);
      {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<p class="text-gray-500">Loading users...</p>`);
      }
      $$renderer3.push(`<!--]--></div></div> `);
      {
        let footer = function($$renderer4) {
          $$renderer4.push(`<button type="button" class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"${attr("disabled", !canEditUser(), true)}>${escape_html("Save")}</button> <button type="button" class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">Cancel</button>`);
        };
        Modal($$renderer3, {
          title: "Edit User",
          onClose: () => editModalOpen = false,
          get open() {
            return editModalOpen;
          },
          set open($$value) {
            editModalOpen = $$value;
            $$settled = false;
          },
          footer,
          children: ($$renderer4) => {
            $$renderer4.push(`<div class="space-y-4"><div><label class="block text-sm font-medium text-gray-700">Username</label> <input type="text"${attr("value", editUserName)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Password</label> <input type="password"${attr("value", editUserPassword)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Active</label> `);
            $$renderer4.select(
              {
                value: editUserActive,
                class: "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              },
              ($$renderer5) => {
                $$renderer5.option({ value: "Y" }, ($$renderer6) => {
                  $$renderer6.push(`Active`);
                });
                $$renderer5.option({ value: "N" }, ($$renderer6) => {
                  $$renderer6.push(`Inactive`);
                });
              }
            );
            $$renderer4.push(`</div></div>`);
          },
          $$slots: { footer: true, default: true }
        });
      }
      $$renderer3.push(`<!---->`);
    }
    do {
      $$settled = true;
      $$inner_renderer = $$renderer2.copy();
      $$render_inner($$inner_renderer);
    } while (!$$settled);
    $$renderer2.subsume($$inner_renderer);
  });
}
export {
  _page as default
};
