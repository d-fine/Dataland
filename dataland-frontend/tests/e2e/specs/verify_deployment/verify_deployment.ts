describe('Check that the Health Endpoint returns and states "up"', () => {
    it('retrieve health info and check that its up', function () {
        cy.request('GET', `${Cypress.env("API")}/actuator/health`).then( healthInfo => {return healthInfo.body.status}).should("equal", "UP")
    });
});

describe('Check that the correct version is deployed', () => {
    it('retrieve info endpoint and check commit', function () {
        cy.request('GET', `${Cypress.env("API")}/actuator/info`).then( info => {return info.body.git.commit.id.full}).should("equal", Cypress.env('commit_id'))
    });
});
