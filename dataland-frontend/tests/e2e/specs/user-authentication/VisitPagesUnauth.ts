import { DataTypeEnum } from "@clients/backend";

describe("As a user I expect to be redirected to the login page if I am unauthenticated", () => {
  const pages = [
    "/companies",
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
  ];

  pages.forEach((page) => {
    it(`Test Login Redirect for ${page}`, () => {
      cy.visit(page);
      cy.get("input[name=login]").should("exist").url().should("contain", "keycloak");
    });
  });
});
