

export const index = 3;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/benchmark/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/3.BF05c-p-.js","_app/immutable/chunks/BMBwq750.js","_app/immutable/chunks/CCM0Gor-.js","_app/immutable/chunks/6fyJ3vAh.js","_app/immutable/chunks/D4PHqqzK.js","_app/immutable/chunks/DyKAOMPO.js"];
export const stylesheets = [];
export const fonts = [];
