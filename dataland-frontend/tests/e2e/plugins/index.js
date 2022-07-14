module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });
  if (config.isTextTerminal) {
    config.excludeSpecPattern = ["tests/e2e/specs/runAll.ts"];
  }
  switch (process.env.NODE_ENV) {
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
    case "production": {
      config.specPattern = ["tests/e2e/specs"];
      break;
    }
    case "local": {
      config.specPattern = ["tests/e2e/specs"];
      break;
    }
    default: {
      config.specPattern = ["tests/e2e/specs"];
      break;
    }
  }
  return config;
};
