
// this file is generated — do not edit it


declare module "svelte/elements" {
	export interface HTMLAttributes<T> {
		'data-sveltekit-keepfocus'?: true | '' | 'off' | undefined | null;
		'data-sveltekit-noscroll'?: true | '' | 'off' | undefined | null;
		'data-sveltekit-preload-code'?:
			| true
			| ''
			| 'eager'
			| 'viewport'
			| 'hover'
			| 'tap'
			| 'off'
			| undefined
			| null;
		'data-sveltekit-preload-data'?: true | '' | 'hover' | 'tap' | 'off' | undefined | null;
		'data-sveltekit-reload'?: true | '' | 'off' | undefined | null;
		'data-sveltekit-replacestate'?: true | '' | 'off' | undefined | null;
	}
}

export {};


declare module "$app/types" {
	type MatcherParam<M> = M extends (param : string) => param is (infer U extends string) ? U : string;

	export interface AppTypes {
		RouteId(): "/" | "/api" | "/benchmark" | "/bookings" | "/cleaners" | "/controls" | "/cost-profiles" | "/crud" | "/export" | "/file-upload" | "/houses" | "/login" | "/ollama" | "/owners" | "/report" | "/rest-services" | "/schedules" | "/signup" | "/sql-access" | "/users" | "/verify-email";
		RouteParams(): {
			
		};
		LayoutParams(): {
			"/": Record<string, never>;
			"/api": Record<string, never>;
			"/benchmark": Record<string, never>;
			"/bookings": Record<string, never>;
			"/cleaners": Record<string, never>;
			"/controls": Record<string, never>;
			"/cost-profiles": Record<string, never>;
			"/crud": Record<string, never>;
			"/export": Record<string, never>;
			"/file-upload": Record<string, never>;
			"/houses": Record<string, never>;
			"/login": Record<string, never>;
			"/ollama": Record<string, never>;
			"/owners": Record<string, never>;
			"/report": Record<string, never>;
			"/rest-services": Record<string, never>;
			"/schedules": Record<string, never>;
			"/signup": Record<string, never>;
			"/sql-access": Record<string, never>;
			"/users": Record<string, never>;
			"/verify-email": Record<string, never>
		};
		Pathname(): "/" | "/benchmark" | "/bookings" | "/cleaners" | "/controls" | "/cost-profiles" | "/crud" | "/export" | "/file-upload" | "/houses" | "/login" | "/ollama" | "/owners" | "/report" | "/rest-services" | "/schedules" | "/signup" | "/sql-access" | "/users" | "/verify-email";
		ResolvedPathname(): `${"" | `/${string}`}${ReturnType<AppTypes['Pathname']>}`;
		Asset(): string & {};
	}
}