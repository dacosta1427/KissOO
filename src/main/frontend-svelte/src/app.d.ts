// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

// Import type definitions from kiss-client
export type {
	KissResponse,
	KissCallOptions,
	Cleaner,
	Booking,
	Schedule,
	House
} from '$lib/kiss-client';

export {};
