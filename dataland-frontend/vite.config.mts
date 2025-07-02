import { defineConfig } from 'vite';
import path from 'path';
import vue from '@vitejs/plugin-vue';
import istanbul from 'vite-plugin-istanbul';

export default defineConfig({
  //This section is to prevent the vite cold start issue https://github.com/cypress-io/cypress/issues/22557
  optimizeDeps: {
    include: [
      '@vue/test-utils',
      'cypress/vue',
      'vue-router',
      'keycloak-js',
      'vue',
      'primevue/button',
      'primevue/card',
      'primevue/message',
      'primevue/datatable',
      'primevue/column',
      'primevue/tooltip',
      'primevue/autocomplete',
      'primevue/menu',
      'primevue/progressbar',
      'primevue/tabview',
      'primevue/tab',
      'primevue/tablist',
      'primevue/tabpanels',
      'primevue/tabpanel',
      'primevue/tabs',
      'primevue/inputtext',
      'primevue/inputswitch',
      'primevue/dynamicdialog',
      'primevue/usedialog',
      'primevue/dialog',
      'primevue/textarea',
      'primevue/dropdown',
      'primevue/calendar',
      'primevue/multiselect',
      'primevue/overlaypanel',
      'primevue/fileupload',
      'primevue/tree',
      'primevue/checkbox',
      'primevue/row',
      'primevue/columngroup',
      'primevue/radiobutton',
      '@primevue/icons/chevrondown',
      '@primevue/icons/chevronleft',
      'primevue/config',
      'primevue/dialogservice',
      'pinia',
      'pinia-shared-state',
      '@formkit/vue',
      'axios',
      'i18n-iso-countries',
      'currency-codes/data',
      'dompurify',
      'vue-markdown-render',
      'markdown-it',
      '@faker-js/faker',
      '@primevue/icons/chevronup',
      'primevue/accordion',
      'primevue/accordiontab',
      'primevue/badge',
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
    proxy: {
      '^/(api|documents|qa|community|users|specifications)': {
        target: 'https://dev2.dataland.com',
        changeOrigin: true,
        headers: {
          authorization: 'Bearer MTM2YTkzOTQtNDg3My00YTYxLWEyNWItNjViMWU4ZTdjYzJm_d3a39f57023758d116a5aa5b6eae3322cd4fe144c0aef85bc60c989cb3686ddcab30a9ee3a0be72d_1862309878',
        },
      },
    },
  },
});
