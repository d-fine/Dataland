import { defineConfig } from "vite";
import path from "path";
import vue from "@vitejs/plugin-vue";
import istanbul from "vite-plugin-istanbul";

export default defineConfig({
  //This section is to prevent the vite cold start issue https://github.com/cypress-io/cypress/issues/22557
  optimizeDeps: {
    include: [
      "@vue/test-utils",
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
      "@formkit/vue",
      "axios",
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
      usePolling: true,
    },
  },
});
