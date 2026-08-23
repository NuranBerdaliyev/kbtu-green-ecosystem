import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// Spring Boot runs on 65535 — see backend/green/src/main/resources/application-dev.yaml
const BACKEND = 'http://localhost:65535'

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
      // Most controllers sit under /api, but AuthController is mapped to
      // /auth and ProfileController to /profiles — no prefix. All three
      // are proxied so the browser never issues a cross-origin request.
      // (The backend has no CORS config, so the proxy is not optional.)
      '/api': { target: BACKEND, changeOrigin: true },
      '/auth': { target: BACKEND, changeOrigin: true },
      '/profiles': { target: BACKEND, changeOrigin: true },
      // SockJS endpoint from WebSocketConfig.registerStompEndpoints
      '/ws-green': { target: BACKEND, changeOrigin: true, ws: true },
    },
  },
})
