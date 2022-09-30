import { ActuatorApi } from "../../../../build/clients/backend";

describe("As a developer, I want to ensure that the deployment is okay", async () => {
  it("retrieve health info and check that its up", async () => {
    const health = await new ActuatorApi().health();
    expect(JSON.parse(JSON.stringify(health.data)).status).to.equal("UP");
    // new ActuatorApi().health().then((health) => expect(JSON.parse(JSON.stringify(health.data)).status).to.equal("UP"));
  });

  it("retrieve health info and check that its up", async () => {
    const health = await new ActuatorApi().health();
    expect(JSON.parse(JSON.stringify(health.data)).status).not.to.equal("UP");
    // new ActuatorApi().health().then((health) => expect(JSON.parse(JSON.stringify(health.data)).status).to.equal("UP"));
  });

  it("retrieve info endpoint and check commit", async () => {
    const info = await new ActuatorApi().info();
    expect(JSON.parse(JSON.stringify(info.data)).git.commit.id.full).to.equal(Cypress.env("commit_id"));
  });
});
