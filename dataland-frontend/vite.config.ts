import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import istanbul from "vite-plugin-istanbul";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    istanbul({
      include: "src/*",
      exclude: ["node_modules", "test/"],
      extension: [".js", ".ts", ".vue"],
      requireEnv: true,
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
      "@clients": fileURLToPath(new URL("./build/clients", import.meta.url)),
    },
  },
  build: {
    sourcemap: true,
  },
});
