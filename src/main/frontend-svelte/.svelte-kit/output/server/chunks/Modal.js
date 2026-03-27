import "./async.js";
import { b as attr_class, i as bind_props, c as stringify } from "./index2.js";
import { e as escape_html } from "./attributes.js";
function Modal($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let {
      open = false,
      title = "",
      onClose,
      size = "md",
      children,
      footer
    } = $$props;
    const sizeClasses = {
      sm: "max-w-sm",
      md: "max-w-md",
      lg: "max-w-lg",
      xl: "max-w-xl"
    };
    if (
      // Focus trap when modal opens
      // Could add focus trap logic here
      open
    ) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true"><div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true"></div> <div class="flex min-h-full items-center justify-center p-4"><div${attr_class(`relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all w-full ${stringify(sizeClasses[size])}`)}><div class="bg-white px-4 py-3 border-b border-gray-200"><div class="flex items-center justify-between"><h3 class="text-lg font-medium leading-6 text-gray-900" id="modal-title">${escape_html(title)}</h3> <button type="button" class="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"><span class="sr-only">Close</span> <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"></path></svg></button></div></div> <div class="bg-white px-4 py-5 sm:p-6">`);
      children?.($$renderer2);
      $$renderer2.push(`<!----></div> `);
      if (footer) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6 border-t border-gray-200">`);
        footer?.($$renderer2);
        $$renderer2.push(`<!----></div>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></div></div></div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]-->`);
    bind_props($$props, { open });
  });
}
export {
  Modal as M
};
