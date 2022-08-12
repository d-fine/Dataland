import Chainable = Cypress.Chainable;

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<string>> {
  return cy
    .getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
    .then((token) => {
      return cy.request({
        url: `/api/${endpoint}`,
        method: "GET",
        headers: { Authorization: "Bearer " + token },
      });
    })
    .then((response) => {
      return response.body.map((e: any) => e[idKey]);
    });
}

export function retrieveDataIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("dataId", "metadata");
}

export function retrieveCompanyIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("companyId", "companies");
}
