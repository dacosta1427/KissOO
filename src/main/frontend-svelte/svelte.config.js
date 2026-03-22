import adapter from '@sveltejs/adapter-auto';
import { resolve } from 'path';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		experimental: {
			remoteFunctions: true
		},
		adapter: adapter(),
		alias: {
			$lib: resolve('./src/lib'),
			$remote: resolve('./src/lib/remote'),
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
