module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });

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
  return config;
};
