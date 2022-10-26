module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });

  if (process.env.REALDATA === "true") {
    config.env["DATA_ENVIRONMENT"] = "realData";
  } else {
    config.env["DATA_ENVIRONMENT"] = "fakeFixtures";
  }

  switch (process.env.ENVIRONMENT) {
    case "preview":
    case "development":
    case "development_2": {
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
