import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	server: {
		// Prevent the browser from caching the dev HTML shell, which otherwise
		// can serve a stale page (e.g. referencing /src/main.js) after logout/navigation.
		headers: {
			'Cache-Control': 'no-store'
		}
	}
});
