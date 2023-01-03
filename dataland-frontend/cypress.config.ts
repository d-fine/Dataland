import {defineConfig} from "cypress";
import {rmdir} from "fs";

let returnEmail: string;
let returnPassword: string;
let returnTotpKey: string;
export default defineConfig({

    env: {
        commit_id: require("git-commit-id")({cwd: "../"})
    },

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

            if (process.env.REALDATA === "true") {
                config.env["DATA_ENVIRONMENT"] = "realData";
            } else {
                config.env["DATA_ENVIRONMENT"] = "fakeFixtures";
            }

            if (config.env["EXECUTION_ENVIRONMENT"] !== "developmentLocal") {
                console.log("Detected preview / development CI environment. Only loading index.ts to run all tests");
                config.specPattern = ["tests/e2e/specs/index.ts"];
            } else {
                console.log("Detected local development run. Loading all spec files to allow the user to pick the tests to run");
                config.specPattern = ["tests/e2e/specs"];
            }

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
                        rmdir(folderName, {recursive: true}, (err) => {
                            if (err) {
                                console.error(err)
                                return reject(err)
                            }
                            resolve(null)
                        })
                    })
                },
            })
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
