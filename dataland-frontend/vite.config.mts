import { defineConfig } from 'vite';
import path from 'path';
import vue from '@vitejs/plugin-vue';
import istanbul from 'vite-plugin-istanbul';

export default defineConfig({
  //This section is to prevent the vite cold start issue https://github.com/cypress-io/cypress/issues/22557
  optimizeDeps: {
    include: [
      '@faker-js/faker',
      '@formkit/vue',
      '@vue/test-utils',
      'axios',
      'currency-codes/data',
      'cypress/vue',
      'dompurify',
      'i18n-iso-countries',
      'keycloak-js',
      'markdown-it',
      'pinia',
      'pinia-shared-state',
      'primevue/accordion',
      'primevue/accordiontab',
      'primevue/autocomplete',
      'primevue/badge',
      'primevue/button',
      'primevue/card',
      'primevue/checkbox',
      'primevue/column',
      'primevue/columngroup',
      'primevue/config',
      'primevue/datatable',
      'primevue/datepicker',
      'primevue/dialog',
      'primevue/dialogservice',
      'primevue/dynamicdialog',
      'primevue/fileupload',
      'primevue/inputtext',
      'primevue/menu',
      'primevue/message',
      'primevue/multiselect',
      'primevue/popover',
      'primevue/progressbar',
      'primevue/radiobutton',
      'primevue/row',
      'primevue/select',
      'primevue/tabpanel',
      'primevue/tabview',
      'primevue/textarea',
      'primevue/toggleswitch',
      'primevue/tooltip',
      'primevue/tree',
      'primevue/usedialog',
      'vue',
      'vue-markdown-render',
      'vue-router',
    ],
  },
  plugins: [
    vue(),
    istanbul({
      include: 'src/*',
      exclude: ['node_modules', 'tests'],
      extension: ['.js', '.ts', '.vue'],
      requireEnv: true,
      forceBuildInstrument: process.env.BUILD_INSTRUMENTED === 'true',
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@clients': path.resolve(__dirname, './build/clients'),
      '@ct': path.resolve(__dirname, './tests/component'),
      '@sharedUtils': path.resolve(__dirname, './tests/sharedUtils'),
    },
  },
  build: {
    sourcemap: true,
  },
  server: {
    port: 8090,
    host: '0.0.0.0',
    strictPort: true,
    allowedHosts: true,
    warmup: {
      clientFiles: [
        './src/components/*/*.vue',
        './src/components/*/*/*.vue',
        './src/assets/*/*.scss',
        './src/assets/*/*/*.scss',
      ],
    },
    watch: {
      ignored: ['**/coverage/**'],
      usePolling: process.env.POLLING === 'true',
    },
  },
});
