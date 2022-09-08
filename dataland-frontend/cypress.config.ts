import { defineConfig } from "cypress";

export default defineConfig({
  numTestsKeptInMemory: 2,
  defaultCommandTimeout: 10000,
  viewportHeight: 684,
  viewportWidth: 1536,

  retries: {
    runMode: 2,
    openMode: 1,
  },

  fixturesFolder: "../testing/data",

  e2e: {
    baseUrl: "https://dataland-local.duckdns.org",
    setupNodeEvents(on, config) {
      return require("./cypress/e2e/plugins/index.js")(on, config);
    },
    experimentalSessionAndOrigin: true,
    supportFile: "cypress/e2e/support/index.ts",
  },

  component: {
    devServer: {
      framework: "vue",
      bundler: "vite",
    },
  },
});
