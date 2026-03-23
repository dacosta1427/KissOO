import "../../../chunks/async.js";
import { d as derived } from "../../../chunks/index2.js";
import { a as attr, e as escape_html } from "../../../chunks/attributes.js";
import "ag-grid-community";
import { M as Modal } from "../../../chunks/Modal.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let loading = false;
    let editModalOpen = false;
    let editFirstName = "";
    let editLastName = "";
    let editPhoneNumber = "";
    let canSave = derived(() => editFirstName.length >= 1 && editLastName.length >= 1);
    let $$settled = true;
    let $$inner_renderer;
    function $$render_inner($$renderer3) {
      $$renderer3.push(`<div class="p-6 max-w-6xl mx-auto"><h1 class="text-2xl font-bold mb-6">Phone Book (CRUD)</h1> `);
      {
        $$renderer3.push("<!--[-1-->");
      }
      $$renderer3.push(`<!--]--> <div class="mb-4 flex gap-2"><button${attr("disabled", loading, true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">New</button> <button${attr("disabled", true, true)} class="bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Edit</button> <button${attr("disabled", true, true)} class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Delete</button> <button${attr("disabled", loading, true)} class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Report</button> <button${attr("disabled", loading, true)} class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Export</button></div> <p class="text-gray-600 mb-4">(Note that the Report function will not work unless the underlying system has groff installed and the ability to generate PDF files.
    This is generally true of Linux and Mac systems but not true on Windows. A version for Windows must be installed.)</p> `);
      {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<p class="text-gray-500">Loading records...</p>`);
      }
      $$renderer3.push(`<!--]--></div> `);
      {
        let footer = function($$renderer4) {
          $$renderer4.push(`<button type="button" class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"${attr("disabled", !canSave(), true)}>${escape_html("OK")}</button> <button type="button" class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">Cancel</button>`);
        };
        Modal($$renderer3, {
          title: "Edit Record",
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
            $$renderer4.push(`<div class="space-y-4"><div><label class="block text-sm font-medium text-gray-700">First Name</label> <input type="text"${attr("value", editFirstName)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" maxlength="20" required=""/></div> <div><label class="block text-sm font-medium text-gray-700">Last Name</label> <input type="text"${attr("value", editLastName)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" maxlength="20" required=""/></div> <div><label class="block text-sm font-medium text-gray-700">Phone Number</label> <input type="text"${attr("value", editPhoneNumber)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" maxlength="25"/></div></div>`);
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
