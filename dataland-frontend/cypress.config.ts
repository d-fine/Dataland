import { defineConfig } from "cypress";
import {rmdir} from "fs";
let returnEmail: string;
let returnPassword: string;
let returnTotpKey: string;
export default defineConfig({
  numTestsKeptInMemory: 2,
  defaultCommandTimeout: 10000,
  viewportHeight: 684,
  viewportWidth: 1536,
  video: false,

  retries: {
    runMode: 2,
    openMode: 1,
  },

  fixturesFolder: "../testing/data",
  downloadsFolder: "./tests/e2e/cypress_downloads",

  e2e: {
    baseUrl: "https://local-dev.dataland.com",
    setupNodeEvents(on, config) {
      on("task", {
        setEmail: (val: string) => {
          return (returnEmail = val);
        },
        getEmail: () => {
          return returnEmail;
        },
      });
      on("task", {
        setPassword: (val: string) => {
          return (returnPassword = val);
        },
        getPassword: () => {
          return returnPassword;
        },
      });
      on("task", {
        setTotpKey: (val: string) => {
          return (returnTotpKey = val);
        },
        getTotpKey: () => {
          return returnTotpKey;
        },
      });
      on('task', {
        deleteFolder(folderName) {
          return new Promise((resolve, reject) => {
            rmdir(folderName, { recursive: true }, (err) => {
              if (err) {
                console.error(err)
                return reject(err)
              }
              resolve(null)
            })
          })
        },
      })
      return require("./tests/e2e/plugins/index.js")(on, config);
    },
    experimentalSessionAndOrigin: true,
    supportFile: "tests/e2e/support/index.ts",
  },
  component: {
    devServer: {
      framework: "vue",
      bundler: "vite",
    },
    specPattern: ["tests/component/**/*.cy.ts"],
    supportFile: "tests/component/component.ts",
    indexHtmlFile: "tests/component/component-index.html",
    setupNodeEvents(on, config) {
      require("@cypress/code-coverage/task")(on, config);
      return config;
    },
  },
});
