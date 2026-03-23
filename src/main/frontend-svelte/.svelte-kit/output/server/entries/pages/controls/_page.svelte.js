import "../../../chunks/async.js";
import { a as attr, e as escape_html } from "../../../chunks/attributes.js";
import { M as Modal } from "../../../chunks/Modal.js";
function _page($$renderer) {
  let textValue = "";
  let numericValue = 0;
  let dateValue = "";
  let timeValue = "";
  let checkboxChecked = false;
  let radioValue = "Yes";
  let dropdownValue = "";
  let listboxValue = "";
  let textareaValue = "";
  let modalOpen = false;
  let $$settled = true;
  let $$inner_renderer;
  function $$render_inner($$renderer2) {
    $$renderer2.push(`<div class="p-6 max-w-4xl mx-auto"><h1 class="text-2xl font-bold mb-6">Controls</h1> <div class="grid grid-cols-1 md:grid-cols-2 gap-6"><div><label class="block text-sm font-medium text-gray-700">Text Input</label> <input type="text"${attr("value", textValue)} maxlength="40" required="" class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Numeric Input</label> <input type="number"${attr("value", numericValue)} min="10" max="100" step="0.01" class="mt-1 block w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Date Input</label> <input type="date"${attr("value", dateValue)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="block text-sm font-medium text-gray-700">Time Input</label> <input type="time"${attr("value", timeValue)} class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"/></div> <div><label class="flex items-center"><input type="checkbox"${attr("checked", checkboxChecked, true)} class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"/> <span class="ml-2 text-sm text-gray-700">My checkbox</span></label></div> <div><label class="block text-sm font-medium text-gray-700">Radio Buttons</label> <div class="mt-1 space-x-4"><label class="inline-flex items-center"><input type="radio"${attr("checked", radioValue === "Yes", true)} value="Yes" class="form-radio text-blue-600"/> <span class="ml-2">Yes</span></label> <label class="inline-flex items-center"><input type="radio"${attr("checked", radioValue === "No", true)} value="No" class="form-radio text-blue-600"/> <span class="ml-2">No</span></label></div></div> <div><label class="block text-sm font-medium text-gray-700">Dropdown List</label> `);
    $$renderer2.select(
      {
        value: dropdownValue,
        class: "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      },
      ($$renderer3) => {
        $$renderer3.option({ value: "" }, ($$renderer4) => {
          $$renderer4.push(`Select an option`);
        });
        $$renderer3.option({ value: "1" }, ($$renderer4) => {
          $$renderer4.push(`Option 1`);
        });
        $$renderer3.option({ value: "2" }, ($$renderer4) => {
          $$renderer4.push(`Option 2`);
        });
        $$renderer3.option({ value: "3" }, ($$renderer4) => {
          $$renderer4.push(`Option 3`);
        });
      }
    );
    $$renderer2.push(`</div> <div><label class="block text-sm font-medium text-gray-700">List Box</label> `);
    $$renderer2.select(
      {
        value: listboxValue,
        size: "3",
        class: "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      },
      ($$renderer3) => {
        $$renderer3.option({ value: "1" }, ($$renderer4) => {
          $$renderer4.push(`Option 1`);
        });
        $$renderer3.option({ value: "2" }, ($$renderer4) => {
          $$renderer4.push(`Option 2`);
        });
        $$renderer3.option({ value: "3" }, ($$renderer4) => {
          $$renderer4.push(`Option 3`);
        });
      }
    );
    $$renderer2.push(`</div> <div class="md:col-span-2"><label class="block text-sm font-medium text-gray-700">Text Area</label> <textarea rows="3" class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">`);
    const $$body = escape_html(textareaValue);
    if ($$body) {
      $$renderer2.push(`${$$body}`);
    }
    $$renderer2.push(`</textarea></div></div> <div class="mt-6 flex gap-2"><button class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded">Popup</button> <button class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">Ok</button></div></div> `);
    {
      let footer = function($$renderer3) {
        $$renderer3.push(`<button type="button" class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm">Ok</button>`);
      };
      Modal($$renderer2, {
        title: "The Header",
        onClose: () => modalOpen = false,
        get open() {
          return modalOpen;
        },
        set open($$value) {
          modalOpen = $$value;
          $$settled = false;
        },
        footer,
        children: ($$renderer3) => {
          $$renderer3.push(`<p>The content</p> <br/><br/><br/>`);
        },
        $$slots: { footer: true, default: true }
      });
    }
    $$renderer2.push(`<!---->`);
  }
  do {
    $$settled = true;
    $$inner_renderer = $$renderer.copy();
    $$render_inner($$inner_renderer);
  } while (!$$settled);
  $$renderer.subsume($$inner_renderer);
}
export {
  _page as default
};
