import adapter from '@sveltejs/adapter-auto';
import { resolve } from 'path';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		adapter: adapter(),
		alias: {
			$lib: resolve('./src/lib'),
			$components: resolve('./src/components'),
			$services: resolve('./src/services'),
			$stores: resolve('./src/stores')
		}
	}
};

export default config;
