import { defineConfig } from 'vite';
import type { Plugin } from 'vite';
import path from 'path';
import fs from 'fs';
import vue from '@vitejs/plugin-vue';
import istanbul from 'vite-plugin-istanbul';

/**
 * Vite plugin to serve Astro pre-rendered pages from public/astro/.
 * Maps clean URLs like /about to /astro/about/index.html,
 * and / to /astro/index.html.
 */
function astroStaticPages(): Plugin {
  const astroRoutes = [
    '/',
    '/about',
    '/dataland-community',
    '/product',
    '/imprint',
    '/legal',
    '/dataprivacy',
    '/testimonials',
    '/partner-stories',
    '/newsletter',
    '/success-stories-meag',
    '/success-stories-nordlb',
    '/success-stories-ovb',
  ];
  return {
    name: 'astro-static-pages',
    configureServer(server): void {
      server.middlewares.use((req, res, next) => {
        const url = req.url?.split('?')[0];
        if (url && astroRoutes.includes(url)) {
          const filePath =
            url === '/' ? path.resolve('public/astro-index.html') : path.resolve(`public${url}/index.html`);
          if (fs.existsSync(filePath)) {
            res.setHeader('Content-Type', 'text/html');
            res.setHeader('Cache-Control', 'no-cache, no-store, max-age=0, must-revalidate');
            fs.createReadStream(filePath).pipe(res);
            return;
          }
        }
        next();
      });
    },
  };
}

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
    astroStaticPages(),
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
    preserveSymlinks: true,
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@clients': path.resolve(__dirname, './build/clients'),
      '@ct': path.resolve(__dirname, './tests/component'),
      '@sharedUtils': path.resolve(__dirname, './tests/sharedUtils'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `$bp-sm: 640px;\n$bp-md: 768px;\n$bp-lg: 1024px;\n$bp-xl: 1440px;\n`,
      },
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
    fs: {
      allow: [
        // Allow serving files from the shared-elements symlink target
        path.resolve(__dirname, '../dataland-sharedElements'),
        // Default: project root
        path.resolve(__dirname),
      ],
    },
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
