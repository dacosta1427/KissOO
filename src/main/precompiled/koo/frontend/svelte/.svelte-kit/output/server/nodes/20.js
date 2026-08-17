

export const index = 20;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/sql-access/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/20.D4sajTzY.js","_app/immutable/chunks/CkrBU4Y_.js","_app/immutable/chunks/BYFhwfgA.js","_app/immutable/chunks/BzWFuMNo.js"];
export const stylesheets = [];
export const fonts = [];
