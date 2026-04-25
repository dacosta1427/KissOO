
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
		RouteId(): "/" | "/api" | "/benchmark" | "/bookings" | "/bookings/[bookingId]" | "/cleaners" | "/cleaners/[cleanerId]" | "/controls" | "/cost-profiles" | "/crud" | "/export" | "/file-upload" | "/houses" | "/houses/[houseId]" | "/login" | "/ollama" | "/owners" | "/owners/[id]" | "/report" | "/rest-services" | "/schedules" | "/schedules/[scheduleId]" | "/signup" | "/sql-access" | "/users" | "/verify-email";
		RouteParams(): {
			"/bookings/[bookingId]": { bookingId: string };
			"/cleaners/[cleanerId]": { cleanerId: string };
			"/houses/[houseId]": { houseId: string };
			"/owners/[id]": { id: string };
			"/schedules/[scheduleId]": { scheduleId: string }
		};
		LayoutParams(): {
			"/": { bookingId?: string; cleanerId?: string; houseId?: string; id?: string; scheduleId?: string };
			"/api": Record<string, never>;
			"/benchmark": Record<string, never>;
			"/bookings": { bookingId?: string };
			"/bookings/[bookingId]": { bookingId: string };
			"/cleaners": { cleanerId?: string };
			"/cleaners/[cleanerId]": { cleanerId: string };
			"/controls": Record<string, never>;
			"/cost-profiles": Record<string, never>;
			"/crud": Record<string, never>;
			"/export": Record<string, never>;
			"/file-upload": Record<string, never>;
			"/houses": { houseId?: string };
			"/houses/[houseId]": { houseId: string };
			"/login": Record<string, never>;
			"/ollama": Record<string, never>;
			"/owners": { id?: string };
			"/owners/[id]": { id: string };
			"/report": Record<string, never>;
			"/rest-services": Record<string, never>;
			"/schedules": { scheduleId?: string };
			"/schedules/[scheduleId]": { scheduleId: string };
			"/signup": Record<string, never>;
			"/sql-access": Record<string, never>;
			"/users": Record<string, never>;
			"/verify-email": Record<string, never>
		};
		Pathname(): "/" | "/benchmark" | "/bookings" | `/bookings/${string}` & {} | "/cleaners" | `/cleaners/${string}` & {} | "/controls" | "/cost-profiles" | "/crud" | "/export" | "/file-upload" | "/houses" | `/houses/${string}` & {} | "/login" | "/ollama" | "/owners" | `/owners/${string}` & {} | "/report" | "/rest-services" | "/schedules" | `/schedules/${string}` & {} | "/signup" | "/sql-access" | "/users" | "/verify-email";
		ResolvedPathname(): `${"" | `/${string}`}${ReturnType<AppTypes['Pathname']>}`;
		Asset(): string & {};
	}
}