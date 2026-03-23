

export const index = 10;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/report/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/10.C5OSN9pe.js","_app/immutable/chunks/B0jycD_D.js","_app/immutable/chunks/7w309cUQ.js","_app/immutable/chunks/BNmvevfW.js"];
export const stylesheets = [];
export const fonts = [];
