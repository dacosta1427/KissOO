import adapter from '@sveltejs/adapter-static';
import { resolve } from 'path';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		adapter: adapter({
			fallback: 'index.html'
		}),
		alias: {
			$lib: resolve('./src/lib'),
			$components: resolve('./src/components'),
			$services: resolve('./src/services'),
			$stores: resolve('./src/stores')
		}
	},
	compilerOptions: {
		experimental: {
			async: true
		}
	}
};

export default config;
