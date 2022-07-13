module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });
  return config;
};
