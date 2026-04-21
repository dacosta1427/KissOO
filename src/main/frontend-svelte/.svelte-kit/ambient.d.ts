
// this file is generated — do not edit it


/// <reference types="@sveltejs/kit" />

/**
 * This module provides access to environment variables that are injected _statically_ into your bundle at build time and are limited to _private_ access.
 * 
 * |         | Runtime                                                                    | Build time                                                               |
 * | ------- | -------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
 * | Private | [`$env/dynamic/private`](https://svelte.dev/docs/kit/$env-dynamic-private) | [`$env/static/private`](https://svelte.dev/docs/kit/$env-static-private) |
 * | Public  | [`$env/dynamic/public`](https://svelte.dev/docs/kit/$env-dynamic-public)   | [`$env/static/public`](https://svelte.dev/docs/kit/$env-static-public)   |
 * 
 * Static environment variables are [loaded by Vite](https://vitejs.dev/guide/env-and-mode.html#env-files) from `.env` files and `process.env` at build time and then statically injected into your bundle at build time, enabling optimisations like dead code elimination.
 * 
 * **_Private_ access:**
 * 
 * - This module cannot be imported into client-side code
 * - This module only includes variables that _do not_ begin with [`config.kit.env.publicPrefix`](https://svelte.dev/docs/kit/configuration#env) _and do_ start with [`config.kit.env.privatePrefix`](https://svelte.dev/docs/kit/configuration#env) (if configured)
 * 
 * For example, given the following build time environment:
 * 
 * ```env
 * ENVIRONMENT=production
 * PUBLIC_BASE_URL=http://site.com
 * ```
 * 
 * With the default `publicPrefix` and `privatePrefix`:
 * 
 * ```ts
 * import { ENVIRONMENT, PUBLIC_BASE_URL } from '$env/static/private';
 * 
 * console.log(ENVIRONMENT); // => "production"
 * console.log(PUBLIC_BASE_URL); // => throws error during build
 * ```
 * 
 * The above values will be the same _even if_ different values for `ENVIRONMENT` or `PUBLIC_BASE_URL` are set at runtime, as they are statically replaced in your code with their build time values.
 */
declare module '$env/static/private' {
	export const XDG_SESSION_PATH: string;
	export const LC_TIME: string;
	export const SAL_USE_VCLPLUGIN: string;
	export const XDG_SEAT_PATH: string;
	export const SNAP_LIBRARY_PATH: string;
	export const FORCE_COLOR: string;
	export const SNAP_NAME: string;
	export const PATH: string;
	export const XDG_MENU_PREFIX: string;
	export const LOGNAME: string;
	export const XDG_CONFIG_DIRS: string;
	export const XAUTHORITY: string;
	export const LC_PAPER: string;
	export const LC_MEASUREMENT: string;
	export const XDG_SESSION_ID: string;
	export const XDG_CACHE_HOME: string;
	export const SNAP_ARCH: string;
	export const GTK_CSD: string;
	export const XMODIFIERS: string;
	export const npm_config_color: string;
	export const SNAP_REVISION: string;
	export const QT_ACCESSIBILITY: string;
	export const SNAP_INSTANCE_NAME: string;
	export const XDG_SEAT: string;
	export const XDG_VTNR: string;
	export const SNAP_USER_DATA: string;
	export const XDG_SESSION_DESKTOP: string;
	export const DBUS_SESSION_BUS_ADDRESS: string;
	export const LC_TELEPHONE: string;
	export const LC_ADDRESS: string;
	export const SHLVL: string;
	export const XDG_DATA_DIRS: string;
	export const SHELL: string;
	export const GTK_IM_MODULE: string;
	export const DEBUG_COLORS: string;
	export const XDG_SESSION_CLASS: string;
	export const BROWSER: string;
	export const COLORTERM: string;
	export const SNAP_REAL_HOME: string;
	export const LC_IDENTIFICATION: string;
	export const LXQT_SESSION_CONFIG: string;
	export const HOME: string;
	export const DISPLAY: string;
	export const GTK_OVERLAY_SCROLLING: string;
	export const XDG_CURRENT_DESKTOP: string;
	export const SSH_AGENT_PID: string;
	export const SNAP_UID: string;
	export const SNAP_EUID: string;
	export const DEBUGINFOD_URLS: string;
	export const TERM: string;
	export const QT_IM_MODULE: string;
	export const LC_NUMERIC: string;
	export const LC_NAME: string;
	export const MOCHA_COLORS: string;
	export const QT_QPA_PLATFORMTHEME: string;
	export const LANG: string;
	export const SNAP_COOKIE: string;
	export const CLUTTER_IM_MODULE: string;
	export const SNAP_VERSION: string;
	export const SNAP_COMMON: string;
	export const QT_PLATFORM_PLUGIN: string;
	export const XDG_CONFIG_HOME: string;
	export const XDG_RUNTIME_DIR: string;
	export const SSH_AUTH_SOCK: string;
	export const GPG_AGENT_INFO: string;
	export const SNAP_DATA: string;
	export const SNAP_REEXEC: string;
	export const DESKTOP_SESSION: string;
	export const USER: string;
	export const SNAP_USER_COMMON: string;
	export const XDG_SESSION_TYPE: string;
	export const XDG_DATA_HOME: string;
	export const SNAP_INSTANCE_KEY: string;
	export const LC_MONETARY: string;
	export const PWD: string;
	export const SNAP_CONTEXT: string;
	export const SNAP: string;
	export const NODE_ENV: string;
}

