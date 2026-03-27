import "./async.js";
import { e as ensure_array_like, b as attr_class, c as stringify, i as bind_props } from "./index2.js";
import { e as escape_html, a as attr } from "./attributes.js";
function Form($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let {
      fields = [],
      data = {},
      errors = {},
      loading = false,
      title = "",
      submitLabel = "Save",
      cancelLabel = "Cancel",
      onSubmit,
      onCancel,
      onUpdate
    } = $$props;
    function updateField(fieldName, value) {
      data[fieldName] = value;
      if (errors[fieldName]) {
        errors[fieldName] = "";
      }
      onUpdate?.({ field: fieldName, value });
    }
    function getFieldError(field) {
      return errors[field.name] || "";
    }
    function isFieldInvalid(field) {
      return !!getFieldError(field);
    }
    $$renderer2.push(`<form class="space-y-6">`);
    if (title) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="border-b border-gray-200 pb-4"><h3 class="text-lg font-medium leading-6 text-gray-900">${escape_html(title)}</h3></div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> <div class="grid grid-cols-1 gap-6 sm:grid-cols-2"><!--[-->`);
    const each_array = ensure_array_like(fields);
    for (let $$index_1 = 0, $$length = each_array.length; $$index_1 < $$length; $$index_1++) {
      let field = each_array[$$index_1];
      $$renderer2.push(`<div class="space-y-1"><label${attr("for", field.name)} class="block text-sm font-medium text-gray-700">${escape_html(field.label)} `);
      if (field.required) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<span class="text-red-500">*</span>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></label> `);
      if (field.type === "text" || field.type === "email" || field.type === "tel" || field.type === "number" || field.type === "password") {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<input${attr("type", field.type)}${attr("id", field.name)}${attr("name", field.name)}${attr("value", data[field.name] ?? "")}${attr("placeholder", field.placeholder)}${attr("required", field.required, true)}${attr("disabled", loading, true)}${attr_class(`block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${stringify(isFieldInvalid(field) ? "border-red-300 text-red-900 placeholder-red-300 focus:border-red-500 focus:ring-red-500" : "")}`)}/>`);
      } else if (field.type === "select") {
        $$renderer2.push("<!--[1-->");
        $$renderer2.select(
          {
            id: field.name,
            name: field.name,
            value: data[field.name] ?? "",
            required: field.required,
            disabled: loading,
            class: `block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${stringify(isFieldInvalid(field) ? "border-red-300 text-red-900 focus:border-red-500 focus:ring-red-500" : "")}`,
            onchange: (e) => updateField(field.name, e.target.value)
          },
          ($$renderer3) => {
            $$renderer3.option({ value: "" }, ($$renderer4) => {
              $$renderer4.push(`-- Select ${escape_html(field.label)} --`);
            });
            $$renderer3.push(`<!--[-->`);
            const each_array_1 = ensure_array_like(field.options ? [...field.options] : []);
            for (let $$index = 0, $$length2 = each_array_1.length; $$index < $$length2; $$index++) {
              let option = each_array_1[$$index];
              $$renderer3.option({ value: option.value }, ($$renderer4) => {
                $$renderer4.push(`${escape_html(option.label)}`);
              });
            }
            $$renderer3.push(`<!--]-->`);
          }
        );
      } else if (field.type === "textarea") {
        $$renderer2.push("<!--[2-->");
        $$renderer2.push(`<textarea${attr("id", field.name)}${attr("name", field.name)}${attr("placeholder", field.placeholder)}${attr("required", field.required, true)}${attr("disabled", loading, true)}${attr("rows", field.rows || 4)}${attr_class(`block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${stringify(isFieldInvalid(field) ? "border-red-300 text-red-900 placeholder-red-300 focus:border-red-500 focus:ring-red-500" : "")}`)}>`);
        const $$body = escape_html(data[field.name] ?? "");
        if ($$body) {
          $$renderer2.push(`${$$body}`);
        }
        $$renderer2.push(`</textarea>`);
      } else if (field.type === "checkbox") {
        $$renderer2.push("<!--[3-->");
        $$renderer2.push(`<div class="flex items-center"><input type="checkbox"${attr("id", field.name)}${attr("name", field.name)}${attr("checked", data[field.name] ?? false, true)}${attr("disabled", loading, true)} class="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"/> <label${attr("for", field.name)} class="ml-2 block text-sm text-gray-700">${escape_html(field.label)}</label></div>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--> `);
      if (isFieldInvalid(field)) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<p class="text-sm text-red-600">${escape_html(getFieldError(field))}</p>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--> `);
      if (field.helpText && !isFieldInvalid(field)) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<p class="text-sm text-gray-500">${escape_html(field.helpText)}</p>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></div>`);
    }
    $$renderer2.push(`<!--]--></div> <div class="flex justify-end space-x-3 border-t border-gray-200 pt-4"><button type="button"${attr("disabled", loading, true)} class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed">${escape_html(cancelLabel)}</button> <button type="submit"${attr("disabled", loading, true)} class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed">${escape_html(loading ? "Saving..." : submitLabel)}</button></div></form>`);
    bind_props($$props, { data, errors });
  });
}
export {
  Form as F
};
