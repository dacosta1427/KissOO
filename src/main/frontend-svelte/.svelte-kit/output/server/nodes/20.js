

export const index = 20;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/sql-access/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/20.DfBoxgBw.js","_app/immutable/chunks/BKx5ofot.js","_app/immutable/chunks/BQla8ufS.js","_app/immutable/chunks/Xohg949J.js"];
export const stylesheets = [];
export const fonts = [];
