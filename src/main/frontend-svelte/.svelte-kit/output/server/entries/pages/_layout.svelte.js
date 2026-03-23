import "../../chunks/async.js";
import { u as unsubscribe_stores, d as derived, s as store_get, a as slot } from "../../chunks/index2.js";
import "clsx";
import { s as session } from "../../chunks/session.svelte.js";
import "@sveltejs/kit/internal";
import "../../chunks/exports.js";
import "../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../chunks/root.js";
import "../../chunks/state.svelte.js";
import { M as Modal } from "../../chunks/Modal.js";
import { w as writable } from "../../chunks/index.js";
import { e as escape_html } from "../../chunks/attributes.js";
function Navbar($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    $$renderer2.push(`<header class="bg-white shadow-sm border-b"><div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"><div class="flex justify-between items-center h-16"><div class="flex items-center"><a href="/" class="text-2xl font-bold text-gray-900 hover:text-gray-700">KissOO Svelte 5</a></div> <nav class="hidden md:flex items-center space-x-4"><a href="/" class="text-gray-600 hover:text-gray-900 font-medium">Home</a> `);
    if (session.isAuthenticated) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">Users</a> <a href="/crud" class="text-gray-600 hover:text-gray-900 font-medium">CRUD</a> <div class="relative"><button class="text-gray-600 hover:text-gray-900 font-medium flex items-center">Demo <svg class="ml-1 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg></button> `);
      {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></div> <button class="text-red-600 hover:text-red-800 font-medium">Logout</button> <span class="text-green-600 text-sm">Authenticated</span>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">Login</a> <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">Sign Up</a>`);
    }
    $$renderer2.push(`<!--]--></nav> <div class="md:hidden"><button class="text-gray-600 hover:text-gray-900"><svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">`);
    {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>`);
    }
    $$renderer2.push(`<!--]--></svg></button></div></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--></div></header>`);
  });
}
const initialState = {
  open: false,
  title: "",
  message: "",
  type: "alert",
  confirmText: "OK",
  cancelText: "Cancel"
};
const modalStore = writable(initialState);
const modal = {
  /**
   * Show an alert modal
   */
  alert(title, message) {
    return new Promise((resolve) => {
      modalStore.set({
        open: true,
        title,
        message,
        type: "alert",
        onConfirm: () => {
          modalStore.update((state) => ({ ...state, open: false }));
          resolve();
        },
        confirmText: "OK"
      });
    });
  },
  /**
   * Show a confirmation modal
   */
  confirm(title, message, confirmText, cancelText) {
    return new Promise((resolve) => {
      modalStore.set({
        open: true,
        title,
        message,
        type: "confirm",
        onConfirm: () => {
          modalStore.update((state) => ({ ...state, open: false }));
          resolve(true);
        },
        onCancel: () => {
          modalStore.update((state) => ({ ...state, open: false }));
          resolve(false);
        },
        confirmText: confirmText || "Yes",
        cancelText: cancelText || "No"
      });
    });
  },
  /**
   * Close the modal
   */
  close() {
    modalStore.update((state) => ({ ...state, open: false }));
  }
};
function GlobalModal($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    let modalState = derived(() => store_get($$store_subs ??= {}, "$modalStore", modalStore));
    function handleCancel() {
      modalState().onCancel?.();
      modal.close();
    }
    let $$settled = true;
    let $$inner_renderer;
    function $$render_inner($$renderer3) {
      {
        let footer = function($$renderer4) {
          $$renderer4.push(`<div class="flex gap-2">`);
          if (modalState().type === "confirm") {
            $$renderer4.push("<!--[0-->");
            $$renderer4.push(`<button type="button" class="inline-flex w-full justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm">${escape_html(modalState().confirmText)}</button> <button type="button" class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">${escape_html(modalState().cancelText)}</button>`);
          } else {
            $$renderer4.push("<!--[-1-->");
            $$renderer4.push(`<button type="button" class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm">${escape_html(modalState().confirmText)}</button>`);
          }
          $$renderer4.push(`<!--]--></div>`);
        };
        Modal($$renderer3, {
          title: modalState().title,
          onClose: handleCancel,
          get open() {
            return modalState().open;
          },
          set open($$value) {
            modalState().open = $$value;
            $$settled = false;
          },
          footer,
          children: ($$renderer4) => {
            $$renderer4.push(`<p class="text-gray-700">${escape_html(modalState().message)}</p>`);
          },
          $$slots: { footer: true, default: true }
        });
      }
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
function _layout($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    GlobalModal($$renderer2);
    $$renderer2.push(`<!----> `);
    Navbar($$renderer2);
    $$renderer2.push(`<!----> <!--[-->`);
    slot($$renderer2, $$props, "default", {});
    $$renderer2.push(`<!--]-->`);
  });
}
export {
  _layout as default
};
