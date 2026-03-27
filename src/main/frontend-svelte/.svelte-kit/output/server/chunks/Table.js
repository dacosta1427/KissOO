import { S as Server } from "./Auth.js";
import { a as notificationActions } from "./stores.svelte.js";
import "./async.js";
import { e as ensure_array_like, b as attr_class } from "./index2.js";
import { e as escape_html, a as attr } from "./attributes.js";
const operationsWithToast = ["create", "add", "delete", "update", "deleteCleaner", "deleteBooking", "deleteHouse", "deleteOwner"];
async function callCleaningService(method, args = {}, operationName) {
  try {
    console.log(`[Cleaning.ts] Calling ${method} with args:`, args);
    const res = await Server.call("services.Cleaning", method, args);
    console.log(`[Cleaning.ts] ${method} response:`, res);
    const showToast = operationsWithToast.some((op) => method.toLowerCase().includes(op.toLowerCase()));
    if (showToast && operationName) {
      if (res._Success) {
        notificationActions.success(`${operationName} completed successfully`);
      } else {
        notificationActions.error(`${operationName} failed: ${res._ErrorMessage || "Unknown error"}`);
      }
    }
    return res;
  } catch (error) {
    const errorMessage = error.message || "Network error";
    console.error(`[Cleaning.ts] ${method} error:`, errorMessage);
    const showToast = operationsWithToast.some((op) => method.toLowerCase().includes(op.toLowerCase()));
    if (showToast && operationName) {
      notificationActions.error(`${operationName} failed: ${errorMessage}`);
    }
    throw error;
  }
}
const cleanersAPI = {
  getAll: async () => {
    const res = await callCleaningService("getCleaners", {}, "Load cleaners");
    return res.data || [];
  },
  getById: async (id) => {
    const res = await callCleaningService("getCleaner", { id }, "Load cleaner");
    return res.data || null;
  },
  create: async (data) => {
    const res = await callCleaningService("createCleaner", { data }, "Create cleaner");
    return res.data;
  },
  update: async (id, data) => {
    const res = await callCleaningService("updateCleaner", { id, data }, "Update cleaner");
    return res.data;
  },
  delete: async (id) => {
    await callCleaningService("deleteCleaner", { id }, "Delete cleaner");
  }
};
const bookingsAPI = {
  getAll: async (filters) => {
    const res = await callCleaningService("getBookings", { filters }, "Load bookings");
    return res.data || [];
  },
  getById: async (id) => {
    const res = await callCleaningService("getBooking", { id }, "Load booking");
    return res.data || null;
  },
  create: async (data) => {
    const res = await callCleaningService("createBooking", { data }, "Create booking");
    return res.data;
  },
  update: async (id, data) => {
    const res = await callCleaningService("updateBooking", { id, data }, "Update booking");
    return res.data;
  },
  delete: async (id) => {
    await callCleaningService("deleteBooking", { id }, "Delete booking");
  },
  getByHouse: async (houseId) => {
    const res = await callCleaningService("getBookingsByHouse", { houseId }, "Load bookings by house");
    return res.data || [];
  },
  getByDateRange: async (startDate, endDate) => {
    const res = await callCleaningService("getBookingsByDateRange", { startDate, endDate }, "Load bookings by date range");
    return res.data || [];
  }
};
const housesAPI = {
  getAll: async () => {
    const res = await callCleaningService("getHouses", {}, "Load houses");
    return res.data || [];
  },
  getById: async (id) => {
    const res = await callCleaningService("getHouse", { id }, "Load house");
    return res.data || null;
  },
  create: async (data) => {
    const res = await callCleaningService("createHouse", { data }, "Create house");
    return res.data;
  },
  update: async (id, data) => {
    const res = await callCleaningService("updateHouse", { id, data }, "Update house");
    return res.data;
  },
  delete: async (id) => {
    await callCleaningService("deleteHouse", { id }, "Delete house");
  }
};
const ownersAPI = {
  getAll: async () => {
    const res = await callCleaningService("getOwners", {}, "Load owners");
    return res.data || [];
  },
  getById: async (id) => {
    const res = await callCleaningService("getOwner", { id }, "Load owner");
    return res.data || null;
  },
  create: async (data) => {
    const res = await callCleaningService("createOwner", { data }, "Create owner");
    return res.data;
  },
  update: async (id, data) => {
    const res = await callCleaningService("updateOwner", { id, data }, "Update owner");
    return res.data;
  },
  delete: async (id) => {
    await callCleaningService("deleteOwner", { id }, "Delete owner");
  }
};
function Table($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let {
      data = [],
      columns = [],
      actions = [],
      loading = false,
      error = null,
      selectedRow = null,
      onAction,
      onRowClick
    } = $$props;
    $$renderer2.push(`<div class="table-container svelte-1iq5b9c">`);
    if (error) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="error-message svelte-1iq5b9c">${escape_html(error)}</div>`);
    } else if (loading) {
      $$renderer2.push("<!--[1-->");
      $$renderer2.push(`<div class="loading-message svelte-1iq5b9c">Loading...</div>`);
    } else if (data.length === 0) {
      $$renderer2.push("<!--[2-->");
      $$renderer2.push(`<div class="empty-message svelte-1iq5b9c">No data available</div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<table class="data-table svelte-1iq5b9c"><thead><tr class="svelte-1iq5b9c"><!--[-->`);
      const each_array = ensure_array_like(columns);
      for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
        let column = each_array[$$index];
        $$renderer2.push(`<th class="svelte-1iq5b9c">${escape_html(column.label)}</th>`);
      }
      $$renderer2.push(`<!--]-->`);
      if (actions.length > 0) {
        $$renderer2.push("<!--[0-->");
        $$renderer2.push(`<th class="svelte-1iq5b9c">Actions</th>`);
      } else {
        $$renderer2.push("<!--[-1-->");
      }
      $$renderer2.push(`<!--]--></tr></thead><tbody><!--[-->`);
      const each_array_1 = ensure_array_like(data);
      for (let index = 0, $$length = each_array_1.length; index < $$length; index++) {
        let row = each_array_1[index];
        $$renderer2.push(`<tr${attr_class("svelte-1iq5b9c", void 0, { "selected": selectedRow === row.id })}><!--[-->`);
        const each_array_2 = ensure_array_like(columns);
        for (let $$index_1 = 0, $$length2 = each_array_2.length; $$index_1 < $$length2; $$index_1++) {
          let column = each_array_2[$$index_1];
          $$renderer2.push(`<td class="svelte-1iq5b9c">`);
          if (column.formatter) {
            $$renderer2.push("<!--[0-->");
            $$renderer2.push(`${escape_html(column.formatter(row[column.key], row))}`);
          } else {
            $$renderer2.push("<!--[-1-->");
            $$renderer2.push(`${escape_html(row[column.key])}`);
          }
          $$renderer2.push(`<!--]--></td>`);
        }
        $$renderer2.push(`<!--]-->`);
        if (actions.length > 0) {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<td class="actions-cell svelte-1iq5b9c"><!--[-->`);
          const each_array_3 = ensure_array_like(actions);
          for (let $$index_2 = 0, $$length2 = each_array_3.length; $$index_2 < $$length2; $$index_2++) {
            let action = each_array_3[$$index_2];
            $$renderer2.push(`<button class="action-btn svelte-1iq5b9c"${attr("title", action.title)}>`);
            if (action.icon) {
              $$renderer2.push("<!--[0-->");
              $$renderer2.push(`<span class="action-icon">${escape_html(action.icon)}</span>`);
            } else {
              $$renderer2.push("<!--[-1-->");
            }
            $$renderer2.push(`<!--]--> ${escape_html(action.label)}</button>`);
          }
          $$renderer2.push(`<!--]--></td>`);
        } else {
          $$renderer2.push("<!--[-1-->");
        }
        $$renderer2.push(`<!--]--></tr>`);
      }
      $$renderer2.push(`<!--]--></tbody></table>`);
    }
    $$renderer2.push(`<!--]--></div>`);
  });
}
export {
  Table as T,
  bookingsAPI as b,
  cleanersAPI as c,
  housesAPI as h,
  ownersAPI as o
};
