import { DataTypeEnum, LksgData, StoredCompany } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { prepareFixture } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describeIf(
  "As a user, I expect to be able to edit datasets with multiple reporting periods",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    let testData: FixtureData<LksgData>;
    const testCompany = generateDummyCompanyInformation(`company-for-testing-edit-button-${new Date().getTime()}`);

    before(function () {
      const fixtureType = "CompanyInformationWithLksgPreparedFixtures";
      prepareFixture<LksgData>(fixtureType).then((preparedFixtures) => {
        testData = getPreparedFixture("LkSG-date-2023-04-18", preparedFixtures);
      });
    });

    it("Check whether Edit Data button has dropdown with 2 different Reporting Periods", () => {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, testCompany).then(async (storedCompany) => {
          cy.ensureLoggedIn(admin_name, admin_pw);

          await uploadOneLksgDatasetViaApi(token, storedCompany.companyId, "2022", testData.t);
          await uploadOneLksgDatasetViaApi(token, storedCompany.companyId, "2021", testData.t);

          testEditDataButton(storedCompany);
        });
      });
    });
  }
);

/**
 * Tests that the item was added and is visible on the QA list
 * @param storedCompany details of the company that was created
 */
function testEditDataButton(storedCompany: StoredCompany): void {
  cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);

  cy.get('[data-test="editDatasetButton"').should("exist").click();
  cy.get('[data-test="select-reporting-period-dialog"')
    .should("exist")
    .get('[data-test="reporting-periods"')
    .last()
    .should("contain", "2021")
    .click();

  cy.url().should("eq", getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/lksg/upload?*`);
}
