

export const index = 0;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/_layout.svelte.js')).default;
export const imports = ["_app/immutable/nodes/0.CJiva7Tr.js","_app/immutable/chunks/B2QSKqcC.js","_app/immutable/chunks/Bi3IMxzF.js"];
export const stylesheets = ["_app/immutable/assets/0.DDTTNl2t.css"];
export const fonts = [];
