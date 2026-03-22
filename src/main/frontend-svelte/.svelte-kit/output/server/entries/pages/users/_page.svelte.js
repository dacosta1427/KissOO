import { c as create_ssr_component, d as add_attribute, e as escape, f as each } from "../../../chunks/ssr.js";
import { S as Server } from "../../../chunks/Server.js";
const Page = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let users = [];
  let loading = false;
  let newUser = { name: "", email: "" };
  async function loadUsers() {
    loading = true;
    try {
      const result = await Server.call("Users", "getRecords", {});
      if (result._Success) {
        users = result.data || [];
      } else {
        console.error("Failed to load users:", result._ErrorMessage);
      }
    } catch (error) {
      console.error("Error loading users:", error);
    } finally {
      loading = false;
    }
  }
  async function createUser() {
    return;
  }
  async function deleteUser(userId) {
    if (!confirm("Are you sure you want to delete this user?")) return;
    loading = true;
    try {
      const result = await Server.call("Users", "deleteRecord", { id: userId });
      if (result._Success) {
        await loadUsers();
      } else {
        console.error("Failed to delete user:", result._ErrorMessage);
      }
    } catch (error) {
      console.error("Error deleting user:", error);
    } finally {
      loading = false;
    }
  }
  return `<div class="p-6 max-w-4xl mx-auto"><h1 class="text-2xl font-bold mb-6" data-svelte-h="svelte-6qq7m">User Management</h1> <div class="mb-6"><h2 class="text-xl font-semibold mb-3" data-svelte-h="svelte-g2oh4b">Add New User</h2> <div class="grid grid-cols-2 gap-4"><input type="text" placeholder="Name" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"${add_attribute("value", newUser.name, 0)}> <input type="email" placeholder="Email" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"${add_attribute("value", newUser.email, 0)}></div> <button${add_attribute("onclick", createUser, 0)} class="mt-3 bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded" ${loading ? "disabled" : ""}>${escape(loading ? "Creating..." : "Create User")}</button></div> <div><h2 class="text-xl font-semibold mb-3" data-svelte-h="svelte-pz9qqz">Users</h2> ${loading ? `<p class="text-gray-500" data-svelte-h="svelte-18bq7qq">Loading users...</p>` : ``} <div class="space-y-3">${each(users, (user) => {
    return `<div class="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm"><div><p class="font-medium">${escape(user.name)}</p> <p class="text-gray-600 text-sm">${escape(user.email)}</p></div> <button${add_attribute("onclick", () => deleteUser(user.id), 0)} class="text-red-600 hover:text-red-800" ${loading ? "disabled" : ""}>Delete</button> </div>`;
  })}</div></div></div>`;
});
export {
  Page as default
};
