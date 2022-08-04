module.exports = (on, config) => {
  require("@cypress/code-coverage/task")(on, config);
  config.fixturesFolder = "../testing/data";
  config.supportFile = "tests/e2e/support/index.ts";
  config.env.commit_id = require("git-commit-id")({ cwd: "../" });
  switch (process.env.REAL_DATA) {
    case "false": {
      console.log(`Processing runAll.ts - REAL_DATA=${process.env.REAL_DATA}`);
      config.specPattern = ["tests/e2e/specs/runAll.ts"];
      break;
    }
    case "true": {
      console.log(`Processing test for scope real data - REAL_DATA=${process.env.REAL_DATA}`);
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
      console.log(`Defaulting to all specs - REAL_DATA=${process.env.REAL_DATA}`);
      config.specPattern = ["tests/e2e/specs"];
      break;
    }
  }
  return config;
};
