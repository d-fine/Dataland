import { defineConfig } from "cypress";
let returnemail: string
let returnpassword: string
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
      on('task', {
        setEmail: (val:string) => {
          return (returnemail = val)
        },
        getEmail: () => {
          return returnemail
        },
      }),
      on('task', {
        setPassword: (val:string) => {
          return (returnpassword = val)
        },
        getPassword: () => {
          return returnpassword
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
