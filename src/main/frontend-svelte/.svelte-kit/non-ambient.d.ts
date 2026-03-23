
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
		RouteId(): "/" | "/benchmark" | "/controls" | "/crud" | "/export" | "/file-upload" | "/login" | "/ollama" | "/report" | "/rest-services" | "/signup" | "/sql-access" | "/users";
		RouteParams(): {
			
		};
		LayoutParams(): {
			"/": Record<string, never>;
			"/benchmark": Record<string, never>;
			"/controls": Record<string, never>;
			"/crud": Record<string, never>;
			"/export": Record<string, never>;
			"/file-upload": Record<string, never>;
			"/login": Record<string, never>;
			"/ollama": Record<string, never>;
			"/report": Record<string, never>;
			"/rest-services": Record<string, never>;
			"/signup": Record<string, never>;
			"/sql-access": Record<string, never>;
			"/users": Record<string, never>
		};
		Pathname(): "/" | "/benchmark" | "/controls" | "/crud" | "/export" | "/file-upload" | "/login" | "/ollama" | "/report" | "/rest-services" | "/signup" | "/sql-access" | "/users";
		ResolvedPathname(): `${"" | `/${string}`}${ReturnType<AppTypes['Pathname']>}`;
		Asset(): string & {};
	}
}