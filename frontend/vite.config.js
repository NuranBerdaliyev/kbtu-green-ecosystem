import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // Dev-time proxy: the browser calls /api/... on :5173,
      // Vite forwards it to Spring Boot on :8080. No CORS config needed.
      // Assumes the backend exposes controllers under /api (agree this in stage 2).
      // If it does not, add: rewrite: (path) => path.replace(/^\/api/, '')
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // WebSocket endpoint used by the Eco Waste module (stage 5).
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
})
