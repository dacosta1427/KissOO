

export const index = 5;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/users/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/5.DnXPLfDD.js","_app/immutable/chunks/B2QSKqcC.js","_app/immutable/chunks/Bi3IMxzF.js","_app/immutable/chunks/CpEWiglc.js"];
export const stylesheets = [];
export const fonts = [];
