module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });
  switch (process.env.ENVIRONMENT) {
    case "development": {
      config.specPattern = [
        "tests/e2e/specs/verify_deployment",
        "tests/e2e/specs/high",
        "tests/e2e/specs/low",
        "tests/e2e/specs/medium",
        "tests/e2e/specs/dev",
        "tests/e2e/specs/previsit",
      ];
      break;
    }
    case "preview": {
      config.specPattern = [
        "tests/e2e/specs/verify_deployment",
        "tests/e2e/specs/high",
        "tests/e2e/specs/low",
        "tests/e2e/specs/medium",
        "tests/e2e/specs/previsit",
      ];
      break;
    }
    default: {
      config.specPattern = ["tests/e2e/specs"];
      break;
    }
  }
  return config;
};
