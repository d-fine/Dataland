import { defineConfig } from "vite";
import path from "path";
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
      forceBuildInstrument: true,
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
    },
  },
});
