import {defineConfig} from "cypress";
import {promises, rmdir} from "fs";

let returnEmail: string;
let returnPassword: string;
let returnTotpKey: string;

function getDataEnvironmentBasedOnOperatingSystemEnv() {
    if (process.env.REALDATA === "true") {
        return "realData"
    } else {
        return "fakeFixtures"
    }
}


export default defineConfig({
    env: {
        commit_id: require("git-commit-id")({cwd: "../"}),
        prepopulate_timeout_s: 180,
        short_timeout_in_ms: 10000,
        medium_timeout_in_ms: 30000,
        long_timeout_in_ms: 120000,
        AWAIT_PREPOPULATION_RETRIES: 250,
        EXECUTION_ENVIRONMENT: "developmentLocal",
        DATA_ENVIRONMENT: getDataEnvironmentBasedOnOperatingSystemEnv(),
        KEYCLOAK_UPLOADER_PASSWORD: process.env.KEYCLOAK_UPLOADER_PASSWORD,
        KEYCLOAK_READER_PASSWORD: process.env.KEYCLOAK_READER_PASSWORD,
        KEYCLOAK_DATALAND_ADMIN_PASSWORD: process.env.KEYCLOAK_DATALAND_ADMIN_PASSWORD,
        KEYCLOAK_ADMIN_PASSWORD: process.env.KEYCLOAK_ADMIN_PASSWORD,
        KEYCLOAK_ADMIN: process.env.KEYCLOAK_ADMIN,
        PGADMIN_PASSWORD: process.env.PGADMIN_PASSWORD,
        RABBITMQ_PASS: process.env.RABBITMQ_PASS,
        RABBITMQ_USER: process.env.RABBITMQ_USER
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
    watchForFileChanges: false,

    fixturesFolder: "../testing/data",
    downloadsFolder: "./tests/e2e/cypress_downloads",

    e2e: {
        baseUrl: "https://local-dev.dataland.com",
        setupNodeEvents(on, config) {
            const executionEnvironment = config.env["EXECUTION_ENVIRONMENT"];
            const dataEnvironment = config.env["DATA_ENVIRONMENT"];

            console.log(`Execution environment: ${executionEnvironment}; dataEnvironment: ${dataEnvironment}`);
            if (executionEnvironment === "developmentLocal") {
                console.log("Detected local development run. Loading all spec files to allow the user to pick the tests to run");
                config.specPattern = ["tests/e2e/specs"];
                config.defaultCommandTimeout = 22000
            } else {
                console.log("Detected preview / development CI environment. Only loading index.ts to run all tests");
                config.specPattern = ["tests/e2e/specs/index.ts"];
            }
            require("@cypress/code-coverage/task")(on, config);

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
                                console.error(err);
                                return reject(err);
                            }
                            resolve(null);
                        });
                    });
                },
            });
            on('task', {
                async readdir(path: string) {
                    return await promises.readdir(path);
                },
            });
            return config
        },
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
