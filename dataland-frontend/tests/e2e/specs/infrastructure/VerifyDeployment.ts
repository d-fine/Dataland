import { ActuatorApi } from '@clients/backend';

interface HealthResponse {
  status: string;
}

describe('As a developer, I want to ensure that the deployment is okay', () => {
  it('retrieve health info and check that its up', () => {
    cy.browserThen(new ActuatorApi().health()).then((healthResponse) => {
      const data = healthResponse.data as HealthResponse;
      expect(data.status).to.equal('UP');
    });
  });

  it('retrieve info endpoint and check commit', () => {
    const commitId = (Cypress.config('expose') as Record<string, unknown>).commit_id as string;
    cy.request(`${getBaseUrl()}/gitinfo`)
      .should('have.a.property', 'body')
      .should('have.a.property', 'commit')
      .should('eq', commitId);
  });
});
