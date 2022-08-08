import { defineConfig } from "cypress";

export default defineConfig({
  numTestsKeptInMemory: 0,
  defaultCommandTimeout: 10000,
  viewportHeight: 684,
  viewportWidth: 1536,
  retries: {
    runMode: 2,
    openMode: 1,
  },
  fixturesFolder: "../testing/data",
  e2e: {
    setupNodeEvents(on, config) {
      return require("./tests/e2e/plugins/index.js")(on, config);
    },
    experimentalSessionAndOrigin: true,
    supportFile: "tests/e2e/support/index.ts",
  },
});
