module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });
  config.specPattern = ["tests/e2e/specs"];
  if (process.env.REALDATA) {
    config.env["DATA_ENVIRONMENT"] = "realData";
  }

  switch (process.env.ENVIRONMENT) {
    case "preview":
    case "development": {
      console.log("Detected preview / development CI environment. Only loading index.ts to run all tests");
      config.specPattern = ["tests/e2e/specs/index.ts"];
      break;
    }
    default: {
      console.log("Detected local development run. Loading all spec files to allow the user to pick the tests to run");
      config.specPattern = ["tests/e2e/specs"];
      break;
    }
  }
  return config;
};
