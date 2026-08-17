import "../../chunks/async.js";
import { a as attr, e as escape_html, s as store_get, u as unsubscribe_stores, b as store_mutate, d as derived, c as ensure_array_like, f as attr_class, g as stringify, h as slot } from "../../chunks/index2.js";
import "@sveltejs/kit/internal";
import "../../chunks/exports.js";
import "../../chunks/utils.js";
import "@sveltejs/kit/internal/server";
import "../../chunks/root.js";
import "../../chunks/state.svelte.js";
import { s as session } from "../../chunks/session.svelte.js";
import { t, c as currentLocale } from "../../chunks/index3.js";
import { o as onDestroy } from "../../chunks/index-server.js";
import { M as Modal } from "../../chunks/Modal.js";
import { w as writable } from "../../chunks/index.js";
import { n as notificationsState } from "../../chunks/stores.svelte.js";
function LanguageSwitcher($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    onDestroy(() => {
    });
    $$renderer2.push(`<div class="language-switcher relative"><button class="flex items-center gap-1 px-2 py-1 text-sm text-gray-600 hover:text-gray-900 rounded-md hover:bg-gray-100"${attr("aria-label", tt("nav.language"))}><svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg> <span class="uppercase">${escape_html(store_get($$store_subs ??= {}, "$currentLocale", currentLocale))}</span> <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg></button> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--></div>`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
function Navbar($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    $$renderer2.push(`<header class="sticky top-0 z-50 bg-white shadow-sm border-b"><div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"><div class="flex justify-between items-center h-16"><div class="flex items-center"><a href="/" class="text-2xl font-bold text-gray-900 hover:text-gray-700">KissOO Svelte 5</a></div> <nav class="hidden md:flex items-center space-x-4"><div class="flex items-center mr-2"${attr("title", session.isAuthenticated ? "Logged in" : "Not logged in")}>`);
    if (session.isAuthenticated) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<span class="inline-block w-3 h-3 rounded-full bg-green-500" title="Logged in"></span>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<span class="inline-block w-3 h-3 rounded-full bg-red-500" title="Not logged in"></span>`);
    }
    $$renderer2.push(`<!--]--></div> <a href="/" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.home"))}</a> `);
    if (session.isAuthenticated) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<span class="text-xs text-red-500 mr-2">isAdmin=${escape_html(session.isAdmin)} ownerOid=${escape_html(session.ownerOid)} cleanerOid=${escape_html(session.cleanerOid)}</span> `);
      if (session.isAdmin) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.houses"))}</a> <a href="/owners" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.owners"))}</a> <a href="/cleaners" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.cleaners"))}</a> <a href="/bookings" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.bookings"))}</a> <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.schedules"))}</a> <a href="/users" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.users"))}</a>`);
      } else if (session.ownerOid > 0) {
        $$renderer2.push("<!--[1-->");
        $$renderer2.push(`<a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.houses"))}</a> <a href="/bookings" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.bookings"))}</a> <a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.schedules"))}</a>`);
      } else if (session.cleanerOid > 0) {
        $$renderer2.push("<!--[2-->");
        $$renderer2.push(`<a href="/schedules" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("nav.schedules"))}</a>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--> `);
      LanguageSwitcher($$renderer2);
      $$renderer2.push(`<!----> <button class="text-red-600 hover:text-red-800 font-medium">${escape_html(tt("nav.logout"))}</button> <span class="text-green-600 text-sm">${escape_html(session.username || "User")}</span>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      LanguageSwitcher($$renderer2);
      $$renderer2.push(`<!----> <a href="/login" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("auth.login_button"))}</a> <a href="/signup" class="text-gray-600 hover:text-gray-900 font-medium">${escape_html(tt("auth.signup_button"))}</a>`);
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
    if ($$store_subs) unsubscribe_stores($$store_subs);
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
            return store_get($$store_subs ??= {}, "$modalStore", modalStore).open;
          },
          set open($$value) {
            store_mutate($$store_subs ??= {}, "$modalStore", modalStore, store_get($$store_subs ??= {}, "$modalStore", modalStore).open = $$value);
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
function NotificationToast($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let notificationList = derived(() => notificationsState.value);
    function getNotificationClass(type) {
      switch (type) {
        case "success":
          return "bg-green-50 border-green-400";
        case "error":
          return "bg-red-50 border-red-400";
        case "warning":
          return "bg-yellow-50 border-yellow-400";
        case "info":
          return "bg-blue-50 border-blue-400";
        default:
          return "bg-gray-50 border-gray-400";
      }
    }
    function getIconColor(type) {
      switch (type) {
        case "success":
          return "text-green-400";
        case "error":
          return "text-red-400";
        case "warning":
          return "text-yellow-400";
        case "info":
          return "text-blue-400";
        default:
          return "text-gray-400";
      }
    }
    function getCloseButtonColor(type) {
      switch (type) {
        case "success":
          return "text-green-500 hover:text-green-600";
        case "error":
          return "text-red-500 hover:text-red-600";
        case "warning":
          return "text-yellow-500 hover:text-yellow-600";
        case "info":
          return "text-blue-500 hover:text-blue-600";
        default:
          return "text-gray-500 hover:text-gray-600";
      }
    }
    if (notificationList().length > 0) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="pointer-events-none fixed inset-0 z-[60] flex items-start justify-end px-4 py-6 sm:p-6"><div class="flex w-full flex-col items-center space-y-4 sm:items-end"><!--[-->`);
      const each_array = ensure_array_like(notificationList());
      for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
        let notification = each_array[$$index];
        $$renderer2.push(`<div${attr_class(`pointer-events-auto w-full max-w-sm overflow-hidden rounded-lg border-l-4 shadow-lg ${stringify(getNotificationClass(notification.type))}`)}><div class="p-4"><div class="flex items-start"><div class="flex-shrink-0">`);
        if (notification.type === "success") {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<svg${attr_class(`h-5 w-5 ${stringify(getIconColor(notification.type))}`)} viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"></path></svg>`);
        } else if (notification.type === "error") {
          $$renderer2.push("<!--[1-->");
          $$renderer2.push(`<svg${attr_class(`h-5 w-5 ${stringify(getIconColor(notification.type))}`)} viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"></path></svg>`);
        } else if (notification.type === "warning") {
          $$renderer2.push("<!--[2-->");
          $$renderer2.push(`<svg${attr_class(`h-5 w-5 ${stringify(getIconColor(notification.type))}`)} viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"></path></svg>`);
        } else {
          $$renderer2.push("<!--[-1-->");
          $$renderer2.push(`<svg${attr_class(`h-5 w-5 ${stringify(getIconColor(notification.type))}`)} viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"></path></svg>`);
        }
        $$renderer2.push(`<!--]--></div> <div class="ml-3 w-0 flex-1 pt-0.5"><p class="text-sm font-medium text-gray-900">${escape_html(notification.message)}</p></div> <div class="ml-4 flex flex-shrink-0"><button type="button"${attr_class(`inline-flex rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 ${stringify(getCloseButtonColor(notification.type))}`)}><span class="sr-only">Close</span> <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z"></path></svg></button></div></div></div></div>`);
      }
      $$renderer2.push(`<!--]--></div></div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]-->`);
  });
}
function _layout($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    GlobalModal($$renderer2);
    $$renderer2.push(`<!----> `);
    NotificationToast($$renderer2);
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
