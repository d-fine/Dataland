import Chainable = Cypress.Chainable;

export function performSimpleGet(endpoint: string): Chainable<any> {
  return cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD")).then((token) => {
    return cy.request({
      url: `/api/${endpoint}`,
      method: "GET",
      headers: { Authorization: "Bearer " + token },
    });
  });
}

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<string>> {
  return performSimpleGet(endpoint).then((response) => {
    return response.body.map((e: any) => e[idKey]);
  });
}

export function retrieveDataIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("dataId", "metadata");
}

export function retrieveCompanyIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("companyId", "companies");
}

export function retrieveFirstCompanyIdWithFrameworkData(framework: string): Chainable<string> {
  return cy
    .getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
    .then((token) => {
      return cy.request({
        url: `/api/companies`,
        method: "GET",
        headers: { Authorization: "Bearer " + token },
      });
    })
    .then((response) => {
      console.log(response);
      return response.body.find((it: any) => it.dataRegisteredByDataland.some((d: any) => d.dataType === framework))
        .companyId;
    });
}
