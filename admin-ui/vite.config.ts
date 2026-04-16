import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  build: {
    chunkSizeWarningLimit: 900,
    rollupOptions: {
      output: {
        manualChunks(id) {
          const normalizedId = id.split('\\').join('/');

          if (!normalizedId.includes('node_modules')) {
            return;
          }

          if (
            normalizedId.includes('node_modules/ant-design-vue/') ||
            normalizedId.includes('node_modules/@ant-design/') ||
            normalizedId.includes('node_modules/@ant-design/icons-vue/') ||
            normalizedId.includes('node_modules/@ant-design/icons-svg/') ||
            normalizedId.includes('node_modules/@ctrl/tinycolor/') ||
            normalizedId.includes('node_modules/async-validator/') ||
            normalizedId.includes('node_modules/dayjs/')
          ) {
            return 'ant-design';
          }

          if (
            normalizedId.includes('node_modules/vue/') ||
            normalizedId.includes('node_modules/@vue/') ||
            normalizedId.includes('node_modules/vue-router/') ||
            normalizedId.includes('node_modules/pinia/')
          ) {
            return 'vue-core';
          }

          if (
            normalizedId.includes('node_modules/axios/') ||
            normalizedId.includes('node_modules/follow-redirects/') ||
            normalizedId.includes('node_modules/form-data/')
          ) {
            return 'http-client';
          }
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
