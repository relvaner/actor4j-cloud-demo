import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'

// production build is served by the Vert.x frontend under /demo/ (see nginx_gateway)
export default defineConfig(({ mode }) => ({
	base: mode === 'production' ? '/demo/' : '/',
	plugins: [
		vue(),
		vuetify({ autoImport: true, styles: { configFile: 'src/styles/settings.scss' } }),
	],
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url))
		}
	},
	build: {
		outDir: 'dist'
	},
	optimizeDeps: {
		include: [
			'vuetify/components/VOverlay',
			'vuetify/components/VDialog',
			'vuetify/components/VMenu',
			'vuetify/components/VSelect',
		],
	},
	server: {
		proxy: {
			'/backend': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				rewrite: path => path.replace(/^\/backend/, '/')
			}
		}
	}
}))
