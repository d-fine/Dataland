import { reader_name, reader_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { DataTypeEnum } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateCompanyInformation } from "@e2e/fixtures/CompanyFixtures";

describeIf(
  "Check if each page is visitable if and only if the corresponding role is given",
  {
    executionEnvironments: ["developmentLocal", "ci"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    let allPages = [] as string[];
    let uploaderPages = [] as string[];
    let companyId: string;
    const noUploaderRightsMessageSelector = "h1:contains('no uploader status')";

    before(() => {
      getKeycloakToken(uploader_name, uploader_pw).then(async (token) => {
        const storedCompany = await uploadCompanyViaApi(token, generateCompanyInformation());
        companyId = storedCompany.companyId;
        allPages = [
          "",
          `samples/${DataTypeEnum.EutaxonomyNonFinancials}`,
          "companies",
          "datasets",
          "requests",
          "/api-key",
          "/dataprivacy",
          "/imprint",
          "/nocontent",
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.Lksg}`,
        ];
        uploaderPages = [
          "/companies/choose",
          `/companies/${companyId}/frameworks/upload`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.Lksg}/upload`,
        ];
      });
    });

    it("Check if a non uploader user can access only the corresponding pages", () => {
      cy.ensureLoggedIn(reader_name, reader_pw);
      allPages.forEach((page) => {
        it(`Non uploader should be able to access ${page}`, () => {
          cy.visit(page);
          cy.get(noUploaderRightsMessageSelector, { timeout: Cypress.env("long_timeout_in_ms") as number }).should(
            "not.exist"
          );
        });
      });
      uploaderPages.forEach((page) => {
        cy.visit(page);
        cy.get(noUploaderRightsMessageSelector, { timeout: Cypress.env("long_timeout_in_ms") as number }).should(
          "exist"
        );
      });
    });

    it("Check if an uploader user can access the corresponding pages", () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      allPages.forEach((page) => {
        cy.visit(page);
        cy.get(noUploaderRightsMessageSelector, { timeout: Cypress.env("long_timeout_in_ms") as number }).should(
          "not.exist"
        );
      });
      uploaderPages.forEach((page) => {
        cy.visit(page);
        cy.get(noUploaderRightsMessageSelector, { timeout: Cypress.env("long_timeout_in_ms") as number }).should(
          "not.exist"
        );
      });
    });
  }
);
