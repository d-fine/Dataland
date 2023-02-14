import { reader_name, reader_pw, uploader_name, uploader_pw } from "../../utils/Cypress";
import { DataTypeEnum } from "../../../../build/clients/backend";
import { describeIf } from "../../support/TestUtility";
import { uploadCompanyViaApi } from "../../utils/CompanyUpload";
import { getKeycloakToken } from "../../utils/Auth";
import { generateCompanyInformation } from "../../fixtures/CompanyFixtures";

describe("Check if each page is visitable if and only if the corresponding role is given", () => {
  describeIf(
    "",
    {
      executionEnvironments: ["developmentLocal", "ci"],
      dataEnvironments: ["fakeFixtures"],
    },
    () => {
      let allPages = [] as string[];
      let uploaderPages = [] as string[];
      const noUploaderRightsMessageSelector = "h1:contains('no uploader status')";

      before(() => {
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          void uploadCompanyViaApi(token, generateCompanyInformation()).then((company) => {
            allPages = [
              "/",
              `/samples/${DataTypeEnum.EutaxonomyNonFinancials}`,
              "/companies",
              "/datasets",
              "/companies-only-search",
              "/requests",
              "/api-key",
              "/dataprivacy",
              "/imprint",
              "/nocontent",
              `/companies/${company.companyId}`,
              `/companies/${company.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
              `/companies/${company.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
              `/companies/${company.companyId}/frameworks/${DataTypeEnum.Lksg}`,
            ];
            uploaderPages = [
              "/companies/upload",
              "/companies/choose",
              `/companies/${company.companyId}/frameworks/upload`,
              `/companies/${company.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
              `/companies/${company.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
              `/companies/${company.companyId}/frameworks/${DataTypeEnum.Lksg}/upload`,
            ];
          });
        });
      });

      it("Check if a non uploader user can access only the corresponding pages", () => {
        cy.ensureLoggedIn(reader_name, reader_pw);
        allPages.forEach((page) => {
          cy.visit(page);
          cy.get(noUploaderRightsMessageSelector).should("not.exist");
        });
        uploaderPages.forEach((page) => {
          cy.visit(page);
          cy.get(noUploaderRightsMessageSelector).should("exist");
        });
      });

      it("Check if an uploader user can access the corresponding pages", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        allPages.forEach((page) => {
          cy.visit(page);
          cy.get(noUploaderRightsMessageSelector).should("not.exist");
        });
        uploaderPages.forEach((page) => {
          cy.visit(page);
          cy.get(noUploaderRightsMessageSelector).should("not.exist");
        });
      });
    }
  );
});
