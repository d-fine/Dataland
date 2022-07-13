import { defineConfig } from "cypress";

export default defineConfig({
  defaultCommandTimeout: 10000,
  viewportHeight: 684,
  viewportWidth: 1536,
  retries: {
    runMode: 2,
    openMode: 1,
  },
  fixturesFolder: "../testing/data",
  e2e: {
    // We've imported your old cypress plugins here.
    // You may want to clean this up later by importing these.
    setupNodeEvents(on, config) {
      return require("./tests/e2e/plugins/index.js")(on, config);
    },
    experimentalSessionAndOrigin: true,
    specPattern: "tests/e2e/specs",
    excludeSpecPattern: process.env.CI ? ["./tests/e2e/runAll.ts"] : [],
    supportFile: "tests/e2e/support/index.ts",
  },
});
