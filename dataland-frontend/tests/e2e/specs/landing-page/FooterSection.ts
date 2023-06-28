import { getStoredCompaniesForDataType } from "@e2e/utils/GeneralApiUtils";
import { DataTypeEnum } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";
import { checkFooter } from "@sharedUtils/ElementChecks";

describe("As a user, I expect the footer section to be present and contain relevant legal links", () => {
  describe("Checks that the footer section is present on many pages", () => {
    beforeEach(() => {
      cy.ensureLoggedIn();
    });

    const pagesToCheck = ["/companies", `/samples/${DataTypeEnum.EutaxonomyNonFinancials}`];

    pagesToCheck.forEach((page) => {
      it(`Checks that the footer is present on ${page}`, () => {
        cy.visitAndCheckAppMount(page);
        checkFooter();
      });
    });

    const frameworksToCheck = Object.values(DataTypeEnum).filter(
      (frameworkName) =>
        ([DataTypeEnum.Sfdr, DataTypeEnum.Sme, DataTypeEnum.P2p] as DataTypeEnum[]).indexOf(frameworkName) === -1
    );
    frameworksToCheck.forEach((framework) => {
      it(`Checks that the footer is present on ${framework}`, () => {
        getKeycloakToken(reader_name, reader_pw).then((token) => {
          cy.browserThen(getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyNonFinancials)).then(
            (storedCompanies) => {
              const companyId = storedCompanies[0].companyId;
              cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${framework}`);
              checkFooter();
            }
          );
        });
      });
    });
  });
});
