/**
 * As a developer, I want to ensure that these tests run against the latest version of the backend
 * and that the backend is up for the tests
 */
describe("Public infrastructure health checks", () => {
  require("./VerifyDeployment");
  require("./VerifyHeaders");
  require("./VisitNotExistingPath");
  require("./Test500Response");
});
