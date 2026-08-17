import "../../../chunks/async.js";
import { f as attr_class, j as attr_style, k as bind_props, a as attr, d as derived, e as escape_html } from "../../../chunks/index2.js";
import { S as Server } from "../../../chunks/Auth.js";
import { o as onDestroy } from "../../../chunks/index-server.js";
import "ag-grid-community";
import { M as Modal } from "../../../chunks/Modal.js";
import { F as Form } from "../../../chunks/Form.js";
import { a as notificationActions } from "../../../chunks/stores.svelte.js";
async function getPhones() {
  const res = await Server.call("services.Crud", "getPhones", {});
  return res.rows || [];
}
async function createPhone(firstName, lastName, phoneNumber) {
  const res = await Server.call("services.Crud", "createPhone", {
    firstName,
    lastName,
    phoneNumber
  });
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    id: res.id
  };
}
async function updatePhone(id, firstName, lastName, phoneNumber) {
  const res = await Server.call("services.Crud", "updatePhone", {
    id,
    firstName,
    lastName,
    phoneNumber
  });
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}
function AgGridWrapper($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let {
      columnDefs,
      rowData = [],
      keyColumn,
      onSelectionChanged,
      onRowDoubleClicked,
      singleSelection = true,
      suppressHorizontalScroll = true,
      style = "height: 400px; width: 100%;",
      class: className = ""
    } = $$props;
    onDestroy(() => {
    });
    function setRowData(data) {
    }
    function addRecords(records) {
    }
    function clear() {
    }
    function getSelectedRows() {
      return [];
    }
    function getSelectedRow() {
      const rows = getSelectedRows();
      return rows.length === 1 ? rows[0] : null;
    }
    function deleteRow(id) {
    }
    function updateRow(row) {
    }
    function deselectAll() {
    }
    function sizeColumnsToFit() {
    }
    $$renderer2.push(`<div${attr_class(
      // Reactive updates for props
      className
    )}${attr_style(style)}></div>`);
    bind_props($$props, {
      setRowData,
      addRecords,
      clear,
      getSelectedRows,
      getSelectedRow,
      deleteRow,
      updateRow,
      deselectAll,
      sizeColumnsToFit
    });
  });
}
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let records = [];
    let loading = false;
    let error = "";
    let dataLoading = true;
    let selectedRow = null;
    let editModalOpen = false;
    let editingRecord = null;
    let editFormData = { firstName: "", lastName: "", phoneNumber: "" };
    let editLoading = false;
    let isAddMode = false;
    const editFields = [
      {
        name: "firstName",
        label: "First Name",
        type: "text",
        required: true,
        placeholder: "Enter first name"
      },
      {
        name: "lastName",
        label: "Last Name",
        type: "text",
        required: true,
        placeholder: "Enter last name"
      },
      {
        name: "phoneNumber",
        label: "Phone Number",
        type: "text",
        required: false,
        placeholder: "Enter phone number"
      }
    ];
    let canSave = derived(() => (editFormData.firstName?.length ?? 0) >= 1 && (editFormData.lastName?.length ?? 0) >= 1);
    const columnDefs = [
      { headerName: "Last Name", field: "lastName", width: 150 },
      { headerName: "First Name", field: "firstName", width: 150 },
      { headerName: "Phone Number", field: "phoneNumber", width: 150 }
    ];
    let gridRef = null;
    async function loadRecords() {
      dataLoading = true;
      error = "";
      try {
        records = await getPhones();
        if (gridRef) ;
      } catch (e) {
        error = "Failed to load records: " + (e.message || "Unknown error");
      } finally {
        dataLoading = false;
      }
    }
    function openEditModal(record) {
      editingRecord = record;
      editFormData = {
        firstName: record.firstName,
        lastName: record.lastName,
        phoneNumber: record.phoneNumber
      };
      isAddMode = false;
      editModalOpen = true;
    }
    async function handleSave(data) {
      if (!canSave()) return;
      editLoading = true;
      error = "";
      try {
        let res;
        if (isAddMode) {
          res = await createPhone(data.firstName, data.lastName, data.phoneNumber);
        } else if (editingRecord) {
          res = await updatePhone(editingRecord.id, data.firstName, data.lastName, data.phoneNumber);
        }
        if (res && res.success) {
          notificationActions.success(isAddMode ? "Record added successfully" : "Record updated successfully");
          editModalOpen = false;
          await loadRecords();
        } else if (res) {
          error = res.error || "Failed to save record";
          notificationActions.error(error);
        }
      } catch (e) {
        error = "Failed to save record: " + (e.message || "Unknown error");
        notificationActions.error(error);
      } finally {
        editLoading = false;
      }
    }
    function handleSelectionChanged(selectedRows) {
      selectedRow = selectedRows.length === 1 ? selectedRows[0] ?? null : null;
    }
    function handleRowDoubleClicked(record) {
      openEditModal(record);
    }
    let $$settled = true;
    let $$inner_renderer;
    function $$render_inner($$renderer3) {
      $$renderer3.push(`<div class="p-6 max-w-6xl mx-auto"><h1 class="text-2xl font-bold mb-6">Phone Book (CRUD)</h1> `);
      if (error) {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">${escape_html(error)}</div>`);
      } else {
        $$renderer3.push("<!--[-1-->");
      }
      $$renderer3.push(`<!--]--> <div class="mb-4 flex gap-2"><button${attr("disabled", loading, true)} class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">New</button> <button${attr("disabled", !selectedRow || loading, true)} class="bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Edit</button> <button${attr("disabled", !selectedRow || loading, true)} class="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Delete</button> <button${attr("disabled", loading, true)} class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Report</button> <button${attr("disabled", loading, true)} class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50">Export</button></div> <p class="text-gray-600 mb-4">(Note that the Report function will not work unless the underlying system has groff installed and the ability to generate PDF files.
    This is generally true of Linux and Mac systems but not true on Windows. A version for Windows must be installed.)</p> `);
      if (dataLoading) {
        $$renderer3.push("<!--[0-->");
        $$renderer3.push(`<p class="text-gray-500">Loading records...</p>`);
      } else {
        $$renderer3.push("<!--[-1-->");
        AgGridWrapper($$renderer3, {
          columnDefs,
          rowData: records,
          keyColumn: "id",
          onSelectionChanged: handleSelectionChanged,
          onRowDoubleClicked: handleRowDoubleClicked,
          style: "height: 400px; width: 100%;"
        });
      }
      $$renderer3.push(`<!--]--></div> `);
      Modal($$renderer3, {
        title: isAddMode ? "Add Record" : "Edit Record",
        onClose: () => editModalOpen = false,
        get open() {
          return editModalOpen;
        },
        set open($$value) {
          editModalOpen = $$value;
          $$settled = false;
        },
        children: ($$renderer4) => {
          Form($$renderer4, {
            fields: editFields,
            loading: editLoading,
            submitLabel: isAddMode ? "Add" : "Update",
            cancelLabel: "Cancel",
            onSubmit: handleSave,
            onCancel: () => editModalOpen = false,
            get data() {
              return editFormData;
            },
            set data($$value) {
              editFormData = $$value;
              $$settled = false;
            }
          });
        },
        $$slots: { default: true }
      });
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
