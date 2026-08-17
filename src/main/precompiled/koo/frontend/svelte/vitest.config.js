import { defineConfig } from 'vitest/config';
import { sveltekit } from '@sveltejs/kit/vite';

export default defineConfig({
	plugins: [sveltekit()],
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}'],
		globals: true,
		environment: 'jsdom',
		setupFiles: ['./src/test-setup.ts'],
		coverage: {
			reporter: ['text', 'json', 'html'],
			exclude: ['node_modules/', 'src/test-setup.ts']
		}
	}
});
