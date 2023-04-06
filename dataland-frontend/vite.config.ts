import { defineConfig } from "vite";
import path from "path";
import vue from "@vitejs/plugin-vue";
import istanbul from "vite-plugin-istanbul";

export default defineConfig({
  //This section is to prevent the vite cold start issue https://github.com/cypress-io/cypress/issues/22557
  optimizeDeps: {
    include: [
      "@clients/backend",
      "@clients/apikeymanager",
      "@vue/test-utils",
      "cypress/vue",
      "vue-router",
      "keycloak-js",
      "vue",
      "primevue/button",
      "primevue/card",
      "primevue/message",
      "primevue/datatable",
      "primevue/column",
      "primevue/tooltip",
      "primevue/autocomplete",
      "primevue/menu",
      "primevue/progressbar",
      "primevue/tabview",
      "primevue/tabpanel",
      "primevue/inputtext",
      "primevue/dynamicdialog",
      "primevue/dialog",
      "primevue/textarea",
      "primevue/dropdown",
      "primevue/calendar",
      "primevue/multiselect",
      "primevue/overlaypanel",
      "primevue/fileupload",
      "@formkit/vue",
      "axios",
      "i18n-iso-countries",
    ],
  },
  plugins: [
    vue(),
    istanbul({
      include: "src/*",
      exclude: ["node_modules", "tests"],
      extension: [".js", ".ts", ".vue"],
      requireEnv: true,
      forceBuildInstrument: process.env.BUILD_INSTRUMENTED === "true",
    }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@clients": path.resolve(__dirname, "./build/clients"),
      "@ct": path.resolve(__dirname, "./tests/component"),
      "@sharedUtils": path.resolve(__dirname, "./tests/sharedUtils"),
    },
  },
  build: {
    sourcemap: true,
  },
  server: {
    port: 8090,
    host: "0.0.0.0",
    strictPort: true,
    watch: {
      ignored: ["**/coverage/**"],
      usePolling: process.env.POLLING === "true",
    },
  },
});
