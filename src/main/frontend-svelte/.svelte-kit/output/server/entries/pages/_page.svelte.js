import "../../chunks/async.js";
import { e as escape_html, a as attr, u as unsubscribe_stores, s as store_get, d as derived } from "../../chunks/index2.js";
import "@sveltejs/kit/internal";
import "../../chunks/exports.js";
import "../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../chunks/root.js";
import "../../chunks/state.svelte.js";
import { s as session } from "../../chunks/session.svelte.js";
import { i as isAuthenticated } from "../../chunks/Auth.js";
import { t, c as currentLocale } from "../../chunks/index3.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let username = "";
    let password = "";
    let rememberMe = false;
    let isAdmin = derived(() => session.isAdmin === true);
    let isSystemAdmin = derived(() => isAdmin() && session.adminType === "system");
    let isContentAdmin = derived(() => isAdmin() && session.adminType === "content");
    let loadingData = false;
    let isValid = derived(() => username.length > 0 && password.length > 0);
    if (isAuthenticated()) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"><h1 class="text-3xl font-bold text-gray-900 mb-8">${escape_html(tt("nav.home"))}</h1> `);
      if (isSystemAdmin()) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg"><h3 class="text-lg font-semibold text-yellow-800 mb-3">Test Data Management</h3> <div class="flex flex-wrap gap-3"><button${attr("disabled", loadingData, true)}${attr("title", "Create sample owners, houses, bookings, cleaners, and schedules for testing")} class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">${escape_html("Load Test Data")}</button> <button${attr("disabled", loadingData, true)}${attr("title", "Remove all test data (cleaners, bookings, schedules, houses, owners)")} class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">${escape_html("Clear Test Data")}</button></div> `);
        {
          $$renderer2.push("<!--[-1-->");
        }
        $$renderer2.push(`<!--]--></div> <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"><a href="/users" title="Add, edit, and delete system users" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.users"))}</h2> <p class="text-gray-600">Manage system users</p></a> <a href="/owners" title="Manage property owners and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.owners"))}</h2> <p class="text-gray-600">Manage property owners</p></a> <a href="/houses" title="Manage rental properties and their details" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.houses"))}</h2> <p class="text-gray-600">Manage all houses</p></a> <a href="/bookings" title="View and manage guest bookings" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.bookings"))}</h2> <p class="text-gray-600">View all bookings</p></a> <a href="/cleaners" title="Manage cleaning staff and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.cleaners"))}</h2> <p class="text-gray-600">Manage cleaners</p></a> <a href="/schedules" title="View and manage cleaning schedules" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.schedules"))}</h2> <p class="text-gray-600">View cleaning schedules</p></a></div>`);
      } else if (isContentAdmin()) {
        $$renderer2.push("<!--[1-->");
        $$renderer2.push(`<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"><a href="/owners" title="Manage property owners and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.owners"))}</h2> <p class="text-gray-600">Manage property owners</p></a> <a href="/houses" title="Manage rental properties and their details" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.houses"))}</h2> <p class="text-gray-600">Manage all houses</p></a> <a href="/bookings" title="View and manage guest bookings" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.bookings"))}</h2> <p class="text-gray-600">View all bookings</p></a> <a href="/cleaners" title="Manage cleaning staff and their information" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.cleaners"))}</h2> <p class="text-gray-600">Manage cleaners</p></a> <a href="/schedules" title="View and manage cleaning schedules" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.schedules"))}</h2> <p class="text-gray-600">View cleaning schedules</p></a></div>`);
      } else {
        $$renderer2.push("<!--[-1-->");
        $$renderer2.push(`<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"><a href="/houses" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.houses"))}</h2> <p class="text-gray-600">View your houses</p></a> <a href="/bookings" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.bookings"))}</h2> <p class="text-gray-600">View your bookings</p></a> <a href="/schedules" class="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"><h2 class="text-xl font-semibold text-gray-900 mb-2">${escape_html(tt("nav.schedules"))}</h2> <p class="text-gray-600">View your schedules</p></a></div>`);
      }
      $$renderer2.push(`<!--]--></main>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"><div class="flex items-center justify-center min-h-[60vh]"><div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md"><h1 class="text-2xl font-bold text-center mb-6">${escape_html(tt("auth.login_title"))}</h1> `);
      {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--> <form><div class="mb-6"><label for="username" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.username"))}</label> <input type="text" id="username"${attr("value", username)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.username"))} autocomplete="username"/></div> <div class="mb-6"><label for="password" class="block text-gray-700 text-sm font-bold mb-2">${escape_html(tt("auth.password"))}</label> <input type="password" id="password"${attr("value", password)} class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"${attr("placeholder", tt("auth.password"))} autocomplete="current-password"/></div> <div class="mb-6 flex items-center"><input type="checkbox" id="rememberMe"${attr("checked", rememberMe, true)} class="mr-2 leading-tight"/> <label for="rememberMe" class="text-sm text-gray-700">${escape_html(tt("auth.remember_me"))}</label></div> <button type="submit"${attr("disabled", !isValid(), true)} class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">${escape_html(t("auth.login_button"))}</button></form> <p class="text-gray-500 text-xs text-center mt-4">${escape_html(tt("auth.no_account"))} <a href="/signup" class="text-blue-600 hover:text-blue-800">${escape_html(tt("auth.sign_up_here"))}</a></p></div></div></main>`);
    }
    $$renderer2.push(`<!--]-->`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
