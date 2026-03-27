import "../../../chunks/async.js";
import { e as ensure_array_like, b as attr_class, c as stringify, d as derived, u as unsubscribe_stores, s as store_get } from "../../../chunks/index2.js";
import { s as session } from "../../../chunks/session.svelte.js";
import { e as escape_html, a as attr } from "../../../chunks/attributes.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
function ScheduleBoard($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    let {
      schedules = [],
      cleaners = [],
      bookings = [],
      dateRange = { start: null, end: null },
      loading = false,
      error = null,
      selectedCleanerId = null
    } = $$props;
    let dragOverDate = null;
    let dragOverCleanerId = null;
    let dates = derived(() => generateDateRange(dateRange.start, dateRange.end));
    let scheduleMatrix = derived(() => buildScheduleMatrix(schedules, cleaners, dates()));
    let filteredCleaners = derived(() => selectedCleanerId ? cleaners.filter((c) => c.id === selectedCleanerId) : cleaners);
    function generateDateRange(start, end) {
      if (!start || !end) return [];
      const dates2 = [];
      const current = new Date(start);
      const endDate = new Date(end);
      while (current <= endDate) {
        dates2.push(new Date(current));
        current.setDate(current.getDate() + 1);
      }
      return dates2;
    }
    function buildScheduleMatrix(schedules2, cleaners2, dates2) {
      const matrix = {};
      cleaners2.forEach((cleaner) => {
        matrix[cleaner.id] = {};
        dates2.forEach((date) => {
          const dateString = date.toISOString().split("T")[0];
          matrix[cleaner.id][dateString] = null;
        });
      });
      schedules2.forEach((schedule) => {
        const dateString = schedule.date;
        cleaners2.forEach((cleaner) => {
          if (cleaner.id === schedule.cleaner_id && matrix[cleaner.id][dateString] !== void 0) {
            matrix[cleaner.id][dateString] = schedule;
          }
        });
      });
      return matrix;
    }
    function getBookingInfo(bookingId) {
      return bookings.find((b) => b.id === bookingId);
    }
    function formatDate(date) {
      return date.toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric" });
    }
    function isWeekend(date) {
      const day = date.getDay();
      return day === 0 || day === 6;
    }
    $$renderer2.push(`<div class="schedule-board svelte-16jlk9j"><div class="status-legend svelte-16jlk9j"><span class="legend-item svelte-16jlk9j"><span class="legend-color status-scheduled svelte-16jlk9j"></span> Scheduled</span> <span class="legend-item svelte-16jlk9j"><span class="legend-color status-completed svelte-16jlk9j"></span> Completed</span> <span class="legend-item svelte-16jlk9j"><span class="legend-color status-cancelled svelte-16jlk9j"></span> Cancelled</span> <span class="legend-item svelte-16jlk9j"><span class="legend-color status-pending svelte-16jlk9j"></span> Pending</span></div> <div class="board-header svelte-16jlk9j"><div class="cleaner-header svelte-16jlk9j">Cleaners</div> <!--[-->`);
    const each_array = ensure_array_like(dates());
    for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
      let date = each_array[$$index];
      $$renderer2.push(`<div${attr_class(`date-header ${stringify(isWeekend(date) ? "weekend" : "")}`, "svelte-16jlk9j")}><div class="date-label svelte-16jlk9j">${escape_html(formatDate(date))}</div></div>`);
    }
    $$renderer2.push(`<!--]--></div> `);
    if (error) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="error-message svelte-16jlk9j">${escape_html(error)}</div>`);
    } else if (loading) {
      $$renderer2.push("<!--[1-->");
      $$renderer2.push(`<div class="loading-message svelte-16jlk9j">Loading schedule...</div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<!--[-->`);
      const each_array_1 = ensure_array_like(filteredCleaners());
      for (let $$index_2 = 0, $$length = each_array_1.length; $$index_2 < $$length; $$index_2++) {
        let cleaner = each_array_1[$$index_2];
        $$renderer2.push(`<div class="board-row svelte-16jlk9j"${attr("key", cleaner.id)} role="row"><div${attr_class(`cleaner-cell ${stringify(selectedCleanerId === cleaner.id ? "selected" : "")}`, "svelte-16jlk9j")} role="button" tabindex="0"><div class="cleaner-name svelte-16jlk9j">${escape_html(cleaner.name)}</div> <div class="cleaner-info svelte-16jlk9j">${escape_html(cleaner.email)}</div></div> <!--[-->`);
        const each_array_2 = ensure_array_like(dates());
        for (let $$index_1 = 0, $$length2 = each_array_2.length; $$index_1 < $$length2; $$index_1++) {
          let date = each_array_2[$$index_1];
          $$renderer2.push(`<div${attr_class(`schedule-cell ${stringify(isWeekend(date) ? "weekend" : "")} ${stringify(dragOverCleanerId === cleaner.id && dragOverDate === date ? "drag-over" : "")}`, "svelte-16jlk9j")} role="gridcell" tabindex="-1">`);
          if (scheduleMatrix()[cleaner.id][date.toISOString().split("T")[0]]) {
            $$renderer2.push("<!--[0-->");
            const item = scheduleMatrix()[cleaner.id][date.toISOString().split("T")[0]];
            $$renderer2.push(`<div${attr_class(`schedule-item status-${stringify(item.status)}`, "svelte-16jlk9j")} draggable="true" role="button" tabindex="0"><div class="schedule-time svelte-16jlk9j">${escape_html(item.start_time || "")} - ${escape_html(item.end_time || "")}</div> <div class="schedule-house svelte-16jlk9j">${escape_html(getBookingInfo(item.booking_id)?.guest_name || "Unknown Guest")}</div> <div class="schedule-status svelte-16jlk9j">${escape_html(item.status)}</div></div>`);
          } else {
            $$renderer2.push("<!--[-1-->");
            $$renderer2.push(`<button class="add-schedule-btn svelte-16jlk9j" title="Add schedule">+</button>`);
          }
          $$renderer2.push(`<!--]--></div>`);
        }
        $$renderer2.push(`<!--]--></div>`);
      }
      $$renderer2.push(`<!--]-->`);
    }
    $$renderer2.push(`<!--]--></div>`);
  });
}
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let isAdmin = derived(() => session.username === "admin" || session.username === "administrator");
    let schedules = [];
    let cleaners = [];
    let bookings = [];
    let loading = false;
    let error = null;
    let viewMode = "calendar";
    let dateRange = {
      start: (/* @__PURE__ */ new Date()).toISOString().split("T")[0],
      end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1e3).toISOString().split("T")[0]
    };
    let selectedCleanerId = null;
    let selectedCleanerName = derived(() => "");
    let filteredSchedules = derived(() => isAdmin() ? schedules : schedules.filter((s) => s.cleaner_id === session.cleanerId));
    $$renderer2.push(`<div class="schedules-page svelte-1uvighd"><div class="page-header svelte-1uvighd"><h1 class="svelte-1uvighd">${escape_html(
      // Load data on mount
      tt("schedules.title")
    )}</h1> <div class="header-actions svelte-1uvighd"><div class="view-toggle svelte-1uvighd"><button${attr_class("toggle-btn svelte-1uvighd", void 0, { "active": viewMode === "calendar" })}>${escape_html(tt("schedules.calendar"))}</button> <button${attr_class("toggle-btn svelte-1uvighd", void 0, { "active": viewMode === "table" })}>${escape_html(tt("schedules.table"))}</button></div> <button class="btn btn-primary svelte-1uvighd">${escape_html(tt("schedules.add_schedule"))}</button></div></div> <div class="date-range-controls svelte-1uvighd"><div class="form-group svelte-1uvighd"><label for="startDate" class="svelte-1uvighd">${escape_html(tt("common.start_date"))}</label> <input type="date" id="startDate"${attr("value", dateRange.start)} class="svelte-1uvighd"/></div> <div class="form-group svelte-1uvighd"><label for="endDate" class="svelte-1uvighd">${escape_html(tt("common.end_date"))}</label> <input type="date" id="endDate"${attr("value", dateRange.end)} class="svelte-1uvighd"/></div></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    if (selectedCleanerName()) {
      $$renderer2.push("<!--[0-->");
      $$renderer2.push(`<div class="filter-banner svelte-1uvighd"><span>${escape_html(tt("schedules.showing"))}: <strong>${escape_html(selectedCleanerName())}</strong></span> <button class="btn btn-secondary btn-sm svelte-1uvighd">${escape_html(tt("schedules.show_all"))}</button></div>`);
    } else {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[0-->");
      ScheduleBoard($$renderer2, {
        schedules: filteredSchedules(),
        cleaners,
        bookings,
        dateRange,
        loading,
        error,
        selectedCleanerId
      });
    }
    $$renderer2.push(`<!--]--></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]-->`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
