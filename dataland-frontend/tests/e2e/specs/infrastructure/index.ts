/**
 * As a developer, I want to ensure that these tests run against the latest version of the backend
 * and that the backend is up for the tests
 */
describe("Backend Health Checks", () => {
  require("./VerifyDeployment");
  require("./VerifyHeaders");
  require("./VisitNotExistingPath");
});