/**
 * This module provides access to environment variables that are injected _statically_ into your bundle at build time and are _publicly_ accessible.
 * 
 * |         | Runtime                                                                    | Build time                                                               |
 * | ------- | -------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
 * | Private | [`$env/dynamic/private`](https://svelte.dev/docs/kit/$env-dynamic-private) | [`$env/static/private`](https://svelte.dev/docs/kit/$env-static-private) |
 * | Public  | [`$env/dynamic/public`](https://svelte.dev/docs/kit/$env-dynamic-public)   | [`$env/static/public`](https://svelte.dev/docs/kit/$env-static-public)   |
 * 
 * Static environment variables are [loaded by Vite](https://vitejs.dev/guide/env-and-mode.html#env-files) from `.env` files and `process.env` at build time and then statically injected into your bundle at build time, enabling optimisations like dead code elimination.
 * 
 * **_Public_ access:**
 * 
 * - This module _can_ be imported into client-side code
 * - **Only** variables that begin with [`config.kit.env.publicPrefix`](https://svelte.dev/docs/kit/configuration#env) (which defaults to `PUBLIC_`) are included
 * 
 * For example, given the following build time environment:
 * 
 * ```env
 * ENVIRONMENT=production
 * PUBLIC_BASE_URL=http://site.com
 * ```
 * 
 * With the default `publicPrefix` and `privatePrefix`:
 * 
 * ```ts
 * import { ENVIRONMENT, PUBLIC_BASE_URL } from '$env/static/public';
 * 
 * console.log(ENVIRONMENT); // => throws error during build
 * console.log(PUBLIC_BASE_URL); // => "http://site.com"
 * ```
 * 
 * The above values will be the same _even if_ different values for `ENVIRONMENT` or `PUBLIC_BASE_URL` are set at runtime, as they are statically replaced in your code with their build time values.
 */
declare module '$env/static/public' {
	
}

/**
 * This module provides access to environment variables set _dynamically_ at runtime and that are limited to _private_ access.
 * 
 * |         | Runtime                                                                    | Build time                                                               |
 * | ------- | -------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
 * | Private | [`$env/dynamic/private`](https://svelte.dev/docs/kit/$env-dynamic-private) | [`$env/static/private`](https://svelte.dev/docs/kit/$env-static-private) |
 * | Public  | [`$env/dynamic/public`](https://svelte.dev/docs/kit/$env-dynamic-public)   | [`$env/static/public`](https://svelte.dev/docs/kit/$env-static-public)   |
 * 
 * Dynamic environment variables are defined by the platform you're running on. For example if you're using [`adapter-node`](https://github.com/sveltejs/kit/tree/main/packages/adapter-node) (or running [`vite preview`](https://svelte.dev/docs/kit/cli)), this is equivalent to `process.env`.
 * 
 * **_Private_ access:**
 * 
 * - This module cannot be imported into client-side code
 * - This module includes variables that _do not_ begin with [`config.kit.env.publicPrefix`](https://svelte.dev/docs/kit/configuration#env) _and do_ start with [`config.kit.env.privatePrefix`](https://svelte.dev/docs/kit/configuration#env) (if configured)
 * 
 * > [!NOTE] In `dev`, `$env/dynamic` includes environment variables from `.env`. In `prod`, this behavior will depend on your adapter.
 * 
 * > [!NOTE] To get correct types, environment variables referenced in your code should be declared (for example in an `.env` file), even if they don't have a value until the app is deployed:
 * >
 * > ```env
 * > MY_FEATURE_FLAG=
 * > ```
 * >
 * > You can override `.env` values from the command line like so:
 * >
 * > ```sh
 * > MY_FEATURE_FLAG="enabled" npm run dev
 * > ```
 * 
 * For example, given the following runtime environment:
 * 
 * ```env
 * ENVIRONMENT=production
 * PUBLIC_BASE_URL=http://site.com
 * ```
 * 
 * With the default `publicPrefix` and `privatePrefix`:
 * 
 * ```ts
 * import { env } from '$env/dynamic/private';
 * 
 * console.log(env.ENVIRONMENT); // => "production"
 * console.log(env.PUBLIC_BASE_URL); // => undefined
 * ```
 */
