

export const index = 3;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/users/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/3.BfCvjMVS.js","_app/immutable/chunks/B2QSKqcC.js","_app/immutable/chunks/BgE6Kk8l.js","_app/immutable/chunks/C5QRPDXU.js"];
export const stylesheets = [];
export const fonts = [];
