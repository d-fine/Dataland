module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });
  if (config.isTextTerminal) {
    config.excludeSpecPattern = ["tests/e2e/specs/runAll.ts"];
  }
  return config;
};