declare module '$env/dynamic/private' {
	export const env: {
		XDG_SESSION_PATH: string;
		LC_TIME: string;
		SAL_USE_VCLPLUGIN: string;
		XDG_SEAT_PATH: string;
		SNAP_LIBRARY_PATH: string;
		FORCE_COLOR: string;
		SNAP_NAME: string;
		PATH: string;
		XDG_MENU_PREFIX: string;
		LOGNAME: string;
		XDG_CONFIG_DIRS: string;
		XAUTHORITY: string;
		LC_PAPER: string;
		LC_MEASUREMENT: string;
		XDG_SESSION_ID: string;
		XDG_CACHE_HOME: string;
		SNAP_ARCH: string;
		GTK_CSD: string;
		XMODIFIERS: string;
		npm_config_color: string;
		SNAP_REVISION: string;
		QT_ACCESSIBILITY: string;
		SNAP_INSTANCE_NAME: string;
		XDG_SEAT: string;
		XDG_VTNR: string;
		SNAP_USER_DATA: string;
		XDG_SESSION_DESKTOP: string;
		DBUS_SESSION_BUS_ADDRESS: string;
		LC_TELEPHONE: string;
		LC_ADDRESS: string;
		SHLVL: string;
		XDG_DATA_DIRS: string;
		SHELL: string;
		GTK_IM_MODULE: string;
		DEBUG_COLORS: string;
		XDG_SESSION_CLASS: string;
		BROWSER: string;
		COLORTERM: string;
		SNAP_REAL_HOME: string;
		LC_IDENTIFICATION: string;
		LXQT_SESSION_CONFIG: string;
		HOME: string;
		DISPLAY: string;
		GTK_OVERLAY_SCROLLING: string;
		XDG_CURRENT_DESKTOP: string;
		SSH_AGENT_PID: string;
		SNAP_UID: string;
		SNAP_EUID: string;
		DEBUGINFOD_URLS: string;
		TERM: string;
		QT_IM_MODULE: string;
		LC_NUMERIC: string;
		LC_NAME: string;
		MOCHA_COLORS: string;
		QT_QPA_PLATFORMTHEME: string;
		LANG: string;
		SNAP_COOKIE: string;
		CLUTTER_IM_MODULE: string;
		SNAP_VERSION: string;
		SNAP_COMMON: string;
		QT_PLATFORM_PLUGIN: string;
		XDG_CONFIG_HOME: string;
		XDG_RUNTIME_DIR: string;
		SSH_AUTH_SOCK: string;
		GPG_AGENT_INFO: string;
		SNAP_DATA: string;
		SNAP_REEXEC: string;
		DESKTOP_SESSION: string;
		USER: string;
		SNAP_USER_COMMON: string;
		XDG_SESSION_TYPE: string;
		XDG_DATA_HOME: string;
		SNAP_INSTANCE_KEY: string;
		LC_MONETARY: string;
		PWD: string;
		SNAP_CONTEXT: string;
		SNAP: string;
		NODE_ENV: string;
		[key: `PUBLIC_${string}`]: undefined;
		[key: `${string}`]: string | undefined;
	}
}

/**
 * This module provides access to environment variables set _dynamically_ at runtime and that are _publicly_ accessible.
 * 
 * |         | Runtime                                                                    | Build time                                                               |
 * | ------- | -------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
 * | Private | [`$env/dynamic/private`](https://svelte.dev/docs/kit/$env-dynamic-private) | [`$env/static/private`](https://svelte.dev/docs/kit/$env-static-private) |
 * | Public  | [`$env/dynamic/public`](https://svelte.dev/docs/kit/$env-dynamic-public)   | [`$env/static/public`](https://svelte.dev/docs/kit/$env-static-public)   |
 * 
 * Dynamic environment variables are defined by the platform you're running on. For example if you're using [`adapter-node`](https://github.com/sveltejs/kit/tree/main/packages/adapter-node) (or running [`vite preview`](https://svelte.dev/docs/kit/cli)), this is equivalent to `process.env`.
 * 
 * **_Public_ access:**
 * 
 * - This module _can_ be imported into client-side code
 * - **Only** variables that begin with [`config.kit.env.publicPrefix`](https://svelte.dev/docs/kit/configuration#env) (which defaults to `PUBLIC_`) are included
 * 
 * > [!NOTE] In `dev`, `$env/dynamic` includes environment variables from `.env`. In `prod`, this behavior will depend on your adapter.
 * 
 * > [!NOTE] To get correct types, environment variables referenced in your code should be declared (for example in an `.env` file), even if they don't have a value until the app is deployed:
 * >
 * > ```env
 * > MY_FEATURE_FLAG=
 * > ```
 * >
 * > You can override `.env` values from the command line like so:
 * >
 * > ```sh
 * > MY_FEATURE_FLAG="enabled" npm run dev
 * > ```
 * 
 * For example, given the following runtime environment:
 * 
 * ```env
 * ENVIRONMENT=production
 * PUBLIC_BASE_URL=http://example.com
 * ```
 * 
 * With the default `publicPrefix` and `privatePrefix`:
 * 
 * ```ts
 * import { env } from '$env/dynamic/public';
 * console.log(env.ENVIRONMENT); // => undefined, not public
 * console.log(env.PUBLIC_BASE_URL); // => "http://example.com"
 * ```
 * 
 * ```
 * 
 * ```
 */
declare module '$env/dynamic/public' {
	export const env: {
		[key: `PUBLIC_${string}`]: string | undefined;
	}
}
