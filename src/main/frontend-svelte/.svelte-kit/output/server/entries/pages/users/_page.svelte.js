import { a as attr, e as escape_html, d as derived } from "../../../chunks/async.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let newUserName = "";
    let newUserPassword = "";
    let canAddUser = derived(() => newUserName.length >= 3 && newUserPassword.length >= 3);
    $$renderer2.push(`<div class="p-6 max-w-4xl mx-auto"><h1 class="text-2xl font-bold mb-6">User Management</h1> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="mb-6"><h2 class="text-xl font-semibold mb-3">Add New User</h2> <div class="space-y-4"><div class="grid grid-cols-2 gap-4"><input type="text" placeholder="Username"${attr("value", newUserName)} class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/> <input type="password" placeholder="Password"${attr("value", newUserPassword)} class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <button${attr("disabled", !canAddUser(), true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">${escape_html("Add User")}</button></div></div> <div><h2 class="text-xl font-semibold mb-3">Users</h2> `);
    {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<p class="text-gray-500">Loading users...</p>`);
    }
    $$renderer2.push(`<!--]--></div></div>`);
  });
}
export {
  _page as default
};
